package services

import (
	"context"
	"encoding/json"
	"net/http"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type ReceiptService struct {
	store *repository.Store
}

func NewReceiptService(store *repository.Store) *ReceiptService { return &ReceiptService{store: store} }

var refSicilNotFound = &apierror.APIError{Code: apierror.SicilNotFound.Code, Status: http.StatusUnprocessableEntity}

type ReceiptCreate struct {
	Kind          string          `json:"kind"`
	DepotID       uint            `json:"depotId"`
	SicilID       uint            `json:"sicilId"`
	Date          models.Date     `json:"date"`
	Method        string          `json:"method"`
	Amount        models.Money    `json:"amount"`
	Currency      string          `json:"currency"`
	BankAccountID *uint           `json:"bankAccountId"`
	Instrument    json.RawMessage `json:"instrument"`
	Description   string          `json:"description"`
}

var receiptPrefix = map[string]string{"COLLECTION": "TAH", "PAYMENT": "TED"}

func (s *ReceiptService) Create(ctx context.Context, orgID uint, in ReceiptCreate) (*models.Receipt, error) {
	prefix, ok := receiptPrefix[in.Kind]
	if !ok {
		return nil, apierror.WithField(apierror.InvalidRequest, "kind")
	}
	switch in.Method {
	case "CASH", "BANK_TRANSFER":
	case "CHEQUE", "NOTE":
		return nil, &apierror.Detailed{Err: apierror.InvalidRequest, Field: "method", Detail: "cheque and note receipts arrive with instruments"}
	default:
		return nil, apierror.WithField(apierror.InvalidRequest, "method")
	}
	if in.Date.IsZero() {
		return nil, apierror.WithField(apierror.InvalidRequest, "date")
	}
	if in.Amount <= 0 {
		return nil, apierror.InvalidAmount
	}
	if in.Currency == "" {
		in.Currency = "TRY"
	}
	if !currencyCode.MatchString(in.Currency) {
		return nil, apierror.WithField(apierror.InvalidRequest, "currency")
	}
	in.Description = strings.TrimSpace(in.Description)

	var out *models.Receipt
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if _, err := depotRef(q, orgID, in.DepotID); err != nil {
			return err
		}
		sicil, err := q.GetSicilRef(orgID, in.SicilID)
		if repository.IsNotFound(err) {
			return refSicilNotFound
		}
		if err != nil {
			return err
		}
		counter, err := receiptCounterAccount(q, orgID, in)
		if err != nil {
			return err
		}
		current, err := sicilAccount(ctx, q, orgID, sicil)
		if err != nil {
			return err
		}
		if counter.Currency != in.Currency || current.Currency != in.Currency {
			return apierror.CurrencyMismatch
		}
		no, err := q.NextNumber(orgID, in.DepotID, prefix, in.Date.Year())
		if err != nil {
			return err
		}
		debit, credit := counter.ID, current.ID
		if in.Kind == "PAYMENT" {
			debit, credit = credit, debit
		}
		desc := in.Description
		if desc == "" {
			desc = no + " " + sicil.HesapAdi
		}
		e, err := postEntry(ctx, q, orgID, entryInput{
			depotID: in.DepotID, date: in.Date, description: desc, source: models.SourceReceipt, sourceID: &no,
			lines: []LineInput{{AccountID: debit, Side: models.Debit, Amount: in.Amount}, {AccountID: credit, Side: models.Credit, Amount: in.Amount}},
		})
		if err != nil {
			return err
		}
		r := &models.Receipt{ReceiptNo: no, Kind: in.Kind, DepotID: in.DepotID, SicilID: sicil.ID, Date: in.Date, Method: in.Method,
			Amount: in.Amount, Currency: in.Currency, BankAccountID: in.BankAccountID, Description: in.Description, JournalEntryID: e.ID}
		if in.Method != "BANK_TRANSFER" {
			r.BankAccountID = nil
		}
		if err := q.InsertReceipt(orgID, r, actorFrom(ctx).userID()); err != nil {
			return err
		}
		out, err = q.GetReceipt(orgID, r.ID)
		if err != nil {
			return err
		}
		if err := emit(ctx, q, orgID, in.DepotID, "ReceiptPosted", "receipt", out.ID, nil, receiptPayload(out)); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "receipt.created", "receipt", out.ID, nil, out)
	})
	return out, err
}

func receiptPayload(r *models.Receipt) map[string]any {
	return map[string]any{
		"receiptId": r.ID, "receiptNo": r.ReceiptNo, "kind": r.Kind, "method": r.Method, "origin": r.Origin,
		"instrumentId": r.InstrumentID, "sicilId": r.SicilID, "date": r.Date, "amount": r.Amount,
		"currency": r.Currency, "journalEntryId": r.JournalEntryID,
	}
}

func receiptCounterAccount(q *repository.Q, orgID uint, in ReceiptCreate) (*models.Account, error) {
	if in.Method == "CASH" {
		return cashAccount(q, orgID, in.DepotID)
	}
	if in.BankAccountID == nil {
		return nil, apierror.BankAccountRequired
	}
	return bankAccount(q, orgID, *in.BankAccountID)
}

func cashAccount(q *repository.Q, orgID, depotID uint) (*models.Account, error) {
	return roleAccount(q, orgID, "CASH", depotID)
}

func roleAccount(q *repository.Q, orgID uint, role string, depotID uint) (*models.Account, error) {
	a, err := q.RoleAccount(orgID, role, depotID)
	if repository.IsNotFound(err) {
		return nil, apierror.WithDetail(apierror.AccountRoleMissing, role)
	}
	return a, err
}

// Bank accounts are the 102 group of the uniform chart of accounts.
func bankAccount(q *repository.Q, orgID, id uint) (*models.Account, error) {
	a, err := q.GetAccount(orgID, id)
	if repository.IsNotFound(err) {
		return nil, refAccountNotFound
	}
	if err != nil {
		return nil, err
	}
	if !strings.HasPrefix(a.Code, "102") {
		return nil, apierror.WithDetail(apierror.BankAccountRequired, a.Code+" is not a bank account")
	}
	return a, nil
}

var sicilAccountGroup = map[string]struct {
	prefix string
	typ    models.AccountType
}{
	string(models.SicilTipiMusteri): {"120", "ASSET"},
	string(models.SicilTipiFirma):   {"320", "LIABILITY"},
	string(models.SicilTipiBanka):   {"102", "ASSET"},
}

// sicilAccount returns the card's current account, opening it (120 / 320 / 102) on first use.
func sicilAccount(ctx context.Context, q *repository.Q, orgID uint, s *repository.SicilRef) (*models.Account, error) {
	a, err := q.AccountBySicil(orgID, s.ID)
	if err == nil || !repository.IsNotFound(err) {
		return a, err
	}
	g, ok := sicilAccountGroup[s.SicilTipi]
	if !ok {
		g = sicilAccountGroup[string(models.SicilTipiMusteri)]
	}
	side, _ := g.typ.NormalSide()
	sicilID := s.ID
	a = &models.Account{OrgID: orgID, Code: g.prefix + "." + s.HesapNo, Name: s.HesapAdi, Type: g.typ, NormalSide: side, SicilID: &sicilID, Currency: "TRY"}
	created, err := q.CreateAccountIfAbsent(a)
	if err != nil {
		return nil, err
	}
	if !created {
		existing, err := q.AccountBySicil(orgID, s.ID)
		if repository.IsNotFound(err) {
			return nil, apierror.WithDetail(apierror.AccountCodeExists, a.Code)
		}
		return existing, err
	}
	return a, audit(ctx, q, orgID, "account.created", "account", a.ID, nil, a)
}

func (s *ReceiptService) Get(ctx context.Context, orgID, id uint) (*models.Receipt, error) {
	r, err := s.store.Read(ctx).GetReceipt(orgID, id)
	if repository.IsNotFound(err) {
		return nil, apierror.ReceiptNotFound
	}
	return r, err
}

func (s *ReceiptService) Void(ctx context.Context, orgID, id uint, reason string) (*models.Receipt, error) {
	reason = strings.TrimSpace(reason)
	if len([]rune(reason)) < 3 {
		return nil, apierror.WithField(apierror.InvalidRequest, "reason")
	}
	var out *models.Receipt
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if err := q.LockReceipt(orgID, id); err != nil {
			return err
		}
		r, err := q.GetReceipt(orgID, id)
		if repository.IsNotFound(err) {
			return apierror.ReceiptNotFound
		}
		if err != nil {
			return err
		}
		switch {
		case r.Origin == "INSTRUMENT":
			return apierror.ReceiptOwnedByInstrument
		case r.VoidedByEntryID != nil:
			return apierror.ReceiptAlreadyVoided
		}
		e, err := entry(q, orgID, r.JournalEntryID)
		if err != nil {
			return err
		}
		rev, err := reverseEntry(ctx, q, e, reason)
		if err != nil {
			return err
		}
		if err := q.VoidReceipt(orgID, id, rev.ID); err != nil {
			return err
		}
		out, err = q.GetReceipt(orgID, id)
		if err != nil {
			return err
		}
		return audit(ctx, q, orgID, "receipt.voided", "receipt", id, r, out)
	})
	return out, err
}

func (s *ReceiptService) Grid(ctx context.Context, orgID uint, req repository.GridRequest) (*repository.GridResponse, error) {
	return s.store.Read(ctx).GridReceipts(orgID, req)
}

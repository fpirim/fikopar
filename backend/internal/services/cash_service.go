package services

import (
	"context"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type CashService struct {
	store *repository.Store
}

func NewCashService(store *repository.Store) *CashService { return &CashService{store: store} }

type CashBookLine struct {
	Date           models.Date  `json:"date"`
	Kind           string       `json:"kind"`
	DocumentNo     *string      `json:"documentNo"`
	ReceiptID      *uint        `json:"receiptId"`
	VoucherID      *uint        `json:"voucherId"`
	JournalEntryID uint         `json:"journalEntryId"`
	SicilID        *uint        `json:"sicilId"`
	SicilName      *string      `json:"sicilName"`
	Description    string       `json:"description"`
	Income         models.Money `json:"income"`
	Expense        models.Money `json:"expense"`
	RunningBalance models.Money `json:"runningBalance"`
	ResetsBalance  bool         `json:"resetsBalance"`
}

type CashBook struct {
	DepotID        uint           `json:"depotId"`
	From           models.Date    `json:"from"`
	To             models.Date    `json:"to"`
	OpeningBalance models.Money   `json:"openingBalance"`
	TotalIn        models.Money   `json:"totalIn"`
	TotalOut       models.Money   `json:"totalOut"`
	Lines          []CashBookLine `json:"lines"`
	ClosingBalance models.Money   `json:"closingBalance"`
}

func validPeriod(from, to *models.Date) error {
	if from == nil {
		return apierror.WithField(apierror.InvalidRequest, "from")
	}
	if to == nil || to.Before(from.Time) {
		return apierror.WithField(apierror.InvalidRequest, "to")
	}
	return nil
}

func (s *CashService) Book(ctx context.Context, orgID, depotID uint, from, to *models.Date) (*CashBook, error) {
	if depotID == 0 {
		return nil, apierror.DepotRequired
	}
	if err := validPeriod(from, to); err != nil {
		return nil, err
	}
	var book *CashBook
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if _, err := depotRef(q, orgID, depotID); err != nil {
			return err
		}
		cash, err := cashAccount(q, orgID, depotID)
		if err != nil {
			return err
		}
		opening, err := q.OpeningBalanceAt(cash.ID, depotID, *from)
		if err != nil {
			return err
		}
		rows, err := q.CashLines(orgID, cash.ID, depotID, *from, *to)
		if err != nil {
			return err
		}
		carried, err := q.ActiveRunYears(orgID, from.Year()+1, to.Year())
		if err != nil {
			return err
		}
		book = &CashBook{DepotID: depotID, From: *from, To: *to, OpeningBalance: opening, Lines: make([]CashBookLine, len(rows))}
		run := opening
		// A carried 1 January starts the books again (L6); its opening line, if any, sets the balance.
		restart := func(d models.Date) {
			for len(carried) > 0 && !d.Before(models.NewDate(carried[0], time.January, 1).Time) {
				carried = carried[1:]
				run = 0
			}
		}
		for i, r := range rows {
			restart(r.Date)
			run += r.Income - r.Expense
			if !r.ResetsBalance {
				book.TotalIn += r.Income
				book.TotalOut += r.Expense
			}
			book.Lines[i] = CashBookLine{Date: r.Date, Kind: r.Kind, DocumentNo: r.DocumentNo, ReceiptID: r.ReceiptID,
				VoucherID: r.VoucherID, JournalEntryID: r.JournalEntryID, SicilID: r.SicilID, SicilName: r.SicilName,
				Description: r.Description, Income: r.Income, Expense: r.Expense, RunningBalance: run, ResetsBalance: r.ResetsBalance}
		}
		restart(*to)
		book.ClosingBalance = run
		return nil
	})
	return book, err
}

type CashVoucherCreate struct {
	Kind        string       `json:"kind"`
	DepotID     uint         `json:"depotId"`
	Date        models.Date  `json:"date"`
	AccountID   uint         `json:"accountId"`
	Amount      models.Money `json:"amount"`
	Description string       `json:"description"`
}

var voucherPrefix = map[string]string{"EXPENSE": "GDR", "BANK_DEPOSIT": "VRM", "BANK_WITHDRAWAL": "VRM"}

var notExpenseAccount = &apierror.APIError{Code: apierror.InvalidRequest.Code, Status: http.StatusUnprocessableEntity}

func (s *CashService) CreateVoucher(ctx context.Context, orgID uint, in CashVoucherCreate) (*models.CashVoucher, error) {
	prefix, ok := voucherPrefix[in.Kind]
	in.Description = strings.TrimSpace(in.Description)
	switch {
	case !ok:
		return nil, apierror.WithField(apierror.InvalidRequest, "kind")
	case in.Date.IsZero():
		return nil, apierror.WithField(apierror.InvalidRequest, "date")
	case in.Description == "":
		return nil, apierror.WithField(apierror.InvalidRequest, "description")
	case in.Amount <= 0:
		return nil, apierror.InvalidAmount
	}
	var out *models.CashVoucher
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if _, err := depotRef(q, orgID, in.DepotID); err != nil {
			return err
		}
		cash, err := cashAccount(q, orgID, in.DepotID)
		if err != nil {
			return err
		}
		var other *models.Account
		if in.Kind == "EXPENSE" {
			other, err = q.GetAccount(orgID, in.AccountID)
			if repository.IsNotFound(err) {
				return apierror.UnknownRef(apierror.AccountNotFound, "accountId")
			}
			if err == nil && other.Type != "EXPENSE" {
				return apierror.WithField(notExpenseAccount, "accountId")
			}
		} else {
			other, err = bankAccount(q, orgID, in.AccountID)
		}
		if err != nil {
			return err
		}
		if other.Currency != cash.Currency {
			return apierror.CurrencyMismatch
		}
		no, err := q.NextNumber(orgID, in.DepotID, prefix, in.Date.Year())
		if err != nil {
			return err
		}
		debit, credit := other.ID, cash.ID
		if in.Kind == "BANK_WITHDRAWAL" {
			debit, credit = credit, debit
		}
		e, err := postEntry(ctx, q, orgID, entryInput{
			depotID: in.DepotID, date: in.Date, description: no + " " + in.Description,
			source: models.SourceCashVoucher, sourceID: &no,
			lines: []LineInput{{AccountID: debit, Side: models.Debit, Amount: in.Amount}, {AccountID: credit, Side: models.Credit, Amount: in.Amount}},
		})
		if err != nil {
			return err
		}
		v := &models.CashVoucher{VoucherNo: no, Kind: in.Kind, DepotID: in.DepotID, Date: in.Date, AccountID: other.ID,
			Amount: in.Amount, Description: in.Description, JournalEntryID: e.ID}
		if err := q.InsertCashVoucher(orgID, v, actorFrom(ctx).userID()); err != nil {
			return err
		}
		out = v
		return audit(ctx, q, orgID, "cash_voucher.created", "cash_voucher", v.ID, nil, v)
	})
	return out, err
}

func (s *CashService) VoidVoucher(ctx context.Context, orgID, id uint, reason string) (*models.CashVoucher, error) {
	reason = strings.TrimSpace(reason)
	if len([]rune(reason)) < 3 {
		return nil, apierror.WithField(apierror.InvalidRequest, "reason")
	}
	var out *models.CashVoucher
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		v, err := q.GetCashVoucherForUpdate(orgID, id)
		if repository.IsNotFound(err) {
			return apierror.CashVoucherNotFound
		}
		if err != nil {
			return err
		}
		if v.VoidedByEntryID != nil {
			return apierror.CashVoucherAlreadyVoided
		}
		e, err := entry(q, orgID, v.JournalEntryID)
		if err != nil {
			return err
		}
		rev, err := reverseEntry(ctx, q, e, today(), reason)
		if err != nil {
			return err
		}
		if err := q.VoidCashVoucher(orgID, id, rev.ID); err != nil {
			return err
		}
		out, err = q.GetCashVoucher(orgID, id)
		if err != nil {
			return err
		}
		return audit(ctx, q, orgID, "cash_voucher.voided", "cash_voucher", id, v, out)
	})
	return out, err
}

type CashCountCreate struct {
	DepotID        uint         `json:"depotId"`
	CountedAmount  models.Money `json:"countedAmount"`
	PostDifference bool         `json:"postDifference"`
	Note           string       `json:"note"`
}

// CreateCount compares the till with the book balance as of today; the difference optionally
// posts against CASH_OVER_SHORT.
func (s *CashService) CreateCount(ctx context.Context, orgID uint, in CashCountCreate) (*models.CashCount, error) {
	if in.CountedAmount < 0 {
		return nil, apierror.InvalidAmount
	}
	var out *models.CashCount
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if _, err := depotRef(q, orgID, in.DepotID); err != nil {
			return err
		}
		cash, err := cashAccount(q, orgID, in.DepotID)
		if err != nil {
			return err
		}
		book, err := q.AccountBalanceAsOf(cash.ID, in.DepotID, today())
		if err != nil {
			return err
		}
		id, err := q.NextCashCountID()
		if err != nil {
			return err
		}
		c := &models.CashCount{ID: id, DepotID: in.DepotID, CountedAmount: in.CountedAmount, BookBalance: book,
			Note: strings.TrimSpace(in.Note), CountedBy: actorFrom(ctx).userID()}
		if diff := in.CountedAmount - book; in.PostDifference && diff != 0 {
			over, err := roleAccount(q, orgID, "CASH_OVER_SHORT", in.DepotID)
			if err != nil {
				return err
			}
			debit, credit := cash.ID, over.ID
			if diff < 0 {
				debit, credit = credit, debit
			}
			src := strconv.FormatUint(uint64(id), 10)
			e, err := postEntry(ctx, q, orgID, entryInput{
				depotID: in.DepotID, date: today(), description: "Cash count difference", source: models.SourceCashCount, sourceID: &src,
				lines: []LineInput{{AccountID: debit, Side: models.Debit, Amount: diff.Abs()}, {AccountID: credit, Side: models.Credit, Amount: diff.Abs()}},
			})
			if err != nil {
				return err
			}
			c.JournalEntryID = &e.ID
		}
		if err := q.InsertCashCount(orgID, c); err != nil {
			return err
		}
		c.PostDifference = in.PostDifference
		out = c
		return audit(ctx, q, orgID, "cash_count.created", "cash_count", c.ID, nil, c)
	})
	return out, err
}

type CashCountPage struct {
	Items      []models.CashCount `json:"items"`
	NextCursor *string            `json:"nextCursor"`
}

func (s *CashService) ListCounts(ctx context.Context, orgID, depotID uint, limit int, cursor string) (*CashCountPage, error) {
	if depotID == 0 {
		return nil, apierror.WithField(apierror.InvalidRequest, "depotId")
	}
	var before uint64
	if cursor != "" {
		var err error
		if before, err = strconv.ParseUint(cursor, 10, 32); err != nil {
			return nil, apierror.WithField(apierror.InvalidRequest, "cursor")
		}
	}
	limit = clampLimit(limit)
	rows, err := s.store.Read(ctx).ListCashCounts(orgID, depotID, uint(before), limit+1)
	if err != nil {
		return nil, err
	}
	page := &CashCountPage{Items: rows}
	if len(rows) > limit {
		page.Items = rows[:limit]
		c := strconv.FormatUint(uint64(rows[limit-1].ID), 10)
		page.NextCursor = &c
	}
	return page, nil
}

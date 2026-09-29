package services

import (
	"context"
	"fmt"
	"sort"
	"strings"
	"time"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type CFCheck struct {
	Code     string `json:"code"`
	OK       bool   `json:"ok"`
	Blocking bool   `json:"blocking"`
	Detail   string `json:"detail,omitempty"`
}

type CFRow struct {
	AccountID      uint         `json:"accountId"`
	AccountCode    string       `json:"accountCode"`
	AccountName    string       `json:"accountName"`
	DepotID        uint         `json:"depotId"`
	ClosingBalance models.Money `json:"closingBalance"`
	OpeningDebit   models.Money `json:"openingDebit"`
	OpeningCredit  models.Money `json:"openingCredit"`
	Status         string       `json:"status"`
	OverrideReason *string      `json:"overrideReason"`
	JournalEntryID *uint        `json:"journalEntryId"`
}

type CarryForward struct {
	Year         int          `json:"year"`
	Status       string       `json:"status"`
	Checks       []CFCheck    `json:"checks"`
	AccountCount int          `json:"accountCount"`
	EntryCount   int          `json:"entryCount"`
	TotalDebit   models.Money `json:"totalDebit"`
	TotalCredit  models.Money `json:"totalCredit"`
	Rows         []CFRow      `json:"rows"`
}

func validYear(year int) error {
	if year < 2000 || year > 2999 {
		return apierror.WithField(apierror.InvalidRequest, "year")
	}
	return nil
}

func (s *LedgerService) CarryForward(ctx context.Context, orgID uint, year int, depotID *uint) (*CarryForward, error) {
	if err := validYear(year); err != nil {
		return nil, err
	}
	var cf *CarryForward
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		var err error
		cf, err = buildCarryForward(q, orgID, year, depotID)
		return err
	})
	return cf, err
}

type cfKey struct{ account, depot uint }

// buildCarryForward derives opening rows for `year` from the closing balances of 31 December of
// year − 1; overrides replace a row's amounts and both zero skip it.
func buildCarryForward(q *repository.Q, orgID uint, year int, depotID *uint) (*CarryForward, error) {
	posted, err := q.CarryForwardPosted(orgID, year)
	if err != nil {
		return nil, err
	}
	closeDate := models.NewDate(year-1, time.December, 31)
	closings, err := q.ClosingBalances(orgID, closeDate, depotID)
	if err != nil {
		return nil, err
	}
	excluded, err := q.RoleAccountIDs(orgID, "OPENING")
	if err != nil {
		return nil, err
	}
	skip := map[uint]bool{}
	for _, id := range excluded {
		skip[id] = true
	}
	overrides, err := q.CarryForwardOverrides(orgID, year, depotID)
	if err != nil {
		return nil, err
	}
	rows := map[cfKey]*CFRow{}
	closing := map[cfKey]models.Money{}
	for _, c := range closings {
		k := cfKey{c.AccountID, c.DepotID}
		closing[k] = c.Closing
		if skip[c.AccountID] || posted {
			continue
		}
		r := &CFRow{AccountID: c.AccountID, AccountCode: c.AccountCode, AccountName: c.AccountName, DepotID: c.DepotID, ClosingBalance: c.Closing, Status: "READY"}
		if c.Closing > 0 {
			r.OpeningDebit = c.Closing
		} else {
			r.OpeningCredit = -c.Closing
		}
		rows[k] = r
	}
	for _, o := range overrides {
		k := cfKey{o.AccountID, o.DepotID}
		reason := o.Reason
		if posted {
			if r, ok := rows[k]; ok {
				r.OverrideReason = &reason
			}
			continue
		}
		r, ok := rows[k]
		if !ok {
			r = &CFRow{AccountID: o.AccountID, AccountCode: o.AccountCode, AccountName: o.AccountName, DepotID: o.DepotID, ClosingBalance: closing[k]}
			rows[k] = r
		}
		r.OpeningDebit, r.OpeningCredit, r.OverrideReason = o.Debit, o.Credit, &reason
		r.Status = "EDITED"
		if o.Debit == 0 && o.Credit == 0 {
			r.Status = "SKIP"
		}
	}
	if posted {
		obs, err := q.OpeningBalances(orgID, year, depotID)
		if err != nil {
			return nil, err
		}
		for _, ob := range obs {
			k := cfKey{ob.AccountID, ob.DepotID}
			entryID := ob.JournalEntryID
			rows[k] = &CFRow{AccountID: ob.AccountID, AccountCode: ob.AccountCode, AccountName: ob.AccountName, DepotID: ob.DepotID,
				ClosingBalance: closing[k], OpeningDebit: ob.Debit, OpeningCredit: ob.Credit, Status: "POSTED", JournalEntryID: &entryID}
		}
		for _, o := range overrides {
			if r, ok := rows[cfKey{o.AccountID, o.DepotID}]; ok {
				reason := o.Reason
				r.OverrideReason = &reason
			}
		}
	}

	cf := &CarryForward{Year: year, Status: "PREVIEW", Rows: make([]CFRow, 0, len(rows))}
	if posted {
		cf.Status = "POSTED"
	}
	accounts := map[uint]bool{}
	for _, r := range rows {
		cf.Rows = append(cf.Rows, *r)
		if r.Status == "SKIP" {
			continue
		}
		cf.EntryCount++
		accounts[r.AccountID] = true
		cf.TotalDebit += r.OpeningDebit
		cf.TotalCredit += r.OpeningCredit
	}
	cf.AccountCount = len(accounts)
	sort.Slice(cf.Rows, func(i, j int) bool {
		a, b := cf.Rows[i], cf.Rows[j]
		if a.AccountCode != b.AccountCode {
			return a.AccountCode < b.AccountCode
		}
		return a.DepotID < b.DepotID
	})
	cf.Checks, err = carryForwardChecks(q, orgID, closeDate, posted)
	return cf, err
}

func carryForwardChecks(q *repository.Q, orgID uint, closeDate models.Date, posted bool) ([]CFCheck, error) {
	net, err := q.LedgerNet(orgID, closeDate)
	if err != nil {
		return nil, err
	}
	drift, err := q.OrgDriftCount(orgID)
	if err != nil {
		return nil, err
	}
	balanced := CFCheck{Code: "LEDGER_BALANCED", OK: net == 0 && drift == 0, Blocking: true}
	if !balanced.OK {
		balanced.Detail = fmt.Sprintf("net %s, %d drifting balances", net, drift)
	}
	return []CFCheck{
		{Code: "NO_DRAFT_DOCUMENTS", OK: true, Blocking: true},
		{Code: "STOCK_COUNTS_POSTED", OK: true, Blocking: false},
		balanced,
		{Code: "YEAR_NOT_OPENED", OK: !posted, Blocking: true},
	}, nil
}

// PostCarryForward writes one opening entry per (account, depot) dated 1 January against the
// OPENING account. A second run is refused by the run row, the entries' source key and the
// opening_balances key, so re-running can never post twice.
func (s *LedgerService) PostCarryForward(ctx context.Context, orgID uint, year int) (*CarryForward, error) {
	if err := validYear(year); err != nil {
		return nil, err
	}
	var out *CarryForward
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if err := q.LockCarryForward(orgID, year); err != nil {
			return err
		}
		posted, err := q.CarryForwardPosted(orgID, year)
		if err != nil {
			return err
		}
		if posted {
			return apierror.AlreadyCarriedForward
		}
		cf, err := buildCarryForward(q, orgID, year, nil)
		if err != nil {
			return err
		}
		var blocked []string
		for _, c := range cf.Checks {
			if c.Blocking && !c.OK {
				blocked = append(blocked, c.Code)
			}
		}
		if len(blocked) > 0 {
			return apierror.WithDetail(apierror.CarryForwardBlocked, strings.Join(blocked, ","))
		}
		jan1 := models.NewDate(year, time.January, 1)
		for _, r := range cf.Rows {
			if r.Status == "SKIP" {
				continue
			}
			if err := postOpening(ctx, q, orgID, year, jan1, r); err != nil {
				return err
			}
		}
		if err := q.InsertCarryForwardRun(orgID, year, cf.EntryCount, cf.TotalDebit, cf.TotalCredit, actorFrom(ctx).userID()); err != nil {
			if repository.IsUniqueViolation(err) {
				return apierror.AlreadyCarriedForward
			}
			return err
		}
		out, err = buildCarryForward(q, orgID, year, nil)
		if err != nil {
			return err
		}
		return audit(ctx, q, orgID, "carry_forward.posted", "carry_forward", uint(year), nil,
			map[string]any{"year": year, "entryCount": out.EntryCount, "totalDebit": out.TotalDebit, "totalCredit": out.TotalCredit})
	})
	return out, err
}

func postOpening(ctx context.Context, q *repository.Q, orgID uint, year int, jan1 models.Date, r CFRow) error {
	opening, err := roleAccount(q, orgID, "OPENING", r.DepotID)
	if err != nil {
		return err
	}
	side, amount := models.Debit, r.OpeningDebit
	if r.OpeningCredit > 0 {
		side, amount = models.Credit, r.OpeningCredit
	}
	src := fmt.Sprintf("%d:%d:%d", year, r.AccountID, r.DepotID)
	e, err := postEntry(ctx, q, orgID, entryInput{
		depotID: r.DepotID, date: jan1, description: fmt.Sprintf("Opening balance %d", year),
		source: models.SourceOpening, sourceID: &src,
		lines: []LineInput{{AccountID: r.AccountID, Side: side, Amount: amount}, {AccountID: opening.ID, Side: side.Opposite(), Amount: amount}},
	})
	if err != nil {
		if repository.IsUniqueViolation(err) {
			return apierror.AlreadyCarriedForward
		}
		return err
	}
	return q.InsertOpeningBalance(orgID, models.OpeningBalance{AccountID: r.AccountID, DepotID: r.DepotID, Year: year,
		Debit: r.OpeningDebit, Credit: r.OpeningCredit, Source: "CARRY_FORWARD", JournalEntryID: e.ID})
}

type OverrideInput struct {
	AccountID uint         `json:"accountId"`
	DepotID   uint         `json:"depotId"`
	Debit     models.Money `json:"debit"`
	Credit    models.Money `json:"credit"`
	Reason    string       `json:"reason"`
}

func (s *LedgerService) SetOverride(ctx context.Context, orgID uint, year int, in OverrideInput) (*CarryForward, error) {
	in.Reason = strings.TrimSpace(in.Reason)
	switch {
	case validYear(year) != nil:
		return nil, validYear(year)
	case in.Debit < 0 || in.Credit < 0:
		return nil, apierror.InvalidAmount
	case in.Debit > 0 && in.Credit > 0:
		return nil, apierror.WithDetail(apierror.InvalidRequest, "debit or credit, not both")
	case len([]rune(in.Reason)) < 3:
		return nil, apierror.WithField(apierror.InvalidRequest, "reason")
	}
	return s.changeOverride(ctx, orgID, year, in.AccountID, in.DepotID, func(q *repository.Q) error {
		return q.UpsertCarryForwardOverride(orgID, year, repository.Override{AccountID: in.AccountID, DepotID: in.DepotID,
			Debit: in.Debit, Credit: in.Credit, Reason: in.Reason}, actorFrom(ctx).userID())
	})
}

func (s *LedgerService) DeleteOverride(ctx context.Context, orgID uint, year int, accountID, depotID uint) (*CarryForward, error) {
	if err := validYear(year); err != nil {
		return nil, err
	}
	return s.changeOverride(ctx, orgID, year, accountID, depotID, func(q *repository.Q) error {
		return q.DeleteCarryForwardOverride(orgID, year, accountID, depotID)
	})
}

func (s *LedgerService) changeOverride(ctx context.Context, orgID uint, year int, accountID, depotID uint, change func(q *repository.Q) error) (*CarryForward, error) {
	var out *CarryForward
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if err := q.LockCarryForward(orgID, year); err != nil {
			return err
		}
		posted, err := q.CarryForwardPosted(orgID, year)
		if err != nil {
			return err
		}
		if posted {
			return apierror.AlreadyCarriedForward
		}
		if _, err := account(q, orgID, accountID); err != nil {
			return err
		}
		if _, err := q.GetDepot(orgID, depotID); err != nil {
			if repository.IsNotFound(err) {
				return apierror.DepotNotFound
			}
			return err
		}
		if err := change(q); err != nil {
			return err
		}
		if err := audit(ctx, q, orgID, "carry_forward.override_changed", "account", accountID, nil, map[string]any{"year": year, "depotId": depotID}); err != nil {
			return err
		}
		out, err = buildCarryForward(q, orgID, year, nil)
		return err
	})
	return out, err
}

func (s *LedgerService) OpeningBalances(ctx context.Context, orgID uint, year int, depotID *uint) ([]models.OpeningBalance, error) {
	if err := validYear(year); err != nil {
		return nil, err
	}
	rows, err := s.store.Read(ctx).OpeningBalances(orgID, year, depotID)
	if err != nil {
		return nil, err
	}
	out := make([]models.OpeningBalance, len(rows))
	for i, r := range rows {
		out[i] = r.OpeningBalance
	}
	return out, nil
}

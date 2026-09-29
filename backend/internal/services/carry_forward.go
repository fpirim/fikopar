package services

import (
	"context"
	"fmt"
	"sort"
	"strconv"
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

type CFStep struct {
	Code   string `json:"code"`
	Status string `json:"status"`
}

type CarryForward struct {
	Year         int          `json:"year"`
	Status       string       `json:"status"`
	Version      int          `json:"version"`
	PostedAt     *time.Time   `json:"postedAt"`
	ReopenedAt   *time.Time   `json:"reopenedAt"`
	ReopenReason *string      `json:"reopenReason"`
	Steps        []CFStep     `json:"steps"`
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
	active, reopened, err := q.CarryForwardRuns(orgID, year)
	if err != nil {
		return nil, err
	}
	posted := active != nil
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
	if reopened != nil {
		cf.Status, cf.ReopenedAt, cf.ReopenReason = "REOPENED", reopened.ReopenedAt, reopened.ReopenReason
	}
	if posted {
		cf.Status, cf.PostedAt = "POSTED", &active.PostedAt
	}
	if cf.Version, err = q.PreviewVersion(orgID, year); err != nil {
		return nil, err
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
	if cf.Checks, err = carryForwardChecks(q, orgID, closeDate, posted); err != nil {
		return nil, err
	}
	cf.Steps = carryForwardSteps(cf.Checks, posted)
	return cf, nil
}

var stepChecks = [...][2]string{
	{"CLOSE_DOCUMENTS", "NO_DRAFT_DOCUMENTS"}, {"STOCK_COUNTS", "STOCK_COUNTS_POSTED"},
	{"ACCOUNT_BALANCES", "LEDGER_BALANCED"}, {"OPEN_YEAR", "YEAR_NOT_OPENED"},
}

// carryForwardSteps maps the checks onto the four screen steps; OPEN_YEAR is done by posting.
func carryForwardSteps(checks []CFCheck, posted bool) []CFStep {
	byCode := make(map[string]CFCheck, len(checks))
	for _, c := range checks {
		byCode[c.Code] = c
	}
	steps := make([]CFStep, len(stepChecks))
	seenOpen := false
	for i, sc := range stepChecks {
		c := byCode[sc[1]]
		status := "TODO"
		switch {
		case posted:
			status = "DONE"
		case c.Blocking && !c.OK:
			status = "BLOCKED"
		case c.OK && sc[0] != "OPEN_YEAR":
			status = "DONE"
		}
		if status != "DONE" && !seenOpen {
			seenOpen = true
			if status == "TODO" {
				status = "CURRENT"
			}
		}
		steps[i] = CFStep{Code: sc[0], Status: status}
	}
	return steps
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
	open, err := q.OpenCountsThrough(orgID, closeDate)
	if err != nil {
		return nil, err
	}
	counts := CFCheck{Code: "STOCK_COUNTS_POSTED", OK: len(open) == 0, Blocking: true, Detail: strings.Join(open, ", ")}
	uncounted, err := q.DepotsWithoutFinalizedCount(orgID, closeDate.Year())
	if err != nil {
		return nil, err
	}
	yearEnd := CFCheck{Code: "YEAR_END_COUNTS", OK: len(uncounted) == 0, Blocking: false}
	if !yearEnd.OK {
		yearEnd.Detail = "depots " + strings.Join(uncounted, ", ")
	}
	drafts, err := q.DraftDocumentsThrough(orgID, closeDate)
	if err != nil {
		return nil, err
	}
	ids := make([]string, len(drafts))
	for i, id := range drafts {
		ids[i] = strconv.FormatUint(uint64(id), 10)
	}
	return []CFCheck{
		{Code: "NO_DRAFT_DOCUMENTS", OK: len(drafts) == 0, Blocking: true, Detail: strings.Join(ids, ", ")},
		counts,
		yearEnd,
		balanced,
		{Code: "YEAR_NOT_OPENED", OK: !posted, Blocking: true},
	}, nil
}

func lockYear(q *repository.Q, orgID uint, year int) error {
	if err := q.LockLedger(orgID, true); err != nil {
		return err
	}
	return q.LockCarryForward(orgID, year)
}

// PostCarryForward writes one opening entry per (account, depot) dated 1 January against the
// OPENING account and closes year − 1. A second active run is refused by the run index, the
// entries' source key and the opening_balances index, so re-running can never post twice.
func (s *LedgerService) PostCarryForward(ctx context.Context, orgID uint, year int) (*CarryForward, error) {
	if err := validYear(year); err != nil {
		return nil, err
	}
	jan1 := models.NewDate(year, time.January, 1)
	var out *CarryForward
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if err := lockYear(q, orgID, year); err != nil {
			return err
		}
		posted, err := q.CarryForwardPosted(orgID, year)
		if err != nil {
			return err
		}
		if posted {
			return apierror.AlreadyCarriedForward
		}
		if err := yearOpen(q, orgID, jan1); err != nil {
			return err
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
		runID, err := q.InsertCarryForwardRun(orgID, year, cf.EntryCount, cf.TotalDebit, cf.TotalCredit, actorFrom(ctx).userID())
		if repository.IsUniqueViolation(err) {
			return apierror.AlreadyCarriedForward
		}
		if err != nil {
			return err
		}
		for _, r := range cf.Rows {
			if r.Status == "SKIP" {
				continue
			}
			if err := postOpening(ctx, q, orgID, runID, jan1, r); err != nil {
				return err
			}
		}
		if err := q.BumpPreviewVersion(orgID, year); err != nil {
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

func postOpening(ctx context.Context, q *repository.Q, orgID, runID uint, jan1 models.Date, r CFRow) error {
	opening, err := roleAccount(q, orgID, "OPENING", r.DepotID)
	if err != nil {
		return err
	}
	side, amount := models.Debit, r.OpeningDebit
	if r.OpeningCredit > 0 {
		side, amount = models.Credit, r.OpeningCredit
	}
	year := jan1.Year()
	src := fmt.Sprintf("%d:%d:%d:%d", year, runID, r.AccountID, r.DepotID)
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
	return q.InsertOpeningBalance(orgID, runID, models.OpeningBalance{AccountID: r.AccountID, DepotID: r.DepotID, Year: year,
		Debit: r.OpeningDebit, Credit: r.OpeningCredit, Source: "CARRY_FORWARD", JournalEntryID: e.ID})
}

type OverrideInput struct {
	AccountID uint         `json:"accountId"`
	DepotID   uint         `json:"depotId"`
	Debit     models.Money `json:"debit"`
	Credit    models.Money `json:"credit"`
	Reason    string       `json:"reason"`
}

// ReopenCarryForward voids the year's active run: each opening entry is reversed on 1 January and
// the year before is open again. A later carried year has to be reopened first.
func (s *LedgerService) ReopenCarryForward(ctx context.Context, orgID uint, year, version int, reason string) (*CarryForward, error) {
	if err := validYear(year); err != nil {
		return nil, err
	}
	reason = strings.TrimSpace(reason)
	if len([]rune(reason)) < 3 {
		return nil, apierror.WithField(apierror.InvalidRequest, "reason")
	}
	jan1 := models.NewDate(year, time.January, 1)
	ctx = withIdempotencyKey(ctx, "")
	var out *CarryForward
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if err := lockYear(q, orgID, year); err != nil {
			return err
		}
		if err := checkPreviewVersion(q, orgID, year, version); err != nil {
			return err
		}
		active, _, err := q.CarryForwardRuns(orgID, year)
		if err != nil {
			return err
		}
		if active == nil {
			return apierror.NotCarriedForward
		}
		if err := yearOpen(q, orgID, jan1); err != nil {
			return err
		}
		ids, err := q.RunOpeningEntryIDs(active.ID)
		if err != nil {
			return err
		}
		for _, id := range ids {
			e, err := entry(q, orgID, id)
			if err != nil {
				return err
			}
			rev, err := reverseEntry(ctx, q, e, jan1, reason)
			if err != nil {
				return err
			}
			if err := q.VoidOpening(id, rev.ID); err != nil {
				return err
			}
		}
		if err := q.ReopenCarryForwardRun(active.ID, actorFrom(ctx).userID(), reason); err != nil {
			return err
		}
		if err := q.BumpPreviewVersion(orgID, year); err != nil {
			return err
		}
		if out, err = buildCarryForward(q, orgID, year, nil); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "carry_forward.reopened", "carry_forward", uint(year), nil,
			map[string]any{"year": year, "runId": active.ID, "reversed": len(ids), "reason": reason})
	})
	return out, err
}

func checkPreviewVersion(q *repository.Q, orgID uint, year, version int) error {
	v, err := q.PreviewVersion(orgID, year)
	if err == nil && v != version {
		return apierror.VersionConflict
	}
	return err
}

// PreviewQuery names the expected preview version and the depot the returned preview is restricted to.
type PreviewQuery struct {
	Version int
	DepotID *uint
}

func (s *LedgerService) SetOverride(ctx context.Context, orgID uint, year int, pq PreviewQuery, in OverrideInput) (*CarryForward, error) {
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
	return s.changeOverride(ctx, orgID, year, pq, in.AccountID, in.DepotID, func(q *repository.Q) error {
		return q.UpsertCarryForwardOverride(orgID, year, repository.Override{AccountID: in.AccountID, DepotID: in.DepotID,
			Debit: in.Debit, Credit: in.Credit, Reason: in.Reason}, actorFrom(ctx).userID())
	})
}

func (s *LedgerService) DeleteOverride(ctx context.Context, orgID uint, year int, pq PreviewQuery, accountID, depotID uint) (*CarryForward, error) {
	if err := validYear(year); err != nil {
		return nil, err
	}
	return s.changeOverride(ctx, orgID, year, pq, accountID, depotID, func(q *repository.Q) error {
		return q.DeleteCarryForwardOverride(orgID, year, accountID, depotID)
	})
}

func (s *LedgerService) changeOverride(ctx context.Context, orgID uint, year int, pq PreviewQuery, accountID, depotID uint, change func(q *repository.Q) error) (*CarryForward, error) {
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
		if _, err := q.GetAccount(orgID, accountID); err != nil {
			if repository.IsNotFound(err) {
				return apierror.UnknownRef(apierror.AccountNotFound, "accountId")
			}
			return err
		}
		if _, err := q.GetDepot(orgID, depotID); err != nil {
			if repository.IsNotFound(err) {
				return apierror.UnknownRef(apierror.DepotNotFound, "depotId")
			}
			return err
		}
		if err := checkPreviewVersion(q, orgID, year, pq.Version); err != nil {
			return err
		}
		if err := change(q); err != nil {
			return err
		}
		if err := q.BumpPreviewVersion(orgID, year); err != nil {
			return err
		}
		if err := audit(ctx, q, orgID, "carry_forward.override_changed", "account", accountID, nil, map[string]any{"year": year, "depotId": depotID}); err != nil {
			return err
		}
		out, err = buildCarryForward(q, orgID, year, pq.DepotID)
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

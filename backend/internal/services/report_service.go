package services

import (
	"context"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type ReportService struct {
	store *repository.Store
}

func NewReportService(store *repository.Store) *ReportService { return &ReportService{store: store} }

type BalanceGridRequest struct {
	repository.ReportGridRequest
	AsOf      *models.Date `json:"asOf"`
	DepotID   *uint        `json:"depotId"`
	SicilTipi *string      `json:"sicilTipi"`
}

type BalancePage struct {
	Rows            []models.BalanceRow `json:"rows"`
	TotalCount      int64               `json:"totalCount"`
	UnfilteredCount int64               `json:"unfilteredCount"`
	Totals          map[string]string   `json:"totals"`
}

// Balances is the as-of current-account list; all=true ignores paging (exports).
func (s *ReportService) Balances(ctx context.Context, orgID uint, in BalanceGridRequest, all bool) (*BalancePage, error) {
	if in.AsOf == nil {
		return nil, apierror.WithField(apierror.InvalidRequest, "asOf")
	}
	if in.SicilTipi != nil && !models.ValidSicilTipi(models.SicilTipi(*in.SicilTipi)) {
		return nil, apierror.WithField(apierror.InvalidRequest, "sicilTipi")
	}
	q := s.store.Read(ctx)
	if in.DepotID != nil {
		if _, err := depotRef(q, orgID, *in.DepotID); err != nil {
			return nil, err
		}
	}
	rows, t, err := q.Balances(orgID, repository.BalanceQuery{AsOf: *in.AsOf, DepotID: in.DepotID, SicilTipi: in.SicilTipi, Grid: in.ReportGridRequest, All: all})
	if repository.IsBadFilter(err) {
		return nil, apierror.WithDetail(apierror.InvalidRequest, err.Error())
	}
	if err != nil {
		return nil, err
	}
	return &BalancePage{
		Rows: rows, TotalCount: t.TotalCount, UnfilteredCount: t.UnfilteredCount,
		Totals: map[string]string{"debit": t.Debit.String(), "credit": t.Credit.String(), "balance": t.Balance.String(), "overdue": t.Overdue.String()},
	}, nil
}

type StatementResult struct {
	*models.Statement
	Account *models.Account `json:"-"`
	Depots  []models.Depot  `json:"-"`
}

// Statement lists the account's movements between from and to (inclusive) after one
// brought-forward row per depot.
func (s *ReportService) Statement(ctx context.Context, orgID, accountID uint, from, to *models.Date, depotID *uint) (*StatementResult, error) {
	if err := validPeriod(from, to); err != nil {
		return nil, err
	}
	var res *StatementResult
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		acc, err := account(q, orgID, accountID)
		if err != nil {
			return err
		}
		depots, err := statementDepots(q, orgID, depotID)
		if err != nil {
			return err
		}
		st := &models.Statement{AccountID: acc.ID, From: *from, To: *to, BroughtForward: []models.StatementLine{}}
		totals := map[uint]*models.DepotTotal{}
		var run models.Money
		for _, d := range depots {
			bf, err := q.OpeningBalanceAt(acc.ID, d.ID, *from)
			if err != nil {
				return err
			}
			id := d.ID
			line := models.StatementLine{EntryDate: *from, DepotID: &id, SourceType: models.SourceOpening, Description: "Brought forward"}
			if bf >= 0 {
				line.Debit = bf
			} else {
				line.Credit = -bf
			}
			run += bf
			line.RunningBalance = run
			st.BroughtForward = append(st.BroughtForward, line)
			totals[d.ID] = &models.DepotTotal{DepotID: d.ID, Debit: line.Debit, Credit: line.Credit}
		}
		lines, err := q.StatementLines(orgID, acc.ID, depotID, *from, *to)
		if err != nil {
			return err
		}
		for i := range lines {
			l := &lines[i]
			run += l.Debit - l.Credit
			l.RunningBalance = run
			if t, ok := totals[*l.DepotID]; ok {
				t.Debit += l.Debit
				t.Credit += l.Credit
			}
		}
		st.Lines = lines
		for _, d := range depots {
			t := totals[d.ID]
			t.Balance = t.Debit - t.Credit
			st.DepotTotals = append(st.DepotTotals, *t)
		}
		st.ClosingBalance = run
		res = &StatementResult{Statement: st, Account: acc, Depots: depots}
		return nil
	})
	return res, err
}

func statementDepots(q *repository.Q, orgID uint, depotID *uint) ([]models.Depot, error) {
	if depotID == nil {
		return q.ListDepots(orgID)
	}
	d, err := q.GetDepot(orgID, *depotID)
	if repository.IsNotFound(err) {
		return nil, apierror.DepotNotFound
	}
	if err != nil {
		return nil, err
	}
	return []models.Depot{*d}, nil
}

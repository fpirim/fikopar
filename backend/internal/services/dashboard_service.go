package services

import (
	"context"
	"time"

	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type BalanceTile struct {
	Total    models.Money `json:"total"`
	Accounts int          `json:"accounts"`
}

type DepotCash struct {
	DepotID uint         `json:"depotId"`
	Balance models.Money `json:"balance"`
}

type CashTile struct {
	Total  models.Money `json:"total"`
	Depots []DepotCash  `json:"depots"`
}

type DueTile struct {
	Total        models.Money `json:"total"`
	OverdueCount int          `json:"overdueCount"`
}

type LedgerCheckTile struct {
	Status    string     `json:"status"`
	CheckedAt *time.Time `json:"checkedAt"`
}

type Dashboard struct {
	Date             models.Date                `json:"date"`
	Receivables      BalanceTile                `json:"receivables"`
	Payables         BalanceTile                `json:"payables"`
	Cash             CashTile                   `json:"cash"`
	InstrumentsDue30 DueTile                    `json:"instrumentsDue30"`
	Latest           []repository.Activity      `json:"latest"`
	DueThisWeek      []repository.InstrumentDue `json:"dueThisWeek"`
	BelowMin         []models.StockLevel        `json:"belowMin"`
	BelowMinCount    int64                      `json:"belowMinCount"`
	NegativeCount    int64                      `json:"negativeCount"`
	LedgerCheck      LedgerCheckTile            `json:"ledgerCheck"`
}

// Dashboard gathers the start page tiles as of a date, for one depot or all.
func (s *ReportService) Dashboard(ctx context.Context, orgID uint, depotID uint, on *models.Date) (*Dashboard, error) {
	d := &Dashboard{Date: today(), Cash: CashTile{Depots: []DepotCash{}}}
	if on != nil {
		d.Date = *on
	}
	var depot *uint
	if depotID != 0 {
		depot = &depotID
	}
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		depots, err := statementDepots(q, orgID, depot)
		if err != nil {
			return err
		}
		if d.Receivables.Total, d.Receivables.Accounts, err = q.OpenBalances(orgID, "120.", 1, d.Date, depot); err != nil {
			return err
		}
		if d.Payables.Total, d.Payables.Accounts, err = q.OpenBalances(orgID, "320.", -1, d.Date, depot); err != nil {
			return err
		}
		for _, dp := range depots {
			cash, err := q.RoleAccount(orgID, "CASH", dp.ID)
			if repository.IsNotFound(err) {
				continue
			}
			if err != nil {
				return err
			}
			b, err := q.AccountBalanceAsOf(cash.ID, dp.ID, d.Date)
			if err != nil {
				return err
			}
			d.Cash.Depots = append(d.Cash.Depots, DepotCash{DepotID: dp.ID, Balance: b})
			d.Cash.Total += b
		}
		if d.InstrumentsDue30.Total, d.InstrumentsDue30.OverdueCount, err = q.InstrumentsDue(orgID, d.Date, 30, depot); err != nil {
			return err
		}
		if d.DueThisWeek, err = q.InstrumentsDueList(orgID, d.Date, 7, depot, 20); err != nil {
			return err
		}
		if d.Latest, err = q.LatestActivity(orgID, d.Date, depot, 10); err != nil {
			return err
		}
		rows, err := q.StockLevels(orgID, repository.LevelFilter{DepotID: depotID, Only: "BELOW_MIN_OR_NEGATIVE", Limit: 10})
		if err != nil {
			return err
		}
		d.BelowMin = make([]models.StockLevel, len(rows))
		for i, r := range rows {
			d.BelowMin[i] = r.StockLevel
		}
		if d.BelowMinCount, d.NegativeCount, err = q.StockLevelCounts(orgID, depotID); err != nil {
			return err
		}
		d.LedgerCheck = LedgerCheckTile{Status: "NOT_RUN"}
		lc, err := q.LastLedgerCheck(orgID)
		if lc != nil {
			d.LedgerCheck = LedgerCheckTile{Status: lc.Status, CheckedAt: lc.CheckedAt}
		}
		return err
	})
	return d, err
}

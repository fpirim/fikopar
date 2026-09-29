package services

import (
	"context"
	"math"
	"strconv"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

// Search is the top-bar search: cards (with their current account) and document numbers.
// Parts arrive with the catalog (F2).
func (s *ReportService) Search(ctx context.Context, orgID uint, term string, limit int) (*models.SearchResult, error) {
	term = strings.TrimSpace(term)
	switch {
	case len([]rune(term)) < 2:
		return nil, apierror.WithField(apierror.InvalidRequest, "q")
	case limit == 0:
		limit = 5
	case limit < 1 || limit > 20:
		return nil, apierror.WithField(apierror.InvalidRequest, "limit")
	}
	q := s.store.Read(ctx)
	accounts, err := q.SearchAccounts(orgID, term, limit)
	if err != nil {
		return nil, err
	}
	docs, err := q.SearchDocuments(orgID, term, limit)
	if err != nil {
		return nil, err
	}
	return &models.SearchResult{Accounts: accounts, Parts: []any{}, Documents: docs}, nil
}

// SicilSummary is the credit panel of a card as of a date (today when nil).
func (s *ReportService) SicilSummary(ctx context.Context, orgID, sicilID uint, depotID *uint, asOf *models.Date) (*models.SicilSummary, error) {
	day := today()
	if asOf != nil {
		day = *asOf
	}
	q := s.store.Read(ctx)
	terms, err := q.SicilTerms(orgID, sicilID)
	if repository.IsNotFound(err) {
		return nil, apierror.SicilNotFound
	}
	if err != nil {
		return nil, err
	}
	if _, err := statementDepots(q, orgID, depotID); err != nil {
		return nil, err
	}
	acc, err := q.AccountBySicil(orgID, sicilID)
	if repository.IsNotFound(err) {
		return nil, apierror.AccountNotFound
	}
	if err != nil {
		return nil, err
	}
	rows, _, err := q.Balances(orgID, repository.BalanceQuery{AsOf: day, DepotID: depotID, AccountID: &acc.ID, All: true})
	if err != nil {
		return nil, err
	}
	sum := &models.SicilSummary{SicilID: sicilID, AccountID: acc.ID, AccountCode: acc.Code, CreditLimit: terms.CreditLimit,
		Discount1Pct: models.TrimDecimal(terms.Iskonto), Discount2Pct: models.TrimDecimal(terms.Iskonto2)}
	if len(rows) > 0 {
		sum.Balance, sum.Overdue, sum.LastMoveDate = rows[0].Balance, rows[0].Overdue, rows[0].LastMoveDate
	}
	if l := terms.CreditLimit; l != nil && *l > 0 {
		pct := int(math.Round(float64(sum.Balance) * 100 / float64(*l)))
		sum.CreditUsedPct = &pct
	}
	if days, err := strconv.Atoi(strings.TrimSpace(terms.Gun)); err == nil {
		sum.PaymentTermDays = &days
	}
	sum.ChequesInPortfolio, err = q.ChequesInPortfolio(orgID, sicilID, depotID)
	return sum, err
}

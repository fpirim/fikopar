package services

import (
	"context"
	"regexp"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type DepotService struct {
	store *repository.Store
}

func NewDepotService(store *repository.Store) *DepotService { return &DepotService{store: store} }

var (
	depotCode = regexp.MustCompile(`^[0-9A-Z]{1,2}$`)
	percent   = regexp.MustCompile(`^\d{1,3}(\.\d{1,4})?$`)
)

func (s *DepotService) List(ctx context.Context, orgID uint) ([]models.Depot, error) {
	depots, err := s.store.Read(ctx).ListDepots(orgID)
	for i := range depots {
		depots[i].VatRate = models.TrimDecimal(depots[i].VatRate)
	}
	return depots, err
}

func (s *DepotService) Create(ctx context.Context, orgID uint, d models.Depot) (*models.Depot, error) {
	d.Code = strings.TrimSpace(d.Code)
	d.Name = strings.TrimSpace(d.Name)
	if !depotCode.MatchString(d.Code) {
		return nil, apierror.WithField(apierror.InvalidRequest, "code")
	}
	if d.Name == "" {
		return nil, apierror.WithField(apierror.InvalidRequest, "name")
	}
	if d.VatRate == "" {
		d.VatRate = "0"
	}
	if !percent.MatchString(d.VatRate) {
		return nil, apierror.WithField(apierror.InvalidRequest, "vatRate")
	}
	d.OrgID = orgID
	err := s.store.InTx(ctx, func(q *repository.Q) error { return q.CreateDepot(&d) })
	if repository.IsUniqueViolation(err) {
		return nil, apierror.DepotCodeExists
	}
	if err != nil {
		return nil, err
	}
	d.VatRate = models.TrimDecimal(d.VatRate)
	return &d, nil
}

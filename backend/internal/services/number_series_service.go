package services

import (
	"context"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type NumberSeriesService struct {
	store *repository.Store
}

func NewNumberSeriesService(store *repository.Store) *NumberSeriesService {
	return &NumberSeriesService{store: store}
}

func (s *NumberSeriesService) List(ctx context.Context, orgID uint, f repository.SeriesFilter) ([]models.NumberSeries, error) {
	return s.store.Read(ctx).ListNumberSeries(orgID, f)
}

type SeriesUpdate struct {
	Width     *int   `json:"width"`
	NextValue *int64 `json:"nextValue"`
}

func (s *NumberSeriesService) Update(ctx context.Context, orgID, id uint, version int, in SeriesUpdate) (*models.NumberSeries, error) {
	if in.Width != nil && (*in.Width < 4 || *in.Width > 10) {
		return nil, apierror.WithField(apierror.InvalidRequest, "width")
	}
	var out *models.NumberSeries
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		cur, err := q.GetNumberSeries(orgID, id)
		if repository.IsNotFound(err) {
			return apierror.WithDetail(apierror.InvalidRequest, "number series not found")
		}
		if err != nil {
			return err
		}
		if cur.Version != version {
			return apierror.VersionConflict
		}
		if in.NextValue != nil && *in.NextValue < cur.NextValue {
			return apierror.NumberSeriesBackwards
		}
		out, err = q.UpdateNumberSeries(orgID, id, version, in.Width, in.NextValue)
		if err != nil {
			return err
		}
		if out == nil {
			return apierror.VersionConflict
		}
		return audit(ctx, q, orgID, "number_series.updated", "number_series", id, cur, out)
	})
	return out, err
}

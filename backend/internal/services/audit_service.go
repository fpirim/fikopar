package services

import (
	"context"
	"time"

	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type AuditService struct{ store *repository.Store }

func NewAuditService(store *repository.Store) *AuditService { return &AuditService{store: store} }

type AuditQuery struct {
	EntityType, EntityID string
	ActorUserID          uint
	From, To             *time.Time
	Limit                int
	Cursor               string
}

type AuditPage struct {
	Items      []models.AuditEvent `json:"items"`
	NextCursor *string             `json:"nextCursor"`
}

type auditCursor struct {
	At time.Time `json:"t"`
	ID uint      `json:"i"`
}

func (s *AuditService) List(ctx context.Context, orgID uint, in AuditQuery) (*AuditPage, error) {
	limit := clampLimit(in.Limit)
	f := repository.AuditFilter{EntityType: in.EntityType, EntityID: in.EntityID, ActorUserID: in.ActorUserID,
		From: in.From, To: in.To, Limit: limit + 1}
	var c auditCursor
	ok, err := decodeCursorInto(in.Cursor, &c)
	if err != nil {
		return nil, err
	}
	if ok {
		f.Before = &repository.AuditCursor{At: c.At, ID: c.ID}
	}
	rows, err := s.store.Read(ctx).ListAudit(orgID, f)
	if err != nil {
		return nil, err
	}
	page := &AuditPage{Items: rows}
	if len(rows) > limit {
		page.Items = rows[:limit]
		last := rows[limit-1]
		next := encodeCursor(auditCursor{At: last.OccurredAt, ID: last.ID})
		page.NextCursor = &next
	}
	return page, nil
}

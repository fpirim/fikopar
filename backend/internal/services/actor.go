package services

import (
	"context"
	"encoding/json"
	"strconv"

	"github.com/fpirim/fikopar/backend/internal/repository"
)

// Actor is who made the request; it flows through the context into audit and outbox rows.
type Actor struct {
	UserID    uint
	Admin     bool
	RequestID string
	IP        string
}

type actorKey struct{}

func WithActor(ctx context.Context, a Actor) context.Context {
	return context.WithValue(ctx, actorKey{}, a)
}

func actorFrom(ctx context.Context) Actor {
	a, _ := ctx.Value(actorKey{}).(Actor)
	return a
}

func (a Actor) userID() *uint {
	if a.UserID == 0 {
		return nil
	}
	return &a.UserID
}

func audit(ctx context.Context, q *repository.Q, orgID uint, action, entityType string, entityID uint, before, after any) error {
	a := actorFrom(ctx)
	enc := func(v any) (*string, error) {
		if v == nil {
			return nil, nil
		}
		b, err := json.Marshal(v)
		s := string(b)
		return &s, err
	}
	b, err := enc(before)
	if err != nil {
		return err
	}
	af, err := enc(after)
	if err != nil {
		return err
	}
	return q.AppendAudit(repository.AuditEvent{
		OrgID: orgID, ActorUserID: a.userID(), ActorIP: a.IP, RequestID: a.RequestID,
		Action: action, EntityType: entityType, EntityID: strconv.FormatUint(uint64(entityID), 10),
		Before: b, After: af,
	})
}

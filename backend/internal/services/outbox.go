package services

import (
	"context"
	"encoding/json"
	"log/slog"
	"strconv"
	"sync"
	"time"

	"github.com/fpirim/fikopar/backend/internal/repository"

	"github.com/google/uuid"
)

// Envelope is api/events/envelope.schema.json.
type Envelope struct {
	ID               string          `json:"id"`
	Type             string          `json:"type"`
	Version          int             `json:"version"`
	OccurredAt       time.Time       `json:"occurredAt"`
	OrgID            uint            `json:"orgId"`
	DepotID          uint            `json:"depotId"`
	AggregateType    string          `json:"aggregateType"`
	AggregateID      string          `json:"aggregateId"`
	AggregateVersion *int            `json:"aggregateVersion,omitempty"`
	CorrelationID    *string         `json:"correlationId,omitempty"`
	ActorUserID      *uint           `json:"actorUserId"`
	Payload          json.RawMessage `json:"payload"`
}

// Consumer handles delivered events inside the relay's transaction; the relay records the
// delivery in the same transaction, so a redelivered event is skipped.
type Consumer interface {
	Name() string
	Handles(eventType string) bool
	Handle(ctx context.Context, q *repository.Q, env Envelope) error
}

func emit(ctx context.Context, q *repository.Q, orgID, depotID uint, eventType, aggregateType string, aggregateID uint, aggregateVersion *int, payload any) error {
	body, err := json.Marshal(payload)
	if err != nil {
		return err
	}
	a := actorFrom(ctx)
	var corr *string
	if a.RequestID != "" {
		corr = &a.RequestID
	}
	return q.AppendOutbox(repository.OutboxEvent{
		ID: uuid.NewString(), OrgID: orgID, DepotID: depotID, EventType: eventType, EventVersion: 1,
		AggregateType: aggregateType, AggregateID: strconv.FormatUint(uint64(aggregateID), 10),
		AggregateVersion: aggregateVersion, Payload: string(body), CorrelationID: corr, ActorUserID: a.userID(),
	})
}

type idemKey struct{}

func withIdempotencyKey(ctx context.Context, key string) context.Context {
	return context.WithValue(ctx, idemKey{}, key)
}

func idempotencyKeyFrom(ctx context.Context) *string {
	if k, ok := ctx.Value(idemKey{}).(string); ok && k != "" {
		return &k
	}
	return nil
}

// OutboxRelay delivers unpublished outbox rows at least once, one row per transaction.
type OutboxRelay struct {
	store     *repository.Store
	mu        sync.RWMutex
	consumers []Consumer
}

func NewOutboxRelay(store *repository.Store, consumers ...Consumer) *OutboxRelay {
	return &OutboxRelay{store: store, consumers: consumers}
}

func (r *OutboxRelay) Register(c Consumer) {
	r.mu.Lock()
	defer r.mu.Unlock()
	r.consumers = append(r.consumers, c)
}

// RunOnce delivers one batch and returns how many events were published.
func (r *OutboxRelay) RunOnce(ctx context.Context) (int, error) {
	ids, err := r.store.Read(ctx).PendingOutboxIDs(100)
	if err != nil {
		return 0, err
	}
	published := 0
	for _, id := range ids {
		ok, err := r.deliver(ctx, id)
		if err != nil {
			slog.Warn("outbox delivery failed", "event", id, "err", err)
			if ferr := r.store.InTx(ctx, func(q *repository.Q) error { return q.RecordOutboxFailure(id, err.Error()) }); ferr != nil {
				return published, ferr
			}
			continue
		}
		if ok {
			published++
		}
	}
	return published, nil
}

func (r *OutboxRelay) deliver(ctx context.Context, id string) (bool, error) {
	r.mu.RLock()
	consumers := append([]Consumer(nil), r.consumers...)
	r.mu.RUnlock()
	published := false
	err := r.store.InTx(ctx, func(q *repository.Q) error {
		ev, err := q.LockOutboxEvent(id)
		if err != nil || ev == nil {
			return err
		}
		env := Envelope{
			ID: ev.ID, Type: ev.EventType, Version: ev.EventVersion, OccurredAt: ev.OccurredAt.UTC(),
			OrgID: ev.OrgID, DepotID: ev.DepotID, AggregateType: ev.AggregateType, AggregateID: ev.AggregateID,
			AggregateVersion: ev.AggregateVersion, CorrelationID: ev.CorrelationID, ActorUserID: ev.ActorUserID,
			Payload: json.RawMessage(ev.Payload),
		}
		for _, c := range consumers {
			if !c.Handles(env.Type) {
				continue
			}
			first, err := q.MarkDelivered(env.ID, c.Name())
			if err != nil {
				return err
			}
			if !first {
				continue
			}
			if err := c.Handle(ctx, q, env); err != nil {
				return err
			}
		}
		published = true
		return q.MarkOutboxPublished(id)
	})
	return published, err
}

// Run polls until ctx is done.
func (r *OutboxRelay) Run(ctx context.Context, every time.Duration) {
	t := time.NewTicker(every)
	defer t.Stop()
	for {
		if _, err := r.RunOnce(ctx); err != nil && ctx.Err() == nil {
			slog.Error("outbox relay", "err", err)
		}
		select {
		case <-ctx.Done():
			return
		case <-t.C:
		}
	}
}

// LedgerCheck re-checks every posted entry and the org's balance projection; the dashboard
// reads its last result.
type LedgerCheck struct{}

func (LedgerCheck) Name() string            { return "ledger-check" }
func (LedgerCheck) Handles(typ string) bool { return typ == "LedgerEntryPosted" }

func (LedgerCheck) Handle(_ context.Context, q *repository.Q, env Envelope) error {
	var p struct {
		EntryID uint `json:"entryId"`
	}
	if err := json.Unmarshal(env.Payload, &p); err != nil {
		return err
	}
	bad, err := q.EntryUnbalancedCurrencies(p.EntryID)
	if err != nil {
		return err
	}
	drift, err := q.OrgDriftCount(env.OrgID)
	if err != nil {
		return err
	}
	return q.RecordLedgerCheck(env.OrgID, bad == 0 && drift == 0)
}

package services

import (
	"context"
	"log/slog"
	"time"

	"github.com/fpirim/fikopar/backend/internal/repository"
)

// sweepBatch is how many expired reservations one sweep releases (ADR-0013 §8).
const sweepBatch = 500

// ReservationSweeper releases draft reservations 48 h after their last save.
type ReservationSweeper struct{ store *repository.Store }

func NewReservationSweeper(store *repository.Store) *ReservationSweeper {
	return &ReservationSweeper{store: store}
}

// Sweep releases up to one batch of reservations expired at now and emits one StockReleased EXPIRED per document in
// the same transaction. Rows locked by a save or post in flight are skipped; a row is deleted once, so reruns and
// concurrent sweeps release nothing twice. It returns how many reservations it released.
func (s *ReservationSweeper) Sweep(ctx context.Context, now time.Time) (int, error) {
	var n int
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		keys, err := q.ExpiredReservationKeys(now, sweepBatch)
		if err != nil {
			return err
		}
		if err := q.LockStockLevels(keys); err != nil {
			return err
		}
		rows, err := q.ReleaseExpiredReservations(now, sweepBatch)
		if err != nil {
			return err
		}
		n = len(rows)
		byDoc := map[uint][]repository.ReservationRow{}
		var order []uint
		for _, r := range rows {
			if _, ok := byDoc[r.DocumentID]; !ok {
				order = append(order, r.DocumentID)
			}
			byDoc[r.DocumentID] = append(byDoc[r.DocumentID], r)
		}
		for _, id := range order {
			rs := byDoc[id]
			if err := emitReleased(ctx, q, rs[0].OrgID, rs[0].DepotID, id, "EXPIRED", rs); err != nil {
				return err
			}
		}
		return nil
	})
	return n, err
}

// Run sweeps every interval until ctx is done.
func (s *ReservationSweeper) Run(ctx context.Context, every time.Duration) {
	t := time.NewTicker(every)
	defer t.Stop()
	for {
		if _, err := s.Sweep(ctx, time.Now()); err != nil && ctx.Err() == nil {
			slog.Error("reservation sweeper", "err", err)
		}
		select {
		case <-ctx.Done():
			return
		case <-t.C:
		}
	}
}

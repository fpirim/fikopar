package services

import (
	"context"
	"errors"
	"time"

	"github.com/fpirim/fikopar/backend/internal/repository"
)

// ReservationSweeper releases draft reservations 48 h after their last save.
type ReservationSweeper struct{ store *repository.Store }

func NewReservationSweeper(store *repository.Store) *ReservationSweeper {
	return &ReservationSweeper{store: store}
}

// Sweep releases the reservations expired at now and returns how many rows it released.
func (s *ReservationSweeper) Sweep(ctx context.Context, now time.Time) (int, error) {
	return 0, errors.New("not implemented")
}

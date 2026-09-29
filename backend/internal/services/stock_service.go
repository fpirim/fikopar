package services

import "github.com/fpirim/fikopar/backend/internal/repository"

// StockService owns levels, per-depot settings, the movements ledger and transfers.
type StockService struct {
	store *repository.Store
}

func NewStockService(store *repository.Store) *StockService { return &StockService{store: store} }

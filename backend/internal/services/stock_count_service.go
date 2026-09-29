package services

import "github.com/fpirim/fikopar/backend/internal/repository"

// StockCountService runs count sessions: open (freeze), autosave lines, finalize into ADJUST movements.
type StockCountService struct {
	store *repository.Store
}

func NewStockCountService(store *repository.Store) *StockCountService {
	return &StockCountService{store: store}
}

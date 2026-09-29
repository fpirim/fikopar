package services

import "github.com/fpirim/fikopar/backend/internal/repository"

// PriceImportService runs price list imports: upload, parse, preview diff, apply once.
type PriceImportService struct {
	store *repository.Store
}

func NewPriceImportService(store *repository.Store) *PriceImportService {
	return &PriceImportService{store: store}
}

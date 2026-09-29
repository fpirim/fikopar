package services

import "github.com/fpirim/fikopar/backend/internal/repository"

// CatalogService owns catalogs, parts, codes and list prices.
type CatalogService struct {
	store *repository.Store
}

func NewCatalogService(store *repository.Store) *CatalogService { return &CatalogService{store: store} }

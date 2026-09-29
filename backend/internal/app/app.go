// Package app wires repositories, services, handlers and the router.
package app

import (
	"github.com/fpirim/fikopar/backend/internal/config"
	"github.com/fpirim/fikopar/backend/internal/handlers"
	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/router"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
	"gorm.io/gorm"
)

type App struct {
	Echo        *echo.Echo
	UserService *services.UserService
	Relay       *services.OutboxRelay
	Sweeper     *services.ReservationSweeper
}

func New(db *gorm.DB, cfg *config.Config) *App {
	userRepo := repository.NewUserRepository(db)
	orgRepo := repository.NewOrganizationRepository(db)
	sicilRepo := repository.NewSicilRepository(db)
	authService := services.NewAuthService(cfg)
	userService := services.NewUserService(userRepo, authService, cfg)
	orgService := services.NewOrganizationService(orgRepo, cfg)
	sicilService := services.NewSicilService(sicilRepo)
	store := repository.NewStore(db)
	idem := services.NewIdempotencyService(store)
	receiptService := services.NewReceiptService(store)
	cashService := services.NewCashService(store)
	reports := handlers.NewReportHandler(services.NewReportService(store), receiptService, sicilService)

	e := router.New(router.Handlers{
		Auth:    handlers.NewAuthHandler(userRepo, authService),
		User:    handlers.NewUserHandler(userRepo, userService, authService),
		Admin:   handlers.NewAdminHandler(userService, userRepo),
		Org:     handlers.NewOrganizationHandler(orgService),
		Sicil:   handlers.NewSicilHandler(sicilService, reports),
		Depot:   handlers.NewDepotHandler(services.NewDepotService(store), idem),
		Ledger:  handlers.NewLedgerHandler(services.NewLedgerService(store), services.NewNumberSeriesService(store), idem),
		Receipt: handlers.NewReceiptHandler(receiptService, cashService, idem, reports),
		Report:  reports,
		Catalog: handlers.NewCatalogHandler(services.NewCatalogService(store), idem),
		Stock:   handlers.NewStockHandler(services.NewStockService(store), idem),
		Import:  handlers.NewPriceImportHandler(services.NewPriceImportService(store), idem),
		Count:   handlers.NewStockCountHandler(services.NewStockCountService(store), idem),
	}, router.Services{Auth: authService, Membership: services.NewMembershipService(orgRepo)}, cfg)
	return &App{Echo: e, UserService: userService, Relay: services.NewOutboxRelay(store, services.LedgerCheck{}),
		Sweeper: services.NewReservationSweeper(store)}
}

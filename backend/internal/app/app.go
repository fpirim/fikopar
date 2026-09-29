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

	e := router.New(router.Handlers{
		Auth:  handlers.NewAuthHandler(userRepo, authService),
		User:  handlers.NewUserHandler(userRepo, userService, authService),
		Admin: handlers.NewAdminHandler(userService, userRepo),
		Org:   handlers.NewOrganizationHandler(orgService),
		Sicil: handlers.NewSicilHandler(sicilService),
		Depot: handlers.NewDepotHandler(services.NewDepotService(store), idem),
	}, router.Services{Auth: authService, Membership: services.NewMembershipService(orgRepo)}, cfg)
	return &App{Echo: e, UserService: userService}
}

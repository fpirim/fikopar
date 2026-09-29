package main

import (
	"log/slog"
	"os"

	"fikopar/backend/internal/config"
	"fikopar/backend/internal/db"
	"fikopar/backend/internal/handlers"
	"fikopar/backend/internal/observability"
	"fikopar/backend/internal/repository"
	"fikopar/backend/internal/router"
	"fikopar/backend/internal/services"
)

func main() {
	cfg := config.Load()

	observability.SetupLogger(cfg.Env)
	flush := observability.InitSentry(cfg.SentryDSN, cfg.Env, cfg.SentryRelease)
	defer flush()

	if err := cfg.Validate(); err != nil {
		slog.Error("invalid configuration", slog.Any("error", err))
		os.Exit(1)
	}

	database, err := db.Connect(cfg.DatabaseURL)
	if err != nil {
		slog.Error("failed to connect to database", slog.Any("error", err))
		os.Exit(1)
	}

	userRepo := repository.NewUserRepository(database)
	orgRepo := repository.NewOrganizationRepository(database)
	sicilRepo := repository.NewSicilRepository(database)
	authService := services.NewAuthService(cfg)
	userService := services.NewUserService(userRepo, authService, cfg)
	orgService := services.NewOrganizationService(orgRepo, cfg)
	sicilService := services.NewSicilService(sicilRepo)

	if err := userService.CreateDefaultAdmin(); err != nil {
		slog.Error("failed to create default admin", slog.Any("error", err))
		os.Exit(1)
	}

	authHandler := handlers.NewAuthHandler(userRepo, authService)
	userHandler := handlers.NewUserHandler(userRepo, userService, authService)
	adminHandler := handlers.NewAdminHandler(userService, userRepo)
	orgHandler := handlers.NewOrganizationHandler(orgService)
	sicilHandler := handlers.NewSicilHandler(sicilService)

	e := router.New(authHandler, userHandler, adminHandler, orgHandler, sicilHandler, authService, cfg)

	slog.Info("server starting", slog.String("port", cfg.Port), slog.String("env", cfg.Env))
	if err := e.Start(":" + cfg.Port); err != nil {
		slog.Error("server stopped", slog.Any("error", err))
		os.Exit(1)
	}
}

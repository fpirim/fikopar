package main

import (
	"log/slog"
	"os"

	"github.com/fpirim/fikopar/backend/internal/app"
	"github.com/fpirim/fikopar/backend/internal/config"
	"github.com/fpirim/fikopar/backend/internal/db"
	"github.com/fpirim/fikopar/backend/internal/observability"
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

	a := app.New(database, cfg)
	if err := a.UserService.CreateDefaultAdmin(); err != nil {
		slog.Error("failed to create default admin", slog.Any("error", err))
		os.Exit(1)
	}

	slog.Info("server starting", slog.String("port", cfg.Port), slog.String("env", cfg.Env))
	if err := a.Echo.Start(":" + cfg.Port); err != nil {
		slog.Error("server stopped", slog.Any("error", err))
		os.Exit(1)
	}
}

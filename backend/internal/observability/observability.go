// Package observability wires structured logging and error tracking. Output is
// JSON to stdout (collected by Loki/Promtail = the black box); errors are sent
// to GlitchTip/Sentry when SENTRY_DSN is set, tagged with the environment.
package observability

import (
	"log/slog"
	"os"
	"time"

	"github.com/getsentry/sentry-go"
)

// SetupLogger installs a JSON slog logger as the default. Debug level in
// non-production environments, Info otherwise.
func SetupLogger(env string) *slog.Logger {
	level := slog.LevelInfo
	if env != "production" {
		level = slog.LevelDebug
	}
	logger := slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{Level: level}))
	logger = logger.With(slog.String("env", env))
	slog.SetDefault(logger)
	return logger
}

// InitSentry initializes Sentry/GlitchTip error reporting. Returns a flush
// function (call it on shutdown). No-op when dsn is empty.
func InitSentry(dsn, env, release string) func() {
	if dsn == "" {
		slog.Info("sentry disabled (no SENTRY_DSN)")
		return func() {}
	}
	err := sentry.Init(sentry.ClientOptions{
		Dsn:              dsn,
		Environment:      env,
		Release:          release,
		EnableTracing:    false,
		AttachStacktrace: true,
	})
	if err != nil {
		slog.Error("sentry init failed", slog.Any("error", err))
		return func() {}
	}
	slog.Info("sentry enabled", slog.String("env", env))
	return func() { sentry.Flush(2 * time.Second) }
}

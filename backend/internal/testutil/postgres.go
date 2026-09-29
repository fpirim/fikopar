//go:build integration

// Package testutil provides shared helpers for integration tests. It is only
// compiled under the `integration` build tag and requires Docker at runtime.
package testutil

import (
	"context"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/db"

	"github.com/stretchr/testify/require"
	tcpostgres "github.com/testcontainers/testcontainers-go/modules/postgres"
	"gorm.io/gorm"
)

// NewPostgres spins up a throwaway PostgreSQL container, migrates the schema,
// and returns a connected *gorm.DB plus a cleanup function. It takes no
// *testing.T so it can be used from TestMain to share one container per package.
func NewPostgres(ctx context.Context) (*gorm.DB, func(), error) {
	container, err := tcpostgres.Run(ctx,
		"postgres:16-alpine",
		tcpostgres.WithDatabase("fikopar_test"),
		tcpostgres.WithUsername("test"),
		tcpostgres.WithPassword("test"),
		tcpostgres.BasicWaitStrategies(),
	)
	if err != nil {
		return nil, nil, err
	}
	cleanup := func() { _ = container.Terminate(ctx) }

	dsn, err := container.ConnectionString(ctx, "sslmode=disable")
	if err != nil {
		cleanup()
		return nil, nil, err
	}

	// db.Connect connects (the container is already healthy) and runs Migrate.
	gdb, err := db.Connect(dsn)
	if err != nil {
		cleanup()
		return nil, nil, err
	}
	return gdb, cleanup, nil
}

// SetupTestDB spins up a throwaway PostgreSQL container, migrates the schema,
// and returns a connected *gorm.DB. The container is terminated via t.Cleanup.
func SetupTestDB(t *testing.T) *gorm.DB {
	t.Helper()
	gdb, cleanup, err := NewPostgres(context.Background())
	require.NoError(t, err, "failed to start postgres container")
	t.Cleanup(cleanup)
	return gdb
}

// TruncateAll clears all rows between tests that share a container.
func TruncateAll(t *testing.T, gdb *gorm.DB) {
	t.Helper()
	require.NoError(t, gdb.Exec("TRUNCATE sicils, user_organizations, users, organizations RESTART IDENTITY CASCADE").Error)
}

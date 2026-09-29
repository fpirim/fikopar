//go:build integration

// Package testutil provides shared helpers for integration tests. It is only
// compiled under the `integration` build tag and requires Docker at runtime.
package testutil

import (
	"context"
	"net/url"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/db"

	"github.com/stretchr/testify/require"
	tcpostgres "github.com/testcontainers/testcontainers-go/modules/postgres"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

// NewPostgres spins up a throwaway PostgreSQL container, migrates the schema as the owner,
// and returns a *gorm.DB connected as AppRole plus a cleanup function. It takes no
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
	ownerDSN = dsn

	owner, err = db.Connect(dsn)
	if err == nil {
		err = db.ProvisionAppRole(owner, appPassword)
	}
	if err != nil {
		cleanup()
		return nil, nil, err
	}
	gdb, err := db.Open(appDSN())
	if err != nil {
		cleanup()
		return nil, nil, err
	}
	return gdb, cleanup, nil
}

// Owner is the migrating superuser of the package's container.
func Owner() *gorm.DB { return owner }

func appDSN() string {
	u, _ := url.Parse(ownerDSN)
	u.User = url.UserPassword(AppRole, appPassword)
	return u.String()
}

var (
	ownerDSN string
	owner    *gorm.DB
)

// AppRole is the login the server runs as; migrations run as the owner.
const AppRole = "fikopar_app"

const appPassword = "app"

// App connects to the package's container as AppRole.
func App(t *testing.T) *gorm.DB {
	t.Helper()
	gdb, err := gorm.Open(postgres.Open(appDSN()), &gorm.Config{})
	require.NoError(t, err)
	sqlDB, err := gdb.DB()
	require.NoError(t, err)
	t.Cleanup(func() { _ = sqlDB.Close() })
	return gdb
}

// EmptyDatabase creates an unmigrated database in the package's container and connects to it as the owner.
func EmptyDatabase(t *testing.T, name string) *gorm.DB {
	t.Helper()
	require.NoError(t, owner.Exec(`CREATE DATABASE `+name).Error)
	u, _ := url.Parse(ownerDSN)
	u.Path = "/" + name
	gdb, err := gorm.Open(postgres.Open(u.String()), &gorm.Config{})
	require.NoError(t, err)
	sqlDB, err := gdb.DB()
	require.NoError(t, err)
	t.Cleanup(func() {
		_ = sqlDB.Close()
		owner.Exec(`DROP DATABASE IF EXISTS ` + name + ` WITH (FORCE)`)
	})
	return gdb
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

// TruncateAll clears all rows between tests that share a container, as the owner in replica
// mode so the append-only TRUNCATE guards stay out of the way.
func TruncateAll(t *testing.T) {
	t.Helper()
	require.NoError(t, owner.Transaction(func(tx *gorm.DB) error {
		if err := tx.Exec(`SET LOCAL session_replication_role = replica`).Error; err != nil {
			return err
		}
		return tx.Exec(`TRUNCATE sicils, user_organizations, users, organizations RESTART IDENTITY CASCADE`).Error
	}))
}

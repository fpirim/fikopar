//go:build integration

package repository_test

import (
	"context"
	"fmt"
	"os"
	"testing"

	"fikopar/backend/internal/testutil"

	"gorm.io/gorm"
)

// testDB is a single Postgres container shared across this package's
// integration tests. Each test calls testutil.TruncateAll first for isolation.
var testDB *gorm.DB

func TestMain(m *testing.M) {
	gdb, cleanup, err := testutil.NewPostgres(context.Background())
	if err != nil {
		fmt.Fprintf(os.Stderr, "could not start postgres: %v\n", err)
		os.Exit(1)
	}
	testDB = gdb
	code := m.Run()
	cleanup()
	os.Exit(code)
}

//go:build integration

package router_test

import (
	"context"
	"fmt"
	"os"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/testutil"

	"gorm.io/gorm"
)

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

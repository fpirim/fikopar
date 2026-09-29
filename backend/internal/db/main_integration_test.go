//go:build integration

package db_test

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
	_, cleanup, err := testutil.NewPostgres(context.Background())
	if err != nil {
		fmt.Fprintf(os.Stderr, "could not start postgres: %v\n", err)
		os.Exit(1)
	}
	testDB = testutil.Owner()
	code := m.Run()
	cleanup()
	os.Exit(code)
}

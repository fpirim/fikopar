package db

import (
	"fmt"
	"io/fs"
	"log"
	"sort"
	"strings"
	"time"

	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/migrations"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

func Connect(dsn string) (*gorm.DB, error) {
	var db *gorm.DB
	var err error

	for i := 0; i < 30; i++ {
		db, err = gorm.Open(postgres.Open(dsn), &gorm.Config{})
		if err == nil {
			break
		}
		log.Printf("waiting for database... (%d/30)", i+1)
		time.Sleep(2 * time.Second)
	}
	if err != nil {
		return nil, err
	}

	if err := Migrate(db); err != nil {
		return nil, err
	}
	return db, nil
}

// Migrate brings the identity tables up with AutoMigrate, then applies the SQL files in
// migrations/ once each, in name order, under an advisory lock.
func Migrate(db *gorm.DB) error {
	if err := db.AutoMigrate(&models.User{}, &models.Organization{}, &models.UserOrganization{}, &models.Sicil{}, &models.SicilTelefon{}); err != nil {
		return err
	}
	return applySQL(db, migrations.FS)
}

const migrationLock = 7_310_201

func applySQL(db *gorm.DB, files fs.FS) error {
	names, err := fs.Glob(files, "*.sql")
	if err != nil {
		return err
	}
	sort.Strings(names)
	return db.Transaction(func(tx *gorm.DB) error {
		if err := tx.Exec(`SELECT pg_advisory_xact_lock(?)`, migrationLock).Error; err != nil {
			return err
		}
		if err := tx.Exec(`CREATE TABLE IF NOT EXISTS schema_migrations (
			version text PRIMARY KEY, applied_at timestamptz NOT NULL DEFAULT now())`).Error; err != nil {
			return err
		}
		var applied []string
		if err := tx.Raw(`SELECT version FROM schema_migrations`).Scan(&applied).Error; err != nil {
			return err
		}
		done := make(map[string]bool, len(applied))
		for _, v := range applied {
			done[v] = true
		}
		for _, name := range names {
			version := strings.TrimSuffix(name, ".sql")
			if done[version] {
				continue
			}
			body, err := fs.ReadFile(files, name)
			if err != nil {
				return err
			}
			if err := tx.Exec(string(body)).Error; err != nil {
				return fmt.Errorf("migration %s: %w", name, err)
			}
			if err := tx.Exec(`INSERT INTO schema_migrations (version) VALUES (?)`, version).Error; err != nil {
				return err
			}
		}
		return nil
	})
}

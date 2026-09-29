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

// Connect opens dsn and migrates through it.
func Connect(dsn string) (*gorm.DB, error) {
	db, err := Open(dsn)
	if err != nil {
		return nil, err
	}
	if err := Migrate(db); err != nil {
		return nil, err
	}
	return db, nil
}

// Open connects without migrating, retrying while the server starts.
func Open(dsn string) (*gorm.DB, error) {
	var db *gorm.DB
	var err error
	for i := 0; i < 30; i++ {
		db, err = gorm.Open(postgres.Open(dsn), &gorm.Config{})
		if err == nil {
			return db, nil
		}
		log.Printf("waiting for database... (%d/30)", i+1)
		time.Sleep(2 * time.Second)
	}
	return nil, err
}

// Setup migrates as the owner (ownerDSN), lets fikopar_app log in with appPassword when given,
// and returns the app connection. Without ownerDSN the app DSN migrates itself.
func Setup(appDSN, ownerDSN, appPassword string) (*gorm.DB, error) {
	if ownerDSN == "" {
		return Connect(appDSN)
	}
	owner, err := Connect(ownerDSN)
	if err != nil {
		return nil, err
	}
	if appPassword != "" {
		err = ProvisionAppRole(owner, appPassword)
	}
	if sqlDB, e := owner.DB(); e == nil {
		_ = sqlDB.Close()
	}
	if err != nil {
		return nil, err
	}
	return Open(appDSN)
}

// ProvisionAppRole gives fikopar_app a login with password.
func ProvisionAppRole(owner *gorm.DB, password string) error {
	var stmt string
	if err := owner.Raw(`SELECT format('ALTER ROLE fikopar_app LOGIN PASSWORD %L', ?::text)`, password).Scan(&stmt).Error; err != nil {
		return err
	}
	return owner.Exec(stmt).Error
}

// Migrate brings the identity tables up with AutoMigrate, then applies the SQL files in
// migrations/ once each, in name order, under an advisory lock.
func Migrate(db *gorm.DB) error { return MigrateFS(db, migrations.FS) }

// MigrateFS is Migrate over the given SQL files (a prefix of migrations/ in upgrade tests).
func MigrateFS(db *gorm.DB, files fs.FS) error {
	if err := db.AutoMigrate(&models.User{}, &models.Organization{}, &models.UserOrganization{}, &models.Sicil{}, &models.SicilTelefon{}); err != nil {
		return err
	}
	return ApplySQL(db, files)
}

const migrationLock = 7_310_201

// ApplySQL runs the not yet applied *.sql files of files in one transaction.
func ApplySQL(db *gorm.DB, files fs.FS) error {
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

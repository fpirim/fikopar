package db

import (
	"fikopar/backend/internal/models"
	"log"
	"time"

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

// Migrate applies the schema to an already-open connection. It is shared by
// Connect and by integration tests that bring up their own database.
func Migrate(db *gorm.DB) error {
	return db.AutoMigrate(&models.User{}, &models.Organization{}, &models.UserOrganization{}, &models.Sicil{}, &models.SicilTelefon{})
}

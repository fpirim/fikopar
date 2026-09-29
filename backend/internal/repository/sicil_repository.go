package repository

import (
	"github.com/fpirim/fikopar/backend/internal/models"

	"gorm.io/gorm"
)

type SicilRepository interface {
	Create(card *models.Sicil) error
	GetByID(id uint) (*models.Sicil, error)
	Update(card *models.Sicil) error
	UpdateFields(id uint, fields map[string]any) error
	Delete(id uint) error
	ListForGrid(req GridRequest, organizationID uint) (*GridResponse, error)
	// Called inside the service's transaction so card and phones change atomically.
	ReplaceTelefonlar(sicilID uint, telefonlar []models.SicilTelefon) error
}

type sicilRepo struct {
	db *gorm.DB
}

func NewSicilRepository(db *gorm.DB) SicilRepository {
	return &sicilRepo{db: db}
}

func (r *sicilRepo) Create(card *models.Sicil) error {
	return r.db.Create(card).Error
}

func (r *sicilRepo) GetByID(id uint) (*models.Sicil, error) {
	var card models.Sicil
	if err := r.db.Preload("Telefonlar").First(&card, id).Error; err != nil {
		return nil, err
	}
	return &card, nil
}

func (r *sicilRepo) Update(card *models.Sicil) error {
	return r.db.Save(card).Error
}

// The service layer whitelists fields; organization_id must never reach here.
func (r *sicilRepo) UpdateFields(id uint, fields map[string]any) error {
	if len(fields) == 0 {
		return nil
	}
	return r.db.Model(&models.Sicil{}).Where("id = ?", id).Updates(fields).Error
}

func (r *sicilRepo) Delete(id uint) error {
	return r.db.Delete(&models.Sicil{}, id).Error
}

// The org constraint comes from the service, never from the client filterModel.
func (r *sicilRepo) ListForGrid(req GridRequest, organizationID uint) (*GridResponse, error) {
	colMap := map[string]string{
		"id":         "id",
		"hesap_no":   "hesap_no",
		"hesap_adi":  "hesap_adi",
		"sehir":      "sehir",
		"ilce":       "ilce",
		"sicil_tipi": "sicil_tipi",
		"tc_no":      "tc_no",
		"v_no":       "v_no",
		"created_at": "created_at",
	}
	scoped := r.db.Model(&models.Sicil{}).Where("organization_id = ?", organizationID).Preload("Telefonlar")
	return paginatedGridWithMapping(scoped, req, &[]models.Sicil{}, colMap, []string{"hesap_no", "hesap_adi", "sehir"})
}

func (r *sicilRepo) ReplaceTelefonlar(sicilID uint, telefonlar []models.SicilTelefon) error {
	return r.db.Transaction(func(tx *gorm.DB) error {
		if err := tx.Where("sicil_id = ?", sicilID).Delete(&models.SicilTelefon{}).Error; err != nil {
			return err
		}
		if len(telefonlar) == 0 {
			return nil
		}
		for i := range telefonlar {
			telefonlar[i].ID = 0
			telefonlar[i].SicilID = sicilID
		}
		return tx.Create(&telefonlar).Error
	})
}

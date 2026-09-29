package repository

import (
	"github.com/fpirim/fikopar/backend/internal/models"

	"gorm.io/gorm"
)

type SicilRepository interface {
	Create(card *models.Sicil) error
	GetByID(organizationID, id uint) (*models.Sicil, error)
	UpdateFields(organizationID, id uint, fields map[string]any) error
	Delete(organizationID, id uint) error
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

func (r *sicilRepo) GetByID(organizationID, id uint) (*models.Sicil, error) {
	var card models.Sicil
	if err := r.db.Preload("Telefonlar").Where("organization_id = ?", organizationID).First(&card, id).Error; err != nil {
		return nil, err
	}
	return &card, nil
}

// The service layer whitelists fields; organization_id must never reach here.
func (r *sicilRepo) UpdateFields(organizationID, id uint, fields map[string]any) error {
	if len(fields) == 0 {
		return nil
	}
	return r.db.Model(&models.Sicil{}).Where("organization_id = ? AND id = ?", organizationID, id).Updates(fields).Error
}

func (r *sicilRepo) Delete(organizationID, id uint) error {
	res := r.db.Where("organization_id = ?", organizationID).Delete(&models.Sicil{}, id)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
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

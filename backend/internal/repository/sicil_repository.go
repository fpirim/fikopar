package repository

import (
	"github.com/fpirim/fikopar/backend/internal/models"

	"gorm.io/gorm"
)

type SicilRepository interface {
	Create(card *models.Sicil) error
	GetByID(organizationID, id uint) (*models.Sicil, error)
	// Update applies fields (and, when non-nil, replaces the phones) at the expected version in one
	// transaction; false means no card of the org has that version.
	Update(organizationID, id uint, version int, fields map[string]any, telefonlar *[]models.SicilTelefon) (bool, error)
	Delete(organizationID, id uint) error
	ListForGrid(req GridRequest, organizationID uint) (*GridResponse, error)
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
	if err := r.db.Preload("Telefonlar").Select(`sicils.*, (SELECT u.name FROM users u WHERE u.id = sicils.sales_rep_user_id) AS sales_rep_name`).
		Where("organization_id = ?", organizationID).First(&card, id).Error; err != nil {
		return nil, err
	}
	return &card, nil
}

// The service layer whitelists fields; organization_id must never reach here.
func (r *sicilRepo) Update(organizationID, id uint, version int, fields map[string]any, telefonlar *[]models.SicilTelefon) (bool, error) {
	set := make(map[string]any, len(fields)+1)
	for k, v := range fields {
		set[k] = v
	}
	set["version"] = gorm.Expr("version + 1")
	ok := false
	err := r.db.Transaction(func(tx *gorm.DB) error {
		res := tx.Model(&models.Sicil{}).Where("organization_id = ? AND id = ? AND version = ?", organizationID, id, version).Updates(set)
		if res.Error != nil || res.RowsAffected == 0 {
			return res.Error
		}
		ok = true
		if telefonlar == nil {
			return nil
		}
		return replaceTelefonlar(tx, id, *telefonlar)
	})
	return ok, err
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

func replaceTelefonlar(tx *gorm.DB, sicilID uint, telefonlar []models.SicilTelefon) error {
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
}

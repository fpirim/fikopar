package repository

import (
	"github.com/fpirim/fikopar/backend/internal/models"

	"gorm.io/gorm"
)

type OrganizationRepository interface {
	Create(org *models.Organization) error
	GetByID(id uint) (*models.Organization, error)
	GetAll() ([]models.Organization, error)
	Update(org *models.Organization) error
	Delete(id uint) error
	ListForGrid(req GridRequest) (*GridResponse, error)
	IsMember(userID, orgID uint) (bool, error)
}

type orgRepo struct {
	db *gorm.DB
}

func NewOrganizationRepository(db *gorm.DB) OrganizationRepository {
	return &orgRepo{db: db}
}

func (r *orgRepo) Create(org *models.Organization) error {
	return r.db.Create(org).Error
}

func (r *orgRepo) GetByID(id uint) (*models.Organization, error) {
	var org models.Organization
	if err := r.db.First(&org, id).Error; err != nil {
		return nil, err
	}
	return &org, nil
}

func (r *orgRepo) GetAll() ([]models.Organization, error) {
	var orgs []models.Organization
	if err := r.db.Find(&orgs).Error; err != nil {
		return nil, err
	}
	return orgs, nil
}

func (r *orgRepo) Update(org *models.Organization) error {
	return r.db.Save(org).Error
}

func (r *orgRepo) Delete(id uint) error {
	return r.db.Delete(&models.Organization{}, id).Error
}

func (r *orgRepo) ListForGrid(req GridRequest) (*GridResponse, error) {
	orgColumnMapping := map[string]string{
		"id":         "id",
		"name":       "name",
		"slug":       "slug",
		"created_at": "created_at",
		"updated_at": "updated_at",
	}
	return paginatedGridWithMapping(r.db.Model(&models.Organization{}), req, &[]models.Organization{}, orgColumnMapping, []string{"name", "slug"})
}

func (r *orgRepo) IsMember(userID, orgID uint) (bool, error) {
	var n int64
	err := r.db.Model(&models.UserOrganization{}).
		Where("user_id = ? AND organization_id = ?", userID, orgID).Count(&n).Error
	return n > 0, err
}

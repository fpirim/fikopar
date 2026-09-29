package repository

import (
	"github.com/fpirim/fikopar/backend/internal/models"

	"gorm.io/gorm"
)

type UserRepository interface {
	Create(user *models.User) error
	GetByID(id uint) (*models.User, error)
	GetByEmail(email string) (*models.User, error)
	GetAll() ([]models.User, error)
	Update(user *models.User) error
	Delete(id uint) error
	CountAdmins() (int64, error)
	SetOrganizations(userID uint, orgIDs []uint) error
	ListForGrid(req GridRequest) (*GridResponse, error)
}

type userRepo struct {
	db *gorm.DB
}

func NewUserRepository(db *gorm.DB) UserRepository {
	return &userRepo{db: db}
}

func (r *userRepo) Create(user *models.User) error {
	return r.db.Create(user).Error
}

func (r *userRepo) GetByID(id uint) (*models.User, error) {
	var user models.User
	if err := r.db.Preload("Organizations").First(&user, id).Error; err != nil {
		return nil, err
	}
	return &user, nil
}

func (r *userRepo) GetByEmail(email string) (*models.User, error) {
	var user models.User
	if err := r.db.Preload("Organizations").Where("email = ?", email).First(&user).Error; err != nil {
		return nil, err
	}
	return &user, nil
}

func (r *userRepo) GetAll() ([]models.User, error) {
	var users []models.User
	if err := r.db.Preload("Organizations").Find(&users).Error; err != nil {
		return nil, err
	}
	return users, nil
}

func (r *userRepo) Update(user *models.User) error {
	return r.db.Session(&gorm.Session{FullSaveAssociations: false}).Save(user).Error
}

func (r *userRepo) Delete(id uint) error {
	return r.db.Select("Organizations").Delete(&models.User{}, id).Error
}

func (r *userRepo) CountAdmins() (int64, error) {
	var count int64
	if err := r.db.Model(&models.User{}).Where("role = ?", models.RoleAdmin).Count(&count).Error; err != nil {
		return 0, err
	}
	return count, nil
}

func (r *userRepo) SetOrganizations(userID uint, orgIDs []uint) error {
	user := &models.User{ID: userID}
	var orgs []models.Organization
	if err := r.db.Where("id IN ?", orgIDs).Find(&orgs).Error; err != nil {
		return err
	}
	return r.db.Model(user).Association("Organizations").Replace(orgs)
}

func (r *userRepo) ListForGrid(req GridRequest) (*GridResponse, error) {
	db := r.db.Model(&models.User{}).Preload("Organizations")
	userColumnMapping := map[string]string{
		"id":         "id",
		"name":       "name",
		"email":      "email",
		"role":       "role",
		"created_at": "created_at",
		"updated_at": "updated_at",
	}
	return paginatedGridWithMapping(db, req, &[]models.User{}, userColumnMapping, []string{"name", "email", "role"})
}

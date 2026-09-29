// Package mocks provides testify mocks for the repository interfaces, for use
// in service-layer unit tests (no database required).
package mocks

import (
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"

	"github.com/stretchr/testify/mock"
)

// UserRepository is a mock implementation of repository.UserRepository.
type UserRepository struct {
	mock.Mock
}

var _ repository.UserRepository = (*UserRepository)(nil)

func (m *UserRepository) Create(user *models.User) error {
	return m.Called(user).Error(0)
}

func (m *UserRepository) GetByID(id uint) (*models.User, error) {
	args := m.Called(id)
	if u, ok := args.Get(0).(*models.User); ok {
		return u, args.Error(1)
	}
	return nil, args.Error(1)
}

func (m *UserRepository) GetByEmail(email string) (*models.User, error) {
	args := m.Called(email)
	if u, ok := args.Get(0).(*models.User); ok {
		return u, args.Error(1)
	}
	return nil, args.Error(1)
}

func (m *UserRepository) GetAll() ([]models.User, error) {
	args := m.Called()
	if u, ok := args.Get(0).([]models.User); ok {
		return u, args.Error(1)
	}
	return nil, args.Error(1)
}

func (m *UserRepository) Update(user *models.User) error {
	return m.Called(user).Error(0)
}

func (m *UserRepository) Delete(id uint) error {
	return m.Called(id).Error(0)
}

func (m *UserRepository) CountAdmins() (int64, error) {
	args := m.Called()
	return args.Get(0).(int64), args.Error(1)
}

func (m *UserRepository) SetOrganizations(userID uint, orgIDs []uint) error {
	return m.Called(userID, orgIDs).Error(0)
}

func (m *UserRepository) ListForGrid(req repository.GridRequest) (*repository.GridResponse, error) {
	args := m.Called(req)
	if r, ok := args.Get(0).(*repository.GridResponse); ok {
		return r, args.Error(1)
	}
	return nil, args.Error(1)
}

// OrganizationRepository is a mock implementation of
// repository.OrganizationRepository.
type OrganizationRepository struct {
	mock.Mock
}

var _ repository.OrganizationRepository = (*OrganizationRepository)(nil)

func (m *OrganizationRepository) Create(org *models.Organization) error {
	return m.Called(org).Error(0)
}

func (m *OrganizationRepository) GetByID(id uint) (*models.Organization, error) {
	args := m.Called(id)
	if o, ok := args.Get(0).(*models.Organization); ok {
		return o, args.Error(1)
	}
	return nil, args.Error(1)
}

func (m *OrganizationRepository) GetAll() ([]models.Organization, error) {
	args := m.Called()
	if o, ok := args.Get(0).([]models.Organization); ok {
		return o, args.Error(1)
	}
	return nil, args.Error(1)
}

func (m *OrganizationRepository) Update(org *models.Organization) error {
	return m.Called(org).Error(0)
}

func (m *OrganizationRepository) Delete(id uint) error {
	return m.Called(id).Error(0)
}

func (m *OrganizationRepository) IsMember(userID, orgID uint) (bool, error) {
	args := m.Called(userID, orgID)
	return args.Bool(0), args.Error(1)
}

func (m *OrganizationRepository) ListForGrid(req repository.GridRequest) (*repository.GridResponse, error) {
	args := m.Called(req)
	if r, ok := args.Get(0).(*repository.GridResponse); ok {
		return r, args.Error(1)
	}
	return nil, args.Error(1)
}

// SicilRepository is a mock implementation of
// repository.SicilRepository.
type SicilRepository struct {
	mock.Mock
}

var _ repository.SicilRepository = (*SicilRepository)(nil)

func (m *SicilRepository) Create(card *models.Sicil) error {
	return m.Called(card).Error(0)
}

func (m *SicilRepository) GetByID(organizationID, id uint) (*models.Sicil, error) {
	args := m.Called(organizationID, id)
	if v := args.Get(0); v != nil {
		return v.(*models.Sicil), args.Error(1)
	}
	return nil, args.Error(1)
}

func (m *SicilRepository) UpdateFields(organizationID, id uint, fields map[string]any) error {
	return m.Called(organizationID, id, fields).Error(0)
}

func (m *SicilRepository) Delete(organizationID, id uint) error {
	return m.Called(organizationID, id).Error(0)
}

func (m *SicilRepository) ListForGrid(req repository.GridRequest, organizationID uint) (*repository.GridResponse, error) {
	args := m.Called(req, organizationID)
	if r, ok := args.Get(0).(*repository.GridResponse); ok {
		return r, args.Error(1)
	}
	return nil, args.Error(1)
}

func (m *SicilRepository) ReplaceTelefonlar(sicilID uint, telefonlar []models.SicilTelefon) error {
	return m.Called(sicilID, telefonlar).Error(0)
}

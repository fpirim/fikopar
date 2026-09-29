package services

import (
	"errors"
	"testing"

	"fikopar/backend/internal/config"
	"fikopar/backend/internal/models"
	"fikopar/backend/internal/repository/mocks"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
)

func newOrgService(t *testing.T) (*OrganizationService, *mocks.OrganizationRepository) {
	t.Helper()
	repo := &mocks.OrganizationRepository{}
	svc := NewOrganizationService(repo, &config.Config{LogoUploadDir: t.TempDir()})
	return svc, repo
}

func TestOrgCreate_ValidSlugNormalized(t *testing.T) {
	svc, repo := newOrgService(t)
	repo.On("Create", mock.Anything).Return(nil)

	org, err := svc.Create("Acme", "  ACME-Co  ", nil)
	require.NoError(t, err)
	assert.Equal(t, "acme-co", org.Slug, "slug must be trimmed and lowercased")
	repo.AssertCalled(t, "Create", mock.Anything)
}

func TestOrgCreate_InvalidSlugRejected(t *testing.T) {
	svc, repo := newOrgService(t)

	_, err := svc.Create("Acme", "bad slug!", nil)
	require.Error(t, err)
	repo.AssertNotCalled(t, "Create", mock.Anything)
}

func TestOrgUpdate_InvalidSlugRejected(t *testing.T) {
	svc, repo := newOrgService(t)

	err := svc.Update(&models.Organization{ID: 1}, "Acme", "Has Space", nil)
	require.Error(t, err)
	repo.AssertNotCalled(t, "Update", mock.Anything)
}

func TestOrgDelete_ReturnsLookupError(t *testing.T) {
	svc, repo := newOrgService(t)
	repo.On("GetByID", uint(99)).Return(nil, errors.New("record not found"))

	err := svc.Delete(99)
	require.Error(t, err, "Delete must surface the GetByID error, not report success")
	repo.AssertNotCalled(t, "Delete", mock.Anything)
}

func TestOrgDelete_Succeeds(t *testing.T) {
	svc, repo := newOrgService(t)
	repo.On("GetByID", uint(3)).Return(&models.Organization{ID: 3}, nil)
	repo.On("Delete", uint(3)).Return(nil)

	err := svc.Delete(3)
	require.NoError(t, err)
	repo.AssertCalled(t, "Delete", uint(3))
}

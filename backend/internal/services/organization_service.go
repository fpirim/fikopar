package services

import (
	"io"
	"os"
	"path/filepath"
	"regexp"
	"strings"

	"fikopar/backend/internal/apierror"
	"fikopar/backend/internal/config"
	"fikopar/backend/internal/models"
	"fikopar/backend/internal/repository"

	"github.com/google/uuid"
)

type OrganizationService struct {
	repo repository.OrganizationRepository
	cfg  *config.Config
}

func NewOrganizationService(repo repository.OrganizationRepository, cfg *config.Config) *OrganizationService {
	return &OrganizationService{repo: repo, cfg: cfg}
}

var slugPattern = regexp.MustCompile(`^[a-z0-9]+(?:-[a-z0-9]+)*$`)

func (s *OrganizationService) Create(name, slug string, logoURL *string) (*models.Organization, error) {
	slug = strings.TrimSpace(strings.ToLower(slug))
	if !slugPattern.MatchString(slug) {
		return nil, apierror.SlugInvalid
	}
	org := &models.Organization{
		Name:    name,
		Slug:    slug,
		LogoURL: logoURL,
	}
	if err := s.repo.Create(org); err != nil {
		if isDuplicate(err) {
			return nil, apierror.SlugExists
		}
		return nil, err
	}
	return org, nil
}

func (s *OrganizationService) Update(org *models.Organization, name, slug string, logoURL *string) error {
	slug = strings.TrimSpace(strings.ToLower(slug))
	if !slugPattern.MatchString(slug) {
		return apierror.SlugInvalid
	}
	org.Name = name
	org.Slug = slug
	org.LogoURL = logoURL
	if err := s.repo.Update(org); err != nil {
		if isDuplicate(err) {
			return apierror.SlugExists
		}
		return err
	}
	return nil
}

func (s *OrganizationService) ListForGrid(req repository.GridRequest) (*repository.GridResponse, error) {
	return s.repo.ListForGrid(req)
}

func (s *OrganizationService) GetByID(id uint) (*models.Organization, error) {
	return s.repo.GetByID(id)
}

func (s *OrganizationService) GetAll() ([]models.Organization, error) {
	return s.repo.GetAll()
}

func (s *OrganizationService) Delete(id uint) error {
	org, err := s.repo.GetByID(id)
	if err != nil {
		return err
	}
	if org.LogoURL != nil {
		oldPath := filepath.Join(s.cfg.LogoUploadDir, filepath.Base(*org.LogoURL))
		_ = os.Remove(oldPath)
	}
	return s.repo.Delete(id)
}

func (s *OrganizationService) UploadLogo(org *models.Organization, fileName string, fileContent io.Reader) error {
	if org.LogoURL != nil {
		oldPath := filepath.Join(s.cfg.LogoUploadDir, filepath.Base(*org.LogoURL))
		_ = os.Remove(oldPath)
	}

	if err := os.MkdirAll(s.cfg.LogoUploadDir, 0o750); err != nil {
		return err
	}

	ext := strings.ToLower(filepath.Ext(fileName))
	if ext == "" {
		ext = ".png"
	}
	newFileName := uuid.New().String() + ext
	filePath := filepath.Join(s.cfg.LogoUploadDir, newFileName)

	file, err := os.Create(filePath)
	if err != nil {
		return err
	}
	defer func() { _ = file.Close() }()

	if _, err := io.Copy(file, fileContent); err != nil {
		return err
	}

	logoURL := "/api/uploads/logos/" + newFileName
	org.LogoURL = &logoURL
	return s.repo.Update(org)
}

func (s *OrganizationService) DeleteLogo(org *models.Organization) error {
	if org.LogoURL != nil {
		oldPath := filepath.Join(s.cfg.LogoUploadDir, filepath.Base(*org.LogoURL))
		_ = os.Remove(oldPath)
	}
	org.LogoURL = nil
	return s.repo.Update(org)
}

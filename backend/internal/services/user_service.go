package services

import (
	"io"
	"os"
	"path/filepath"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/config"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"

	"github.com/google/uuid"
)

// isDuplicate detects unique violations independent of the database driver.
func isDuplicate(err error) bool {
	msg := strings.ToLower(err.Error())
	return strings.Contains(msg, "unique") || strings.Contains(msg, "duplicate")
}

type UserService struct {
	repo repository.UserRepository
	auth *AuthService
	cfg  *config.Config
}

func NewUserService(repo repository.UserRepository, auth *AuthService, cfg *config.Config) *UserService {
	return &UserService{repo: repo, auth: auth, cfg: cfg}
}

func (s *UserService) CreateDefaultAdmin() error {
	_, err := s.repo.GetByEmail(s.cfg.AdminDefaultEmail)
	if err == nil {
		return nil
	}

	hash, err := s.auth.HashPassword(s.cfg.AdminDefaultPassword)
	if err != nil {
		return err
	}

	admin := &models.User{
		Email:    s.cfg.AdminDefaultEmail,
		Password: hash,
		Name:     "Admin",
		Role:     models.RoleAdmin,
	}
	return s.repo.Create(admin)
}

func (s *UserService) CreateUser(email, password, name string, role models.Role, orgIDs []uint, avatarURL *string) (*models.User, error) {
	hash, err := s.auth.HashPassword(password)
	if err != nil {
		return nil, err
	}
	user := &models.User{
		Email:     email,
		Password:  hash,
		Name:      name,
		Role:      role,
		AvatarURL: avatarURL,
	}
	if err := s.repo.Create(user); err != nil {
		if isDuplicate(err) {
			return nil, apierror.EmailExists
		}
		return nil, err
	}
	if len(orgIDs) > 0 {
		if err := s.repo.SetOrganizations(user.ID, orgIDs); err != nil {
			return nil, err
		}
	}
	return s.repo.GetByID(user.ID)
}

func (s *UserService) UpdateUser(user *models.User, updates map[string]interface{}) error {
	if name, ok := updates["name"].(string); ok && name != "" {
		user.Name = name
	}
	if email, ok := updates["email"].(string); ok && email != "" {
		user.Email = email
	}
	if password, ok := updates["password"].(string); ok && password != "" {
		hash, err := s.auth.HashPassword(password)
		if err != nil {
			return err
		}
		user.Password = hash
	}
	if role, ok := updates["role"].(string); ok && role != "" {
		user.Role = models.Role(role)
	}
	// Only touch the avatar when the caller explicitly includes the key.
	// Admin edits send avatar_url (possibly nil to clear it); profile edits
	// omit it and must keep the existing avatar instead of wiping it.
	if v, ok := updates["avatar_url"]; ok {
		if ptr, ok := v.(*string); ok {
			user.AvatarURL = ptr
		}
	}
	return s.persist(user)
}

func (s *UserService) persist(user *models.User) error {
	if err := s.repo.Update(user); err != nil {
		if isDuplicate(err) {
			return apierror.EmailExists
		}
		return err
	}
	return nil
}

func (s *UserService) deleteFile(filePath string) {
	_ = os.Remove(filePath)
}

func (s *UserService) UploadAvatar(user *models.User, fileName string, fileContent io.Reader) error {
	if user.AvatarURL != nil {
		oldPath := filepath.Join(s.cfg.UploadDir, filepath.Base(*user.AvatarURL))
		s.deleteFile(oldPath)
	}

	if err := os.MkdirAll(s.cfg.UploadDir, 0o750); err != nil {
		return err
	}

	ext := strings.ToLower(filepath.Ext(fileName))
	if ext == "" {
		ext = ".png"
	}
	newFileName := uuid.New().String() + ext
	filePath := filepath.Join(s.cfg.UploadDir, newFileName)

	file, err := os.Create(filePath)
	if err != nil {
		return err
	}
	defer func() { _ = file.Close() }()

	if _, err := io.Copy(file, fileContent); err != nil {
		return err
	}

	avatarURL := "/api/uploads/avatars/" + newFileName
	user.AvatarURL = &avatarURL
	return s.repo.Update(user)
}

func (s *UserService) DeleteAvatar(user *models.User) error {
	if user.AvatarURL != nil {
		oldPath := filepath.Join(s.cfg.UploadDir, filepath.Base(*user.AvatarURL))
		s.deleteFile(oldPath)
	}
	user.AvatarURL = nil
	return s.repo.Update(user)
}

func (s *UserService) DeleteUser(id uint, currentUserID uint) error {
	user, err := s.repo.GetByID(id)
	if err != nil {
		return err
	}
	if user.ID == currentUserID {
		return apierror.CannotDeleteSelf
	}
	if user.Role == models.RoleAdmin {
		count, err := s.repo.CountAdmins()
		if err != nil {
			return err
		}
		if count <= 1 {
			return apierror.CannotDeleteLastAdmin
		}
	}
	return s.repo.Delete(id)
}

func (s *UserService) SetUserOrganizations(userID uint, orgIDs []uint) error {
	return s.repo.SetOrganizations(userID, orgIDs)
}

func (s *UserService) ListForGrid(req repository.GridRequest) (*repository.GridResponse, error) {
	return s.repo.ListForGrid(req)
}

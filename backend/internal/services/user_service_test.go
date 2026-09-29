package services

import (
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/config"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository/mocks"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
)

func newUserService(t *testing.T) (*UserService, *mocks.UserRepository) {
	t.Helper()
	repo := &mocks.UserRepository{}
	auth := newAuthService("secret", 24)
	svc := NewUserService(repo, auth, &config.Config{UploadDir: t.TempDir()})
	return svc, repo
}

func strptr(s string) *string { return &s }

func TestUpdateUser_PreservesAvatarWhenKeyAbsent(t *testing.T) {
	svc, repo := newUserService(t)
	repo.On("Update", mock.Anything).Return(nil)

	existing := strptr("/api/uploads/avatars/keep.png")
	user := &models.User{ID: 1, Name: "Old", Email: "old@x.com", AvatarURL: existing}

	// Profile-style update: no avatar_url key.
	err := svc.UpdateUser(user, map[string]interface{}{"name": "New", "email": "new@x.com"})
	require.NoError(t, err)

	assert.Equal(t, "New", user.Name)
	assert.Equal(t, "new@x.com", user.Email)
	require.NotNil(t, user.AvatarURL, "avatar must be preserved when avatar_url is not in the update")
	assert.Equal(t, "/api/uploads/avatars/keep.png", *user.AvatarURL)
}

func TestUpdateUser_ClearsAvatarWhenKeyPresentNil(t *testing.T) {
	svc, repo := newUserService(t)
	repo.On("Update", mock.Anything).Return(nil)

	user := &models.User{ID: 1, AvatarURL: strptr("/api/uploads/avatars/x.png")}
	var none *string
	err := svc.UpdateUser(user, map[string]interface{}{"avatar_url": none})
	require.NoError(t, err)
	assert.Nil(t, user.AvatarURL, "explicit nil avatar_url must clear the avatar")
}

func TestUpdateUser_SetsAvatarWhenKeyPresentValue(t *testing.T) {
	svc, repo := newUserService(t)
	repo.On("Update", mock.Anything).Return(nil)

	user := &models.User{ID: 1}
	err := svc.UpdateUser(user, map[string]interface{}{"avatar_url": strptr("/api/uploads/avatars/new.png")})
	require.NoError(t, err)
	require.NotNil(t, user.AvatarURL)
	assert.Equal(t, "/api/uploads/avatars/new.png", *user.AvatarURL)
}

func TestUpdateUser_IgnoresEmptyNameEmail(t *testing.T) {
	svc, repo := newUserService(t)
	repo.On("Update", mock.Anything).Return(nil)

	user := &models.User{ID: 1, Name: "Keep", Email: "keep@x.com"}
	err := svc.UpdateUser(user, map[string]interface{}{"name": "", "email": ""})
	require.NoError(t, err)
	assert.Equal(t, "Keep", user.Name, "empty name must not overwrite")
	assert.Equal(t, "keep@x.com", user.Email, "empty email must not overwrite")
}

func TestUpdateUser_PasswordHashedWhenSet(t *testing.T) {
	svc, repo := newUserService(t)
	repo.On("Update", mock.Anything).Return(nil)

	user := &models.User{ID: 1, Password: "old-hash"}
	err := svc.UpdateUser(user, map[string]interface{}{"password": "newpass"})
	require.NoError(t, err)
	assert.NotEqual(t, "old-hash", user.Password)
	assert.NotEqual(t, "newpass", user.Password, "password must be hashed, not stored plain")
	assert.True(t, svc.auth.CheckPassword(user.Password, "newpass"))

	// Empty password leaves the existing hash untouched.
	prev := user.Password
	err = svc.UpdateUser(user, map[string]interface{}{"password": ""})
	require.NoError(t, err)
	assert.Equal(t, prev, user.Password)
}

func TestDeleteUser_CannotDeleteSelf(t *testing.T) {
	svc, repo := newUserService(t)
	repo.On("GetByID", uint(5)).Return(&models.User{ID: 5, Role: models.RoleUser}, nil)

	err := svc.DeleteUser(5, 5)
	require.Error(t, err)
	assert.ErrorIs(t, err, apierror.CannotDeleteSelf)
	repo.AssertNotCalled(t, "Delete", mock.Anything)
}

func TestDeleteUser_CannotDeleteLastAdmin(t *testing.T) {
	svc, repo := newUserService(t)
	repo.On("GetByID", uint(2)).Return(&models.User{ID: 2, Role: models.RoleAdmin}, nil)
	repo.On("CountAdmins").Return(int64(1), nil)

	err := svc.DeleteUser(2, 1)
	require.Error(t, err)
	assert.ErrorIs(t, err, apierror.CannotDeleteLastAdmin)
	repo.AssertNotCalled(t, "Delete", mock.Anything)
}

func TestDeleteUser_Succeeds(t *testing.T) {
	svc, repo := newUserService(t)
	repo.On("GetByID", uint(2)).Return(&models.User{ID: 2, Role: models.RoleAdmin}, nil)
	repo.On("CountAdmins").Return(int64(3), nil)
	repo.On("Delete", uint(2)).Return(nil)

	err := svc.DeleteUser(2, 1)
	require.NoError(t, err)
	repo.AssertCalled(t, "Delete", uint(2))
}

func TestUploadAndDeleteAvatar_WritesAndRemovesFile(t *testing.T) {
	svc, repo := newUserService(t)
	repo.On("Update", mock.Anything).Return(nil)

	user := &models.User{ID: 1}
	err := svc.UploadAvatar(user, "pic.png", strings.NewReader("imagebytes"))
	require.NoError(t, err)
	require.NotNil(t, user.AvatarURL)
	assert.True(t, strings.HasPrefix(*user.AvatarURL, "/api/uploads/avatars/"))

	// The file should exist on disk under UploadDir.
	stored := filepath.Join(svc.cfg.UploadDir, filepath.Base(*user.AvatarURL))
	_, statErr := os.Stat(stored)
	require.NoError(t, statErr, "uploaded avatar file should exist")

	err = svc.DeleteAvatar(user)
	require.NoError(t, err)
	assert.Nil(t, user.AvatarURL)
	_, statErr = os.Stat(stored)
	assert.True(t, os.IsNotExist(statErr), "avatar file should be removed on delete")
}

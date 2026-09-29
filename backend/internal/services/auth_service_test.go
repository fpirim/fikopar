package services

import (
	"testing"

	"github.com/fpirim/fikopar/backend/internal/config"
	"github.com/fpirim/fikopar/backend/internal/models"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"golang.org/x/crypto/bcrypt"
)

func newAuthService(secret string, expiryHours int) *AuthService {
	return NewAuthService(&config.Config{JWTSecret: secret, JWTExpiryHours: expiryHours})
}

func TestHashAndCheckPassword(t *testing.T) {
	s := newAuthService("secret", 24)

	hash, err := s.HashPassword("hunter2")
	require.NoError(t, err)
	assert.NotEqual(t, "hunter2", hash, "password must not be stored in plain text")

	assert.True(t, s.CheckPassword(hash, "hunter2"))
	assert.False(t, s.CheckPassword(hash, "wrong"))
}

func TestHashPassword_UsesConfiguredCost(t *testing.T) {
	for cost, want := range map[int]int{0: bcrypt.DefaultCost, bcrypt.MinCost: bcrypt.MinCost} {
		hash, err := NewAuthService(&config.Config{BcryptCost: cost}).HashPassword("pw")
		require.NoError(t, err)
		got, err := bcrypt.Cost([]byte(hash))
		require.NoError(t, err)
		assert.Equal(t, want, got)
	}
}

func TestGenerateAndParseToken_RoundTrip(t *testing.T) {
	s := newAuthService("secret", 24)
	user := &models.User{
		ID:    7,
		Email: "a@b.com",
		Role:  models.RoleAdmin,
		Organizations: []models.Organization{
			{ID: 1}, {ID: 2},
		},
	}

	token, err := s.GenerateToken(user)
	require.NoError(t, err)

	claims, err := s.ParseToken(token)
	require.NoError(t, err)
	assert.Equal(t, uint(7), claims.UserID)
	assert.Equal(t, "a@b.com", claims.Email)
	assert.Equal(t, string(models.RoleAdmin), claims.Role)
	assert.Equal(t, []uint{1, 2}, claims.OrganizationIDs)
}

func TestParseToken_WrongSecretRejected(t *testing.T) {
	signer := newAuthService("secret-a", 24)
	verifier := newAuthService("secret-b", 24)

	token, err := signer.GenerateToken(&models.User{ID: 1, Role: models.RoleUser})
	require.NoError(t, err)

	_, err = verifier.ParseToken(token)
	assert.Error(t, err, "token signed with a different secret must be rejected")
}

func TestParseToken_ExpiredRejected(t *testing.T) {
	s := newAuthService("secret", -1) // already expired on issue

	token, err := s.GenerateToken(&models.User{ID: 1, Role: models.RoleUser})
	require.NoError(t, err)

	_, err = s.ParseToken(token)
	assert.Error(t, err, "expired token must be rejected")
}

func TestParseToken_GarbageRejected(t *testing.T) {
	s := newAuthService("secret", 24)
	_, err := s.ParseToken("not-a-jwt")
	assert.Error(t, err)
}

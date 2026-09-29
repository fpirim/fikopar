package config

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestValidate_ProductionRejectsDefaultSecret(t *testing.T) {
	cfg := &Config{Env: "production", JWTSecret: defaultJWTSecret, AdminDefaultPassword: "strong"}
	err := cfg.Validate()
	require.Error(t, err)
	assert.Contains(t, err.Error(), "production")
}

func TestValidate_ProductionRejectsDefaultAdminPassword(t *testing.T) {
	cfg := &Config{Env: "production", JWTSecret: "strong", AdminDefaultPassword: defaultAdminPassword}
	err := cfg.Validate()
	require.Error(t, err)
}

func TestValidate_ProductionPassesWithStrongSecrets(t *testing.T) {
	cfg := &Config{Env: "production", JWTSecret: "strong-secret", AdminDefaultPassword: "strong-pass"}
	assert.NoError(t, cfg.Validate())
}

func TestValidate_DevelopmentOnlyWarns(t *testing.T) {
	cfg := &Config{Env: "development", JWTSecret: defaultJWTSecret, AdminDefaultPassword: defaultAdminPassword}
	assert.NoError(t, cfg.Validate(), "defaults in development should warn, not error")
}

func TestGetEnv(t *testing.T) {
	assert.Equal(t, "fallback", getEnv("FIKOPAR_TEST_UNSET", "fallback"))

	t.Setenv("FIKOPAR_TEST_SET", "value")
	assert.Equal(t, "value", getEnv("FIKOPAR_TEST_SET", "fallback"))

	t.Setenv("FIKOPAR_TEST_EMPTY", "")
	assert.Equal(t, "fallback", getEnv("FIKOPAR_TEST_EMPTY", "fallback"),
		"empty env value should be treated as unset")
}

func TestDefaultDatabaseURL(t *testing.T) {
	// No password -> credentials are just the user (no ":pass@").
	dsn := defaultDatabaseURL()
	assert.Contains(t, dsn, "postgres://fikopar@")
	assert.NotContains(t, dsn, "fikopar:")

	// With DB_PASSWORD -> included in the credentials.
	t.Setenv("DB_PASSWORD", "secret")
	t.Setenv("DB_USER", "bob")
	t.Setenv("DB_HOST", "dbhost")
	t.Setenv("DB_NAME", "mydb")
	dsn = defaultDatabaseURL()
	assert.Contains(t, dsn, "postgres://bob:secret@dbhost:5432/mydb")
}

func TestLoad_ReadsEnv(t *testing.T) {
	t.Setenv("PORT", "9999")
	t.Setenv("JWT_SECRET", "from-env")
	cfg := Load()
	assert.Equal(t, "9999", cfg.Port)
	assert.Equal(t, "from-env", cfg.JWTSecret)
}

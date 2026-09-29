package config

import (
	"fmt"
	"log"
	"os"
	"strconv"

	"github.com/joho/godotenv"
)

const (
	defaultJWTSecret     = "change-me-in-production"
	defaultAdminPassword = "admin123"
)

type Config struct {
	Port                 string
	Env                  string
	DatabaseURL          string
	JWTSecret            string
	JWTExpiryHours       int
	UploadDir            string
	LogoUploadDir        string
	AdminDefaultEmail    string
	AdminDefaultPassword string
	CORSAllowedOrigins   string
	SentryDSN            string
	SentryRelease        string
	Version              string
	StaticDir            string
}

func Load() *Config {
	_ = godotenv.Load()

	jwtExpiry, _ := strconv.Atoi(getEnv("JWT_EXPIRY_HOURS", "24"))

	return &Config{
		Port:                 getEnv("PORT", "8080"),
		Env:                  getEnv("APP_ENV", "development"),
		DatabaseURL:          getEnv("DATABASE_URL", defaultDatabaseURL()),
		JWTSecret:            getEnv("JWT_SECRET", defaultJWTSecret),
		JWTExpiryHours:       jwtExpiry,
		UploadDir:            getEnv("UPLOAD_DIR", "uploads/avatars"),
		LogoUploadDir:        getEnv("LOGO_UPLOAD_DIR", "uploads/logos"),
		AdminDefaultEmail:    getEnv("ADMIN_DEFAULT_EMAIL", "admin@fikopar.local"),
		AdminDefaultPassword: getEnv("ADMIN_DEFAULT_PASSWORD", defaultAdminPassword),
		CORSAllowedOrigins:   getEnv("CORS_ALLOWED_ORIGINS", "*"),
		SentryDSN:            getEnv("SENTRY_DSN", ""),
		SentryRelease:        getEnv("SENTRY_RELEASE", ""),
		Version:              getEnv("APP_VERSION", "dev"),
		StaticDir:            getEnv("STATIC_DIR", ""),
	}
}

// Validate checks for insecure default credentials. In production
// (APP_ENV=production) it returns an error so the server refuses to start;
// otherwise it only logs warnings so local development stays frictionless.
func (c *Config) Validate() error {
	var issues []string
	if c.JWTSecret == defaultJWTSecret {
		issues = append(issues, "JWT_SECRET is using the insecure default value")
	}
	if c.AdminDefaultPassword == defaultAdminPassword {
		issues = append(issues, "ADMIN_DEFAULT_PASSWORD is using the insecure default value")
	}
	if c.Env == "production" && len(issues) > 0 {
		return fmt.Errorf("refusing to start in production: %v", issues)
	}
	for _, issue := range issues {
		log.Printf("WARNING: %s — set it via the environment before deploying", issue)
	}
	return nil
}

// defaultDatabaseURL assembles a development DSN from individual env vars so no
// database password is hardcoded in source. DB_PASSWORD is omitted when unset;
// production deployments should set DATABASE_URL (or DB_PASSWORD) explicitly.
func defaultDatabaseURL() string {
	user := getEnv("DB_USER", "fikopar")
	host := getEnv("DB_HOST", "localhost")
	port := getEnv("DB_PORT", "5432")
	name := getEnv("DB_NAME", "fikopar")
	cred := user
	if pass := os.Getenv("DB_PASSWORD"); pass != "" {
		cred = user + ":" + pass
	}
	return fmt.Sprintf("postgres://%s@%s:%s/%s?sslmode=disable", cred, host, port, name)
}

func getEnv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}

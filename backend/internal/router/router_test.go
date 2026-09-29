package router

import (
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"strings"
	"testing"

	"fikopar/backend/internal/config"
	"fikopar/backend/internal/handlers"
	"fikopar/backend/internal/services"
)

func newTestRouter(t *testing.T, cfg *config.Config) http.Handler {
	t.Helper()
	cfg.JWTSecret = "test-secret"
	authSvc := services.NewAuthService(cfg)
	return New(&handlers.AuthHandler{}, &handlers.UserHandler{}, &handlers.AdminHandler{},
		&handlers.OrganizationHandler{}, &handlers.SicilHandler{}, authSvc, cfg)
}

func TestHealthzReportsVersion(t *testing.T) {
	h := newTestRouter(t, &config.Config{Version: "1.2.3", CORSAllowedOrigins: "*"})
	rec := httptest.NewRecorder()
	h.ServeHTTP(rec, httptest.NewRequest(http.MethodGet, "/healthz", nil))
	if rec.Code != http.StatusOK {
		t.Fatalf("status = %d, want 200", rec.Code)
	}
	if !strings.Contains(rec.Body.String(), `"version":"1.2.3"`) {
		t.Fatalf("body %q does not contain version", rec.Body.String())
	}
}

func TestStaticDirServesSPAWithFallback(t *testing.T) {
	dir := t.TempDir()
	if err := os.WriteFile(filepath.Join(dir, "index.html"), []byte("<html>spa</html>"), 0o600); err != nil {
		t.Fatal(err)
	}
	h := newTestRouter(t, &config.Config{StaticDir: dir, CORSAllowedOrigins: "*"})

	for _, path := range []string{"/", "/sicil/42"} {
		rec := httptest.NewRecorder()
		h.ServeHTTP(rec, httptest.NewRequest(http.MethodGet, path, nil))
		if rec.Code != http.StatusOK || !strings.Contains(rec.Body.String(), "spa") {
			t.Fatalf("%s: status=%d body=%q, want SPA index", path, rec.Code, rec.Body.String())
		}
	}

	rec := httptest.NewRecorder()
	h.ServeHTTP(rec, httptest.NewRequest(http.MethodGet, "/api/does-not-exist", nil))
	if strings.Contains(rec.Body.String(), "spa") {
		t.Fatalf("/api path served SPA index")
	}
}

func TestNoStaticDirReturns404ForUnknownPaths(t *testing.T) {
	h := newTestRouter(t, &config.Config{CORSAllowedOrigins: "*"})
	rec := httptest.NewRecorder()
	h.ServeHTTP(rec, httptest.NewRequest(http.MethodGet, "/sicil/42", nil))
	if rec.Code != http.StatusNotFound {
		t.Fatalf("status = %d, want 404", rec.Code)
	}
}

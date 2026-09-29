package router

import (
	"net/http"
	"strings"

	"fikopar/backend/internal/config"
	"fikopar/backend/internal/handlers"
	appMiddleware "fikopar/backend/internal/middleware"
	"fikopar/backend/internal/models"
	"fikopar/backend/internal/services"

	"github.com/labstack/echo-contrib/echoprometheus"
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/collectors"
)

func New(
	authHandler *handlers.AuthHandler,
	userHandler *handlers.UserHandler,
	adminHandler *handlers.AdminHandler,
	orgHandler *handlers.OrganizationHandler,
	sicilHandler *handlers.SicilHandler,
	authService *services.AuthService,
	cfg *config.Config,
) *echo.Echo {
	e := echo.New()

	e.Use(middleware.RequestID())
	e.Use(appMiddleware.SlogLogger())
	e.Use(middleware.Recover())
	if cfg.SentryDSN != "" {
		e.Use(appMiddleware.Sentry())
	}
	// Per-instance registry (avoids global duplicate-registration when multiple
	// routers are built, e.g. in tests) with Go runtime + process collectors.
	reg := prometheus.NewRegistry()
	reg.MustRegister(collectors.NewGoCollector())
	reg.MustRegister(collectors.NewProcessCollector(collectors.ProcessCollectorOpts{}))
	e.Use(echoprometheus.NewMiddlewareWithConfig(echoprometheus.MiddlewareConfig{
		Subsystem:  "fikopar",
		Registerer: reg,
	}))
	e.Use(middleware.CORSWithConfig(middleware.CORSConfig{
		AllowOrigins:     []string{cfg.CORSAllowedOrigins},
		AllowMethods:     []string{echo.GET, echo.POST, echo.PUT, echo.DELETE, echo.OPTIONS},
		AllowHeaders:     []string{echo.HeaderOrigin, echo.HeaderContentType, echo.HeaderAccept, echo.HeaderAuthorization},
		AllowCredentials: true,
	}))

	// Prometheus metrics (scraped internally; restrict at the proxy in prod).
	e.GET("/metrics", echoprometheus.NewHandlerWithConfig(echoprometheus.HandlerConfig{Gatherer: reg}))

	e.GET("/healthz", func(c echo.Context) error {
		return c.JSON(http.StatusOK, map[string]string{"status": "ok", "version": cfg.Version})
	})

	// Public
	e.POST("/api/auth/login", authHandler.Login)
	e.GET("/api/uploads/avatars/:filename", userHandler.GetAvatar)
	e.GET("/api/uploads/logos/:filename", orgHandler.GetLogo)

	// Authenticated
	api := e.Group("/api")
	api.Use(appMiddleware.JWT(authService))
	api.GET("/users/me", userHandler.Me)
	api.PUT("/users/me", userHandler.UpdateMe)
	api.POST("/users/me/avatar", userHandler.UploadAvatar)
	api.DELETE("/users/me/avatar", userHandler.DeleteMyAvatar)

	// Org membership is enforced in the service layer.
	const sicilIDPath = "/sicil/:id"
	api.POST("/sicil/grid", sicilHandler.Grid)
	api.POST("/sicil", sicilHandler.Create)
	api.GET(sicilIDPath, sicilHandler.Get)
	api.PUT(sicilIDPath, sicilHandler.Update)
	api.DELETE(sicilIDPath, sicilHandler.Delete)

	// Admin only
	admin := api.Group("/admin")
	admin.Use(appMiddleware.RequireRole(models.RoleAdmin))
	admin.GET("/users", adminHandler.ListUsers)
	admin.POST("/users", adminHandler.CreateUser)
	admin.POST("/users/grid", adminHandler.ListUsersGrid)
	admin.PUT("/users/:id", adminHandler.UpdateUser)
	admin.DELETE("/users/:id", adminHandler.DeleteUser)
	admin.POST("/users/:id/avatar", adminHandler.UploadUserAvatar)
	admin.DELETE("/users/:id/avatar", adminHandler.DeleteUserAvatar)

	admin.GET("/organizations", orgHandler.List)
	admin.POST("/organizations", orgHandler.Create)
	admin.POST("/organizations/grid", orgHandler.Grid)
	admin.PUT("/organizations/:id", orgHandler.Update)
	admin.DELETE("/organizations/:id", orgHandler.Delete)
	admin.POST("/organizations/:id/logo", orgHandler.UploadLogo)
	admin.DELETE("/organizations/:id/logo", orgHandler.DeleteLogo)

	if cfg.StaticDir != "" {
		e.Use(middleware.StaticWithConfig(middleware.StaticConfig{
			Root:  cfg.StaticDir,
			HTML5: true,
			Skipper: func(c echo.Context) bool {
				p := c.Request().URL.Path
				return strings.HasPrefix(p, "/api/") || p == "/metrics" || p == "/healthz"
			},
		}))
	}

	return e
}

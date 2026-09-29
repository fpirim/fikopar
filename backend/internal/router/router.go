package router

import (
	"net/http"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/config"
	"github.com/fpirim/fikopar/backend/internal/handlers"
	appMiddleware "github.com/fpirim/fikopar/backend/internal/middleware"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo-contrib/echoprometheus"
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/collectors"
)

type Handlers struct {
	Auth    *handlers.AuthHandler
	User    *handlers.UserHandler
	Admin   *handlers.AdminHandler
	Org     *handlers.OrganizationHandler
	Sicil   *handlers.SicilHandler
	Depot   *handlers.DepotHandler
	Ledger  *handlers.LedgerHandler
	Receipt *handlers.ReceiptHandler
	Report  *handlers.ReportHandler
}

type Services struct {
	Auth       *services.AuthService
	Membership *services.MembershipService
}

func New(h Handlers, svc Services, cfg *config.Config) *echo.Echo {
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
		AllowMethods:     []string{echo.GET, echo.POST, echo.PUT, echo.PATCH, echo.DELETE, echo.OPTIONS},
		AllowHeaders:     []string{echo.HeaderOrigin, echo.HeaderContentType, echo.HeaderAccept, echo.HeaderAuthorization, "Idempotency-Key", "If-Match"},
		ExposeHeaders:    []string{"ETag", "Idempotent-Replayed"},
		AllowCredentials: true,
	}))

	// Prometheus metrics (scraped internally; restrict at the proxy in prod).
	e.GET("/metrics", echoprometheus.NewHandlerWithConfig(echoprometheus.HandlerConfig{Gatherer: reg}))

	e.GET("/healthz", func(c echo.Context) error {
		return c.JSON(http.StatusOK, map[string]string{"status": "ok", "version": cfg.Version})
	})

	// Public
	e.POST("/api/auth/login", h.Auth.Login)
	e.GET("/api/uploads/avatars/:filename", h.User.GetAvatar)
	e.GET("/api/uploads/logos/:filename", h.Org.GetLogo)

	// Authenticated
	api := e.Group("/api")
	api.Use(appMiddleware.JWT(svc.Auth))
	api.GET("/users/me", h.User.Me)
	api.PUT("/users/me", h.User.UpdateMe)
	api.POST("/users/me/avatar", h.User.UploadAvatar)
	api.DELETE("/users/me/avatar", h.User.DeleteMyAvatar)

	org := api.Group("/orgs/:orgId")
	org.Use(appMiddleware.RequireOrgMember(svc.Membership))
	orgAdmin := appMiddleware.RequireRole(models.RoleAdmin)

	const sicilIDPath = "/sicils/:sicilId"
	org.POST("/sicils/grid", h.Sicil.Grid)
	org.POST("/sicils", h.Sicil.Create)
	org.GET(sicilIDPath, h.Sicil.Get)
	org.PUT(sicilIDPath, h.Sicil.Update)
	org.DELETE(sicilIDPath, h.Sicil.Delete)
	org.GET(sicilIDPath+"/summary", h.Report.SicilSummary)
	org.GET("/search", h.Report.Search)

	org.GET("/depots", h.Depot.List)
	org.POST("/depots", h.Depot.Create, orgAdmin)

	org.GET("/number-series", h.Ledger.ListNumberSeries)
	org.PATCH("/number-series/:seriesId", h.Ledger.UpdateNumberSeries, orgAdmin)

	const accountPath = "/ledger/accounts/:accountId"
	org.GET("/ledger/accounts", h.Ledger.ListAccounts)
	org.POST("/ledger/accounts", h.Ledger.CreateAccount)
	org.GET(accountPath, h.Ledger.GetAccount)
	org.GET(accountPath+"/statement", h.Report.Statement)
	org.POST("/ledger/balances/grid", h.Report.Balances)
	org.PATCH(accountPath, h.Ledger.UpdateAccount)
	org.GET("/ledger/account-roles", h.Ledger.ListAccountRoles)
	org.PUT("/ledger/account-roles", h.Ledger.SetAccountRoles, orgAdmin)

	const cfPath = "/ledger/carry-forwards/:year"
	org.GET("/ledger/opening-balances", h.Ledger.ListOpeningBalances)
	org.GET(cfPath, h.Ledger.GetCarryForward)
	org.PUT(cfPath+"/overrides", h.Ledger.SetCarryForwardOverride, orgAdmin)
	org.DELETE(cfPath+"/overrides", h.Ledger.DeleteCarryForwardOverride, orgAdmin)
	org.POST(cfPath+"/post", h.Ledger.PostCarryForward, orgAdmin)
	org.POST(cfPath+"/reopen", h.Ledger.ReopenCarryForward, orgAdmin)

	const entryPath = "/ledger/entries/:entryId"
	org.GET("/ledger/entries", h.Ledger.ListEntries)
	org.POST("/ledger/entries", h.Ledger.PostEntry)
	org.GET(entryPath, h.Ledger.GetEntry)
	org.GET(entryPath+"/trail", h.Ledger.EntryTrail)
	org.POST(entryPath+"/reversal", h.Ledger.ReverseEntry)

	org.POST("/receipts/grid", h.Receipt.Grid)
	org.POST("/receipts", h.Receipt.Create)
	org.GET("/receipts/:receiptId", h.Receipt.Get)
	org.POST("/receipts/:receiptId/void", h.Receipt.Void)
	org.GET("/cash-book", h.Receipt.CashBook)
	org.POST("/cash-vouchers", h.Receipt.CreateVoucher)
	org.POST("/cash-vouchers/:voucherId/void", h.Receipt.VoidVoucher)
	org.GET("/cash-counts", h.Receipt.ListCounts)
	org.POST("/cash-counts", h.Receipt.CreateCount)

	// Admin only
	admin := api.Group("/admin")
	admin.Use(appMiddleware.RequireRole(models.RoleAdmin))
	admin.GET("/users", h.Admin.ListUsers)
	admin.POST("/users", h.Admin.CreateUser)
	admin.POST("/users/grid", h.Admin.ListUsersGrid)
	admin.PUT("/users/:id", h.Admin.UpdateUser)
	admin.DELETE("/users/:id", h.Admin.DeleteUser)
	admin.POST("/users/:id/avatar", h.Admin.UploadUserAvatar)
	admin.DELETE("/users/:id/avatar", h.Admin.DeleteUserAvatar)

	admin.GET("/organizations", h.Org.List)
	admin.POST("/organizations", h.Org.Create)
	admin.POST("/organizations/grid", h.Org.Grid)
	admin.PUT("/organizations/:id", h.Org.Update)
	admin.DELETE("/organizations/:id", h.Org.Delete)
	admin.POST("/organizations/:id/logo", h.Org.UploadLogo)
	admin.DELETE("/organizations/:id/logo", h.Org.DeleteLogo)

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

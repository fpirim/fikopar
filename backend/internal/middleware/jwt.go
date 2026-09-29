package middleware

import (
	"fikopar/backend/internal/apierror"
	"fikopar/backend/internal/services"
	"strings"

	"github.com/labstack/echo/v4"
)

func JWT(authService *services.AuthService) echo.MiddlewareFunc {
	return func(next echo.HandlerFunc) echo.HandlerFunc {
		return func(c echo.Context) error {
			authHeader := c.Request().Header.Get("Authorization")
			if authHeader == "" {
				return apierror.Respond(c, apierror.MissingToken)
			}

			parts := strings.SplitN(authHeader, " ", 2)
			if len(parts) != 2 || strings.ToLower(parts[0]) != "bearer" {
				return apierror.Respond(c, apierror.InvalidTokenFormat)
			}

			claims, err := authService.ParseToken(parts[1])
			if err != nil {
				return apierror.Respond(c, apierror.InvalidToken)
			}

			c.Set("user", claims)
			return next(c)
		}
	}
}

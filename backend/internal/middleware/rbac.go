package middleware

import (
	"fikopar/backend/internal/apierror"
	"fikopar/backend/internal/models"
	"fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

func RequireRole(roles ...models.Role) echo.MiddlewareFunc {
	return func(next echo.HandlerFunc) echo.HandlerFunc {
		return func(c echo.Context) error {
			user, ok := c.Get("user").(*services.Claims)
			if !ok {
				return apierror.Respond(c, apierror.Unauthorized)
			}

			for _, r := range roles {
				if string(r) == user.Role {
					return next(c)
				}
			}
			return apierror.Respond(c, apierror.Forbidden)
		}
	}
}

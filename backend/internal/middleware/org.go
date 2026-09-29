package middleware

import (
	"strconv"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

// OrgIDKey holds the authorized organization for handlers under /api/orgs/:orgId.
const OrgIDKey = "orgID"

// RequireOrgMember is the single tenancy gate: handlers below it trust OrgIDKey and scope every
// query by it, so a foreign id in the path resolves to 404, never to another company's row.
func RequireOrgMember(membership *services.MembershipService) echo.MiddlewareFunc {
	return func(next echo.HandlerFunc) echo.HandlerFunc {
		return func(c echo.Context) error {
			id, err := strconv.ParseUint(c.Param("orgId"), 10, 32)
			if err != nil || id == 0 {
				return apierror.Respond(c, apierror.InvalidID)
			}
			claims, _ := c.Get("user").(*services.Claims)
			if err := membership.Authorize(claims, uint(id)); err != nil {
				return apierror.Respond(c, err)
			}
			c.Set(OrgIDKey, uint(id))
			return next(c)
		}
	}
}

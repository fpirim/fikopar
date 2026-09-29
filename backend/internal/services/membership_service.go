package services

import (
	"errors"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"

	"gorm.io/gorm"
)

type MembershipService struct {
	orgs repository.OrganizationRepository
}

func NewMembershipService(orgs repository.OrganizationRepository) *MembershipService {
	return &MembershipService{orgs: orgs}
}

// Authorize checks the database, not the token, so a revoked membership takes effect at once.
func (s *MembershipService) Authorize(claims *Claims, orgID uint) error {
	if claims == nil {
		return apierror.Unauthorized
	}
	if claims.Role == string(models.RoleAdmin) {
		if _, err := s.orgs.GetByID(orgID); err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return apierror.OrgNotFound
			}
			return err
		}
		return nil
	}
	ok, err := s.orgs.IsMember(claims.UserID, orgID)
	if err != nil {
		return err
	}
	if !ok {
		return apierror.Forbidden
	}
	return nil
}

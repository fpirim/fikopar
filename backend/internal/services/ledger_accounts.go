package services

import (
	"context"
	"encoding/base64"
	"fmt"
	"net/http"
	"regexp"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type LedgerService struct {
	store *repository.Store
}

func NewLedgerService(store *repository.Store) *LedgerService { return &LedgerService{store: store} }

var (
	accountCode  = regexp.MustCompile(`^[0-9]{3}(\.[0-9A-Za-z-]+)*$`)
	currencyCode = regexp.MustCompile(`^[A-Z]{3}$`)
	accountRoles = map[string]bool{
		"CASH": true, "SALES": true, "SALES_RETURNS": true, "PURCHASES": true, "PURCHASE_RETURNS": true,
		"PRICE_DIFFERENCE": true, "OUTPUT_VAT": true, "INPUT_VAT": true, "RECEIVABLE_CHEQUES": true,
		"RECEIVABLE_NOTES": true, "CHEQUES_AT_BANK": true, "PAYABLE_CHEQUES": true, "PAYABLE_NOTES": true,
		"OPENING": true, "CASH_OVER_SHORT": true,
	}
	// setAccountRoles answers 422 for unknown references, not 404.
	roleAccountNotFound = &apierror.APIError{Code: apierror.AccountNotFound.Code, Status: http.StatusUnprocessableEntity}
	roleDepotNotFound   = &apierror.APIError{Code: apierror.DepotNotFound.Code, Status: http.StatusUnprocessableEntity}
)

type AccountPage struct {
	Items      []models.Account `json:"items"`
	NextCursor *string          `json:"nextCursor"`
}

type AccountQuery struct {
	Type    string
	SicilID uint
	Limit   int
	Cursor  string
}

func (s *LedgerService) ListAccounts(ctx context.Context, orgID uint, in AccountQuery) (*AccountPage, error) {
	limit := clampLimit(in.Limit)
	after := ""
	if in.Cursor != "" {
		b, err := base64.RawURLEncoding.DecodeString(in.Cursor)
		if err != nil {
			return nil, apierror.WithField(apierror.InvalidRequest, "cursor")
		}
		after = string(b)
	}
	rows, err := s.store.Read(ctx).ListAccounts(orgID, repository.AccountFilter{
		Type: in.Type, SicilID: in.SicilID, AfterCode: after, Limit: limit + 1,
	})
	if err != nil {
		return nil, err
	}
	page := &AccountPage{Items: rows}
	if len(rows) > limit {
		page.Items = rows[:limit]
		c := base64.RawURLEncoding.EncodeToString([]byte(rows[limit-1].Code))
		page.NextCursor = &c
	}
	return page, nil
}

func clampLimit(n int) int {
	if n <= 0 {
		return 50
	}
	return min(n, 200)
}

func (s *LedgerService) GetAccount(ctx context.Context, orgID, id uint) (*models.Account, error) {
	return account(s.store.Read(ctx), orgID, id)
}

func account(q *repository.Q, orgID, id uint) (*models.Account, error) {
	a, err := q.GetAccount(orgID, id)
	if repository.IsNotFound(err) {
		return nil, apierror.AccountNotFound
	}
	return a, err
}

type AccountCreate struct {
	Code     string             `json:"code"`
	Name     string             `json:"name"`
	Type     models.AccountType `json:"type"`
	SicilID  *uint              `json:"sicilId"`
	Currency string             `json:"currency"`
}

func (s *LedgerService) CreateAccount(ctx context.Context, orgID uint, in AccountCreate) (*models.Account, error) {
	in.Code = strings.TrimSpace(in.Code)
	in.Name = strings.TrimSpace(in.Name)
	side, ok := in.Type.NormalSide()
	switch {
	case !accountCode.MatchString(in.Code):
		return nil, apierror.WithField(apierror.InvalidRequest, "code")
	case in.Name == "":
		return nil, apierror.WithField(apierror.InvalidRequest, "name")
	case !ok:
		return nil, apierror.WithField(apierror.InvalidRequest, "type")
	}
	if in.Currency == "" {
		in.Currency = "TRY"
	}
	if !currencyCode.MatchString(in.Currency) {
		return nil, apierror.WithField(apierror.InvalidRequest, "currency")
	}
	a := &models.Account{OrgID: orgID, Code: in.Code, Name: in.Name, Type: in.Type, NormalSide: side, SicilID: in.SicilID, Currency: in.Currency}
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if in.SicilID != nil {
			ok, err := q.SicilExists(orgID, *in.SicilID)
			if err != nil {
				return err
			}
			if !ok {
				return apierror.SicilNotFound
			}
		}
		if err := q.CreateAccount(a); err != nil {
			if repository.IsUniqueViolation(err) {
				return apierror.AccountCodeExists
			}
			return err
		}
		return audit(ctx, q, orgID, "account.created", "account", a.ID, nil, a)
	})
	if err != nil {
		return nil, err
	}
	return a, nil
}

type AccountUpdate struct {
	Name     *string `json:"name"`
	IsActive *bool   `json:"isActive"`
}

func (s *LedgerService) UpdateAccount(ctx context.Context, orgID, id uint, version int, in AccountUpdate) (*models.Account, error) {
	if in.Name != nil {
		n := strings.TrimSpace(*in.Name)
		if n == "" {
			return nil, apierror.WithField(apierror.InvalidRequest, "name")
		}
		in.Name = &n
	}
	var out *models.Account
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		before, err := account(q, orgID, id)
		if err != nil {
			return err
		}
		out, err = q.UpdateAccount(orgID, id, version, in.Name, in.IsActive)
		if err != nil {
			return err
		}
		if out == nil {
			return apierror.VersionConflict
		}
		return audit(ctx, q, orgID, "account.updated", "account", id, before, out)
	})
	return out, err
}

func (s *LedgerService) ListAccountRoles(ctx context.Context, orgID uint) ([]models.AccountRole, error) {
	return s.store.Read(ctx).ListAccountRoles(orgID)
}

func (s *LedgerService) SetAccountRoles(ctx context.Context, orgID uint, roles []models.AccountRole) ([]models.AccountRole, error) {
	seen := map[string]bool{}
	for _, r := range roles {
		if !accountRoles[r.Role] {
			return nil, apierror.WithField(&apierror.APIError{Code: apierror.InvalidRequest.Code, Status: http.StatusUnprocessableEntity}, "role")
		}
		k := r.Role
		if r.DepotID != nil {
			k = fmt.Sprintf("%s/%d", r.Role, *r.DepotID)
		}
		if seen[k] {
			return nil, apierror.WithDetail(&apierror.APIError{Code: apierror.InvalidRequest.Code, Status: http.StatusUnprocessableEntity}, "duplicate role "+r.Role)
		}
		seen[k] = true
	}
	var out []models.AccountRole
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		for _, r := range roles {
			if _, err := q.GetAccount(orgID, r.AccountID); err != nil {
				if repository.IsNotFound(err) {
					return roleAccountNotFound
				}
				return err
			}
			if r.DepotID != nil {
				if _, err := q.GetDepot(orgID, *r.DepotID); err != nil {
					if repository.IsNotFound(err) {
						return roleDepotNotFound
					}
					return err
				}
			}
		}
		if err := q.ReplaceAccountRoles(orgID, roles); err != nil {
			return err
		}
		var err error
		out, err = q.ListAccountRoles(orgID)
		if err != nil {
			return err
		}
		return audit(ctx, q, orgID, "account_roles.replaced", "account_roles", orgID, nil, out)
	})
	return out, err
}

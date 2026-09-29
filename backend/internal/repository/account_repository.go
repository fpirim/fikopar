package repository

import (
	"github.com/fpirim/fikopar/backend/internal/models"
)

const (
	accountCols  = `id, org_id, code, name, type, normal_side, sicil_id, currency, is_active, version`
	accountColsA = `a.id, a.org_id, a.code, a.name, a.type, a.normal_side, a.sicil_id, a.currency, a.is_active, a.version`
)

type AccountFilter struct {
	Type         string
	SicilID      uint
	CodePrefixes []string
	AfterCode    string
	Limit        int
}

func (q *Q) ListAccounts(orgID uint, f AccountFilter) ([]models.Account, error) {
	sql := `SELECT ` + accountCols + ` FROM accounts WHERE org_id = ?`
	args := []any{orgID}
	if f.Type != "" {
		sql += ` AND type = ?`
		args = append(args, f.Type)
	}
	if f.SicilID != 0 {
		sql += ` AND sicil_id = ?`
		args = append(args, f.SicilID)
	}
	for _, p := range f.CodePrefixes {
		sql += ` AND code LIKE ?`
		args = append(args, p+"%")
	}
	if f.AfterCode != "" {
		sql += ` AND code > ?`
		args = append(args, f.AfterCode)
	}
	sql += ` ORDER BY code LIMIT ?`
	args = append(args, f.Limit)
	return list[models.Account](q.db, sql, args...)
}

func (q *Q) GetAccount(orgID, id uint) (*models.Account, error) {
	return first[models.Account](q.db, `SELECT `+accountCols+` FROM accounts WHERE org_id = ? AND id = ?`, orgID, id)
}

func (q *Q) AccountBySicil(orgID, sicilID uint) (*models.Account, error) {
	return first[models.Account](q.db, `SELECT `+accountCols+` FROM accounts WHERE org_id = ? AND sicil_id = ? ORDER BY id LIMIT 1`, orgID, sicilID)
}

// AccountsForUpdate locks the accounts an entry touches so a concurrent deactivation waits.
func (q *Q) AccountsForUpdate(orgID uint, ids []uint) ([]models.Account, error) {
	return list[models.Account](q.db, `SELECT `+accountCols+` FROM accounts WHERE org_id = ? AND id IN ? ORDER BY id FOR SHARE`, orgID, ids)
}

func (q *Q) CreateAccount(a *models.Account) error {
	return q.db.Raw(`INSERT INTO accounts (org_id, code, name, type, normal_side, sicil_id, currency)
		VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING `+accountCols,
		a.OrgID, a.Code, a.Name, a.Type, a.NormalSide, a.SicilID, a.Currency).Scan(a).Error
}

// UpdateAccount applies the change only at the expected version; nil means no row matched.
func (q *Q) UpdateAccount(orgID, id uint, version int, name *string, active *bool) (*models.Account, error) {
	rows, err := list[models.Account](q.db, `UPDATE accounts
		SET name = coalesce(?, name), is_active = coalesce(?, is_active), version = version + 1, updated_at = now()
		WHERE org_id = ? AND id = ? AND version = ? RETURNING `+accountCols, name, active, orgID, id, version)
	if err != nil || len(rows) == 0 {
		return nil, err
	}
	return &rows[0], nil
}

func (q *Q) SicilExists(orgID, sicilID uint) (bool, error) {
	var n int64
	err := q.db.Raw(`SELECT count(*) FROM sicils WHERE organization_id = ? AND id = ?`, orgID, sicilID).Scan(&n).Error
	return n > 0, err
}

func (q *Q) ListAccountRoles(orgID uint) ([]models.AccountRole, error) {
	return list[models.AccountRole](q.db, `SELECT role, depot_id, account_id FROM account_roles
		WHERE org_id = ? ORDER BY role, depot_id NULLS FIRST`, orgID)
}

func (q *Q) ReplaceAccountRoles(orgID uint, roles []models.AccountRole) error {
	if err := q.db.Exec(`DELETE FROM account_roles WHERE org_id = ?`, orgID).Error; err != nil {
		return err
	}
	for _, r := range roles {
		if err := q.db.Exec(`INSERT INTO account_roles (org_id, role, depot_id, account_id) VALUES (?, ?, ?, ?)`,
			orgID, r.Role, r.DepotID, r.AccountID).Error; err != nil {
			return err
		}
	}
	return nil
}

// RoleAccount resolves a posting-rule account: the depot's row wins over the company default.
func (q *Q) RoleAccount(orgID uint, role string, depotID uint) (*models.Account, error) {
	return first[models.Account](q.db, `SELECT `+accountColsA+`
		FROM account_roles r JOIN accounts a ON a.id = r.account_id
		WHERE r.org_id = ? AND r.role = ? AND (r.depot_id = ? OR r.depot_id IS NULL)
		ORDER BY r.depot_id NULLS LAST LIMIT 1`, orgID, role, depotID)
}

// CreateAccountIfAbsent returns false when the code already exists; a concurrent insert of the
// same code waits for the other transaction instead of failing.
func (q *Q) CreateAccountIfAbsent(a *models.Account) (bool, error) {
	rows, err := list[models.Account](q.db, `INSERT INTO accounts (org_id, code, name, type, normal_side, sicil_id, currency)
		VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (org_id, code) DO NOTHING RETURNING `+accountCols,
		a.OrgID, a.Code, a.Name, a.Type, a.NormalSide, a.SicilID, a.Currency)
	if err != nil || len(rows) == 0 {
		return false, err
	}
	*a = rows[0]
	return true, nil
}

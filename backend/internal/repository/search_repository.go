package repository

import "github.com/fpirim/fikopar/backend/internal/models"

// SearchAccounts matches cards by number, name or current-account code; accountId is the card's
// current account (120 / 320 / 102), nil if it has none yet.
func (q *Q) SearchAccounts(orgID uint, term string, limit int) ([]models.SearchAccountHit, error) {
	like := "%" + escapeLike(term) + "%"
	return list[models.SearchAccountHit](q.db, `SELECT s.id AS sicil_id, a.id AS account_id, s.hesap_no, s.hesap_adi
		FROM sicils s
		LEFT JOIN LATERAL (SELECT id, code FROM accounts WHERE org_id = s.organization_id AND sicil_id = s.id ORDER BY id LIMIT 1) a ON true
		WHERE s.organization_id = ? AND (s.hesap_no ILIKE ? OR s.hesap_adi ILIKE ? OR a.code ILIKE ?)
		ORDER BY s.hesap_no LIMIT ?`, orgID, like, like, like, limit)
}

// SearchDocuments matches receipt and cash voucher numbers, newest first.
func (q *Q) SearchDocuments(orgID uint, term string, limit int) ([]models.ActivityItem, error) {
	like := "%" + escapeLike(term) + "%"
	return list[models.ActivityItem](q.db, `SELECT * FROM (
		  SELECT 'RECEIPT' AS kind, r.id, r.receipt_no AS number, r.kind AS subtype, r.sicil_id, s.hesap_adi AS sicil_name,
		         r.receipt_date AS date, r.depot_id, r.amount
		  FROM receipts r JOIN sicils s ON s.id = r.sicil_id
		  WHERE r.org_id = ? AND r.receipt_no ILIKE ?
		  UNION ALL
		  SELECT 'CASH_VOUCHER', v.id, v.voucher_no, v.kind, NULL, NULL, v.voucher_date, v.depot_id, v.amount
		  FROM cash_vouchers v
		  WHERE v.org_id = ? AND v.voucher_no ILIKE ?
		) d ORDER BY date DESC, number DESC LIMIT ?`, orgID, like, orgID, like, limit)
}

type SicilTerms struct {
	ID          uint          `gorm:"column:id"`
	Gun         string        `gorm:"column:gun"`
	Iskonto     string        `gorm:"column:iskonto"`
	Iskonto2    string        `gorm:"column:iskonto2"`
	CreditLimit *models.Money `gorm:"column:credit_limit"`
}

func (q *Q) SicilTerms(orgID, id uint) (*SicilTerms, error) {
	return first[SicilTerms](q.db, `SELECT id, coalesce(gun, '') AS gun, iskonto::numeric(7,4)::text AS iskonto,
			iskonto2::text AS iskonto2, credit_limit
		FROM sicils WHERE organization_id = ? AND id = ?`, orgID, id)
}

// ChequesInPortfolio is Σ remaining of the card's receivable instruments still held.
func (q *Q) ChequesInPortfolio(orgID, sicilID uint, depotID *uint) (models.Money, error) {
	var m models.Money
	err := q.db.Raw(`SELECT coalesce(sum(remaining), 0) FROM instruments
		WHERE org_id = ? AND sicil_id = ? AND type LIKE 'RECEIVABLE_%' AND status IN ('PORTFOLIO', 'PARTIALLY_COLLECTED')
		  AND (?::bigint IS NULL OR depot_id = ?)`, orgID, sicilID, depotID, depotID).Row().Scan(&m)
	return m, err
}

package repository

import "time"

type IdempotencyRecord struct {
	Endpoint     string    `gorm:"column:endpoint"`
	RequestHash  []byte    `gorm:"column:request_hash"`
	Status       string    `gorm:"column:status"`
	ResponseCode *int      `gorm:"column:response_code"`
	ResponseBody *string   `gorm:"column:response_body"`
	ExpiresAt    time.Time `gorm:"column:expires_at"`
}

// ClaimIdempotencyKey inserts the key as IN_PROGRESS; when it already exists the stored row is
// returned instead. An expired row is replaced.
func (q *Q) ClaimIdempotencyKey(orgID uint, key, endpoint string, hash []byte) (bool, *IdempotencyRecord, error) {
	if err := q.db.Exec(`DELETE FROM idempotency_keys WHERE org_id = ? AND key = ? AND expires_at < now()`, orgID, key).Error; err != nil {
		return false, nil, err
	}
	res := q.db.Exec(`INSERT INTO idempotency_keys (org_id, key, endpoint, request_hash) VALUES (?, ?, ?, ?)
		ON CONFLICT (org_id, key) DO NOTHING`, orgID, key, endpoint, hash)
	if res.Error != nil {
		return false, nil, res.Error
	}
	if res.RowsAffected == 1 {
		return true, nil, nil
	}
	rec, err := first[IdempotencyRecord](q.db, `SELECT endpoint, request_hash, status, response_code, response_body::text AS response_body, expires_at
		FROM idempotency_keys WHERE org_id = ? AND key = ?`, orgID, key)
	return false, rec, err
}

func (q *Q) CompleteIdempotencyKey(orgID uint, key string, code int, body []byte) error {
	return q.db.Exec(`UPDATE idempotency_keys SET status = 'COMPLETED', response_code = ?, response_body = ?::jsonb
		WHERE org_id = ? AND key = ?`, code, string(body), orgID, key).Error
}

func (q *Q) ReleaseIdempotencyKey(orgID uint, key string) error {
	return q.db.Exec(`DELETE FROM idempotency_keys WHERE org_id = ? AND key = ? AND status = 'IN_PROGRESS'`, orgID, key).Error
}

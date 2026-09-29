package repository

import (
	"net"
	"time"

	"github.com/fpirim/fikopar/backend/internal/models"
)

type AuditEvent struct {
	OrgID       uint
	ActorUserID *uint
	ActorIP     string
	RequestID   string
	Action      string
	EntityType  string
	EntityID    string
	Before      *string
	After       *string
}

func (q *Q) AppendAudit(e AuditEvent) error {
	var ip, req *string
	if net.ParseIP(e.ActorIP) != nil {
		ip = &e.ActorIP
	}
	if e.RequestID != "" {
		req = &e.RequestID
	}
	return q.db.Exec(`INSERT INTO audit_events (org_id, actor_user_id, actor_ip, request_id, action, entity_type, entity_id, before, after)
		VALUES (?, ?, ?::inet, ?, ?, ?, ?, ?::jsonb, ?::jsonb)`,
		e.OrgID, e.ActorUserID, ip, req, e.Action, e.EntityType, e.EntityID, e.Before, e.After).Error
}

type AuditFilter struct {
	EntityType, EntityID string
	ActorUserID          uint
	From, To             *time.Time
	Before               *AuditCursor
	Limit                int
}

type AuditCursor struct {
	At time.Time
	ID uint
}

// ListAudit reads a company's audit events newest first.
func (q *Q) ListAudit(orgID uint, f AuditFilter) ([]models.AuditEvent, error) {
	sql := `SELECT id, org_id, occurred_at, actor_user_id, host(actor_ip) AS actor_ip, request_id, action, entity_type,
		entity_id, before, after FROM audit_events WHERE org_id = ?`
	args := []any{orgID}
	if f.EntityType != "" {
		sql += ` AND entity_type = ?`
		args = append(args, f.EntityType)
	}
	if f.EntityID != "" {
		sql += ` AND entity_id = ?`
		args = append(args, f.EntityID)
	}
	if f.ActorUserID != 0 {
		sql += ` AND actor_user_id = ?`
		args = append(args, f.ActorUserID)
	}
	if f.From != nil {
		sql += ` AND occurred_at >= ?`
		args = append(args, *f.From)
	}
	if f.To != nil {
		sql += ` AND occurred_at <= ?`
		args = append(args, *f.To)
	}
	if f.Before != nil {
		sql += ` AND (occurred_at, id) < (?, ?)`
		args = append(args, f.Before.At, f.Before.ID)
	}
	sql += ` ORDER BY occurred_at DESC, id DESC LIMIT ?`
	args = append(args, f.Limit)
	return list[models.AuditEvent](q.db, sql, args...)
}

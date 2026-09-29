package repository

import "net"

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

//go:build integration

package router_test

import (
	"encoding/json"
	"net/http"
	"net/url"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type auditEventJSON struct {
	ID          uint            `json:"id"`
	OrgID       uint            `json:"orgId"`
	OccurredAt  time.Time       `json:"occurredAt"`
	ActorUserID *uint           `json:"actorUserId"`
	ActorIP     *string         `json:"actorIp"`
	RequestID   *string         `json:"requestId"`
	Action      string          `json:"action"`
	EntityType  string          `json:"entityType"`
	EntityID    string          `json:"entityId"`
	Before      json.RawMessage `json:"before"`
	After       json.RawMessage `json:"after"`
}

type auditPage struct {
	Items      []auditEventJSON `json:"items"`
	NextCursor *string          `json:"nextCursor"`
}

func (f *docFix) audit(t *testing.T, org uint, query string) auditPage {
	t.Helper()
	rec := f.do(t, http.MethodGet, orgPath(org, "/audit-events?"+query), f.admin, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[auditPage](t, rec)
}

func TestAudit_ListIsAdminOnlyOrgScopedAndFiltered(t *testing.T) {
	f := newDocs(t)
	f.setCreditLimit(t, f.cust, "100")
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "",
		lineJSON(f.p1, "1", "100", `,"discount1Pct":"0","discount2Pct":"0"`)))
	rec := f.postRaw(t, f.admin, d.ID, d.Version, `{"creditOverride":{"reason":"known customer"}}`)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())

	other := f.seedOrg(t, "audit-other", f.clerkID)
	require.NoError(t, testDB.Exec(`INSERT INTO audit_events (org_id, action, entity_type, entity_id)
		VALUES (?, 'sicil.updated', 'document', ?)`, other, idStr(d.ID)).Error)

	rec = f.do(t, http.MethodGet, f.path("/audit-events"), f.token, "")
	assert.Equal(t, http.StatusForbidden, rec.Code, "admins only")

	all := f.audit(t, f.org, "")
	require.NotEmpty(t, all.Items)
	for i, e := range all.Items {
		assert.Equal(t, f.org, e.OrgID)
		if i > 0 {
			prev := all.Items[i-1]
			assert.True(t, !e.OccurredAt.After(prev.OccurredAt) && (e.OccurredAt.Before(prev.OccurredAt) || e.ID < prev.ID),
				"newest first")
		}
	}

	doc := f.audit(t, f.org, "entityType=document&entityId="+idStr(d.ID))
	var override *auditEventJSON
	for i, e := range doc.Items {
		assert.Equal(t, "document", e.EntityType)
		assert.Equal(t, idStr(d.ID), e.EntityID)
		assert.NotEqual(t, "sicil.updated", e.Action, "another company's event")
		if e.Action == "document.credit_override" {
			override = &doc.Items[i]
		}
	}
	require.NotNil(t, override, "the credit override is readable")
	require.NotNil(t, override.ActorUserID)
	assert.Equal(t, f.adminID, *override.ActorUserID)
	assert.Contains(t, string(override.After), "known customer")
	assert.Contains(t, string(override.After), "20.00")
	assert.Equal(t, "null", string(override.Before))

	mine := f.audit(t, f.org, "actorUserId="+idStr(f.adminID)+"&entityType=document")
	require.NotEmpty(t, mine.Items)
	for _, e := range mine.Items {
		require.NotNil(t, e.ActorUserID)
		assert.Equal(t, f.adminID, *e.ActorUserID)
	}
	clerk := f.audit(t, f.org, "actorUserId="+idStr(f.clerkID)+"&entityType=document")
	require.NotEmpty(t, clerk.Items)
	for _, e := range clerk.Items {
		require.NotNil(t, e.ActorUserID)
		assert.Equal(t, f.clerkID, *e.ActorUserID)
		assert.NotEqual(t, "document.credit_override", e.Action)
	}

	at := override.OccurredAt
	window := func(from, to time.Time) string {
		return "from=" + url.QueryEscape(from.Format(time.RFC3339Nano)) + "&to=" + url.QueryEscape(to.Format(time.RFC3339Nano))
	}
	assert.NotEmpty(t, f.audit(t, f.org, window(at, at)).Items, "from and to are inclusive")
	assert.Empty(t, f.audit(t, f.org, window(at.Add(time.Second), at.Add(time.Hour))).Items)
	assert.Empty(t, f.audit(t, f.org, window(at.Add(-2*time.Hour), at.Add(-time.Hour))+"&entityType=document&entityId="+idStr(d.ID)).Items)

	theirs := f.audit(t, other, "")
	require.Len(t, theirs.Items, 1)
	assert.Equal(t, "sicil.updated", theirs.Items[0].Action)
	assert.Equal(t, other, theirs.Items[0].OrgID)
	rec = f.do(t, http.MethodGet, orgPath(other, "/audit-events"), f.token, "")
	assert.Equal(t, http.StatusForbidden, rec.Code, "a member who is not an admin")
}

func TestAudit_ListPagesWithACursor(t *testing.T) {
	f := newDocs(t)
	total := len(f.audit(t, f.org, "limit=200").Items)
	require.GreaterOrEqual(t, total, 3)

	seen := map[uint]bool{}
	cursor := ""
	pages := 0
	for {
		q := "limit=2"
		if cursor != "" {
			q += "&cursor=" + url.QueryEscape(cursor)
		}
		p := f.audit(t, f.org, q)
		pages++
		require.LessOrEqual(t, len(p.Items), 2)
		for _, e := range p.Items {
			assert.False(t, seen[e.ID], "no event twice")
			seen[e.ID] = true
		}
		if p.NextCursor == nil {
			break
		}
		cursor = *p.NextCursor
		require.Less(t, pages, total+1)
	}
	assert.Len(t, seen, total)
	assert.Equal(t, (total+1)/2, pages)
}

func TestAudit_ListRejectsBadFilters(t *testing.T) {
	f := newDocs(t)
	for _, q := range []string{"actorUserId=x", "from=yesterday", "to=2026-13-01", "cursor=%25%25"} {
		rec := f.do(t, http.MethodGet, f.path("/audit-events?"+q), f.admin, "")
		assert.Equal(t, http.StatusBadRequest, rec.Code, q)
		assert.Equal(t, "ERR_INVALID_REQUEST", errCode(t, rec), q)
	}
}

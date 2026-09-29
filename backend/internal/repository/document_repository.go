package repository

import (
	"strings"
	"time"

	"github.com/fpirim/fikopar/backend/internal/models"
)

// DocumentRow is a documents row.
type DocumentRow struct {
	ID                   uint          `gorm:"column:id"`
	OrgID                uint          `gorm:"column:org_id"`
	DepotID              uint          `gorm:"column:depot_id"`
	DocumentNo           *string       `gorm:"column:document_no"`
	Type                 string        `gorm:"column:type"`
	Status               string        `gorm:"column:status"`
	Origin               string        `gorm:"column:origin"`
	SicilID              uint          `gorm:"column:sicil_id"`
	DocumentDate         models.Date   `gorm:"column:document_date"`
	ShipDate             *models.Date  `gorm:"column:ship_date"`
	DueDate              *models.Date  `gorm:"column:due_date"`
	SalesRepUserID       *uint         `gorm:"column:sales_rep_user_id"`
	LinkedDocumentID     *uint         `gorm:"column:linked_document_id"`
	PriceDirection       *string       `gorm:"column:price_direction"`
	WaybillID            *uint         `gorm:"column:waybill_id"`
	GrossTotal           models.Money  `gorm:"column:gross_total"`
	DiscountTotal        models.Money  `gorm:"column:discount_total"`
	NetTotal             models.Money  `gorm:"column:net_total"`
	VatTotal             models.Money  `gorm:"column:vat_total"`
	GrandTotal           models.Money  `gorm:"column:grand_total"`
	Description          *string       `gorm:"column:description"`
	SicilSnapshot        *string       `gorm:"column:sicil_snapshot"`
	CreditOverrideReason *string       `gorm:"column:credit_override_reason"`
	CreditOverrideBy     *uint         `gorm:"column:credit_override_by"`
	CreditOverrideAt     *time.Time    `gorm:"column:credit_override_at"`
	CreditOverrideExcess *models.Money `gorm:"column:credit_override_excess"`
	PostedAt             *time.Time    `gorm:"column:posted_at"`
	PostedBy             *uint         `gorm:"column:posted_by"`
	CancelledAt          *time.Time    `gorm:"column:cancelled_at"`
	CancelDate           *models.Date  `gorm:"column:cancel_date"`
	CancelReason         *string       `gorm:"column:cancel_reason"`
	Version              int           `gorm:"column:version"`
}

const documentCols = `d.id, d.org_id, d.depot_id, d.document_no, d.type, d.status, d.origin, d.sicil_id, d.document_date,
	d.ship_date, d.due_date, d.sales_rep_user_id, d.linked_document_id, d.price_direction, d.waybill_id, d.gross_total,
	d.discount_total, d.net_total, d.vat_total, d.grand_total, d.description, d.sicil_snapshot::text AS sicil_snapshot,
	d.credit_override_reason, d.credit_override_by, d.credit_override_at, d.credit_override_excess, d.posted_at,
	d.posted_by, d.cancelled_at, d.cancel_date, d.cancel_reason, d.version`

func (q *Q) GetDocument(orgID, id uint) (*DocumentRow, error) {
	return first[DocumentRow](q.db, `SELECT `+documentCols+` FROM documents d WHERE d.org_id = ? AND d.id = ?`, orgID, id)
}

// LockDocument reads the document for update; concurrent writers of one document queue here.
func (q *Q) LockDocument(orgID, id uint) (*DocumentRow, error) {
	return first[DocumentRow](q.db, `SELECT `+documentCols+` FROM documents d WHERE d.org_id = ? AND d.id = ? FOR UPDATE`, orgID, id)
}

func (q *Q) InsertDocument(d *DocumentRow, createdBy *uint) error {
	return q.db.Raw(`INSERT INTO documents (org_id, depot_id, type, sicil_id, document_date, ship_date, due_date,
			sales_rep_user_id, linked_document_id, price_direction, waybill_id, gross_total, discount_total, net_total,
			vat_total, grand_total, description, created_by)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, version, status, origin`,
		d.OrgID, d.DepotID, d.Type, d.SicilID, d.DocumentDate, d.ShipDate, d.DueDate, d.SalesRepUserID,
		d.LinkedDocumentID, d.PriceDirection, d.WaybillID, d.GrossTotal, d.DiscountTotal, d.NetTotal, d.VatTotal,
		d.GrandTotal, d.Description, createdBy).Row().Scan(&d.ID, &d.Version, &d.Status, &d.Origin)
}

// UpdateDraft replaces the header of a draft locked by the caller and moves its version by one.
func (q *Q) UpdateDraft(d *DocumentRow) error {
	return q.db.Raw(`UPDATE documents SET depot_id = ?, type = ?, sicil_id = ?, document_date = ?, ship_date = ?,
			due_date = ?, sales_rep_user_id = ?, linked_document_id = ?, price_direction = ?, waybill_id = ?, gross_total = ?,
			discount_total = ?, net_total = ?, vat_total = ?, grand_total = ?, description = ?, version = version + 1,
			updated_at = now()
		WHERE org_id = ? AND id = ? RETURNING version`,
		d.DepotID, d.Type, d.SicilID, d.DocumentDate, d.ShipDate, d.DueDate, d.SalesRepUserID, d.LinkedDocumentID,
		d.PriceDirection, d.WaybillID, d.GrossTotal, d.DiscountTotal, d.NetTotal, d.VatTotal, d.GrandTotal,
		d.Description, d.OrgID, d.ID).Row().Scan(&d.Version)
}

func (q *Q) DeleteDocument(orgID, id uint) error {
	return q.db.Exec(`DELETE FROM documents WHERE org_id = ? AND id = ?`, orgID, id).Error
}

// DocumentPost is what posting writes on the document row.
type DocumentPost struct {
	No             string
	PostedBy       *uint
	Snapshot       string
	OverrideReason *string
	OverrideBy     *uint
	OverrideExcess *models.Money
}

func (q *Q) PostDocument(orgID, id uint, p DocumentPost) error {
	return q.db.Exec(`UPDATE documents SET status = 'POSTED', document_no = ?, posted_at = now(), posted_by = ?,
			sicil_snapshot = ?::jsonb, credit_override_reason = ?, credit_override_by = ?,
			credit_override_at = CASE WHEN ?::text IS NULL THEN NULL ELSE now() END, credit_override_excess = ?,
			version = version + 1, updated_at = now()
		WHERE org_id = ? AND id = ?`, p.No, p.PostedBy, p.Snapshot, p.OverrideReason, p.OverrideBy, p.OverrideReason,
		p.OverrideExcess, orgID, id).Error
}

func (q *Q) CancelDocument(orgID, id uint, by *uint, date models.Date, reason string) error {
	return q.db.Exec(`UPDATE documents SET status = 'CANCELLED', cancelled_at = now(), cancelled_by = ?, cancel_date = ?,
			cancel_reason = ?, version = version + 1, updated_at = now()
		WHERE org_id = ? AND id = ?`, by, date, reason, orgID, id).Error
}

// LineRow is a document line with its part's display fields.
type LineRow struct {
	ID             uint         `gorm:"column:id"`
	DocumentID     uint         `gorm:"column:document_id"`
	LineNo         int          `gorm:"column:line_no"`
	PartID         *uint        `gorm:"column:part_id"`
	Qty            models.Dec4  `gorm:"column:qty"`
	UnitPrice      models.Dec4  `gorm:"column:unit_price"`
	Discount1Pct   models.Dec4  `gorm:"column:discount1_pct"`
	Discount2Pct   models.Dec4  `gorm:"column:discount2_pct"`
	VatPct         models.Dec4  `gorm:"column:vat_pct"`
	GrossAmount    models.Money `gorm:"column:gross_amount"`
	DiscountAmount models.Money `gorm:"column:discount_amount"`
	NetAmount      models.Money `gorm:"column:net_amount"`
	VatAmount      models.Money `gorm:"column:vat_amount"`
	OriginLineID   *uint        `gorm:"column:origin_line_id"`
	BilledLineID   *uint        `gorm:"column:billed_line_id"`
	Description    *string      `gorm:"column:description"`
	StockNo        *string      `gorm:"column:stock_no"`
	Name           *string      `gorm:"column:name"`
	CatalogCode    *int         `gorm:"column:catalog_code"`
}

func (q *Q) DocumentLines(orgID, documentID uint) ([]LineRow, error) {
	return list[LineRow](q.db, `SELECT l.id, l.document_id, l.line_no, l.part_id, l.qty, l.unit_price, l.discount1_pct,
			l.discount2_pct, l.vat_pct, l.gross_amount, l.discount_amount, l.net_amount, l.vat_amount, l.origin_line_id,
			l.billed_line_id, l.description, p.stock_no, p.name, c.code AS catalog_code
		FROM document_lines l
		LEFT JOIN parts p ON p.id = l.part_id
		LEFT JOIN catalogs c ON c.id = p.catalog_id
		WHERE l.org_id = ? AND l.document_id = ? ORDER BY l.line_no`, orgID, documentID)
}

func (q *Q) DeleteDocumentLines(orgID, documentID uint) error {
	return q.db.Exec(`DELETE FROM document_lines WHERE org_id = ? AND document_id = ?`, orgID, documentID).Error
}

func (q *Q) InsertDocumentLine(orgID uint, l *LineRow) error {
	return q.db.Raw(`INSERT INTO document_lines (org_id, document_id, line_no, part_id, qty, unit_price, discount1_pct,
			discount2_pct, vat_pct, gross_amount, discount_amount, net_amount, vat_amount, origin_line_id, billed_line_id,
			description)
		VALUES (?, ?, ?, ?, ?::numeric, ?::numeric, ?::numeric, ?::numeric, ?::numeric, ?, ?, ?, ?, ?, ?, ?) RETURNING id`,
		orgID, l.DocumentID, l.LineNo, l.PartID, l.Qty, l.UnitPrice, l.Discount1Pct, l.Discount2Pct, l.VatPct,
		l.GrossAmount, l.DiscountAmount, l.NetAmount, l.VatAmount, l.OriginLineID, l.BilledLineID, l.Description).Row().Scan(&l.ID)
}

// ReservationRow is a held reservation.
type ReservationRow struct {
	OrgID      uint        `gorm:"column:org_id"`
	DocumentID uint        `gorm:"column:document_id"`
	LineID     uint        `gorm:"column:line_id"`
	PartID     uint        `gorm:"column:part_id"`
	DepotID    uint        `gorm:"column:depot_id"`
	Qty        models.Dec4 `gorm:"column:qty"`
	ExpiresAt  time.Time   `gorm:"column:expires_at"`
}

// ReleaseReservations deletes the document's reservations and returns what was held.
func (q *Q) ReleaseReservations(orgID, documentID uint) ([]ReservationRow, error) {
	return list[ReservationRow](q.db, `DELETE FROM stock_reservations WHERE org_id = ? AND document_id = ?
		RETURNING org_id, document_id, line_id, part_id, depot_id, qty, expires_at`, orgID, documentID)
}

func (q *Q) DocumentReservations(orgID, documentID uint) ([]ReservationRow, error) {
	return list[ReservationRow](q.db, `SELECT org_id, document_id, line_id, part_id, depot_id, qty, expires_at
		FROM stock_reservations WHERE org_id = ? AND document_id = ? ORDER BY line_id`, orgID, documentID)
}

func (q *Q) InsertReservation(r ReservationRow) error {
	return q.db.Exec(`INSERT INTO stock_reservations (org_id, document_id, line_id, part_id, depot_id, qty, expires_at)
		VALUES (?, ?, ?, ?, ?, ?::numeric, ?)`, r.OrgID, r.DocumentID, r.LineID, r.PartID, r.DepotID, r.Qty, r.ExpiresAt).Error
}

// ReleaseExpiredReservations runs the sweeper function (release_expired_reservations).
func (q *Q) ReleaseExpiredReservations(now time.Time, limit int) ([]ReservationRow, error) {
	return list[ReservationRow](q.db, `SELECT org_id, document_id, line_id, part_id, depot_id, qty
		FROM release_expired_reservations(?, ?)`, now, limit)
}

// Level is the stock level of a part in a depot; no row reads as zero.
type Level struct {
	OnHand   models.Dec4 `gorm:"column:on_hand"`
	Reserved models.Dec4 `gorm:"column:reserved"`
}

func (q *Q) StockLevel(partID, depotID uint) (Level, error) {
	rows, err := list[Level](q.db, `SELECT on_hand, reserved FROM stock_levels WHERE part_id = ? AND depot_id = ?`, partID, depotID)
	if err != nil || len(rows) == 0 {
		return Level{}, err
	}
	return rows[0], nil
}

// PartDepotStock is the stock of a part in one depot of the company.
type PartDepotStock struct {
	PartID uint `gorm:"column:part_id"`
	models.LineDepotStock
}

// LineStockByDepot lists every depot of the company for each part with its reserved quantity, depot code order.
func (q *Q) LineStockByDepot(orgID uint, partIDs []uint) ([]PartDepotStock, error) {
	return list[PartDepotStock](q.db, `SELECT p.id AS part_id, d.id AS depot_id, d.code AS depot_code,
			coalesce(l.on_hand, 0) AS on_hand, coalesce(l.reserved, 0) AS reserved,
			coalesce(l.on_hand, 0) - coalesce(l.reserved, 0) AS available
		FROM unnest(?::bigint[]) AS p(id)
		CROSS JOIN depots d
		LEFT JOIN stock_levels l ON l.part_id = p.id AND l.depot_id = d.id
		WHERE d.org_id = ? ORDER BY p.id, d.code, d.id`, uintArray(partIDs), orgID)
}

// ReturnedQty is Σ qty of posted goods-moving returns per original line.
func (q *Q) ReturnedQty(orgID uint, lineIDs []uint) (map[uint]models.Dec4, error) {
	type row struct {
		LineID uint        `gorm:"column:line_id"`
		Qty    models.Dec4 `gorm:"column:qty"`
	}
	rows, err := list[row](q.db, `SELECT rl.origin_line_id AS line_id, sum(rl.qty) AS qty
		FROM document_lines rl JOIN documents r ON r.org_id = rl.org_id AND r.id = rl.document_id
		WHERE rl.org_id = ? AND rl.origin_line_id = ANY(?::bigint[]) AND is_return(r.type) AND r.waybill_id IS NULL
		  AND r.status = 'POSTED'
		GROUP BY rl.origin_line_id`, orgID, uintArray(lineIDs))
	out := make(map[uint]models.Dec4, len(rows))
	for _, r := range rows {
		out[r.LineID] = r.Qty
	}
	return out, err
}

// PostedDependents are the posted returns and price differences that link the document.
func (q *Q) PostedDependents(orgID, id uint) ([]models.DocumentDependent, error) {
	return list[models.DocumentDependent](q.db, `SELECT id, document_no, type FROM documents
		WHERE org_id = ? AND linked_document_id = ? AND status = 'POSTED' ORDER BY id`, orgID, id)
}

// DraftDependents are the other documents that bill or link the draft or its lines.
func (q *Q) DraftDependents(orgID, id uint) ([]models.DocumentDependent, error) {
	return list[models.DocumentDependent](q.db, `SELECT x.id, x.document_no, x.type FROM documents x
		WHERE x.org_id = ? AND x.id <> ? AND (x.waybill_id = ? OR x.linked_document_id = ? OR EXISTS (
			SELECT 1 FROM document_lines xl JOIN document_lines l ON l.org_id = xl.org_id
			  AND l.id IN (xl.billed_line_id, xl.origin_line_id)
			WHERE xl.org_id = x.org_id AND xl.document_id = x.id AND l.document_id = ?))
		ORDER BY x.id`, orgID, id, id, id, id)
}

// BilledBy is the non-cancelled document that bills the waybill.
func (q *Q) BilledBy(orgID, waybillID uint) (*uint, error) {
	var ids []uint
	err := q.db.Raw(`SELECT id FROM documents WHERE org_id = ? AND waybill_id = ? AND status <> 'CANCELLED'`, orgID, waybillID).Scan(&ids).Error
	if err != nil || len(ids) == 0 {
		return nil, err
	}
	return &ids[0], nil
}

// DocumentEntries are the document's entry and its reversal.
func (q *Q) DocumentEntries(orgID, id uint) (entry, reversal *uint, err error) {
	type row struct {
		EntryID    *uint `gorm:"column:entry_id"`
		ReversalID *uint `gorm:"column:reversal_id"`
	}
	rows, err := list[row](q.db, `SELECT e.id AS entry_id, r.id AS reversal_id FROM journal_entries e
		LEFT JOIN journal_entries r ON r.reverses_entry_id = e.id
		WHERE e.org_id = ? AND e.source_type = 'document' AND e.source_id = ?`, orgID, uintText(id))
	if err != nil || len(rows) == 0 {
		return nil, nil, err
	}
	return rows[0].EntryID, rows[0].ReversalID, nil
}

// DocumentMoved reports whether the lines have a movement of kind (the document's own direction).
func (q *Q) DocumentMoved(orgID uint, lineIDs []uint, kind string) (bool, error) {
	var n int64
	err := q.db.Raw(`SELECT count(*) FROM stock_movements WHERE org_id = ? AND source_type = 'document'
		AND source_id = ANY(?::text[]) AND kind = ?`, orgID, uintArray(lineIDs), kind).Scan(&n).Error
	return n > 0, err
}

func (q *Q) DocumentNos(orgID uint, ids []uint) (map[uint]string, error) {
	type row struct {
		ID uint   `gorm:"column:id"`
		No string `gorm:"column:document_no"`
	}
	rows, err := list[row](q.db, `SELECT id, document_no FROM documents WHERE org_id = ? AND id = ANY(?::bigint[])
		AND document_no IS NOT NULL`, orgID, uintArray(ids))
	out := make(map[uint]string, len(rows))
	for _, r := range rows {
		out[r.ID] = r.No
	}
	return out, err
}

// DocumentCard is the account card as documents need it: defaults, credit data and the printed address.
type DocumentCard struct {
	ID             uint          `gorm:"column:id"`
	HesapNo        string        `gorm:"column:hesap_no"`
	HesapAdi       string        `gorm:"column:hesap_adi"`
	SicilTipi      string        `gorm:"column:sicil_tipi"`
	Gun            string        `gorm:"column:gun"`
	Iskonto        models.Dec4   `gorm:"column:iskonto"`
	Iskonto2       models.Dec4   `gorm:"column:iskonto2"`
	CreditLimit    *models.Money `gorm:"column:credit_limit"`
	SalesRepUserID *uint         `gorm:"column:sales_rep_user_id"`
	Adres          string        `gorm:"column:adres"`
	Ilce           string        `gorm:"column:ilce"`
	Sehir          string        `gorm:"column:sehir"`
	VDairesi       string        `gorm:"column:v_dairesi"`
	VNo            string        `gorm:"column:v_no"`
	TCNo           string        `gorm:"column:tc_no"`
}

func (q *Q) DocumentCard(orgID, id uint) (*DocumentCard, error) {
	return first[DocumentCard](q.db, `SELECT id, hesap_no, hesap_adi, sicil_tipi, coalesce(gun, '') AS gun,
			round(iskonto::numeric, 4) AS iskonto, iskonto2, credit_limit, sales_rep_user_id, coalesce(adres, '') AS adres,
			coalesce(ilce, '') AS ilce, coalesce(sehir, '') AS sehir, coalesce(v_dairesi, '') AS v_dairesi,
			coalesce(v_no, '') AS v_no, coalesce(tc_no, '') AS tc_no
		FROM sicils WHERE organization_id = ? AND id = ?`, orgID, id)
}

// LockCard serialises the credit check of one card's posts.
func (q *Q) LockCard(orgID, id uint) error {
	return q.db.Exec(`SELECT id FROM sicils WHERE organization_id = ? AND id = ? FOR UPDATE`, orgID, id).Error
}

// DocumentPart is what a line needs of its part.
type DocumentPart struct {
	ID       uint        `gorm:"column:id"`
	IsActive bool        `gorm:"column:is_active"`
	VatPct   models.Dec4 `gorm:"column:vat_pct"`
}

func (q *Q) DocumentParts(orgID uint, ids []uint) ([]DocumentPart, error) {
	return list[DocumentPart](q.db, `SELECT id, is_active, vat_pct FROM parts WHERE org_id = ? AND id = ANY(?::bigint[])`,
		orgID, uintArray(ids))
}

func (q *Q) IsMember(orgID, userID uint) (bool, error) {
	var n int64
	err := q.db.Raw(`SELECT count(*) FROM user_organizations WHERE organization_id = ? AND user_id = ?`, orgID, userID).Scan(&n).Error
	return n > 0, err
}

func (q *Q) Members(orgID uint) ([]models.Member, error) {
	return list[models.Member](q.db, `SELECT u.id AS user_id, u.name, u.email FROM users u
		JOIN user_organizations uo ON uo.user_id = u.id
		WHERE uo.organization_id = ? ORDER BY u.name, u.id`, orgID)
}

func (q *Q) UserName(id uint) (string, error) {
	var names []string
	err := q.db.Raw(`SELECT name FROM users WHERE id = ?`, id).Scan(&names).Error
	if err != nil || len(names) == 0 {
		return "", err
	}
	return names[0], nil
}

// AccountBalance is Σ debit − Σ credit of the account over all depots and dates.
func (q *Q) AccountBalance(accountID uint) (models.Money, error) {
	var m models.Money
	err := q.db.Raw(`SELECT coalesce(sum(debit_total - credit_total), 0) FROM account_balances WHERE account_id = ?`,
		accountID).Row().Scan(&m)
	return m, err
}

// DraftDocumentsThrough lists up to 20 drafts dated on or before d.
func (q *Q) DraftDocumentsThrough(orgID uint, d models.Date) ([]uint, error) {
	ids := []uint{}
	err := q.db.Raw(`SELECT id FROM documents WHERE org_id = ? AND status = 'DRAFT' AND document_date <= ?
		ORDER BY document_date, id LIMIT 20`, orgID, d).Scan(&ids).Error
	return ids, err
}

// EntryIDBySource finds a posted entry by its source key.
func (q *Q) EntryIDBySource(orgID uint, sourceType, sourceID string) (*uint, error) {
	var ids []uint
	err := q.db.Raw(`SELECT id FROM journal_entries WHERE org_id = ? AND source_type = ? AND source_id = ?`,
		orgID, sourceType, sourceID).Scan(&ids).Error
	if err != nil || len(ids) == 0 {
		return nil, err
	}
	return &ids[0], nil
}

// TryInsertReservation inserts under a savepoint; a reservation above what is available now (the projection's
// check, ERR_RESERVATION_EXCEEDS_AVAILABLE) is rolled back and reported as false.
func (q *Q) TryInsertReservation(r ReservationRow) (bool, error) {
	if err := q.db.Exec(`SAVEPOINT reservation`).Error; err != nil {
		return false, err
	}
	err := q.InsertReservation(r)
	if err != nil && strings.HasPrefix(PgMessage(err), "ERR_RESERVATION_EXCEEDS_AVAILABLE") {
		return false, q.db.Exec(`ROLLBACK TO SAVEPOINT reservation`).Error
	}
	if err != nil {
		return false, err
	}
	return true, q.db.Exec(`RELEASE SAVEPOINT reservation`).Error
}

func (q *Q) OrgName(orgID uint) (string, error) {
	var names []string
	err := q.db.Raw(`SELECT name FROM organizations WHERE id = ?`, orgID).Scan(&names).Error
	if err != nil || len(names) == 0 {
		return "", err
	}
	return names[0], nil
}

// StockKey is a (part, depot) stock level.
type StockKey struct {
	PartID  uint `gorm:"column:part_id"`
	DepotID uint `gorm:"column:depot_id"`
}

// LockStockLevels locks the existing levels in (part, depot) order before a document touches any of them.
func (q *Q) LockStockLevels(keys []StockKey) error {
	if len(keys) == 0 {
		return nil
	}
	parts, depots := make([]uint, len(keys)), make([]uint, len(keys))
	for i, k := range keys {
		parts[i], depots[i] = k.PartID, k.DepotID
	}
	return q.db.Exec(`SELECT lock_stock_levels(?::bigint[], ?::bigint[])`, uintArray(parts), uintArray(depots)).Error
}

// ExpiredReservationKeys are the levels the next sweep will release, in lock order.
func (q *Q) ExpiredReservationKeys(now time.Time, limit int) ([]StockKey, error) {
	return list[StockKey](q.db, `SELECT DISTINCT part_id, depot_id FROM (SELECT part_id, depot_id FROM stock_reservations
		WHERE expires_at <= ? ORDER BY expires_at, id LIMIT ?) x ORDER BY part_id, depot_id`, now, limit)
}

// ReservationKeys are the levels the document's reservations hold.
func (q *Q) ReservationKeys(orgID, documentID uint) ([]StockKey, error) {
	return list[StockKey](q.db, `SELECT DISTINCT part_id, depot_id FROM stock_reservations WHERE org_id = ? AND document_id = ?`,
		orgID, documentID)
}

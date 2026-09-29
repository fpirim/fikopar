package services

import (
	"context"
	"strings"
	"unicode/utf8"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

// StockService owns levels, per-depot settings, the movements ledger and transfers.
type StockService struct {
	store *repository.Store
}

func NewStockService(store *repository.Store) *StockService { return &StockService{store: store} }

func partExists(q *repository.Q, orgID, partID uint) error {
	_, err := q.GetPartRef(orgID, partID)
	if repository.IsNotFound(err) {
		return apierror.PartNotFound
	}
	return err
}

type PartStock struct {
	PartID uint                `json:"partId"`
	Depots []models.StockLevel `json:"depots"`
	Total  models.StockLevel   `json:"total"`
}

func (s *StockService) PartStock(ctx context.Context, orgID, partID uint) (*PartStock, error) {
	q := s.store.Read(ctx)
	if err := partExists(q, orgID, partID); err != nil {
		return nil, err
	}
	rows, err := q.PartStock(orgID, partID)
	if err != nil {
		return nil, err
	}
	total := models.StockLevel{PartID: partID}
	for _, r := range rows {
		total.StockNo, total.Name, total.CatalogCode = r.StockNo, r.Name, r.CatalogCode
		total.OnHand += r.OnHand
		total.Reserved += r.Reserved
		total.Available += r.Available
		total.MinQty += r.MinQty
		total.BelowMin = total.BelowMin || r.BelowMin
	}
	total.Negative = total.OnHand < 0
	return &PartStock{PartID: partID, Depots: rows, Total: total}, nil
}

type DepotSettingsWrite struct {
	MinQty *string `json:"minQty"`
	MaxQty *string `json:"maxQty"`
	Shelf  string  `json:"shelf"`
}

func (w DepotSettingsWrite) parse() (repository.DepotSettings, error) {
	var out repository.DepotSettings
	if w.MinQty != nil {
		v, err := models.ParseQuantity(*w.MinQty)
		if err != nil {
			return out, apierror.WithField(apierror.InvalidRequest, "minQty")
		}
		out.MinQty = v
	}
	if w.MaxQty != nil {
		v, err := models.ParseQuantity(*w.MaxQty)
		if err != nil {
			return out, apierror.WithField(apierror.InvalidRequest, "maxQty")
		}
		if v < out.MinQty {
			return out, apierror.UnknownRef(apierror.InvalidRequest, "maxQty")
		}
		out.MaxQty = &v
	}
	out.Shelf = strings.TrimSpace(w.Shelf)
	if utf8.RuneCountInString(out.Shelf) > 20 {
		return out, apierror.WithField(apierror.InvalidRequest, "shelf")
	}
	return out, nil
}

// SetDepotSettings upserts min, max and shelf; version 0 creates the row.
func (s *StockService) SetDepotSettings(ctx context.Context, orgID, partID, depotID uint, version int, in DepotSettingsWrite) (*models.StockLevel, error) {
	set, err := in.parse()
	if err != nil {
		return nil, err
	}
	var out *models.StockLevel
	err = s.store.InTx(ctx, func(q *repository.Q) error {
		if err := partExists(q, orgID, partID); err != nil {
			return err
		}
		if _, err := q.GetDepot(orgID, depotID); repository.IsNotFound(err) {
			return apierror.DepotNotFound
		} else if err != nil {
			return err
		}
		before, err := q.PartDepotLevel(orgID, partID, depotID)
		if err != nil {
			return err
		}
		ok, err := q.UpsertPartDepot(orgID, partID, depotID, version, set)
		if err != nil {
			return err
		}
		if !ok {
			return apierror.VersionConflict
		}
		if out, err = q.PartDepotLevel(orgID, partID, depotID); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "part_depot.updated", "part", partID, before, out)
	})
	return out, err
}

type LevelPage struct {
	Items      []models.StockLevel `json:"items"`
	NextCursor *string             `json:"nextCursor"`
}

var levelFilters = map[string]bool{"": true, "ALL": true, "BELOW_MIN": true, "NEGATIVE": true, "BELOW_MIN_OR_NEGATIVE": true}

func (s *StockService) Levels(ctx context.Context, orgID, partID, depotID uint, only string, limit int, cursor string) (*LevelPage, error) {
	if !levelFilters[only] {
		return nil, apierror.WithField(apierror.InvalidRequest, "only")
	}
	var after repository.LevelCursor
	has, err := decodeCursorInto(cursor, &after)
	if err != nil {
		return nil, err
	}
	f := repository.LevelFilter{PartID: partID, DepotID: depotID, Only: only, Limit: clampLimit(limit) + 1}
	if has {
		f.After = &after
	}
	rows, err := s.store.Read(ctx).StockLevels(orgID, f)
	if err != nil {
		return nil, err
	}
	return levelPage(rows, f.Limit-1), nil
}

func levelPage(rows []repository.LevelRow, limit int) *LevelPage {
	page := &LevelPage{Items: make([]models.StockLevel, 0, len(rows))}
	for i, r := range rows {
		if i == limit {
			last := rows[i-1]
			c := encodeCursor(repository.LevelCursor{Key: last.SortKey, StockNo: last.StockNo, PartID: last.PartID, DepotID: *last.DepotID})
			page.NextCursor = &c
			break
		}
		page.Items = append(page.Items, r.StockLevel)
	}
	return page
}

type MovementGridRequest struct {
	repository.ReportGridRequest
	PartID  uint  `json:"partId"`
	DepotID *uint `json:"depotId"`
}

type MovementGrid struct {
	Rows          []models.StockMovement `json:"rows"`
	TotalCount    int64                  `json:"totalCount"`
	Totals        map[string]string      `json:"totals"`
	OpeningOnHand *models.Dec4           `json:"openingOnHand"`
	ClosingOnHand *models.Dec4           `json:"closingOnHand"`
}

// Movements pages the ledger; running on hand needs a part and a date sort (asc or desc, first key).
func (s *StockService) Movements(ctx context.Context, orgID uint, in MovementGridRequest, all bool) (*MovementGrid, error) {
	running := in.PartID != 0 && (len(in.SortModel) == 0 || in.SortModel[0].ColID == "date")
	q := s.store.Read(ctx)
	g, err := q.MovementsGrid(orgID, in.PartID, in.DepotID, in.ReportGridRequest, running, all)
	if repository.IsBadFilter(err) {
		return nil, apierror.WithDetail(apierror.InvalidRequest, err.Error())
	}
	if err != nil {
		return nil, err
	}
	out := &MovementGrid{Rows: g.Rows, TotalCount: g.TotalCount,
		Totals: map[string]string{"qtyIn": g.QtyIn.String(), "qtyOut": g.QtyOut.String()}}
	if in.PartID == 0 {
		return out, nil
	}
	from, to := dateRange(in.FilterModel["date"])
	if from != nil {
		v, err := q.OnHandThrough(orgID, in.PartID, in.DepotID, models.Date{Time: from.AddDate(0, 0, -1)})
		if err != nil {
			return nil, err
		}
		out.OpeningOnHand = &v
	}
	end := today()
	if to != nil {
		end = *to
	}
	v, err := q.OnHandThrough(orgID, in.PartID, in.DepotID, end)
	if err != nil {
		return nil, err
	}
	out.ClosingOnHand = &v
	if from == nil {
		zero := models.Dec4(0)
		out.OpeningOnHand = &zero
	}
	return out, nil
}

// dateRange reads a date column filter as [from, to]; open ends are nil.
func dateRange(f repository.TypedFilter) (from, to *models.Date) {
	parse := func(p *string, alt repository.FlexString) *models.Date {
		s := string(alt)
		if p != nil {
			s = *p
		}
		if s == "" {
			return nil
		}
		d, err := models.ParseDate(s)
		if err != nil {
			return nil
		}
		return &d
	}
	a, b := parse(f.DateFrom, f.Filter), parse(f.DateTo, f.FilterTo)
	switch f.Type {
	case "inRange":
		return a, b
	case "equals":
		return a, a
	case "greaterThan", "greaterThanOrEqual":
		return a, nil
	case "lessThan", "lessThanOrEqual":
		return nil, a
	}
	return nil, nil
}

type TransferCreate struct {
	FromDepotID uint                  `json:"fromDepotId"`
	ToDepotID   uint                  `json:"toDepotId"`
	Date        models.Date           `json:"date"`
	Note        *string               `json:"note"`
	Lines       []models.TransferLine `json:"lines"`
}

func (in *TransferCreate) validate() error {
	switch {
	case in.FromDepotID == 0:
		return apierror.WithField(apierror.InvalidRequest, "fromDepotId")
	case in.ToDepotID == 0:
		return apierror.WithField(apierror.InvalidRequest, "toDepotId")
	case in.Date.IsZero():
		return apierror.WithField(apierror.InvalidRequest, "date")
	case len(in.Lines) == 0 || len(in.Lines) > 500:
		return apierror.WithField(apierror.InvalidRequest, "lines")
	case in.FromDepotID == in.ToDepotID:
		return apierror.UnknownRef(apierror.SameDepot, "toDepotId")
	}
	seen := map[uint]bool{}
	for i, l := range in.Lines {
		field := "lines[" + itoa(i) + "]"
		if l.Qty <= 0 {
			return apierror.WithField(apierror.InvalidRequest, field+".qty")
		}
		if seen[l.PartID] {
			return apierror.UnknownRef(apierror.DuplicateLine, field+".partId")
		}
		seen[l.PartID] = true
	}
	if in.Note != nil {
		n := strings.TrimSpace(*in.Note)
		in.Note = &n
	}
	return nil
}

// Transfer moves every line out of the source and into the target in one transaction under one TRF number.
func (s *StockService) Transfer(ctx context.Context, orgID uint, in TransferCreate) (*models.StockTransfer, error) {
	if err := in.validate(); err != nil {
		return nil, err
	}
	var out *models.StockTransfer
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		for _, d := range [][2]any{{in.FromDepotID, "fromDepotId"}, {in.ToDepotID, "toDepotId"}} {
			if _, err := q.GetDepot(orgID, d[0].(uint)); repository.IsNotFound(err) {
				return apierror.UnknownRef(apierror.DepotNotFound, d[1].(string))
			} else if err != nil {
				return err
			}
		}
		if err := activeParts(q, orgID, in.Lines); err != nil {
			return err
		}
		if err := q.LockLedger(orgID, false); err != nil {
			return err
		}
		if err := yearOpen(q, orgID, in.Date); err != nil {
			return err
		}
		no, err := q.NextNumber(orgID, in.FromDepotID, "TRF", in.Date.Year())
		if err != nil {
			return err
		}
		t := &models.StockTransfer{TransferNo: no, FromDepotID: in.FromDepotID, ToDepotID: in.ToDepotID, Date: in.Date, Note: in.Note}
		if err := q.InsertTransfer(orgID, t, actorFrom(ctx).userID()); err != nil {
			return err
		}
		moves := make([]repository.MovementInsert, 0, 2*len(in.Lines))
		for _, l := range in.Lines {
			for _, side := range []struct {
				kind  string
				depot uint
			}{{models.MoveTransferOut, in.FromDepotID}, {models.MoveTransferIn, in.ToDepotID}} {
				moves = append(moves, repository.MovementInsert{PartID: l.PartID, DepotID: side.depot, Kind: side.kind, Qty: l.Qty,
					SourceType: "transfer", TransferID: &t.ID, Date: in.Date})
			}
		}
		_, warnings, err := postMovements(ctx, q, orgID, moves)
		if err != nil {
			return err
		}
		if out, err = loadTransfer(q, orgID, t.ID); err != nil {
			return err
		}
		out.Warnings = warnings
		return audit(ctx, q, orgID, "stock_transfer.created", "stock_transfer", t.ID, nil, out)
	})
	return out, err
}

func activeParts(q *repository.Q, orgID uint, lines []models.TransferLine) error {
	ids := make([]uint, len(lines))
	for i, l := range lines {
		ids[i] = l.PartID
	}
	refs, err := q.PartRefs(orgID, ids)
	if err != nil {
		return err
	}
	byID := make(map[uint]repository.PartRef, len(refs))
	for _, r := range refs {
		byID[r.ID] = r
	}
	for i, l := range lines {
		r, ok := byID[l.PartID]
		switch {
		case !ok:
			return apierror.UnknownRef(apierror.PartNotFound, "lines["+itoa(i)+"].partId")
		case !r.IsActive:
			return apierror.UnknownRef(apierror.PartInactive, "lines["+itoa(i)+"].partId")
		}
	}
	return nil
}

func loadTransfer(q *repository.Q, orgID, id uint) (*models.StockTransfer, error) {
	t, err := q.GetTransfer(orgID, id)
	if repository.IsNotFound(err) {
		return nil, apierror.TransferNotFound
	}
	if err != nil {
		return nil, err
	}
	ids, err := q.TransferMovementIDs(orgID, id)
	if err != nil {
		return nil, err
	}
	if t.Movements, err = q.MovementsByIDs(orgID, ids); err != nil {
		return nil, err
	}
	t.Lines = []models.TransferLine{}
	for _, m := range t.Movements {
		if m.Kind == models.MoveTransferOut {
			t.Lines = append(t.Lines, models.TransferLine{PartID: m.PartID, Qty: m.Qty})
		}
	}
	t.Warnings = []models.StockWarning{}
	return t, nil
}

func (s *StockService) GetTransfer(ctx context.Context, orgID, id uint) (*models.StockTransfer, error) {
	return loadTransfer(s.store.Read(ctx), orgID, id)
}

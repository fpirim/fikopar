package services

import (
	"context"
	"strconv"
	"strings"
	"unicode/utf8"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

// StockCountService runs count sessions: open (freeze), autosave lines, finalize into ADJUST movements.
type StockCountService struct {
	store *repository.Store
}

func NewStockCountService(store *repository.Store) *StockCountService {
	return &StockCountService{store: store}
}

type StockCountOpen struct {
	DepotID   uint        `json:"depotId"`
	CountDate models.Date `json:"countDate"`
	ShelfFrom *string     `json:"shelfFrom"`
	ShelfTo   *string     `json:"shelfTo"`
}

func shelfArg(p *string, field string) (*string, error) {
	if p == nil {
		return nil, nil
	}
	v := strings.TrimSpace(*p)
	if v == "" {
		return nil, nil
	}
	if utf8.RuneCountInString(v) > 20 {
		return nil, apierror.WithField(apierror.InvalidRequest, field)
	}
	return &v, nil
}

func shelfBounds(from, to *string) (*string, *string, error) {
	f, err := shelfArg(from, "shelfFrom")
	if err != nil {
		return nil, nil, err
	}
	t, err := shelfArg(to, "shelfTo")
	if err != nil {
		return nil, nil, err
	}
	if f != nil && t != nil && *f > *t {
		return nil, nil, apierror.WithField(apierror.InvalidRequest, "shelfTo")
	}
	return f, t, nil
}

func (s *StockCountService) Open(ctx context.Context, orgID uint, in StockCountOpen) (*models.StockCount, error) {
	if in.CountDate.IsZero() {
		return nil, apierror.WithField(apierror.InvalidRequest, "countDate")
	}
	from, to, err := shelfBounds(in.ShelfFrom, in.ShelfTo)
	if err != nil {
		return nil, err
	}
	var out *models.StockCount
	err = s.store.InTx(ctx, func(q *repository.Q) error {
		if _, err := depotRef(q, orgID, in.DepotID); err != nil {
			return err
		}
		if err := yearOpen(q, orgID, in.CountDate); err != nil {
			return err
		}
		no, err := q.NextNumber(orgID, in.DepotID, "SAY", in.CountDate.Year())
		if err != nil {
			return err
		}
		id, err := q.InsertStockCount(orgID, repository.CountOpen{DepotID: in.DepotID, CountDate: in.CountDate, CountNo: no,
			ShelfFrom: from, ShelfTo: to, CreatedBy: actorFrom(ctx).userID()})
		if repository.IsUniqueViolation(err) {
			return apierror.StockCountOpenExists
		}
		if err != nil {
			return err
		}
		head, err := q.LockStockCount(orgID, id, true)
		if err != nil {
			return err
		}
		if err := q.InsertCountLines(orgID, id, in.DepotID, head.StartedAt, from, to); err != nil {
			return err
		}
		if out, err = q.GetStockCount(orgID, id); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "stock_count.opened", "stock_count", id, nil, out)
	})
	return out, err
}

func (s *StockCountService) Get(ctx context.Context, orgID, id uint) (*models.StockCount, error) {
	c, err := s.store.Read(ctx).GetStockCount(orgID, id)
	if repository.IsNotFound(err) {
		return nil, apierror.StockCountNotFound
	}
	return c, err
}

var countStatuses = map[string]bool{"": true, "OPEN": true, "FINALIZED": true, "CANCELLED": true}

func (s *StockCountService) List(ctx context.Context, orgID uint, f repository.CountFilter) ([]models.StockCount, error) {
	if !countStatuses[f.Status] {
		return nil, apierror.WithField(apierror.InvalidRequest, "status")
	}
	return s.store.Read(ctx).ListStockCounts(orgID, f)
}

// lockOpen locks the count and requires it to be OPEN.
func lockOpen(q *repository.Q, orgID, id uint, exclusive bool) (*repository.CountHead, error) {
	c, err := q.LockStockCount(orgID, id, exclusive)
	if repository.IsNotFound(err) {
		return nil, apierror.StockCountNotFound
	}
	if err != nil {
		return nil, err
	}
	if c.Status != "OPEN" {
		return nil, apierror.StockCountNotOpen
	}
	return c, nil
}

type StockCountLineAdd struct {
	PartID     uint    `json:"partId"`
	Shelf      *string `json:"shelf"`
	CountedQty *string `json:"countedQty"`
}

func countedQty(p *string) (*models.Dec4, error) {
	if p == nil {
		return nil, nil
	}
	v, err := models.ParseQuantity(*p)
	if err != nil {
		return nil, apierror.WithField(apierror.InvalidRequest, "countedQty")
	}
	return &v, nil
}

func (s *StockCountService) AddLine(ctx context.Context, orgID, countID uint, in StockCountLineAdd) (*models.StockCountLine, error) {
	qty, err := countedQty(in.CountedQty)
	if err != nil {
		return nil, err
	}
	shelf, err := shelfArg(in.Shelf, "shelf")
	if err != nil {
		return nil, err
	}
	if in.Shelf != nil && shelf == nil {
		empty := ""
		shelf = &empty
	}
	var out *models.StockCountLine
	err = s.store.InTx(ctx, func(q *repository.Q) error {
		c, err := lockOpen(q, orgID, countID, false)
		if err != nil {
			return err
		}
		part, err := q.GetPartRef(orgID, in.PartID)
		if repository.IsNotFound(err) {
			return apierror.UnknownRef(apierror.PartNotFound, "partId")
		}
		if err != nil {
			return err
		}
		if !part.IsActive {
			return apierror.WithField(apierror.PartInactive, "partId")
		}
		var by *uint
		if qty != nil {
			by = actorFrom(ctx).userID()
		}
		id, err := q.AddCountLine(orgID, c, repository.CountLineAdd{PartID: in.PartID, Shelf: shelf, CountedQty: qty, CountedBy: by})
		if repository.IsUniqueViolation(err) {
			return apierror.StockCountLineExists
		}
		if err != nil {
			return err
		}
		out, err = q.GetCountLine(orgID, countID, id)
		return err
	})
	return out, err
}

type StockCountLineSet struct {
	CountedQty *string `json:"countedQty"`
	Shelf      *string `json:"shelf"`
}

// SetLine autosaves one line; the line version is the lock, the header does not move.
// GetLine reads one line of a count in any status.
func (s *StockCountService) GetLine(ctx context.Context, orgID, countID, lineID uint) (*models.StockCountLine, error) {
	q := s.store.Read(ctx)
	if _, err := q.GetStockCount(orgID, countID); repository.IsNotFound(err) {
		return nil, apierror.StockCountNotFound
	} else if err != nil {
		return nil, err
	}
	out, err := q.GetCountLine(orgID, countID, lineID)
	if repository.IsNotFound(err) {
		return nil, apierror.StockCountLineNotFound
	}
	return out, err
}

func (s *StockCountService) SetLine(ctx context.Context, orgID, countID, lineID uint, version int, in StockCountLineSet) (*models.StockCountLine, error) {
	qty, err := countedQty(in.CountedQty)
	if err != nil {
		return nil, err
	}
	shelf, err := shelfArg(in.Shelf, "shelf")
	if err != nil {
		return nil, err
	}
	var out *models.StockCountLine
	err = s.store.InTx(ctx, func(q *repository.Q) error {
		if _, err := lockOpen(q, orgID, countID, false); err != nil {
			return err
		}
		ok, err := q.SetCountLine(orgID, countID, lineID, version, qty, shelf, actorFrom(ctx).userID())
		if err != nil {
			return err
		}
		if !ok {
			exists, err := q.CountLineExists(orgID, countID, lineID)
			if err != nil {
				return err
			}
			if !exists {
				return apierror.StockCountLineNotFound
			}
			return apierror.VersionConflict
		}
		out, err = q.GetCountLine(orgID, countID, lineID)
		return err
	})
	return out, err
}

func (s *StockCountService) GridLines(ctx context.Context, orgID, countID uint, req repository.CountLineGridRequest, all bool) (
	[]models.StockCountLine, *repository.CountLineTotals, error) {
	q := s.store.Read(ctx)
	if _, err := q.GetStockCount(orgID, countID); repository.IsNotFound(err) {
		return nil, nil, apierror.StockCountNotFound
	} else if err != nil {
		return nil, nil, err
	}
	rows, totals, err := q.GridCountLines(orgID, countID, req, all)
	if repository.IsBadFilter(err) {
		return nil, nil, apierror.WithDetail(apierror.InvalidRequest, err.Error())
	}
	return rows, totals, err
}

// Finalize posts counted − system per line as ADJUST movements dated countDate, valued at the last purchase
// cost of that date. Later movements stay on top; negative results are warnings.
func (s *StockCountService) Finalize(ctx context.Context, orgID, id uint, version int, uncounted string) (*models.StockCountFinalized, error) {
	if uncounted != "SKIP" && uncounted != "ZERO" {
		return nil, apierror.WithField(apierror.InvalidRequest, "uncounted")
	}
	var out *models.StockCountFinalized
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		c, err := lockOpen(q, orgID, id, true)
		if err != nil {
			return err
		}
		if c.Version != version {
			return apierror.VersionConflict
		}
		if err := yearOpen(q, orgID, c.CountDate); err != nil {
			return err
		}
		before, err := q.GetStockCount(orgID, id)
		if err != nil {
			return err
		}
		if err := q.SnapshotCountLines(orgID, c, uncounted == "ZERO", actorFrom(ctx).userID()); err != nil {
			return err
		}
		adj, err := q.CountAdjustments(orgID, id)
		if err != nil {
			return err
		}
		moves := make([]repository.MovementInsert, len(adj))
		stockNo := map[uint]string{}
		for i, a := range adj {
			kind, qty := models.MoveAdjustIn, a.Difference
			if qty < 0 {
				kind, qty = models.MoveAdjustOut, -qty
			}
			src := strconv.FormatUint(uint64(a.LineID), 10)
			moves[i] = repository.MovementInsert{PartID: a.PartID, DepotID: c.DepotID, Kind: kind, Qty: qty, UnitCost: a.UnitCost,
				SourceType: "stock_count", SourceID: &src, Date: c.CountDate}
			stockNo[a.PartID] = a.StockNo
		}
		_, warnings, err := postMovements(ctx, q, orgID, moves)
		if err != nil {
			return err
		}
		for i := range warnings {
			warnings[i].StockNo = stockNo[warnings[i].PartID]
		}
		if err := q.FinalizeStockCount(orgID, id, uncounted); err != nil {
			return err
		}
		count, err := q.GetStockCount(orgID, id)
		if err != nil {
			return err
		}
		out = &models.StockCountFinalized{Count: count, MovementCount: len(moves), Warnings: warnings}
		return audit(ctx, q, orgID, "stock_count.finalized", "stock_count", id, before, count)
	})
	return out, err
}

func (s *StockCountService) Cancel(ctx context.Context, orgID, id uint, version int, reason string) (*models.StockCount, error) {
	reason = strings.TrimSpace(reason)
	if utf8.RuneCountInString(reason) < 3 {
		return nil, apierror.WithField(apierror.InvalidRequest, "reason")
	}
	var out *models.StockCount
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		c, err := lockOpen(q, orgID, id, true)
		if err != nil {
			return err
		}
		if c.Version != version {
			return apierror.VersionConflict
		}
		before, err := q.GetStockCount(orgID, id)
		if err != nil {
			return err
		}
		if err := q.CancelStockCount(orgID, id, reason); err != nil {
			return err
		}
		if out, err = q.GetStockCount(orgID, id); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "stock_count.cancelled", "stock_count", id, before, out)
	})
	return out, err
}

// SheetLines lists the lines of the printed sheet by shelf, then stock no.
func (s *StockCountService) SheetLines(ctx context.Context, orgID, id uint, notCounted bool, from, to *string) ([]models.StockCountLine, error) {
	f, t, err := shelfBounds(from, to)
	if err != nil {
		return nil, err
	}
	req := repository.CountLineGridRequest{ShelfFrom: f, ShelfTo: t}
	if notCounted {
		req.Only = "NOT_COUNTED"
	}
	rows, _, err := s.GridLines(ctx, orgID, id, req, true)
	return rows, err
}

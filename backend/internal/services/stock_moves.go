package services

import (
	"context"
	"sort"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

// postMovements appends movements inside the caller's transaction. Level rows are locked in (part, depot)
// order so concurrent writers never deadlock; stock may go negative and is reported as warnings (one per
// part and depot, with the final on hand). One StockMoved per movement. Returns ids in input order.
func postMovements(ctx context.Context, q *repository.Q, orgID uint, moves []repository.MovementInsert) ([]uint, []models.StockWarning, error) {
	if len(moves) == 0 {
		return nil, []models.StockWarning{}, nil
	}
	if err := q.LockLedger(orgID, false); err != nil {
		return nil, nil, err
	}
	checked := map[int]bool{}
	for _, m := range moves {
		if y := m.Date.Year(); !checked[y] {
			if err := yearOpen(q, orgID, m.Date); err != nil {
				return nil, nil, err
			}
			checked[y] = true
		}
	}
	order := make([]int, len(moves))
	for i := range order {
		order[i] = i
	}
	sort.SliceStable(order, func(a, b int) bool {
		x, y := moves[order[a]], moves[order[b]]
		if x.PartID != y.PartID {
			return x.PartID < y.PartID
		}
		return x.DepotID < y.DepotID
	})
	type key struct{ part, depot uint }
	after := map[key]models.Dec4{}
	var keys []key
	ids := make([]uint, len(moves))
	by := actorFrom(ctx).userID()
	for _, i := range order {
		m := moves[i]
		m.CreatedBy = by
		id, onHand, err := q.InsertMovement(orgID, m)
		if repository.IsCheckViolation(err) && isYearClosedErr(err) {
			return nil, nil, apierror.YearClosed
		}
		if err != nil {
			return nil, nil, err
		}
		ids[i] = id
		k := key{m.PartID, m.DepotID}
		if _, seen := after[k]; !seen {
			keys = append(keys, k)
		}
		after[k] = onHand
		if err := emit(ctx, q, orgID, m.DepotID, "StockMoved", "stock_movement", id, nil, map[string]any{
			"movementId": id, "partId": m.PartID, "kind": m.Kind, "qty": m.Qty, "onHandAfter": onHand,
			"sourceType": m.SourceType, "sourceId": m.SourceID,
		}); err != nil {
			return nil, nil, err
		}
	}
	warnings := []models.StockWarning{}
	var parts []uint
	for _, k := range keys {
		if v := after[k]; v < 0 {
			warnings = append(warnings, models.StockWarning{Code: models.WarningNegativeStock, PartID: k.part, DepotID: k.depot, OnHandAfter: v})
			parts = append(parts, k.part)
		}
	}
	if len(parts) > 0 {
		refs, err := q.PartRefs(orgID, parts)
		if err != nil {
			return nil, nil, err
		}
		stockNo := make(map[uint]string, len(refs))
		for _, r := range refs {
			stockNo[r.ID] = r.StockNo
		}
		for i := range warnings {
			warnings[i].StockNo = stockNo[warnings[i].PartID]
		}
	}
	return ids, warnings, nil
}

func isYearClosedErr(err error) bool {
	return err != nil && strings.Contains(err.Error(), "ERR_YEAR_CLOSED")
}

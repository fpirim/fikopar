package services

import (
	"context"
	"strings"
	"unicode/utf8"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

const searchCap = 1000

type SearchRequest struct {
	Q         string
	CatalogID uint
	Date      *models.Date
	InStock   uint
	Limit     int
	Cursor    string
}

type SearchResult struct {
	Items       []models.CatalogHit `json:"items"`
	Total       int                 `json:"total"`
	TotalCapped bool                `json:"totalCapped"`
	Date        models.Date         `json:"date"`
	NextCursor  *string             `json:"nextCursor"`
}

var turkishFold = strings.NewReplacer("I", "i", "İ", "i", "ı", "i", "Ş", "ş", "Ğ", "ğ", "Ü", "ü", "Ö", "ö", "Ç", "ç")

// foldWords splits a name query into case- and dotless-i-insensitive words (parts.name_fold).
func foldWords(q string) []string {
	return strings.Fields(strings.ToLower(turkishFold.Replace(q)))
}

// Search ranks parts by code and name; inactive parts are left out.
func (s *CatalogService) Search(ctx context.Context, orgID uint, in SearchRequest) (*SearchResult, error) {
	q := strings.TrimSpace(in.Q)
	if n := utf8.RuneCountInString(q); n < 2 || n > 60 {
		return nil, apierror.WithField(apierror.InvalidRequest, "q")
	}
	switch {
	case in.Limit == 0:
		in.Limit = 20
	case in.Limit < 1 || in.Limit > 50:
		return nil, apierror.WithField(apierror.InvalidRequest, "limit")
	}
	sq := repository.SearchQuery{Norm: NormalizeCode(q), Words: foldWords(q), CatalogID: in.CatalogID, InStock: in.InStock,
		Date: today(), Limit: in.Limit + 1}
	if in.Date != nil {
		sq.Date = *in.Date
	}
	var after repository.SearchCursor
	has, err := decodeCursorInto(in.Cursor, &after)
	if err != nil {
		return nil, err
	}
	if has {
		sq.After = &after
	}
	rd := s.store.Read(ctx)
	total, err := rd.SearchCount(orgID, sq, searchCap+1)
	if err != nil {
		return nil, err
	}
	hits, err := rd.SearchPage(orgID, sq)
	if err != nil {
		return nil, err
	}
	res := &SearchResult{Total: min(total, searchCap), TotalCapped: total > searchCap, Date: sq.Date}
	if len(hits) > in.Limit {
		hits = hits[:in.Limit]
		last := hits[len(hits)-1]
		c := encodeCursor(repository.SearchCursor{Rank: last.Rank, StockNo: last.StockNo, CatalogCode: last.CatalogCode, PartID: last.PartID})
		res.NextCursor = &c
	}
	if err := enrichHits(rd, orgID, hits); err != nil {
		return nil, err
	}
	res.Items = hits
	return res, nil
}

// enrichHits adds codes, stock of every depot and equivalents to one page.
func enrichHits(q *repository.Q, orgID uint, hits []models.CatalogHit) error {
	if len(hits) == 0 {
		return nil
	}
	ids := make([]uint, len(hits))
	idx := make(map[uint][]int, len(hits))
	for i := range hits {
		ids[i] = hits[i].PartID
		idx[hits[i].PartID] = append(idx[hits[i].PartID], i)
		hits[i].Codes, hits[i].Stock, hits[i].Equivalents = []models.PartCode{}, []models.HitStock{}, []models.Equivalent{}
	}
	codes, err := q.PartCodes(ids)
	if err != nil {
		return err
	}
	for _, c := range codes {
		for _, i := range idx[c.PartID] {
			hits[i].Codes = append(hits[i].Codes, c)
		}
	}
	stock, err := q.StockByDepot(orgID, ids)
	if err != nil {
		return err
	}
	for _, st := range stock {
		for _, i := range idx[st.PartID] {
			hits[i].Stock = append(hits[i].Stock, models.HitStock{DepotID: st.DepotID, OnHand: st.OnHand, Available: st.Available})
			hits[i].OnHandTotal += st.OnHand
		}
	}
	eq, err := q.EquivalentsOf(orgID, ids)
	if err != nil {
		return err
	}
	for _, e := range eq {
		for _, i := range idx[e.OwnerID] {
			hits[i].Equivalents = append(hits[i].Equivalents, e.Equivalent)
		}
	}
	return nil
}

type ResolveRequest struct {
	Codes   []string `json:"codes"`
	DepotID *uint    `json:"depotId"`
}

type Resolved struct {
	Code       string              `json:"code"`
	Status     string              `json:"status"`
	Part       *models.CatalogHit  `json:"part,omitempty"`
	Candidates []models.CatalogHit `json:"candidates,omitempty"`
}

// Resolve matches each code exactly (normalized) against stock numbers and codes of active parts.
func (s *CatalogService) Resolve(ctx context.Context, orgID uint, in ResolveRequest) ([]Resolved, error) {
	if len(in.Codes) == 0 || len(in.Codes) > 500 {
		return nil, apierror.WithField(apierror.InvalidRequest, "codes")
	}
	norms := make([]string, len(in.Codes))
	for i, c := range in.Codes {
		norms[i] = NormalizeCode(c)
	}
	q := s.store.Read(ctx)
	matches, err := q.ResolveCodes(orgID, norms)
	if err != nil {
		return nil, err
	}
	byNorm := map[string][]repository.CodeMatch{}
	var ids []uint
	for _, m := range matches {
		byNorm[m.Norm] = append(byNorm[m.Norm], m)
		ids = append(ids, m.PartID)
	}
	hits, err := q.HitsByIDs(orgID, ids, today())
	if err != nil {
		return nil, err
	}
	if err := enrichHits(q, orgID, hits); err != nil {
		return nil, err
	}
	byID := make(map[uint]models.CatalogHit, len(hits))
	for _, h := range hits {
		byID[h.PartID] = h
	}
	out := make([]Resolved, len(in.Codes))
	for i, code := range in.Codes {
		ms := byNorm[norms[i]]
		r := Resolved{Code: code, Status: "NOT_FOUND"}
		for _, m := range ms {
			h := byID[m.PartID]
			h.MatchKind, h.Rank = m.MatchKind, 1
			code := m.Code
			h.MatchedCode = &code
			r.Candidates = append(r.Candidates, h)
		}
		switch {
		case len(ms) == 1:
			r.Status, r.Part, r.Candidates = "FOUND", &r.Candidates[0], nil
		case len(ms) > 1:
			r.Status = "AMBIGUOUS"
		}
		out[i] = r
	}
	return out, nil
}

type PartGridRequest struct {
	repository.ReportGridRequest
	CatalogID       *uint        `json:"catalogId"`
	DepotID         *uint        `json:"depotId"`
	Stock           string       `json:"stock"`
	IncludeInactive bool         `json:"includeInactive"`
	PriceDate       *models.Date `json:"priceDate"`
}

type PartGrid struct {
	Rows            []models.PartRow  `json:"rows"`
	TotalCount      int64             `json:"totalCount"`
	UnfilteredCount int64             `json:"unfilteredCount"`
	Totals          map[string]string `json:"totals"`
}

var stockSegments = map[string]bool{"": true, "ALL": true, "IN_STOCK": true, "BELOW_MIN": true, "NEGATIVE": true, "BELOW_MIN_OR_NEGATIVE": true}

// GridParts pages the parts grid; all returns every filtered row for Excel.
func (s *CatalogService) GridParts(ctx context.Context, orgID uint, in PartGridRequest, all bool) (*PartGrid, error) {
	if !stockSegments[in.Stock] {
		return nil, apierror.WithField(apierror.InvalidRequest, "stock")
	}
	if f, ok := in.FilterModel["codes"]; ok {
		f.Filter = repository.FlexString(NormalizeCode(string(f.Filter)))
		in.FilterModel["codes"] = f
	}
	pq := repository.PartGridQuery{Grid: in.ReportGridRequest, CatalogID: in.CatalogID, DepotID: in.DepotID, Stock: in.Stock,
		IncludeInactive: in.IncludeInactive, PriceDate: today(), SearchNorm: NormalizeCode(in.Search)}
	if in.PriceDate != nil {
		pq.PriceDate = *in.PriceDate
	}
	q := s.store.Read(ctx)
	g, err := q.PartsGrid(orgID, pq, all)
	if repository.IsBadFilter(err) {
		return nil, apierror.WithDetail(apierror.InvalidRequest, err.Error())
	}
	if err != nil {
		return nil, err
	}
	out := &PartGrid{Rows: g.Rows, TotalCount: g.TotalCount, UnfilteredCount: g.UnfilteredCount,
		Totals: map[string]string{"onHand": g.OnHand.String(), "reserved": g.Reserved.String(), "available": g.Available.String()}}
	if len(g.Rows) > 0 {
		ids := make([]uint, len(g.Rows))
		for i := range g.Rows {
			ids[i] = g.Rows[i].ID
			g.Rows[i].Codes = []models.PartCode{}
		}
		codes, err := q.PartCodes(ids)
		if err != nil {
			return nil, err
		}
		at := make(map[uint]int, len(g.Rows))
		for i, r := range g.Rows {
			at[r.ID] = i
		}
		for _, c := range codes {
			out.Rows[at[c.PartID]].Codes = append(out.Rows[at[c.PartID]].Codes, c)
		}
	}
	return out, nil
}

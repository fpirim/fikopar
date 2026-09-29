//go:build integration

package router_test

import (
	"bytes"
	"fmt"
	"net/http"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type docGrid struct {
	Rows []struct {
		ID                uint    `json:"id"`
		DocumentNo        *string `json:"documentNo"`
		Type              string  `json:"type"`
		Status            string  `json:"status"`
		DepotCode         string  `json:"depotCode"`
		HesapNo           string  `json:"hesapNo"`
		SicilName         string  `json:"sicilName"`
		SalesRepName      *string `json:"salesRepName"`
		GrandTotal        string  `json:"grandTotal"`
		LinkedDocumentNo  *string `json:"linkedDocumentNo"`
		PriceDirection    *string `json:"priceDirection"`
		WaybillDocumentNo *string `json:"waybillDocumentNo"`
		CreditOverride    bool    `json:"creditOverride"`
		PostedAt          *string `json:"postedAt"`
		Version           int     `json:"version"`
	} `json:"rows"`
	TotalCount      int               `json:"totalCount"`
	UnfilteredCount int               `json:"unfilteredCount"`
	Totals          map[string]string `json:"totals"`
}

func (f *docFix) grid(t *testing.T, body string) docGrid {
	t.Helper()
	rec := f.req(t, http.MethodPost, f.path("/documents/grid"), f.token, body, nil)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[docGrid](t, rec)
}

func (g docGrid) ids() []uint {
	out := []uint{}
	for _, r := range g.Rows {
		out = append(out, r.ID)
	}
	return out
}

func TestDocumentsRead_GridFiltersTotalsAndExcel(t *testing.T) {
	f := newDocs(t)
	sale := f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "100", `,"discount1Pct":"0","discount2Pct":"0"`)))
	buy := f.posted(t, docBody("PURCHASE_INVOICE", f.depot2, f.supp, "2026-03-05", "", lineJSON(f.p1, "2", "10", "")))
	draft := f.create(t, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-07", "", lineJSON(f.p1, "1", "10", `,"discount1Pct":"0","discount2Pct":"0"`)))
	gone := f.cancel(t, f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-09", "", lineJSON(f.p1, "1", "50", ""))))
	ff := f.posted(t, docBody("PRICE_DIFFERENCE", f.depot, f.cust, "2026-03-10", fmt.Sprintf(`,"linkedDocumentId":%d,"priceDirection":"DECREASE"`, sale.ID),
		`{"qty":"1","unitPrice":"10","vatPct":"20","description":"Fark"}`))

	all := f.grid(t, `{"startRow":0,"endRow":100}`)
	assert.Equal(t, []uint{ff.ID, gone.ID, draft.ID, buy.ID, sale.ID}, all.ids(), "documentDate desc, id desc")
	assert.Equal(t, 5, all.TotalCount)
	assert.Equal(t, 5, all.UnfilteredCount)
	assert.Equal(t, "168.00", all.Totals["grandTotal"], "120 + 24 + 12 (draft) + 12 (price difference), cancelled left out")
	for _, r := range all.Rows {
		if r.ID == ff.ID {
			require.NotNil(t, r.LinkedDocumentNo)
			assert.Equal(t, sale.no(), *r.LinkedDocumentNo)
			assert.Equal(t, "DECREASE", *r.PriceDirection)
		}
		if r.ID == draft.ID {
			assert.Nil(t, r.DocumentNo)
			assert.Nil(t, r.PostedAt)
		}
		if r.ID == sale.ID {
			assert.Equal(t, "C1", r.HesapNo)
			assert.Equal(t, "Card C1", r.SicilName)
			assert.Equal(t, "1", r.DepotCode)
			require.NotNil(t, r.SalesRepName)
		}
	}

	g := f.grid(t, fmt.Sprintf(`{"types":["SALES_INVOICE","PRICE_DIFFERENCE"],"statuses":["POSTED"],"sicilId":%d,"startRow":0,"endRow":100}`, f.cust))
	assert.Equal(t, []uint{ff.ID, sale.ID}, g.ids())
	assert.Equal(t, 5, g.UnfilteredCount)
	g = f.grid(t, fmt.Sprintf(`{"depotId":%d,"startRow":0,"endRow":100}`, f.depot2))
	assert.Equal(t, []uint{buy.ID}, g.ids())
	assert.Equal(t, 1, g.UnfilteredCount)
	g = f.grid(t, `{"dateFrom":"2026-03-05","dateTo":"2026-03-09","startRow":0,"endRow":100}`)
	assert.Equal(t, []uint{gone.ID, draft.ID, buy.ID}, g.ids(), "inclusive range")
	g = f.grid(t, `{"documentNo":"2026-1-00000","startRow":0,"endRow":100}`)
	assert.ElementsMatch(t, []uint{sale.ID, gone.ID, ff.ID}, g.ids())
	g = f.grid(t, `{"documentNo":"SF-2026-1-00000%","startRow":0,"endRow":100}`)
	assert.Empty(t, g.ids(), "% is literal")
	g = f.grid(t, `{"statuses":["POSTED","CANCELLED"],"sortModel":[{"colId":"postedAt","sort":"desc"}],"startRow":0,"endRow":2}`)
	assert.Equal(t, []uint{ff.ID, gone.ID}, g.ids(), "dashboard latest documents")
	assert.Equal(t, 4, g.TotalCount)
	g = f.grid(t, `{"filterModel":{"grandTotal":{"filterType":"number","type":"greaterThan","filter":100},"sicilName":{"filterType":"text","type":"contains","filter":"c1"}},"startRow":0,"endRow":100}`)
	assert.ElementsMatch(t, []uint{sale.ID}, g.ids())
	g = f.grid(t, `{"search":"S1","startRow":0,"endRow":100}`)
	assert.Equal(t, []uint{buy.ID}, g.ids())

	rec := f.req(t, http.MethodPost, f.path("/documents/grid"), f.token, `{"filterModel":{"nope":{"filterType":"text","type":"contains","filter":"x"}}}`, nil)
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	rec = f.req(t, http.MethodPost, f.path("/documents/grid"), f.token, `{"types":["NOPE"]}`, nil)
	assert.Equal(t, http.StatusBadRequest, rec.Code)

	rec = f.req(t, http.MethodPost, f.path("/documents/grid"), f.token, `{"startRow":0,"endRow":1}`,
		map[string]string{"Accept": xlsxType})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Equal(t, xlsxType, rec.Header().Get("Content-Type"))
	assert.True(t, bytes.HasPrefix(rec.Body.Bytes(), []byte("PK")))
	assert.Contains(t, sheetXML(t, rec.Body.Bytes()), buy.no(), "every filtered row, not one page")
}

func TestDocumentsRead_PDF(t *testing.T) {
	f := newDocs(t)
	f.move(t, f.p2, f.depot, "IN", "5", "2026-01-02")
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "100", ""), lineJSON(f.p2, "2", "50", "")))
	for _, doc := range []docJSON{d, f.postDoc(t, d)} {
		rec := f.req(t, http.MethodGet, f.path("/documents/"+idStr(doc.ID)), f.token, "", map[string]string{"Accept": "application/pdf"})
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		assert.Equal(t, "application/pdf", rec.Header().Get("Content-Type"))
		assert.True(t, bytes.HasPrefix(rec.Body.Bytes(), []byte("%PDF")))
	}
	assert.Equal(t, http.StatusNotFound, f.req(t, http.MethodGet, f.path("/documents/999999"), f.token, "",
		map[string]string{"Accept": "application/pdf"}).Code)
}

func TestDocumentsRead_StatementMovementsDashboardSearch(t *testing.T) {
	f := newDocs(t)
	d := f.posted(t, docBody("SALES_INVOICE_WITH_WAYBILL", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "2", "10", "")))
	c := f.cancel(t, d)

	var account uint
	require.NoError(t, testDB.Raw(`SELECT id FROM accounts WHERE sicil_id = ?`, f.cust).Scan(&account).Error)
	st := decode[struct {
		Lines []struct {
			EntryID      *uint   `json:"entryId"`
			DocumentNo   *string `json:"documentNo"`
			DocumentID   *uint   `json:"documentId"`
			DocumentType *string `json:"documentType"`
			Subtype      *string `json:"subtype"`
			Debit        string  `json:"debit"`
			Credit       string  `json:"credit"`
		} `json:"lines"`
	}](t, f.do(t, http.MethodGet, f.path("/ledger/accounts/"+idStr(account)+"/statement?from=2026-01-01&to=2026-12-31"), f.token, ""))
	require.Len(t, st.Lines, 2, "the entry and its reversal")
	for _, l := range st.Lines {
		require.NotNil(t, l.DocumentID)
		assert.Equal(t, d.ID, *l.DocumentID)
		assert.Equal(t, "SALES_INVOICE_WITH_WAYBILL", *l.DocumentType)
		assert.Equal(t, d.no(), *l.DocumentNo)
	}

	mv := decode[struct {
		Rows []struct {
			Label        string  `json:"label"`
			Kind         string  `json:"kind"`
			SourceNo     *string `json:"sourceNo"`
			DocumentID   *uint   `json:"documentId"`
			DocumentType *string `json:"documentType"`
			SicilName    *string `json:"sicilName"`
			Reversal     bool    `json:"reversal"`
		} `json:"rows"`
	}](t, f.req(t, http.MethodPost, f.path("/stock/movements/grid"), f.token, fmt.Sprintf(`{"partId":%d,"filterModel":{"label":{"filterType":"text","type":"equals","filter":"SALE"}}}`, f.p1), nil))
	require.Len(t, mv.Rows, 2)
	for _, r := range mv.Rows {
		assert.Equal(t, d.no(), *r.SourceNo)
		assert.Equal(t, d.ID, *r.DocumentID)
		assert.Equal(t, "SALES_INVOICE_WITH_WAYBILL", *r.DocumentType)
		assert.Equal(t, "Card C1", *r.SicilName)
		assert.Equal(t, r.Kind == "IN", r.Reversal)
	}

	r := f.posted(t, docBody("PURCHASE_INVOICE", f.depot, f.supp, "2026-03-02", "", lineJSON(f.p1, "1", "10", "")))
	dash := decode[struct {
		Latest []struct {
			Kind   string `json:"kind"`
			ID     uint   `json:"id"`
			Number string `json:"number"`
			Amount string `json:"amount"`
		} `json:"latest"`
	}](t, f.do(t, http.MethodGet, f.path("/dashboard?date=2026-12-31"), f.token, ""))
	require.NotEmpty(t, dash.Latest)
	assert.Equal(t, "DOCUMENT", dash.Latest[0].Kind)
	assert.Equal(t, r.no(), dash.Latest[0].Number)
	assert.Equal(t, r.GrandTotal, dash.Latest[0].Amount)

	search := decode[struct {
		Documents []struct {
			Kind    string `json:"kind"`
			ID      uint   `json:"id"`
			Number  string `json:"number"`
			Subtype string `json:"subtype"`
		} `json:"documents"`
	}](t, f.do(t, http.MethodGet, f.path("/search?q="+c.no()), f.token, ""))
	require.Len(t, search.Documents, 1)
	assert.Equal(t, d.ID, search.Documents[0].ID)
	assert.Equal(t, "SALES_INVOICE_WITH_WAYBILL", search.Documents[0].Subtype)
	search = decode[struct {
		Documents []struct {
			Kind    string `json:"kind"`
			ID      uint   `json:"id"`
			Number  string `json:"number"`
			Subtype string `json:"subtype"`
		} `json:"documents"`
	}](t, f.do(t, http.MethodGet, f.path("/search?q=F-2026-1-0%25"), f.token, ""))
	assert.Empty(t, search.Documents, "% is literal")
}

func TestDocumentsRead_CarryForwardNeedsNoDrafts(t *testing.T) {
	f := newDocs(t)
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-12-31", "", lineJSON(f.p1, "1", "10", "")))
	f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2027-01-01", "", lineJSON(f.p1, "1", "10", "")))
	rec := f.do(t, http.MethodGet, f.path("/ledger/carry-forwards/2027"), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	cf := decode[struct {
		Checks []struct {
			Code     string `json:"code"`
			OK       bool   `json:"ok"`
			Blocking bool   `json:"blocking"`
			Detail   string `json:"detail"`
		} `json:"checks"`
		Steps []struct {
			Code   string `json:"code"`
			Status string `json:"status"`
		} `json:"steps"`
	}](t, rec)
	require.Equal(t, "NO_DRAFT_DOCUMENTS", cf.Checks[0].Code)
	assert.False(t, cf.Checks[0].OK)
	assert.True(t, cf.Checks[0].Blocking)
	assert.Equal(t, idStr(d.ID), cf.Checks[0].Detail)
	assert.Equal(t, "BLOCKED", cf.Steps[0].Status)

	rec = f.req(t, http.MethodDelete, f.path("/documents/"+idStr(d.ID)), f.token, "", map[string]string{"If-Match": fmt.Sprintf(`"%d"`, d.Version)})
	require.Equal(t, http.StatusNoContent, rec.Code)
	cf2 := decode[struct {
		Checks []struct {
			Code string `json:"code"`
			OK   bool   `json:"ok"`
		} `json:"checks"`
	}](t, f.do(t, http.MethodGet, f.path("/ledger/carry-forwards/2027"), f.token, ""))
	assert.True(t, cf2.Checks[0].OK)
}

func TestDocumentsRead_CountValuesAtLastPurchaseCost(t *testing.T) {
	f := newDocs(t)
	f.posted(t, docBody("PURCHASE_INVOICE", f.depot2, f.supp, "2026-05-01", "", lineJSON(f.p1, "4", "30", `,"discount1Pct":"10"`)))
	c := f.openCount(t, f.depot, "")
	l := f.lineOf(t, c.ID, f.p1)
	assert.Equal(t, "LAST_PURCHASE", l.CostSource)
	require.NotNil(t, l.UnitCost)
	assert.Equal(t, "27", *l.UnitCost, "net / qty of the latest posted purchase invoice, any depot")
}

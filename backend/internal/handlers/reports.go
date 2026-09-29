package handlers

import (
	"fmt"
	"mime"
	"net/http"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/export"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

const (
	mimeXLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
	mimePDF  = "application/pdf"
)

func accepts(c echo.Context, m string) bool {
	for _, part := range strings.Split(c.Request().Header.Get(echo.HeaderAccept), ",") {
		if t, _, err := mime.ParseMediaType(strings.TrimSpace(part)); err == nil && t == m {
			return true
		}
	}
	return false
}

func sendFile(c echo.Context, contentType, name string, data []byte, err error) error {
	if err != nil {
		return apierror.Respond(c, err)
	}
	c.Response().Header().Set(echo.HeaderContentDisposition, mime.FormatMediaType("attachment", map[string]string{"filename": name}))
	return c.Blob(http.StatusOK, contentType, data)
}

type ReportHandler struct {
	reports  *services.ReportService
	receipts *services.ReceiptService
	sicils   *services.SicilService
}

func NewReportHandler(reports *services.ReportService, receipts *services.ReceiptService, sicils *services.SicilService) *ReportHandler {
	return &ReportHandler{reports: reports, receipts: receipts, sicils: sicils}
}

func (h *ReportHandler) Balances(c echo.Context) error {
	var in services.BalanceGridRequest
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	xlsx := accepts(c, mimeXLSX)
	page, err := h.reports.Balances(c.Request().Context(), orgID(c), in, xlsx)
	if err != nil {
		return apierror.Respond(c, err)
	}
	if !xlsx {
		return c.JSON(http.StatusOK, page)
	}
	t := export.Table{
		Columns: []export.Column{{Title: "Account", Kind: export.Text}, {Title: "Name", Kind: export.Text}, {Title: "City", Kind: export.Text},
			{Title: "Type", Kind: export.Text}, {Title: "Debit", Kind: export.MoneyCol}, {Title: "Credit", Kind: export.MoneyCol},
			{Title: "Balance", Kind: export.MoneyCol}, {Title: "Overdue", Kind: export.MoneyCol}, {Title: "Last move", Kind: export.DateCol}},
	}
	var sum [4]models.Money
	for _, r := range page.Rows {
		t.Rows = append(t.Rows, []any{r.AccountCode, r.AccountName, deref(r.City), deref(r.SicilTipi), r.DebitTotal, r.CreditTotal, r.Balance, r.Overdue, r.LastMoveDate})
		sum[0] += r.DebitTotal
		sum[1] += r.CreditTotal
		sum[2] += r.Balance
		sum[3] += r.Overdue
	}
	t.Totals = []any{"Total", nil, nil, nil, sum[0], sum[1], sum[2], sum[3]}
	data, err := export.XLSX("Balances", t)
	return sendFile(c, mimeXLSX, "balances-"+in.AsOf.String()+".xlsx", data, err)
}

func deref(s *string) string {
	if s == nil {
		return ""
	}
	return *s
}

func (h *ReportHandler) Statement(c echo.Context) error {
	id, err := pathID(c, "accountId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	from, err := queryDate(c, "from")
	if err != nil {
		return apierror.Respond(c, err)
	}
	to, err := queryDate(c, "to")
	if err != nil {
		return apierror.Respond(c, err)
	}
	var depotID *uint
	if d, err := queryUint(c, "depotId"); err != nil {
		return apierror.Respond(c, err)
	} else if d != 0 {
		depotID = &d
	}
	st, err := h.reports.Statement(c.Request().Context(), orgID(c), id, from, to, depotID)
	if err != nil {
		return apierror.Respond(c, err)
	}
	pdf, xlsx := accepts(c, mimePDF), accepts(c, mimeXLSX)
	if !pdf && !xlsx {
		return c.JSON(http.StatusOK, st)
	}
	depotCode := map[uint]string{}
	for _, d := range st.Depots {
		depotCode[d.ID] = d.Code
	}
	t := export.Table{Columns: []export.Column{{Title: "Date", Kind: export.DateCol}, {Title: "Depot", Kind: export.Text},
		{Title: "Document", Kind: export.Text}, {Title: "Description", Kind: export.Text}, {Title: "Due", Kind: export.DateCol},
		{Title: "Debit", Kind: export.MoneyCol}, {Title: "Credit", Kind: export.MoneyCol}, {Title: "Balance", Kind: export.MoneyCol}}}
	for _, l := range append(append([]models.StatementLine{}, st.BroughtForward...), st.Lines...) {
		t.Rows = append(t.Rows, []any{l.EntryDate, depotCode[*l.DepotID], deref(l.DocumentNo), l.Description, l.DueDate, l.Debit, l.Credit, l.RunningBalance})
	}
	t.Totals = []any{nil, nil, nil, "Closing balance", nil, nil, nil, st.ClosingBalance}
	totals := export.Table{Heading: "Depot totals", Columns: []export.Column{{Title: "Depot", Kind: export.Text},
		{Title: "Debit", Kind: export.MoneyCol}, {Title: "Credit", Kind: export.MoneyCol}, {Title: "Balance", Kind: export.MoneyCol}}}
	for _, d := range st.DepotTotals {
		totals.Rows = append(totals.Rows, []any{depotCode[d.DepotID], d.Debit, d.Credit, d.Balance})
	}
	name := fmt.Sprintf("statement-%s-%s-%s", st.Account.Code, st.From, st.To)
	if xlsx && !pdf {
		data, err := export.XLSX("Statement", t)
		return sendFile(c, mimeXLSX, name+".xlsx", data, err)
	}
	data, err := export.PDF(export.Document{
		Title:  "Account statement · " + st.Account.Code + " " + st.Account.Name,
		Meta:   [][2]string{{"Period", st.From.String() + " – " + st.To.String()}, {"Currency", st.Account.Currency}},
		Tables: []export.Table{t, totals}, Footer: "fikopar", Landscape: true,
	})
	return sendFile(c, mimePDF, name+".pdf", data, err)
}

var receiptTitle = map[string]string{"COLLECTION": "Collection receipt (tahsilat makbuzu)", "PAYMENT": "Payment receipt (tediye makbuzu)"}

// ReceiptPDF answers getReceipt when the client asks for application/pdf.
func (h *ReportHandler) ReceiptPDF(c echo.Context, r *models.Receipt) error {
	meta := [][2]string{
		{"Number", r.ReceiptNo}, {"Date", r.Date.String()}, {"Account", r.SicilName}, {"Method", r.Method},
		{"Amount", r.Amount.String() + " " + r.Currency},
	}
	if r.Description != "" {
		meta = append(meta, [2]string{"Description", r.Description})
	}
	if r.VoidedByEntryID != nil {
		meta = append(meta, [2]string{"Status", "VOID"})
	}
	data, err := export.PDF(export.Document{Title: receiptTitle[r.Kind], Meta: meta, Footer: "fikopar · " + r.ReceiptNo})
	return sendFile(c, mimePDF, r.ReceiptNo+".pdf", data, err)
}

func (h *ReportHandler) CashBookExport(c echo.Context, b *services.CashBook) error {
	t := export.Table{Columns: []export.Column{{Title: "Date", Kind: export.DateCol}, {Title: "Kind", Kind: export.Text},
		{Title: "Document", Kind: export.Text}, {Title: "Account", Kind: export.Text}, {Title: "Description", Kind: export.Text},
		{Title: "In", Kind: export.MoneyCol}, {Title: "Out", Kind: export.MoneyCol}, {Title: "Balance", Kind: export.MoneyCol}}}
	t.Rows = append(t.Rows, []any{b.From, "OPENING", nil, nil, "Opening balance", nil, nil, b.OpeningBalance})
	for _, l := range b.Lines {
		t.Rows = append(t.Rows, []any{l.Date, l.Kind, deref(l.DocumentNo), deref(l.SicilName), l.Description, l.Income, l.Expense, l.RunningBalance})
	}
	t.Totals = []any{nil, nil, nil, nil, "Closing balance", b.TotalIn, b.TotalOut, b.ClosingBalance}
	name := fmt.Sprintf("cash-book-%d-%s-%s", b.DepotID, b.From, b.To)
	if accepts(c, mimeXLSX) {
		data, err := export.XLSX("Cash book", t)
		return sendFile(c, mimeXLSX, name+".xlsx", data, err)
	}
	data, err := export.PDF(export.Document{Title: "Cash book", Meta: [][2]string{{"Period", b.From.String() + " – " + b.To.String()}},
		Tables: []export.Table{t}, Footer: "fikopar", Landscape: true})
	return sendFile(c, mimePDF, name+".pdf", data, err)
}

func (h *ReportHandler) ReceiptGridXLSX(c echo.Context, req repository.GridRequest) error {
	req.StartRow, req.EndRow = 0, 0
	resp, err := h.receipts.Grid(c.Request().Context(), orgID(c), req)
	if err != nil {
		return apierror.Respond(c, err)
	}
	t := export.Table{Columns: []export.Column{{Title: "Number", Kind: export.Text}, {Title: "Date", Kind: export.DateCol},
		{Title: "Kind", Kind: export.Text}, {Title: "Method", Kind: export.Text}, {Title: "Account", Kind: export.Text},
		{Title: "Source", Kind: export.Text}, {Title: "Amount", Kind: export.MoneyCol}}}
	for _, r := range *resp.Rows.(*[]models.Receipt) {
		t.Rows = append(t.Rows, []any{r.ReceiptNo, r.Date, r.Kind, r.Method, r.SicilName, deref(r.InstrumentNo), r.Amount})
	}
	data, err := export.XLSX("Receipts", t)
	return sendFile(c, mimeXLSX, "receipts.xlsx", data, err)
}

func (h *ReportHandler) SicilGridXLSX(c echo.Context, req repository.GridRequest) error {
	req.StartRow, req.EndRow = 0, 0
	resp, err := h.sicils.ListForGrid(orgID(c), req)
	if err != nil {
		return apierror.Respond(c, err)
	}
	t := export.Table{Columns: []export.Column{{Title: "Hesap no", Kind: export.Text}, {Title: "Hesap adı", Kind: export.Text},
		{Title: "Type", Kind: export.Text}, {Title: "City", Kind: export.Text}, {Title: "District", Kind: export.Text},
		{Title: "Tax office", Kind: export.Text}, {Title: "Tax no", Kind: export.Text}}}
	for _, s := range *resp.Rows.(*[]models.Sicil) {
		t.Rows = append(t.Rows, []any{s.HesapNo, s.HesapAdi, string(s.SicilTipi), s.Sehir, s.Ilce, s.VDairesi, s.VNo})
	}
	data, err := export.XLSX("Accounts", t)
	return sendFile(c, mimeXLSX, "sicils.xlsx", data, err)
}

func carryForwardPDF(c echo.Context, cf *services.CarryForward) error {
	checks := export.Table{Heading: "Checks", Columns: []export.Column{{Title: "Check", Kind: export.Text}, {Title: "Result", Kind: export.Text}, {Title: "Detail", Kind: export.Text}}}
	for _, ch := range cf.Checks {
		res := "OK"
		if !ch.OK {
			res = "FAILED"
		}
		checks.Rows = append(checks.Rows, []any{ch.Code, res, ch.Detail})
	}
	rows := export.Table{Heading: "Opening entries", Columns: []export.Column{{Title: "Account", Kind: export.Text}, {Title: "Name", Kind: export.Text},
		{Title: "Depot", Kind: export.Int}, {Title: "Closing", Kind: export.MoneyCol}, {Title: "Debit", Kind: export.MoneyCol},
		{Title: "Credit", Kind: export.MoneyCol}, {Title: "Status", Kind: export.Text}}}
	for _, r := range cf.Rows {
		rows.Rows = append(rows.Rows, []any{r.AccountCode, r.AccountName, r.DepotID, r.ClosingBalance, r.OpeningDebit, r.OpeningCredit, r.Status})
	}
	rows.Totals = []any{"Total", nil, nil, nil, cf.TotalDebit, cf.TotalCredit, nil}
	data, err := export.PDF(export.Document{
		Title:  fmt.Sprintf("Carry forward %d (%s)", cf.Year, cf.Status),
		Meta:   [][2]string{{"Entries", fmt.Sprint(cf.EntryCount)}, {"Accounts", fmt.Sprint(cf.AccountCount)}},
		Tables: []export.Table{checks, rows}, Footer: "fikopar", Landscape: true,
	})
	return sendFile(c, mimePDF, fmt.Sprintf("carry-forward-%d.pdf", cf.Year), data, err)
}

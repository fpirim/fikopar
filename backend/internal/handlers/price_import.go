package handlers

import (
	"bytes"
	"context"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"io"
	"mime"
	"mime/multipart"
	"net/http"
	"strconv"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/export"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/google/uuid"
	"github.com/labstack/echo/v4"
)

type PriceImportHandler struct {
	svc  *services.PriceImportService
	idem *services.IdempotencyService
}

func NewPriceImportHandler(svc *services.PriceImportService, idem *services.IdempotencyService) *PriceImportHandler {
	return &PriceImportHandler{svc: svc, idem: idem}
}

func notImplemented(c echo.Context) error {
	return c.JSON(http.StatusNotImplemented, map[string]string{"code": "ERR_INTERNAL"})
}

// maxUpload leaves room for the multipart envelope around a 10 MiB file.
const maxUpload = services.MaxImportFile + 512<<10

func (h *PriceImportHandler) List(c echo.Context) error {
	catalog, err := queryUint(c, "catalogId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	limit, _ := strconv.Atoi(c.QueryParam("limit"))
	out, err := h.svc.List(c.Request().Context(), orgID(c), catalog, limit, c.QueryParam("cursor"))
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}

func parseUpload(contentType string, body []byte) (services.ImportCreate, error) {
	var in services.ImportCreate
	mt, params, err := mime.ParseMediaType(contentType)
	if err != nil || !strings.HasPrefix(mt, "multipart/") || params["boundary"] == "" {
		return in, apierror.NoFileUploaded
	}
	form, err := multipart.NewReader(bytes.NewReader(body), params["boundary"]).ReadForm(maxUpload)
	if err != nil {
		return in, apierror.InvalidRequest
	}
	defer func() { _ = form.RemoveAll() }()
	value := func(k string) string {
		if v := form.Value[k]; len(v) > 0 {
			return strings.TrimSpace(v[0])
		}
		return ""
	}
	if v := value("catalogId"); v != "" {
		n, err := strconv.ParseUint(v, 10, 32)
		if err != nil {
			return in, apierror.WithField(apierror.InvalidRequest, "catalogId")
		}
		in.CatalogID = uint(n)
	}
	if v := value("validFrom"); v != "" {
		d, err := models.ParseDate(v)
		if err != nil {
			return in, apierror.WithField(apierror.InvalidRequest, "validFrom")
		}
		in.ValidFrom = &d
	}
	if v := value("mapping"); v != "" {
		var m services.ImportMapping
		if err := json.Unmarshal([]byte(v), &m); err != nil {
			return in, apierror.WithField(apierror.InvalidRequest, "mapping")
		}
		in.Mapping = &m
	}
	if files := form.File["file"]; len(files) > 0 {
		f, err := files[0].Open()
		if err != nil {
			return in, apierror.FileOpen
		}
		defer func() { _ = f.Close() }()
		if in.Content, err = io.ReadAll(f); err != nil {
			return in, apierror.FileOpen
		}
		in.FileName = files[0].Filename
	}
	return in, nil
}

// uploadFingerprint is the request as idempotency sees it: a retry sends a new multipart boundary.
func uploadFingerprint(in services.ImportCreate) []byte {
	sum := sha256.Sum256(in.Content)
	b, _ := json.Marshal(map[string]any{"file": hex.EncodeToString(sum[:]), "fileName": in.FileName, "catalogId": in.CatalogID,
		"validFrom": in.ValidFrom, "mapping": in.Mapping})
	return b
}

// Create is idempotent over the whole multipart body, which may exceed the 1 MiB JSON limit.
func (h *PriceImportHandler) Create(c echo.Context) error {
	key := strings.TrimSpace(c.Request().Header.Get("Idempotency-Key"))
	if key == "" {
		return apierror.Respond(c, apierror.IdempotencyKeyRequired)
	}
	if _, err := uuid.Parse(key); err != nil {
		return apierror.Respond(c, apierror.WithField(apierror.InvalidRequest, "Idempotency-Key"))
	}
	body, err := io.ReadAll(io.LimitReader(c.Request().Body, maxUpload+1))
	if err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	if len(body) > maxUpload {
		return apierror.Respond(c, apierror.FileTooLarge)
	}
	in, err := parseUpload(c.Request().Header.Get(echo.HeaderContentType), body)
	if err != nil {
		return apierror.Respond(c, err)
	}
	org := orgID(c)
	endpoint := c.Request().Method + " " + c.Request().URL.Path
	res, err := h.idem.Do(reqCtx(c), org, key, endpoint, uploadFingerprint(in), func(ctx context.Context) (int, any, error) {
		return created(h.svc.Create(ctx, org, in))
	})
	if err != nil {
		return apierror.Respond(c, err)
	}
	if res.Replayed {
		c.Response().Header().Set("Idempotent-Replayed", "true")
	}
	var v struct {
		Version int `json:"version"`
	}
	if json.Unmarshal(res.Body, &v) == nil {
		setETag(c, v.Version)
	}
	return c.Blob(res.Status, echo.MIMEApplicationJSON, res.Body)
}

func (h *PriceImportHandler) Get(c echo.Context) error {
	id, err := pathID(c, "importId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.Get(c.Request().Context(), orgID(c), id)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, out.Version)
	return c.JSON(http.StatusOK, out)
}

func (h *PriceImportHandler) Update(c echo.Context) error {
	id, err := pathID(c, "importId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	var in services.ImportUpdate
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.Update(reqCtx(c), orgID(c), id, version, in)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, out.Version)
	return c.JSON(http.StatusOK, out)
}

func (h *PriceImportHandler) GridRows(c echo.Context) error {
	id, err := pathID(c, "importId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	var in services.ImportGridRequest
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	xlsx := accepts(c, mimeXLSX)
	out, err := h.svc.Grid(c.Request().Context(), orgID(c), id, in, xlsx)
	if err != nil {
		return apierror.Respond(c, err)
	}
	if !xlsx {
		return c.JSON(http.StatusOK, out)
	}
	t := export.Table{Columns: []export.Column{{Title: "Row", Kind: export.Int}, {Title: "Stock no", Kind: export.Text},
		{Title: "Description", Kind: export.Text}, {Title: "Brand", Kind: export.Text}, {Title: "Price", Kind: export.Number},
		{Title: "Current price", Kind: export.Number}, {Title: "Change %", Kind: export.Text}, {Title: "Valid from", Kind: export.DateCol},
		{Title: "Changes", Kind: export.Text}, {Title: "Errors", Kind: export.Text}}}
	for _, r := range out.Rows {
		cells := make([]any, 10)
		if r.RowNo != nil {
			cells[0] = *r.RowNo
		}
		cells[1] = deref(r.StockNo)
		src := r.File
		if src == nil {
			src = r.Current
		}
		if src != nil {
			cells[2], cells[3], cells[4], cells[7] = deref(src.Description), deref(src.Brand), src.Price, src.ValidFrom
		}
		if r.Current != nil {
			cells[5] = r.Current.Price
		}
		cells[6] = deref(r.PriceChangePct)
		cells[8] = strings.Join(r.Changes, ", ")
		codes := make([]string, len(r.Errors))
		for i, e := range r.Errors {
			codes[i] = e.Code
		}
		cells[9] = strings.Join(codes, ", ")
		t.Rows = append(t.Rows, cells)
	}
	data, err := export.XLSX("Import", t)
	return sendFile(c, mimeXLSX, "price-import-"+strconv.FormatUint(uint64(id), 10)+".xlsx", data, err)
}

func (h *PriceImportHandler) Apply(c echo.Context) error {
	id, err := pathID(c, "importId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	org := orgID(c)
	return idempotentETag(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in services.ImportApply
		if len(bytes.TrimSpace(body)) > 0 {
			if err := unmarshal(body, &in); err != nil {
				return 0, nil, err
			}
		}
		return okStatus(h.svc.Apply(ctx, org, id, version, in))
	})
}

func (h *PriceImportHandler) Discard(c echo.Context) error {
	id, err := pathID(c, "importId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.Discard(reqCtx(c), orgID(c), id, version)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, out.Version)
	return c.JSON(http.StatusOK, out)
}

func (h *PriceImportHandler) Presets(c echo.Context) error {
	return c.JSON(http.StatusOK, h.svc.Presets())
}

package services

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"errors"
	"time"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/pricefile"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

// PriceImportService runs price list imports: upload, parse, preview diff, apply once.
type PriceImportService struct {
	store *repository.Store
}

func NewPriceImportService(store *repository.Store) *PriceImportService {
	return &PriceImportService{store: store}
}

// ImportMapping is the contract's PriceImportMapping.
type ImportMapping = pricefile.Mapping

// MaxImportFile is the upload limit (10 MiB).
const MaxImportFile = 10 << 20

type ImportCounts struct {
	Rows       int `json:"rows"`
	New        int `json:"new"`
	Price      int `json:"price"`
	Attributes int `json:"attributes"`
	Unchanged  int `json:"unchanged"`
	Missing    int `json:"missing"`
	Delete     int `json:"delete"`
	Errors     int `json:"errors"`
	Warnings   int `json:"warnings"`
}

type ImportResult struct {
	PartsCreated     int `json:"partsCreated"`
	PricesInserted   int `json:"pricesInserted"`
	PartsUpdated     int `json:"partsUpdated"`
	PartsDeactivated int `json:"partsDeactivated"`
	RowsSkipped      int `json:"rowsSkipped"`
}

// PriceImport is the contract's job.
type PriceImport struct {
	ID                  uint              `json:"id"`
	CatalogID           uint              `json:"catalogId"`
	CatalogCode         int               `json:"catalogCode"`
	FileName            string            `json:"fileName"`
	FileSha256          string            `json:"fileSha256"`
	FileSize            int               `json:"fileSize"`
	Format              string            `json:"format"`
	Mapping             *pricefile.Mapping `json:"mapping"`
	ValidFrom           models.Date       `json:"validFrom"`
	Status              string            `json:"status"`
	FailureCode         *string           `json:"failureCode"`
	FailureDetail       *string           `json:"failureDetail"`
	Headers             []string          `json:"headers"`
	Sample              [][]string        `json:"sample"`
	UnmappedTargets     []string          `json:"unmappedTargets"`
	Counts              ImportCounts      `json:"counts"`
	CatalogVersion      *int64            `json:"catalogVersion"`
	DuplicateOfImportID *uint             `json:"duplicateOfImportId"`
	Result              *ImportResult     `json:"result"`
	CreatedBy           *string           `json:"createdBy"`
	CreatedAt           time.Time         `json:"createdAt"`
	ExpiresAt           time.Time         `json:"expiresAt"`
	AppliedBy           *string           `json:"appliedBy"`
	AppliedAt           *time.Time        `json:"appliedAt"`
	Version             int               `json:"version"`
}

// ImportFields is the contract's PriceImportFields.
type ImportFields struct {
	StockNo          string                `json:"stockNo"`
	Description      *string               `json:"description"`
	Brand            *string               `json:"brand"`
	Type             *string               `json:"type"`
	ManufacturerCode *string               `json:"manufacturerCode"`
	OEM              []string              `json:"oem"`
	AltCodes         []string              `json:"altCodes"`
	Price            *models.Dec4          `json:"price"`
	ValidFrom        *models.Date          `json:"validFrom"`
	PackQty          *int                  `json:"packQty"`
	VatPct           *string               `json:"vatPct"`
	Equivalent       *pricefile.Equivalent `json:"equivalent"`
}

type ImportRow struct {
	ID             uint              `json:"id"`
	RowNo          *int              `json:"rowNo"`
	PartID         *uint             `json:"partId"`
	StockNo        *string           `json:"stockNo"`
	Changes        []string          `json:"changes"`
	File           *ImportFields     `json:"file"`
	Current        *ImportFields     `json:"current"`
	ChangedFields  []string          `json:"changedFields"`
	PriceChangePct *string           `json:"priceChangePct"`
	Errors         []pricefile.Issue `json:"errors"`
	Warnings       []pricefile.Issue `json:"warnings"`
}

func decodeJSON[T any](s string, v *T) {
	if s != "" {
		_ = json.Unmarshal([]byte(s), v)
	}
}

func toJob(r *repository.ImportRecord) *PriceImport {
	j := &PriceImport{ID: r.ID, CatalogID: r.CatalogID, CatalogCode: r.CatalogCode, FileName: r.FileName, FileSha256: r.FileSha256,
		FileSize: r.FileSize, Format: r.Format, ValidFrom: r.ValidFrom, Status: r.Status, FailureCode: r.FailureCode,
		FailureDetail: r.FailureDetail, CatalogVersion: r.CatalogVersion, DuplicateOfImportID: r.DuplicateOf,
		CreatedBy: r.CreatedBy, CreatedAt: r.CreatedAt, ExpiresAt: r.ExpiresAt, AppliedBy: r.AppliedBy, AppliedAt: r.AppliedAt,
		Version: r.Version, Headers: []string{}, Sample: [][]string{}, UnmappedTargets: []string{}}
	decodeJSON(r.Mapping, &j.Mapping)
	if j.Mapping != nil && j.Mapping.Columns == nil {
		j.Mapping.Columns = map[string]pricefile.Column{}
	}
	decodeJSON(r.Headers, &j.Headers)
	decodeJSON(r.Sample, &j.Sample)
	decodeJSON(r.Unmapped, &j.UnmappedTargets)
	decodeJSON(r.Counts, &j.Counts)
	if r.Result != nil {
		j.Result = &ImportResult{}
		decodeJSON(*r.Result, j.Result)
	}
	return j
}

func getJob(q *repository.Q, orgID, id uint) (*PriceImport, error) {
	r, err := q.GetImport(orgID, id)
	if repository.IsNotFound(err) {
		return nil, apierror.ImportNotFound
	}
	if err != nil {
		return nil, err
	}
	return toJob(r), nil
}

type ImportCreate struct {
	FileName  string
	Content   []byte
	CatalogID uint
	ValidFrom *models.Date
	Mapping   *pricefile.Mapping
}

func (s *PriceImportService) Create(ctx context.Context, orgID uint, in ImportCreate) (*PriceImport, error) {
	switch {
	case len(in.Content) == 0:
		return nil, apierror.NoFileUploaded
	case len(in.Content) > MaxImportFile:
		return nil, apierror.FileTooLarge
	case in.CatalogID == 0:
		return nil, apierror.WithField(apierror.InvalidRequest, "catalogId")
	case in.ValidFrom == nil:
		return nil, apierror.WithField(apierror.InvalidRequest, "validFrom")
	}
	requested := pricefile.Mapping{}
	if in.Mapping != nil {
		requested = *in.Mapping
	}
	format, preset := "CSV", (*string)(nil)
	if m, err := pricefile.Resolve(requested, in.FileName); err == nil {
		format, preset = m.Format, m.Preset
	}
	sum := sha256.Sum256(in.Content)
	var out *PriceImport
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		cat, err := q.GetCatalog(orgID, in.CatalogID)
		if repository.IsNotFound(err) {
			return apierror.UnknownRef(apierror.CatalogNotFound, "catalogId")
		}
		if err != nil {
			return err
		}
		mj, _ := json.Marshal(requested)
		id, err := q.InsertImport(orgID, repository.ImportInsert{CatalogID: in.CatalogID, FileName: in.FileName,
			Sha256: hex.EncodeToString(sum[:]), Size: len(in.Content), Format: format, Preset: preset, Mapping: string(mj),
			ValidFrom: *in.ValidFrom, CreatedBy: actorFrom(ctx).userID()}, in.Content)
		if err != nil {
			return err
		}
		if err := buildPreview(q, orgID, id, catalogKey{cat.ID, cat.Code}, in.Content, in.FileName, requested, *in.ValidFrom, false); err != nil {
			return err
		}
		if out, err = getJob(q, orgID, id); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "price_import.created", "price_import", id, nil, out)
	})
	return out, err
}

func failed(code string, detail string) repository.ImportPreview {
	return repository.ImportPreview{Status: "FAILED", FailureCode: &code, FailureDetail: &detail, Headers: "[]", Sample: "[]", Counts: "{}"}
}

// buildPreview parses the file and writes the job status, mapping and preview rows.
func buildPreview(q *repository.Q, orgID, id uint, cat catalogKey, content []byte, fileName string,
	requested pricefile.Mapping, validFrom models.Date, bump bool) error {
	p, rows, err := preview(q, orgID, cat, content, fileName, requested, validFrom)
	if err != nil {
		return err
	}
	if p.Mapping == "" {
		mj, _ := json.Marshal(requested)
		p.Mapping = string(mj)
	}
	p.ValidFrom = validFrom
	if err := q.ReplaceImportRows(orgID, id, rows); err != nil {
		return err
	}
	return q.SaveImportPreview(id, p, bump)
}

func preview(q *repository.Q, orgID uint, cat catalogKey, content []byte, fileName string,
	requested pricefile.Mapping, validFrom models.Date) (repository.ImportPreview, []repository.ImportRowInsert, error) {
	m, err := pricefile.Resolve(requested, fileName)
	if err != nil {
		p := failed(apierror.ImportFormat.Code, "mapping")
		p.Mapping = "null"
		return p, nil, nil
	}
	f, err := pricefile.Read(content, m)
	var p repository.ImportPreview
	switch {
	case errors.Is(err, pricefile.ErrTooManyRows):
		p = failed(apierror.ImportTooManyRows.Code, "more than 100000 rows")
	case errors.Is(err, pricefile.ErrFormat):
		p = failed(apierror.ImportFormat.Code, err.Error())
	case err != nil:
		return repository.ImportPreview{}, nil, err
	}
	if p.Status != "" {
		mj, _ := json.Marshal(m)
		p.Mapping = string(mj)
		return p, nil, nil
	}
	m = pricefile.AutoMap(m, f.Headers)
	mj, _ := json.Marshal(m)
	headers := f.Headers
	if headers == nil {
		headers = []string{}
	}
	hj, _ := json.Marshal(headers)
	sj, _ := json.Marshal(f.Sample(5))
	p = repository.ImportPreview{Mapping: string(mj), Headers: string(hj), Sample: string(sj), Counts: "{}"}
	if p.Unmapped = pricefile.Unmapped(m, f.Headers); len(p.Unmapped) > 0 {
		p.Status = "NEEDS_MAPPING"
		cj, _ := json.Marshal(ImportCounts{Rows: len(f.Rows)})
		p.Counts, p.RowCount = string(cj), len(f.Rows)
		return p, nil, nil
	}
	d, err := newDiffer(q, orgID, cat, m, validFrom)
	if err != nil {
		return p, nil, err
	}
	rows, counts, err := d.run(f)
	if err != nil {
		return p, nil, err
	}
	cj, _ := json.Marshal(counts)
	p.Status, p.Counts, p.RowCount, p.CatalogVersion = "READY", string(cj), counts.Rows, &d.version
	return p, rows, nil
}

func (s *PriceImportService) Get(ctx context.Context, orgID, id uint) (*PriceImport, error) {
	var out *PriceImport
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if err := q.ExpireImports(orgID); err != nil {
			return err
		}
		var err error
		out, err = getJob(q, orgID, id)
		return err
	})
	return out, err
}

type ImportPage struct {
	Items      []*PriceImport `json:"items"`
	NextCursor *string        `json:"nextCursor"`
}

func (s *PriceImportService) List(ctx context.Context, orgID, catalogID uint, limit int, cursor string) (*ImportPage, error) {
	var before uint
	if _, err := decodeCursorInto(cursor, &before); err != nil {
		return nil, err
	}
	limit = clampLimit(limit)
	page := &ImportPage{Items: []*PriceImport{}}
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if err := q.ExpireImports(orgID); err != nil {
			return err
		}
		rows, err := q.ListImports(orgID, catalogID, before, limit+1)
		if err != nil {
			return err
		}
		for i := range rows {
			if i == limit {
				c := encodeCursor(rows[limit-1].ID)
				page.NextCursor = &c
				break
			}
			page.Items = append(page.Items, toJob(&rows[i]))
		}
		return nil
	})
	return page, err
}

// lockJob expires stale jobs, locks the job and checks its status and version.
func lockJob(q *repository.Q, orgID, id uint, version int, statuses ...string) (*PriceImport, error) {
	if err := q.ExpireImports(orgID); err != nil {
		return nil, err
	}
	if err := q.LockImport(orgID, id); repository.IsNotFound(err) {
		return nil, apierror.ImportNotFound
	} else if err != nil {
		return nil, err
	}
	j, err := getJob(q, orgID, id)
	if err != nil {
		return nil, err
	}
	ok := false
	for _, s := range statuses {
		ok = ok || j.Status == s
	}
	if !ok {
		return nil, apierror.WithDetail(apierror.InvalidStateTransition, j.Status)
	}
	if j.Version != version {
		return nil, apierror.VersionConflict
	}
	return j, nil
}

type ImportUpdate struct {
	Mapping   *pricefile.Mapping `json:"mapping"`
	ValidFrom *models.Date       `json:"validFrom"`
}

// Update changes the mapping or default date and rebuilds the preview against the catalog as it is now.
func (s *PriceImportService) Update(ctx context.Context, orgID, id uint, version int, in ImportUpdate) (*PriceImport, error) {
	var out *PriceImport
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		j, err := lockJob(q, orgID, id, version, "NEEDS_MAPPING", "READY")
		if err != nil {
			return err
		}
		content, err := q.ImportFile(id)
		if err != nil {
			return err
		}
		var m pricefile.Mapping
		if j.Mapping != nil {
			m = *j.Mapping
		}
		if in.Mapping != nil {
			m = *in.Mapping
		}
		vf := j.ValidFrom
		if in.ValidFrom != nil {
			vf = *in.ValidFrom
		}
		if err := buildPreview(q, orgID, id, catalogKey{j.CatalogID, j.CatalogCode}, content, j.FileName, m, vf, true); err != nil {
			return err
		}
		if out, err = getJob(q, orgID, id); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "price_import.updated", "price_import", id, j, out)
	})
	return out, err
}

func (s *PriceImportService) Discard(ctx context.Context, orgID, id uint, version int) (*PriceImport, error) {
	var out *PriceImport
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		j, err := lockJob(q, orgID, id, version, "PARSING", "NEEDS_MAPPING", "READY")
		if err != nil {
			return err
		}
		if err := q.DiscardImport(id); err != nil {
			return err
		}
		if out, err = getJob(q, orgID, id); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "price_import.discarded", "price_import", id, j, out)
	})
	return out, err
}

type ImportPresets struct {
	Presets []pricefile.Preset  `json:"presets"`
	Aliases map[string][]string `json:"aliases"`
}

func (s *PriceImportService) Presets() ImportPresets {
	return ImportPresets{Presets: pricefile.Presets(), Aliases: pricefile.Aliases()}
}

type ImportGridRequest struct {
	repository.ReportGridRequest
	Change string `json:"change"`
}

type ImportGrid struct {
	Rows            []ImportRow       `json:"rows"`
	TotalCount      int64             `json:"totalCount"`
	UnfilteredCount int64             `json:"unfilteredCount"`
	Totals          map[string]string `json:"totals"`
}

func (s *PriceImportService) Grid(ctx context.Context, orgID, id uint, in ImportGridRequest, all bool) (*ImportGrid, error) {
	if in.Change == "" {
		in.Change = "ALL"
	}
	if !repository.ValidImportChange(in.Change) {
		return nil, apierror.WithField(apierror.InvalidRequest, "change")
	}
	q := s.store.Read(ctx)
	if _, err := getJob(q, orgID, id); err != nil {
		return nil, err
	}
	rows, total, unfiltered, err := q.GridImportRows(id, in.ReportGridRequest, in.Change, all)
	if repository.IsBadFilter(err) {
		return nil, apierror.WithDetail(apierror.InvalidRequest, err.Error())
	}
	if err != nil {
		return nil, err
	}
	out := &ImportGrid{Rows: make([]ImportRow, len(rows)), TotalCount: total, UnfilteredCount: unfiltered, Totals: map[string]string{}}
	for i, r := range rows {
		row := ImportRow{ID: r.ID, RowNo: r.RowNo, PartID: r.PartID, StockNo: r.StockNo, PriceChangePct: r.PriceChangePct,
			Changes: []string{}, ChangedFields: []string{}, Errors: []pricefile.Issue{}, Warnings: []pricefile.Issue{}}
		decodeJSON(r.Changes, &row.Changes)
		decodeJSON(r.Changed, &row.ChangedFields)
		decodeJSON(r.Errors, &row.Errors)
		decodeJSON(r.Warnings, &row.Warnings)
		if r.RowNo != nil {
			row.File = &ImportFields{}
			decodeJSON(r.Parsed, row.File)
		}
		if r.Current != nil {
			row.Current = &ImportFields{}
			decodeJSON(*r.Current, row.Current)
		}
		out.Rows[i] = row
	}
	return out, nil
}

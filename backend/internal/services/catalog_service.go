package services

import (
	"context"
	"net/http"
	"regexp"
	"strings"
	"unicode/utf8"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

// CatalogService owns catalogs, parts, codes and list prices.
type CatalogService struct {
	store *repository.Store
}

func NewCatalogService(store *repository.Store) *CatalogService { return &CatalogService{store: store} }

type CatalogWrite struct {
	Code            int    `json:"code"`
	Name            string `json:"name"`
	SupplierSicilID *uint  `json:"supplierSicilId"`
	IsActive        *bool  `json:"isActive"`
}

func (w *CatalogWrite) validate() (repository.CatalogWrite, error) {
	w.Name = strings.TrimSpace(w.Name)
	if w.Code < 1 || w.Code > 9999 {
		return repository.CatalogWrite{}, apierror.WithField(apierror.InvalidRequest, "code")
	}
	if w.Name == "" || utf8.RuneCountInString(w.Name) > 60 {
		return repository.CatalogWrite{}, apierror.WithField(apierror.InvalidRequest, "name")
	}
	active := w.IsActive == nil || *w.IsActive
	return repository.CatalogWrite{Code: w.Code, Name: w.Name, SupplierSicilID: w.SupplierSicilID, IsActive: active}, nil
}

func (s *CatalogService) ListCatalogs(ctx context.Context, orgID uint, includeInactive bool) ([]models.Catalog, error) {
	return s.store.Read(ctx).ListCatalogs(orgID, includeInactive)
}

func (s *CatalogService) GetCatalog(ctx context.Context, orgID, id uint) (*models.Catalog, error) {
	c, err := s.store.Read(ctx).GetCatalog(orgID, id)
	if repository.IsNotFound(err) {
		return nil, apierror.CatalogNotFound
	}
	return c, err
}

func supplierRef(q *repository.Q, orgID uint, id *uint) error {
	if id == nil {
		return nil
	}
	_, err := q.GetSicilRef(orgID, *id)
	if repository.IsNotFound(err) {
		return apierror.UnknownRef(apierror.SicilNotFound, "supplierSicilId")
	}
	return err
}

func (s *CatalogService) CreateCatalog(ctx context.Context, orgID uint, in CatalogWrite) (*models.Catalog, error) {
	w, err := in.validate()
	if err != nil {
		return nil, err
	}
	var out *models.Catalog
	err = s.store.InTx(ctx, func(q *repository.Q) error {
		if err := supplierRef(q, orgID, w.SupplierSicilID); err != nil {
			return err
		}
		id, err := q.InsertCatalog(orgID, w)
		if repository.IsUniqueViolation(err) {
			return apierror.CatalogCodeExists
		}
		if err != nil {
			return err
		}
		if out, err = q.GetCatalog(orgID, id); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "catalog.created", "catalog", id, nil, out)
	})
	return out, err
}

func (s *CatalogService) UpdateCatalog(ctx context.Context, orgID, id uint, version int, in CatalogWrite) (*models.Catalog, error) {
	w, err := in.validate()
	if err != nil {
		return nil, err
	}
	var out *models.Catalog
	err = s.store.InTx(ctx, func(q *repository.Q) error {
		before, err := q.GetCatalog(orgID, id)
		if repository.IsNotFound(err) {
			return apierror.CatalogNotFound
		}
		if err != nil {
			return err
		}
		if before.Code != w.Code {
			return apierror.WithField(apierror.CatalogCodeImmutable, "code")
		}
		if err := supplierRef(q, orgID, w.SupplierSicilID); err != nil {
			return err
		}
		ok, err := q.UpdateCatalog(orgID, id, version, w)
		if err != nil {
			return err
		}
		if !ok {
			return apierror.VersionConflict
		}
		if out, err = q.GetCatalog(orgID, id); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "catalog.updated", "catalog", id, before, out)
	})
	return out, err
}

type PartWrite struct {
	CatalogID    uint              `json:"catalogId"`
	StockNo      string            `json:"stockNo"`
	Name         string            `json:"name"`
	Type         *string           `json:"type"`
	Brand        *string           `json:"brand"`
	Unit         string            `json:"unit"`
	PackageQty   *int              `json:"packageQty"`
	VatPct       string            `json:"vatPct"`
	IsActive     *bool             `json:"isActive"`
	Codes        []models.PartCode `json:"codes"`
	InitialPrice *PriceWrite       `json:"initialPrice"`
}

type PriceWrite struct {
	Price     string      `json:"price"`
	ValidFrom models.Date `json:"validFrom"`
}

var codeKinds = map[string]bool{models.CodeManufacturer: true, models.CodeOEM: true, models.CodeALT: true, models.CodeEquivalent: true}

var nonAlnum = regexp.MustCompile(`[^0-9A-Za-z]`)

// asciiFold maps the Turkish letters to ASCII (parts.name_fold and fold_code() in 0011 use the same table).
var asciiFold = strings.NewReplacer("ı", "i", "İ", "I", "ş", "s", "Ş", "S", "ğ", "g", "Ğ", "G", "ü", "u", "Ü", "U",
	"ö", "o", "Ö", "O", "ç", "c", "Ç", "C")

// NormalizeCode is the matching form of a code: Turkish letters folded to ASCII, upper case, 0-9 A-Z only.
func NormalizeCode(s string) string {
	return strings.ToUpper(nonAlnum.ReplaceAllString(asciiFold.Replace(s), ""))
}

func optText(p *string, max int, field string) (*string, error) {
	if p == nil {
		return nil, nil
	}
	v := strings.TrimSpace(*p)
	if v == "" {
		return nil, nil
	}
	if utf8.RuneCountInString(v) > max {
		return nil, apierror.WithField(apierror.InvalidRequest, field)
	}
	return &v, nil
}

func (w *PartWrite) validate() (repository.PartWrite, error) {
	var r repository.PartWrite
	var err error
	w.StockNo = strings.TrimSpace(w.StockNo)
	w.Name = strings.TrimSpace(w.Name)
	switch {
	case w.CatalogID == 0:
		return r, apierror.WithField(apierror.InvalidRequest, "catalogId")
	case w.StockNo == "" || utf8.RuneCountInString(w.StockNo) > 15:
		return r, apierror.WithField(apierror.InvalidRequest, "stockNo")
	case w.Name == "" || utf8.RuneCountInString(w.Name) > 60:
		return r, apierror.WithField(apierror.InvalidRequest, "name")
	}
	r = repository.PartWrite{CatalogID: w.CatalogID, StockNo: w.StockNo, Name: w.Name, Unit: strings.TrimSpace(w.Unit),
		PackageQty: 1, VatPct: strings.TrimSpace(w.VatPct), IsActive: w.IsActive == nil || *w.IsActive}
	if r.Type, err = optText(w.Type, 30, "type"); err != nil {
		return r, err
	}
	if r.Brand, err = optText(w.Brand, 30, "brand"); err != nil {
		return r, err
	}
	if r.Unit == "" {
		r.Unit = "PCS"
	}
	if w.PackageQty != nil {
		if *w.PackageQty < 1 {
			return r, apierror.WithField(apierror.InvalidRequest, "packageQty")
		}
		r.PackageQty = *w.PackageQty
	}
	if r.VatPct == "" {
		r.VatPct = "20"
	}
	if v, err := models.ParseQuantity(r.VatPct); err != nil || v > 100*10000 {
		return r, apierror.WithField(apierror.InvalidRequest, "vatPct")
	}
	seen := map[string]bool{}
	for i := range w.Codes {
		c := &w.Codes[i]
		c.Code = strings.TrimSpace(c.Code)
		field := "codes[" + itoa(i) + "]"
		if !codeKinds[c.Kind] {
			return r, apierror.WithField(apierror.InvalidRequest, field+".kind")
		}
		if c.Kind != models.CodeEquivalent && (c.Code == "" || utf8.RuneCountInString(c.Code) > 40 || NormalizeCode(c.Code) == "") {
			return r, apierror.WithField(apierror.InvalidRequest, field+".code")
		}
		if (c.Kind == models.CodeEquivalent) != (c.EquivalentPartID != nil) {
			return r, apierror.WithField(apierror.InvalidRequest, field+".equivalentPartId")
		}
		if key := c.Kind + " " + NormalizeCode(c.Code); c.Kind != models.CodeEquivalent {
			if seen[key] {
				return r, apierror.WithField(apierror.DuplicateLine, field+".code")
			}
			seen[key] = true
		}
	}
	return r, nil
}

// checkCodes resolves EQUIVALENT links inside the company; their code becomes the linked stock no.
// A second link to the same part is ERR_DUPLICATE_LINE.
func checkCodes(q *repository.Q, orgID, self uint, codes []models.PartCode) error {
	linked := map[uint]bool{}
	for i, c := range codes {
		if c.EquivalentPartID == nil {
			continue
		}
		field := "codes[" + itoa(i) + "].equivalentPartId"
		if *c.EquivalentPartID == self {
			return apierror.WithField(apierror.InvalidRequest, field)
		}
		if linked[*c.EquivalentPartID] {
			return apierror.UnknownRef(apierror.DuplicateLine, field)
		}
		linked[*c.EquivalentPartID] = true
		ref, err := q.GetPartRef(orgID, *c.EquivalentPartID)
		if repository.IsNotFound(err) {
			return apierror.UnknownRef(apierror.PartNotFound, field)
		} else if err != nil {
			return err
		}
		codes[i].Code = ref.StockNo
	}
	return nil
}

func (s *CatalogService) CreatePart(ctx context.Context, orgID uint, in PartWrite) (*models.Part, error) {
	w, err := in.validate()
	if err != nil {
		return nil, err
	}
	var price models.Dec4
	if in.InitialPrice != nil {
		if price, err = in.InitialPrice.parse("initialPrice."); err != nil {
			return nil, err
		}
	}
	var out *models.Part
	err = s.store.InTx(ctx, func(q *repository.Q) error {
		if _, err := q.GetCatalog(orgID, w.CatalogID); repository.IsNotFound(err) {
			return apierror.UnknownRef(apierror.CatalogNotFound, "catalogId")
		} else if err != nil {
			return err
		}
		if err := checkCodes(q, orgID, 0, in.Codes); err != nil {
			return err
		}
		id, err := q.InsertPart(orgID, w)
		if repository.IsUniqueViolation(err) {
			return apierror.WithField(apierror.PartCodeExists, "stockNo")
		}
		if err != nil {
			return err
		}
		if err := q.ReplaceCodes(orgID, id, in.Codes, nil); err != nil {
			return err
		}
		if in.InitialPrice != nil {
			if _, _, err := insertManualPrice(ctx, q, orgID, id, w.CatalogID, price, in.InitialPrice.ValidFrom); err != nil {
				return err
			}
		}
		if out, err = loadPart(q, orgID, id); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "part.created", "part", id, nil, out)
	})
	return out, err
}

func (s *CatalogService) UpdatePart(ctx context.Context, orgID, id uint, version int, in PartWrite) (*models.Part, error) {
	w, err := in.validate()
	if err != nil {
		return nil, err
	}
	var out *models.Part
	err = s.store.InTx(ctx, func(q *repository.Q) error {
		before, err := loadPart(q, orgID, id)
		if err != nil {
			return err
		}
		if before.CatalogID != w.CatalogID {
			return apierror.WithField(apierror.PartCatalogImmutable, "catalogId")
		}
		if err := checkCodes(q, orgID, id, in.Codes); err != nil {
			return err
		}
		ok, err := q.UpdatePart(orgID, id, version, w)
		if repository.IsUniqueViolation(err) {
			return apierror.WithField(apierror.PartCodeExists, "stockNo")
		}
		if err != nil {
			return err
		}
		if !ok {
			return apierror.VersionConflict
		}
		if err := q.ReplaceCodes(orgID, id, in.Codes, nil); err != nil {
			return err
		}
		if out, err = loadPart(q, orgID, id); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "part.updated", "part", id, before, out)
	})
	return out, err
}

func (s *CatalogService) GetPart(ctx context.Context, orgID, id uint) (*models.Part, error) {
	return loadPart(s.store.Read(ctx), orgID, id)
}

func loadPart(q *repository.Q, orgID, id uint) (*models.Part, error) {
	t := today()
	p, err := q.GetPart(orgID, id, t)
	if repository.IsNotFound(err) {
		return nil, apierror.PartNotFound
	}
	if err != nil {
		return nil, err
	}
	if err := withCodes(q, []*models.Part{p}); err != nil {
		return nil, err
	}
	p.NextPrice, err = q.NextPrice(orgID, id, t)
	return p, err
}

func withCodes(q *repository.Q, parts []*models.Part) error {
	ids := make([]uint, len(parts))
	byID := make(map[uint]*models.Part, len(parts))
	for i, p := range parts {
		ids[i] = p.ID
		p.Codes, p.Equivalents = []models.PartCode{}, []models.PartEquivalent{}
		byID[p.ID] = p
	}
	codes, err := q.PartCodes(ids)
	if err != nil {
		return err
	}
	for _, c := range codes {
		byID[c.PartID].Codes = append(byID[c.PartID].Codes, c)
	}
	eq, err := q.PartEquivalents(parts[0].OrgID, ids)
	if err != nil {
		return err
	}
	for _, e := range eq {
		byID[e.OwnerID].Equivalents = append(byID[e.OwnerID].Equivalents, e.PartEquivalent)
	}
	return nil
}

func (s *CatalogService) Lookup(ctx context.Context, orgID uint, code string) ([]models.Part, error) {
	norm := NormalizeCode(code)
	if len(norm) < 2 {
		return nil, apierror.WithField(apierror.InvalidRequest, "code")
	}
	q := s.store.Read(ctx)
	ids, err := q.PartIDsByCode(orgID, norm, false)
	if err != nil || len(ids) == 0 {
		return []models.Part{}, err
	}
	parts, err := q.PartsByIDs(orgID, ids, today())
	if err != nil {
		return nil, err
	}
	ptrs := make([]*models.Part, len(parts))
	for i := range parts {
		ptrs[i] = &parts[i]
	}
	return parts, withCodes(q, ptrs)
}

func (w PriceWrite) parse(prefix string) (models.Dec4, error) {
	p, err := models.ParseQuantity(w.Price)
	if err != nil {
		return 0, apierror.WithField(apierror.InvalidRequest, prefix+"price")
	}
	if w.ValidFrom.IsZero() {
		return 0, apierror.WithField(apierror.InvalidRequest, prefix+"validFrom")
	}
	return p, nil
}

// insertManualPrice records a MANUAL price and emits PriceChanged. A different price on the same date is
// ERR_PRICE_EXISTS; the same price returns the stored row with inserted false.
func insertManualPrice(ctx context.Context, q *repository.Q, orgID, partID, catalogID uint, price models.Dec4, from models.Date) (uint, bool, error) {
	id, ok, err := q.InsertPrice(orgID, repository.PriceInsert{PartID: partID, Price: price, ValidFrom: from, Source: "MANUAL",
		CreatedBy: actorFrom(ctx).userID()})
	if err != nil {
		return 0, false, err
	}
	if !ok {
		existing, err := q.PriceAt(orgID, partID, from)
		if err != nil {
			return 0, false, err
		}
		if existing.Price != price {
			return 0, false, apierror.PriceExists
		}
		return existing.ID, false, nil
	}
	var prev *models.Dec4
	if p, err := q.PriceOn(orgID, partID, models.Date{Time: from.AddDate(0, 0, -1)}); err != nil {
		return 0, false, err
	} else if p != nil {
		prev = &p.Price
	}
	return id, true, emit(ctx, q, orgID, 0, "PriceChanged", "part", partID, nil, map[string]any{
		"priceId": id, "partId": partID, "catalogId": catalogID, "price": price, "previousPrice": prev,
		"validFrom": from, "source": "MANUAL",
	})
}

// SetPartPrice answers 201 with a new row or 200 when the same price is already on that date.
func (s *CatalogService) SetPartPrice(ctx context.Context, orgID, partID uint, in PriceWrite) (int, *models.Price, error) {
	price, err := in.parse("")
	if err != nil {
		return 0, nil, err
	}
	status := http.StatusCreated
	var out *models.Price
	err = s.store.InTx(ctx, func(q *repository.Q) error {
		part, err := q.GetPartRef(orgID, partID)
		if repository.IsNotFound(err) {
			return apierror.PartNotFound
		}
		if err != nil {
			return err
		}
		id, inserted, err := insertManualPrice(ctx, q, orgID, partID, part.CatalogID, price, in.ValidFrom)
		if err != nil {
			return err
		}
		if !inserted {
			status = http.StatusOK
		}
		out, err = priceOut(q, orgID, id)
		return err
	})
	return status, out, err
}

func priceOut(q *repository.Q, orgID, id uint) (*models.Price, error) {
	p, err := q.PriceByID(orgID, id, today())
	if err == nil {
		fillChange(p)
	}
	return p, err
}

func fillChange(p *models.Price) {
	if p.Price != nil && p.PreviousPrice != nil {
		p.ChangePct = models.PercentChange(*p.PreviousPrice, *p.Price)
	}
}

func (s *CatalogService) PriceHistory(ctx context.Context, orgID, partID uint) ([]models.Price, error) {
	q := s.store.Read(ctx)
	if _, err := q.GetPartRef(orgID, partID); repository.IsNotFound(err) {
		return nil, apierror.PartNotFound
	} else if err != nil {
		return nil, err
	}
	rows, err := q.PriceHistory(orgID, partID, today())
	for i := range rows {
		fillChange(&rows[i])
	}
	return rows, err
}

type SetPricesItem struct {
	PartID uint   `json:"partId"`
	Price  string `json:"price"`
}

type SetPrices struct {
	ValidFrom models.Date     `json:"validFrom"`
	Items     []SetPricesItem `json:"items"`
}

// SetPrices is all or nothing; equal prices are no-ops and left out of the result.
func (s *CatalogService) SetPrices(ctx context.Context, orgID, catalogID uint, in SetPrices) ([]models.Price, error) {
	if in.ValidFrom.IsZero() {
		return nil, apierror.WithField(apierror.InvalidRequest, "validFrom")
	}
	if len(in.Items) == 0 || len(in.Items) > 1000 {
		return nil, apierror.WithField(apierror.InvalidRequest, "items")
	}
	prices := make([]models.Dec4, len(in.Items))
	for i, it := range in.Items {
		p, err := models.ParseQuantity(it.Price)
		if err != nil {
			return nil, apierror.WithField(apierror.InvalidRequest, "items["+itoa(i)+"].price")
		}
		prices[i] = p
	}
	out := []models.Price{}
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if _, err := q.LockCatalog(orgID, catalogID); repository.IsNotFound(err) {
			return apierror.CatalogNotFound
		} else if err != nil {
			return err
		}
		for i, it := range in.Items {
			ref, err := q.GetPartRef(orgID, it.PartID)
			if repository.IsNotFound(err) || err == nil && ref.CatalogID != catalogID {
				return apierror.UnknownRef(apierror.PartNotInCatalog, "items["+itoa(i)+"].partId")
			}
			if err != nil {
				return err
			}
			id, inserted, err := insertManualPrice(ctx, q, orgID, it.PartID, catalogID, prices[i], in.ValidFrom)
			if err != nil {
				return err
			}
			if inserted {
				p, err := priceOut(q, orgID, id)
				if err != nil {
					return err
				}
				out = append(out, *p)
			}
		}
		return nil
	})
	return out, err
}

type PricePage struct {
	Items      []models.Price `json:"items"`
	NextCursor *string        `json:"nextCursor"`
}

func (s *CatalogService) ListPrices(ctx context.Context, orgID, catalogID uint, validOn *models.Date, limit int, cursor string) (*PricePage, error) {
	q := s.store.Read(ctx)
	if _, err := q.GetCatalog(orgID, catalogID); repository.IsNotFound(err) {
		return nil, apierror.CatalogNotFound
	} else if err != nil {
		return nil, err
	}
	on := today()
	if validOn != nil {
		on = *validOn
	}
	after, err := decodeCursor(cursor)
	if err != nil {
		return nil, err
	}
	limit = clampLimit(limit)
	rows, err := q.CatalogPrices(orgID, catalogID, on, after, limit+1)
	if err != nil {
		return nil, err
	}
	t := today()
	page := &PricePage{Items: rows}
	for i := range rows {
		r := &rows[i]
		r.IsCurrent = r.ValidFrom != nil && !r.ValidFrom.After(t.Time) && (r.ValidTo == nil || !r.ValidTo.Before(t.Time))
		fillChange(r)
	}
	if len(rows) > limit {
		page.Items = rows[:limit]
		c := encodeCursor(rows[limit-1].StockNo)
		page.NextCursor = &c
	}
	return page, nil
}

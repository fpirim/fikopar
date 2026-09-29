package services

import (
	"context"
	"encoding/json"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/pricefile"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type ImportApply struct {
	SkipErrorRows   bool   `json:"skipErrorRows"`
	ApplyAttributes *bool  `json:"applyAttributes"`
	Missing         string `json:"missing"`
}

type pendingLink struct {
	partID uint
	eq     pricefile.Equivalent
}

type applier struct {
	q        *repository.Q
	orgID    uint
	job      *PriceImport
	opt      ImportApply
	res      ImportResult
	prices   []repository.ImportPrice
	links    []pendingLink
	codes    []repository.NewCode
	created  []repository.NewPart
	attrsOn  bool
	deactive bool

	createdFields []ImportFields
}

// Apply writes the previewed diff once, in the caller's transaction: READY → APPLYING → APPLIED.
func (s *PriceImportService) Apply(ctx context.Context, orgID, id uint, version int, in ImportApply) (*PriceImport, error) {
	if in.Missing == "" {
		in.Missing = "KEEP"
	}
	if in.Missing != "KEEP" && in.Missing != "DEACTIVATE" {
		return nil, apierror.WithField(apierror.InvalidRequest, "missing")
	}
	var out *PriceImport
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		j, err := lockJob(q, orgID, id, version, "READY")
		if err != nil {
			return err
		}
		cv, err := q.LockCatalog(orgID, j.CatalogID)
		if err != nil {
			return err
		}
		if j.CatalogVersion == nil || *j.CatalogVersion != cv {
			return apierror.ImportStale
		}
		if j.Counts.Errors > 0 && !in.SkipErrorRows {
			return apierror.ImportHasErrors
		}
		if err := q.SetImportApplying(id); err != nil {
			return err
		}
		a := &applier{q: q, orgID: orgID, job: j, opt: in, attrsOn: in.ApplyAttributes == nil || *in.ApplyAttributes,
			deactive: in.Missing == "DEACTIVATE"}
		if err := a.run(); err != nil {
			return err
		}
		n, err := q.InsertImportPrices(orgID, id, actorFrom(ctx).userID(), a.prices)
		if repository.IsUniqueViolation(err) {
			return apierror.ImportStale
		}
		if err != nil {
			return err
		}
		a.res.PricesInserted = int(n)
		rj, _ := json.Marshal(a.res)
		if err := q.MarkImportApplied(id, in.Missing, string(rj), actorFrom(ctx).userID()); err != nil {
			return err
		}
		if out, err = getJob(q, orgID, id); err != nil {
			return err
		}
		if err := emit(ctx, q, orgID, 0, "PriceListImported", "price_import", id, &out.Version, map[string]any{
			"importId": id, "catalogId": j.CatalogID, "fileName": j.FileName, "fileSha256": j.FileSha256,
			"validFrom": j.ValidFrom, "missing": in.Missing, "partsCreated": a.res.PartsCreated,
			"pricesInserted": a.res.PricesInserted, "partsUpdated": a.res.PartsUpdated,
			"partsDeactivated": a.res.PartsDeactivated, "rowsSkipped": a.res.RowsSkipped,
		}); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "price_import.applied", "price_import", id, j, out)
	})
	return out, err
}

func has(list []string, v string) bool {
	for _, x := range list {
		if x == v {
			return true
		}
	}
	return false
}

func (a *applier) run() error {
	rows, err := a.q.ImportApplyRows(a.job.ID)
	if err != nil {
		return err
	}
	for _, r := range rows {
		var changes []string
		decodeJSON(r.Changes, &changes)
		if r.RowNo == nil {
			if a.deactive && r.PartID != nil {
				if err := a.deactivate(*r.PartID); err != nil {
					return err
				}
			}
			continue
		}
		if r.HasErrors {
			a.res.RowsSkipped++
			continue
		}
		var f ImportFields
		decodeJSON(r.Parsed, &f)
		if err := a.row(r, f, changes); err != nil {
			return err
		}
	}
	return a.link()
}

func (a *applier) deactivate(partID uint) error {
	ok, err := a.q.DeactivatePart(a.orgID, partID)
	if ok {
		a.res.PartsDeactivated++
	}
	return err
}

func (a *applier) row(r repository.ApplyRow, f ImportFields, changes []string) error {
	switch {
	case has(changes, "NEW"):
		return a.create(f)
	case has(changes, "DELETE"):
		return a.deactivate(*r.PartID)
	case r.PartID == nil:
		return nil
	}
	id := *r.PartID
	if has(changes, "PRICE") && f.Price != nil {
		a.prices = append(a.prices, repository.ImportPrice{PartID: id, Price: *f.Price, ValidFrom: *f.ValidFrom})
	}
	updated := false
	if a.attrsOn && has(changes, "ATTRIBUTES") {
		var changed []string
		decodeJSON(r.Changed, &changed)
		if err := a.q.UpdatePartAttrs(a.orgID, id, repository.PartAttrs{Name: f.Description, Type: f.Type, Brand: f.Brand,
			PackageQty: f.PackQty, VatPct: f.VatPct, Activate: r.Inactive}); err != nil {
			return err
		}
		if err := a.replaceCodes(id, f, changed); err != nil {
			return err
		}
		updated = true
	} else if r.Inactive {
		if err := a.q.UpdatePartAttrs(a.orgID, id, repository.PartAttrs{Activate: true}); err != nil {
			return err
		}
		updated = true
	}
	if a.attrsOn && f.Equivalent != nil {
		a.links = append(a.links, pendingLink{id, *f.Equivalent})
	}
	if updated {
		a.res.PartsUpdated++
	}
	return nil
}

func (a *applier) replaceCodes(id uint, f ImportFields, changed []string) error {
	sets := []struct {
		field, kind string
		codes       []string
	}{{"manufacturerCode", models.CodeManufacturer, nil}, {"oem", models.CodeOEM, f.OEM}, {"altCodes", models.CodeALT, f.AltCodes}}
	if f.ManufacturerCode != nil {
		sets[0].codes = []string{*f.ManufacturerCode}
	}
	for _, s := range sets {
		if !has(changed, s.field) {
			continue
		}
		codes := make([]models.PartCode, len(s.codes))
		for i, c := range s.codes {
			codes[i] = models.PartCode{Kind: s.kind, Code: c}
		}
		if err := a.q.ReplaceCodes(a.orgID, id, codes, []string{s.kind}); err != nil {
			return err
		}
	}
	return nil
}

func (a *applier) create(f ImportFields) error {
	name := f.StockNo
	if f.Description != nil {
		name = *f.Description
	}
	np := repository.NewPart{StockNo: f.StockNo, Name: name, Type: f.Type, Brand: f.Brand, PackageQty: 1, VatPct: "20"}
	if f.PackQty != nil {
		np.PackageQty = *f.PackQty
	}
	if f.VatPct != nil {
		np.VatPct = *f.VatPct
	}
	a.created = append(a.created, np)
	a.createdFields = append(a.createdFields, f)
	return nil
}

// flushCreated inserts the new parts in bulk with their codes and prices.
func (a *applier) flushCreated() error {
	if len(a.created) == 0 {
		return nil
	}
	ids, err := a.q.InsertImportParts(a.orgID, a.job.CatalogID, a.created)
	if repository.IsUniqueViolation(err) {
		return apierror.ImportStale
	}
	if err != nil {
		return err
	}
	a.res.PartsCreated += len(ids)
	for _, f := range a.createdFields {
		id := ids[f.StockNo]
		if f.ManufacturerCode != nil {
			a.codes = append(a.codes, repository.NewCode{PartID: id, Kind: models.CodeManufacturer, Code: *f.ManufacturerCode})
		}
		for _, c := range f.OEM {
			a.codes = append(a.codes, repository.NewCode{PartID: id, Kind: models.CodeOEM, Code: c})
		}
		for _, c := range f.AltCodes {
			a.codes = append(a.codes, repository.NewCode{PartID: id, Kind: models.CodeALT, Code: c})
		}
		if f.Price != nil {
			a.prices = append(a.prices, repository.ImportPrice{PartID: id, Price: *f.Price, ValidFrom: *f.ValidFrom})
		}
		if f.Equivalent != nil {
			a.links = append(a.links, pendingLink{id, *f.Equivalent})
		}
	}
	return nil
}

// link adds EQUIVALENT codes once every new part exists; unknown targets were warnings in the preview.
func (a *applier) link() error {
	if err := a.flushCreated(); err != nil {
		return err
	}
	for _, l := range a.links {
		ref, err := a.q.PartByCatalogCode(a.orgID, l.eq.CatalogCode, l.eq.StockNo)
		if repository.IsNotFound(err) {
			continue
		}
		if err != nil {
			return err
		}
		if ref.ID == l.partID {
			continue
		}
		eq := ref.ID
		a.codes = append(a.codes, repository.NewCode{PartID: l.partID, Kind: models.CodeEquivalent, Code: l.eq.StockNo, EquivalentPartID: &eq})
	}
	return a.q.InsertImportCodes(a.orgID, a.codes)
}

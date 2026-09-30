package models

import "time"

type Catalog struct {
	ID              uint       `json:"id" gorm:"column:id"`
	Code            int        `json:"code" gorm:"column:code"`
	Name            string     `json:"name" gorm:"column:name"`
	SupplierSicilID *uint      `json:"supplierSicilId" gorm:"column:supplier_sicil_id"`
	SupplierName    *string    `json:"supplierName" gorm:"column:supplier_name"`
	IsActive        bool       `json:"isActive" gorm:"column:is_active"`
	Currency        string     `json:"currency" gorm:"column:currency"`
	PartCount       int        `json:"partCount" gorm:"column:part_count"`
	ContentVersion  int64      `json:"contentVersion" gorm:"column:content_version"`
	LastImportAt    *time.Time `json:"lastImportAt" gorm:"column:last_import_at"`
	Version         int        `json:"version" gorm:"column:version"`
}

const (
	CodeManufacturer = "MANUFACTURER"
	CodeOEM          = "OEM"
	CodeALT          = "ALT"
	CodeEquivalent   = "EQUIVALENT"
)

type PartCode struct {
	PartID           uint   `json:"-" gorm:"column:part_id"`
	Kind             string `json:"kind" gorm:"column:kind"`
	Code             string `json:"code" gorm:"column:code"`
	NormalizedCode   string `json:"normalizedCode" gorm:"column:normalized_code"`
	EquivalentPartID *uint  `json:"equivalentPartId" gorm:"column:equivalent_part_id"`
}

// PartEquivalent is a part linked by an EQUIVALENT code, either direction.
type PartEquivalent struct {
	PartID      uint   `json:"partId" gorm:"column:part_id"`
	StockNo     string `json:"stockNo" gorm:"column:stock_no"`
	Name        string `json:"name" gorm:"column:name"`
	CatalogID   uint   `json:"catalogId" gorm:"column:catalog_id"`
	CatalogName string `json:"catalogName" gorm:"column:catalog_name"`
}

type PriceRef struct {
	Price     Dec4 `json:"price" gorm:"column:price"`
	ValidFrom Date `json:"validFrom" gorm:"column:valid_from"`
}

type Part struct {
	ID                 uint             `json:"id" gorm:"column:id"`
	OrgID              uint             `json:"orgId" gorm:"column:org_id"`
	CatalogID          uint             `json:"catalogId" gorm:"column:catalog_id"`
	CatalogCode        int              `json:"catalogCode" gorm:"column:catalog_code"`
	CatalogName        string           `json:"catalogName" gorm:"column:catalog_name"`
	StockNo            string           `json:"stockNo" gorm:"column:stock_no"`
	Name               string           `json:"name" gorm:"column:name"`
	Type               *string          `json:"type" gorm:"column:type"`
	Brand              *string          `json:"brand" gorm:"column:brand"`
	Unit               string           `json:"unit" gorm:"column:unit"`
	PackageQty         int              `json:"packageQty" gorm:"column:package_qty"`
	VatPct             string           `json:"vatPct" gorm:"column:vat_pct"`
	IsActive           bool             `json:"isActive" gorm:"column:is_active"`
	Codes              []PartCode       `json:"codes" gorm:"-"`
	ListPrice          *Dec4            `json:"listPrice" gorm:"column:list_price"`
	ListPriceValidFrom *Date            `json:"listPriceValidFrom" gorm:"column:list_price_valid_from"`
	NextPrice          *PriceRef        `json:"nextPrice" gorm:"-"`
	Equivalents        []PartEquivalent `json:"equivalents" gorm:"-"`
	OnHand             Dec4             `json:"onHand" gorm:"column:on_hand"`
	LastPurchaseCost   *LastPurchase    `json:"lastPurchaseCost" gorm:"-"`
	CreatedAt          time.Time        `json:"createdAt" gorm:"column:created_at"`
	UpdatedAt          time.Time        `json:"updatedAt" gorm:"column:updated_at"`
	Version            int              `json:"version" gorm:"column:version"`
}

// LastPurchase is the unit net of the part's line on its latest posted purchase invoice (contracts v1.5).
type LastPurchase struct {
	PartID     uint   `json:"-" gorm:"column:part_id"`
	Amount     Dec4   `json:"amount" gorm:"column:amount"`
	Date       Date   `json:"date" gorm:"column:date"`
	DocumentNo string `json:"documentNo" gorm:"column:document_no"`
}

type Price struct {
	ID            uint       `json:"id" gorm:"column:id"`
	PartID        uint       `json:"partId" gorm:"column:part_id"`
	CatalogID     uint       `json:"catalogId" gorm:"column:catalog_id"`
	StockNo       string     `json:"stockNo" gorm:"column:stock_no"`
	Name          string     `json:"name" gorm:"column:name"`
	Price         *Dec4      `json:"price" gorm:"column:price"`
	ValidFrom     *Date      `json:"validFrom" gorm:"column:valid_from"`
	ValidTo       *Date      `json:"validTo" gorm:"column:valid_to"`
	PreviousPrice *Dec4      `json:"previousPrice" gorm:"column:previous_price"`
	ChangePct     *string    `json:"changePct" gorm:"-"`
	IsCurrent     bool       `json:"isCurrent" gorm:"column:is_current"`
	Source        *string    `json:"source" gorm:"column:source"`
	ImportID      *uint      `json:"importId" gorm:"column:import_id"`
	CreatedBy     *string    `json:"createdBy" gorm:"column:created_by"`
	CreatedAt     *time.Time `json:"createdAt" gorm:"column:created_at"`
}

// PartRow is one row of the parts grid.
type PartRow struct {
	ID                 uint          `json:"id" gorm:"column:id"`
	CatalogID          uint          `json:"catalogId" gorm:"column:catalog_id"`
	CatalogCode        int           `json:"catalogCode" gorm:"column:catalog_code"`
	StockNo            string        `json:"stockNo" gorm:"column:stock_no"`
	Name               string        `json:"name" gorm:"column:name"`
	Type               *string       `json:"type" gorm:"column:type"`
	Brand              *string       `json:"brand" gorm:"column:brand"`
	Codes              []PartCode    `json:"codes" gorm:"-"`
	Unit               string        `json:"unit" gorm:"column:unit"`
	VatPct             string        `json:"vatPct" gorm:"column:vat_pct"`
	PackageQty         int           `json:"packageQty" gorm:"column:package_qty"`
	ListPrice          *Dec4         `json:"listPrice" gorm:"column:list_price"`
	ListPriceValidFrom *Date         `json:"listPriceValidFrom" gorm:"column:list_price_valid_from"`
	OnHand             Dec4          `json:"onHand" gorm:"column:on_hand"`
	Reserved           Dec4          `json:"reserved" gorm:"column:reserved"`
	Available          Dec4          `json:"available" gorm:"column:available"`
	MinQty             *Dec4         `json:"minQty" gorm:"column:min_qty"`
	MaxQty             *Dec4         `json:"maxQty" gorm:"column:max_qty"`
	Shelf              *string       `json:"shelf" gorm:"column:shelf"`
	LastPurchaseCost   *LastPurchase `json:"lastPurchaseCost" gorm:"-"`
	BelowMin           bool          `json:"belowMin" gorm:"column:below_min"`
	Negative           bool          `json:"negative" gorm:"column:negative"`
	StockByDepot       []DepotStock  `json:"stockByDepot,omitempty" gorm:"-"`
	IsActive           bool          `json:"isActive" gorm:"column:is_active"`
	UpdatedAt          time.Time     `json:"updatedAt" gorm:"column:updated_at"`
	Version            int           `json:"version" gorm:"column:version"`
}

// DepotStock is the stock of a parts grid row in one depot (all-depots scope).
type DepotStock struct {
	DepotID   uint   `json:"depotId"`
	DepotCode string `json:"depotCode"`
	OnHand    Dec4   `json:"onHand"`
	Available Dec4   `json:"available"`
}

type HitStock struct {
	DepotID   uint `json:"depotId"`
	OnHand    Dec4 `json:"onHand"`
	Available Dec4 `json:"available"`
}

type Equivalent struct {
	PartID      uint   `json:"partId" gorm:"column:part_id"`
	StockNo     string `json:"stockNo" gorm:"column:stock_no"`
	CatalogCode int    `json:"catalogCode" gorm:"column:catalog_code"`
	OnHandTotal Dec4   `json:"onHandTotal" gorm:"column:on_hand_total"`
}

type CatalogHit struct {
	PartID             uint         `json:"partId" gorm:"column:part_id"`
	CatalogID          uint         `json:"catalogId" gorm:"column:catalog_id"`
	CatalogCode        int          `json:"catalogCode" gorm:"column:catalog_code"`
	StockNo            string       `json:"stockNo" gorm:"column:stock_no"`
	Name               string       `json:"name" gorm:"column:name"`
	Brand              *string      `json:"brand" gorm:"column:brand"`
	Type               *string      `json:"type" gorm:"column:type"`
	Codes              []PartCode   `json:"codes" gorm:"-"`
	MatchedCode        *string      `json:"matchedCode" gorm:"column:matched_code"`
	MatchKind          string       `json:"matchKind" gorm:"column:match_kind"`
	Equivalents        []Equivalent `json:"equivalents" gorm:"-"`
	Stock              []HitStock   `json:"stock" gorm:"-"`
	OnHandTotal        Dec4         `json:"onHandTotal" gorm:"-"`
	ListPrice          *Dec4        `json:"listPrice" gorm:"column:list_price"`
	ListPriceValidFrom *Date        `json:"listPriceValidFrom" gorm:"column:list_price_valid_from"`
	VatPct             string       `json:"vatPct" gorm:"column:vat_pct"`
	Unit               string       `json:"unit" gorm:"column:unit"`
	PackageQty         int          `json:"packageQty" gorm:"column:package_qty"`
	Rank               int          `json:"-" gorm:"column:rank"`
	Total              int          `json:"-" gorm:"column:total"`
}

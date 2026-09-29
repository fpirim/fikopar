package models

import "time"

// Document is a trade document with its lines and derived read data (contracts v1.4).
type Document struct {
	ID                   uint                 `json:"id"`
	OrgID                uint                 `json:"orgId"`
	DocumentNo           *string              `json:"documentNo"`
	Type                 string               `json:"type"`
	Status               string               `json:"status"`
	DepotID              uint                 `json:"depotId"`
	SicilID              uint                 `json:"sicilId"`
	SicilName            string               `json:"sicilName"`
	DocumentDate         Date                 `json:"documentDate"`
	ShipDate             *Date                `json:"shipDate"`
	DueDate              *Date                `json:"dueDate"`
	SalesRepUserID       *uint                `json:"salesRepUserId"`
	SalesRepName         *string              `json:"salesRepName"`
	LinkedDocumentID     *uint                `json:"linkedDocumentId"`
	LinkedDocumentNo     *string              `json:"linkedDocumentNo"`
	PriceDirection       *string              `json:"priceDirection"`
	WaybillDocumentID    *uint                `json:"waybillDocumentId"`
	WaybillDocumentNo    *string              `json:"waybillDocumentNo"`
	BilledByDocumentID   *uint                `json:"billedByDocumentId"`
	DependentDocumentIDs []uint               `json:"dependentDocumentIds"`
	Currency             string               `json:"currency"`
	GrossTotal           Money                `json:"grossTotal"`
	DiscountTotal        Money                `json:"discountTotal"`
	NetTotal             Money                `json:"netTotal"`
	VatTotal             Money                `json:"vatTotal"`
	GrandTotal           Money                `json:"grandTotal"`
	VatBreakdown         []VatBreakdown       `json:"vatBreakdown"`
	Description          string               `json:"description"`
	Lines                []DocumentLine       `json:"lines"`
	Reservation          *Reservation         `json:"reservation"`
	Effects              DocumentEffects      `json:"effects"`
	Warnings             []DocumentWarning    `json:"warnings"`
	CreditOverride       *CreditOverride      `json:"creditOverride"`
	JournalEntryID       *uint                `json:"journalEntryId"`
	ReversalEntryID      *uint                `json:"reversalEntryId"`
	PostedAt             *time.Time           `json:"postedAt"`
	CancelledAt          *time.Time           `json:"cancelledAt"`
	CancelDate           *Date                `json:"cancelDate"`
	CancelReason         *string              `json:"cancelReason"`
	Version              int                  `json:"version"`
	Snapshot             *DocumentCardSummary `json:"-"`
}

type DocumentLine struct {
	ID             uint             `json:"id"`
	LineNo         int              `json:"lineNo"`
	PartID         *uint            `json:"partId"`
	StockNo        string           `json:"stockNo,omitempty"`
	Name           string           `json:"name,omitempty"`
	CatalogCode    *int             `json:"catalogCode,omitempty"`
	Description    *string          `json:"description"`
	Qty            Dec4             `json:"qty"`
	UnitPrice      Dec4             `json:"unitPrice"`
	Discount1Pct   Dec4             `json:"discount1Pct"`
	Discount2Pct   Dec4             `json:"discount2Pct"`
	VatPct         Dec4             `json:"vatPct"`
	GrossAmount    Money            `json:"grossAmount"`
	DiscountAmount Money            `json:"discountAmount"`
	NetAmount      Money            `json:"netAmount"`
	VatAmount      Money            `json:"vatAmount"`
	LineTotal      Money            `json:"lineTotal"`
	OriginLineID   *uint            `json:"originLineId"`
	BilledLineID   *uint            `json:"billedLineId"`
	OnHand         *Dec4            `json:"onHand,omitempty"`
	Available      *Dec4            `json:"available,omitempty"`
	StockByDepot   []LineDepotStock `json:"stockByDepot,omitempty"`
	ReservedQty    *Dec4            `json:"reservedQty,omitempty"`
	ReturnedQty    *Dec4            `json:"returnedQty,omitempty"`
}

// LineDepotStock is DepotStock of the contract with the reserved quantity.
type LineDepotStock struct {
	DepotID   uint   `json:"depotId" gorm:"column:depot_id"`
	DepotCode string `json:"depotCode" gorm:"column:depot_code"`
	OnHand    Dec4   `json:"onHand" gorm:"column:on_hand"`
	Reserved  Dec4   `json:"reserved" gorm:"column:reserved"`
	Available Dec4   `json:"available" gorm:"column:available"`
}

type VatBreakdown struct {
	VatPct Dec4  `json:"vatPct"`
	Base   Money `json:"base"`
	Vat    Money `json:"vat"`
}

type Reservation struct {
	ExpiresAt time.Time `json:"expiresAt"`
}

type LedgerEffect struct {
	Role        *string `json:"role"`
	AccountCode string  `json:"accountCode"`
	AccountName string  `json:"accountName,omitempty"`
	Side        Side    `json:"side"`
	Amount      Money   `json:"amount"`
}

type StockEffect struct {
	DepotID  uint `json:"depotId"`
	QtyDelta Dec4 `json:"qtyDelta"`
}

type DocumentEffects struct {
	Ledger             []LedgerEffect `json:"ledger"`
	Stock              []StockEffect  `json:"stock"`
	CreditUsedPctAfter *int           `json:"creditUsedPctAfter"`
}

const (
	WarningStockShort     = "STOCK_SHORT"
	WarningCreditLimit    = "CREDIT_LIMIT"
	WarningPriceBelowList = "PRICE_BELOW_LIST"
)

// DocumentWarning is a non-blocking notice on a draft (STOCK_SHORT, CREDIT_LIMIT, PRICE_BELOW_LIST) or on a post
// or cancel response (NEGATIVE_STOCK).
type DocumentWarning struct {
	Code        string `json:"code"`
	LineNo      *int   `json:"lineNo,omitempty"`
	PartID      *uint  `json:"partId,omitempty"`
	DepotID     *uint  `json:"depotId,omitempty"`
	Requested   *Dec4  `json:"requested,omitempty"`
	Available   *Dec4  `json:"available,omitempty"`
	OnHandAfter *Dec4  `json:"onHandAfter,omitempty"`
}

type CreditOverride struct {
	Reason   string    `json:"reason"`
	UserID   uint      `json:"userId"`
	UserName string    `json:"userName,omitempty"`
	At       time.Time `json:"at"`
	Excess   Money     `json:"excess"`
}

// DocumentCardSummary is the card as printed on the document (sicil_snapshot).
type DocumentCardSummary struct {
	HesapNo  string `json:"hesapNo"`
	HesapAdi string `json:"hesapAdi"`
	Adres    string `json:"adres,omitempty"`
	Ilce     string `json:"ilce,omitempty"`
	Sehir    string `json:"sehir,omitempty"`
	VDairesi string `json:"vDairesi,omitempty"`
	VNo      string `json:"vNo,omitempty"`
	TCNo     string `json:"tcNo,omitempty"`
}

// DocumentSummary is a gridDocuments row.
type DocumentSummary struct {
	ID                uint       `json:"id" gorm:"column:id"`
	DocumentNo        *string    `json:"documentNo" gorm:"column:document_no"`
	Type              string     `json:"type" gorm:"column:type"`
	Status            string     `json:"status" gorm:"column:status"`
	DepotID           uint       `json:"depotId" gorm:"column:depot_id"`
	DepotCode         string     `json:"depotCode" gorm:"column:depot_code"`
	SicilID           uint       `json:"sicilId" gorm:"column:sicil_id"`
	HesapNo           string     `json:"hesapNo" gorm:"column:hesap_no"`
	SicilName         string     `json:"sicilName" gorm:"column:sicil_name"`
	DocumentDate      Date       `json:"documentDate" gorm:"column:document_date"`
	DueDate           *Date      `json:"dueDate" gorm:"column:due_date"`
	SalesRepName      *string    `json:"salesRepName" gorm:"column:sales_rep_name"`
	GrossTotal        Money      `json:"grossTotal" gorm:"column:gross_total"`
	DiscountTotal     Money      `json:"discountTotal" gorm:"column:discount_total"`
	NetTotal          Money      `json:"netTotal" gorm:"column:net_total"`
	VatTotal          Money      `json:"vatTotal" gorm:"column:vat_total"`
	GrandTotal        Money      `json:"grandTotal" gorm:"column:grand_total"`
	LinkedDocumentNo  *string    `json:"linkedDocumentNo" gorm:"column:linked_document_no"`
	PriceDirection    *string    `json:"priceDirection" gorm:"column:price_direction"`
	WaybillDocumentNo *string    `json:"waybillDocumentNo" gorm:"column:waybill_document_no"`
	CreditOverride    bool       `json:"creditOverride" gorm:"column:credit_override"`
	PostedAt          *time.Time `json:"postedAt" gorm:"column:posted_at"`
	Version           int        `json:"version" gorm:"column:version"`
}

type DocumentTotals struct {
	GrossTotal    Money `gorm:"column:gross_total"`
	DiscountTotal Money `gorm:"column:discount_total"`
	NetTotal      Money `gorm:"column:net_total"`
	VatTotal      Money `gorm:"column:vat_total"`
	GrandTotal    Money `gorm:"column:grand_total"`
}

type Member struct {
	UserID uint   `json:"userId" gorm:"column:user_id"`
	Name   string `json:"name" gorm:"column:name"`
	Email  string `json:"email" gorm:"column:email"`
}

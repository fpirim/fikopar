package models

import "time"

// StockWarning is the non-blocking NEGATIVE_STOCK notice of a stock-moving operation.
type StockWarning struct {
	Code        string `json:"code"`
	PartID      uint   `json:"partId"`
	StockNo     string `json:"stockNo,omitempty"`
	DepotID     uint   `json:"depotId"`
	OnHandAfter Dec4   `json:"onHandAfter"`
}

const WarningNegativeStock = "NEGATIVE_STOCK"

type StockLevel struct {
	PartID      uint   `json:"partId" gorm:"column:part_id"`
	StockNo     string `json:"stockNo,omitempty" gorm:"column:stock_no"`
	Name        string `json:"name,omitempty" gorm:"column:name"`
	CatalogCode int    `json:"catalogCode,omitempty" gorm:"column:catalog_code"`
	DepotID     *uint  `json:"depotId" gorm:"column:depot_id"`
	OnHand      Dec4   `json:"onHand" gorm:"column:on_hand"`
	Reserved    Dec4   `json:"reserved" gorm:"column:reserved"`
	Available   Dec4   `json:"available" gorm:"column:available"`
	MinQty      Dec4   `json:"minQty" gorm:"column:min_qty"`
	MaxQty      *Dec4  `json:"maxQty" gorm:"column:max_qty"`
	Shelf       string `json:"shelf" gorm:"column:shelf"`
	BelowMin    bool   `json:"belowMin" gorm:"column:below_min"`
	Negative    bool   `json:"negative" gorm:"column:negative"`
	Version     int    `json:"version" gorm:"column:version"`
}

const (
	MoveIn          = "IN"
	MoveOut         = "OUT"
	MoveAdjustIn    = "ADJUST_IN"
	MoveAdjustOut   = "ADJUST_OUT"
	MoveTransferIn  = "TRANSFER_IN"
	MoveTransferOut = "TRANSFER_OUT"
)

type StockMovement struct {
	ID            uint      `json:"id" gorm:"column:id"`
	PartID        uint      `json:"partId" gorm:"column:part_id"`
	StockNo       string    `json:"stockNo" gorm:"column:stock_no"`
	Name          string    `json:"name" gorm:"column:name"`
	DepotID       uint      `json:"depotId" gorm:"column:depot_id"`
	Kind          string    `json:"kind" gorm:"column:kind"`
	Label         string    `json:"label" gorm:"column:label"`
	Qty           Dec4      `json:"qty" gorm:"column:qty"`
	Delta         Dec4      `json:"delta" gorm:"column:delta"`
	QtyIn         *Dec4     `json:"qtyIn" gorm:"column:qty_in"`
	QtyOut        *Dec4     `json:"qtyOut" gorm:"column:qty_out"`
	UnitCost      *Dec4     `json:"unitCost" gorm:"column:unit_cost"`
	SourceType    string    `json:"sourceType" gorm:"column:source_type"`
	SourceID      *string   `json:"sourceId" gorm:"column:source_id"`
	SourceNo      *string   `json:"sourceNo" gorm:"column:source_no"`
	DocumentID    *uint     `json:"documentId" gorm:"column:document_id"`
	DocumentType  *string   `json:"documentType" gorm:"column:document_type"`
	Reversal      bool      `json:"reversal" gorm:"column:reversal"`
	TransferID    *uint     `json:"transferId" gorm:"column:transfer_id"`
	CountID       *uint     `json:"countId" gorm:"column:count_id"`
	SicilName     *string   `json:"sicilName" gorm:"column:sicil_name"`
	OtherDepotID  *uint     `json:"otherDepotId" gorm:"column:other_depot_id"`
	RunningOnHand *Dec4     `json:"runningOnHand" gorm:"column:running_on_hand"`
	Date          Date      `json:"date" gorm:"column:movement_date"`
	OccurredAt    time.Time `json:"occurredAt" gorm:"column:occurred_at"`
}

type TransferLine struct {
	PartID uint `json:"partId"`
	Qty    Dec4 `json:"qty"`
}

type StockTransfer struct {
	ID          uint            `json:"id" gorm:"column:id"`
	TransferNo  string          `json:"transferNo" gorm:"column:transfer_no"`
	FromDepotID uint            `json:"fromDepotId" gorm:"column:from_depot_id"`
	ToDepotID   uint            `json:"toDepotId" gorm:"column:to_depot_id"`
	Date        Date            `json:"date" gorm:"column:transfer_date"`
	Note        *string         `json:"note" gorm:"column:note"`
	Lines       []TransferLine  `json:"lines" gorm:"-"`
	CreatedBy   *string         `json:"createdBy" gorm:"column:created_by"`
	CreatedAt   time.Time       `json:"createdAt" gorm:"column:created_at"`
	Movements   []StockMovement `json:"movements" gorm:"-"`
	Warnings    []StockWarning  `json:"warnings" gorm:"-"`
}

type StockCount struct {
	ID              uint       `json:"id" gorm:"column:id"`
	DepotID         uint       `json:"depotId" gorm:"column:depot_id"`
	CountDate       Date       `json:"countDate" gorm:"column:count_date"`
	ShelfFrom       *string    `json:"shelfFrom" gorm:"column:shelf_from"`
	ShelfTo         *string    `json:"shelfTo" gorm:"column:shelf_to"`
	CountNo         string     `json:"countNo" gorm:"column:count_no"`
	Year            int        `json:"year" gorm:"column:year"`
	Status          string     `json:"status" gorm:"column:status"`
	StartedAt       time.Time  `json:"startedAt" gorm:"column:started_at"`
	LineCount       int        `json:"lineCount" gorm:"column:line_count"`
	CountedCount    int        `json:"countedCount" gorm:"column:counted_count"`
	DifferenceCount int        `json:"differenceCount" gorm:"column:difference_count"`
	DifferenceValue Money      `json:"differenceValue" gorm:"column:difference_value"`
	ValueIn         Money      `json:"valueIn" gorm:"column:value_in"`
	ValueOut        Money      `json:"valueOut" gorm:"column:value_out"`
	UnvaluedLines   int        `json:"unvaluedLines" gorm:"column:unvalued_lines"`
	Uncounted       *string    `json:"uncounted" gorm:"column:uncounted"`
	CreatedBy       *string    `json:"createdBy" gorm:"column:created_by"`
	FinalizedAt     *time.Time `json:"finalizedAt" gorm:"column:finalized_at"`
	CancelledAt     *time.Time `json:"cancelledAt" gorm:"column:cancelled_at"`
	CancelReason    *string    `json:"cancelReason" gorm:"column:cancel_reason"`
	Version         int        `json:"version" gorm:"column:version"`
}

type StockCountLine struct {
	ID              uint       `json:"id" gorm:"column:id"`
	PartID          uint       `json:"partId" gorm:"column:part_id"`
	StockNo         string     `json:"stockNo" gorm:"column:stock_no"`
	Name            string     `json:"name" gorm:"column:name"`
	Brand           *string    `json:"brand" gorm:"column:brand"`
	Unit            string     `json:"unit" gorm:"column:unit"`
	Shelf           string     `json:"shelf" gorm:"column:shelf"`
	SystemQty       Dec4       `json:"systemQty" gorm:"column:system_qty"`
	CountedQty      *Dec4      `json:"countedQty" gorm:"column:counted_qty"`
	Difference      *Dec4      `json:"difference" gorm:"column:difference"`
	MovedSinceOpen  Dec4       `json:"movedSinceOpen" gorm:"column:moved_since_open"`
	OnHandAfterPost *Dec4      `json:"onHandAfterPost" gorm:"column:on_hand_after_post"`
	Added           bool       `json:"added" gorm:"column:added"`
	UnitCost        *Dec4      `json:"unitCost" gorm:"column:unit_cost"`
	CostSource      string     `json:"costSource" gorm:"column:cost_source"`
	Value           *Money     `json:"value" gorm:"column:value"`
	CountedBy       *string    `json:"countedBy" gorm:"column:counted_by"`
	CountedAt       *time.Time `json:"countedAt" gorm:"column:counted_at"`
	Version         int        `json:"version" gorm:"column:version"`
}

type StockCountFinalized struct {
	Count         *StockCount    `json:"count"`
	MovementCount int            `json:"movementCount"`
	Warnings      []StockWarning `json:"warnings"`
}

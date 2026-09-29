package models

import "time"

type Depot struct {
	ID                 uint   `json:"id" gorm:"column:id"`
	OrgID              uint   `json:"-" gorm:"column:org_id"`
	Code               string `json:"code" gorm:"column:code"`
	Name               string `json:"name" gorm:"column:name"`
	VatRate            string `json:"vatRate" gorm:"column:vat_rate"`
	AllowNegativeStock bool   `json:"allowNegativeStock" gorm:"column:allow_negative_stock"`
	Version            int    `json:"version" gorm:"column:version"`
}

type NumberSeries struct {
	ID        uint   `json:"id" gorm:"column:id"`
	DepotID   uint   `json:"depotId" gorm:"column:depot_id"`
	Year      int    `json:"year" gorm:"column:year"`
	Prefix    string `json:"prefix" gorm:"column:prefix"`
	Width     int    `json:"width" gorm:"column:width"`
	NextValue int64  `json:"nextValue" gorm:"column:next_value"`
	Version   int    `json:"version" gorm:"column:version"`
}

type Side string

const (
	Debit  Side = "DEBIT"
	Credit Side = "CREDIT"
)

func (s Side) Opposite() Side {
	if s == Debit {
		return Credit
	}
	return Debit
}

type AccountType string

var normalSides = map[AccountType]Side{
	"ASSET": Debit, "EXPENSE": Debit, "LIABILITY": Credit, "EQUITY": Credit, "INCOME": Credit,
}

// NormalSide reports the side that increases the account and whether the type is known.
func (t AccountType) NormalSide() (Side, bool) {
	s, ok := normalSides[t]
	return s, ok
}

type Account struct {
	ID         uint        `json:"id" gorm:"column:id"`
	OrgID      uint        `json:"orgId" gorm:"column:org_id"`
	Code       string      `json:"code" gorm:"column:code"`
	Name       string      `json:"name" gorm:"column:name"`
	Type       AccountType `json:"type" gorm:"column:type"`
	NormalSide Side        `json:"normalSide" gorm:"column:normal_side"`
	SicilID    *uint       `json:"sicilId" gorm:"column:sicil_id"`
	Currency   string      `json:"currency" gorm:"column:currency"`
	IsActive   bool        `json:"isActive" gorm:"column:is_active"`
	Version    int         `json:"version" gorm:"column:version"`
}

type SourceType string

const (
	SourceManual      SourceType = "manual"
	SourceReceipt     SourceType = "receipt"
	SourceCashVoucher SourceType = "cash_voucher"
	SourceCashCount   SourceType = "cash_count"
	SourceOpening     SourceType = "opening"
	SourceReversal    SourceType = "reversal"
)

type Posting struct {
	ID          uint   `json:"id" gorm:"column:id"`
	EntryID     uint   `json:"-" gorm:"column:entry_id"`
	LineNo      int    `json:"lineNo" gorm:"column:line_no"`
	AccountID   uint   `json:"accountId" gorm:"column:account_id"`
	AccountCode string `json:"accountCode" gorm:"column:account_code"`
	AccountName string `json:"accountName" gorm:"column:account_name"`
	Side        Side   `json:"side" gorm:"column:side"`
	Amount      Money  `json:"amount" gorm:"column:amount"`
	Currency    string `json:"currency" gorm:"column:currency"`
	DueDate     *Date  `json:"dueDate,omitempty" gorm:"column:due_date"`
	Memo        string `json:"memo,omitempty" gorm:"column:memo"`
}

type JournalEntry struct {
	ID                uint       `json:"id" gorm:"column:id"`
	OrgID             uint       `json:"orgId" gorm:"column:org_id"`
	DepotID           uint       `json:"depotId" gorm:"column:depot_id"`
	EntryNo           string     `json:"entryNo" gorm:"column:entry_no"`
	EntryDate         Date       `json:"entryDate" gorm:"column:entry_date"`
	Description       string     `json:"description" gorm:"column:description"`
	SourceType        SourceType `json:"sourceType" gorm:"column:source_type"`
	SourceID          *string    `json:"sourceId" gorm:"column:source_id"`
	ReversesEntryID   *uint      `json:"reversesEntryId" gorm:"column:reverses_entry_id"`
	ReversedByEntryID *uint      `json:"reversedByEntryId" gorm:"column:reversed_by_entry_id"`
	SourceNo          *string    `json:"sourceNo" gorm:"column:source_no"`
	IdempotencyKey    *string    `json:"idempotencyKey" gorm:"column:idempotency_key"`
	Postings          []Posting  `json:"postings" gorm:"-"`
	CreatedBy         *uint      `json:"createdBy" gorm:"column:created_by"`
	CreatedAt         time.Time  `json:"createdAt" gorm:"column:created_at"`
}

type AccountRole struct {
	Role      string `json:"role" gorm:"column:role"`
	DepotID   *uint  `json:"depotId" gorm:"column:depot_id"`
	AccountID uint   `json:"accountId" gorm:"column:account_id"`
}

type TrailItem struct {
	At          time.Time `json:"at" gorm:"column:at"`
	Kind        string    `json:"kind" gorm:"column:kind"`
	Label       string    `json:"label" gorm:"column:label"`
	Ref         *string   `json:"ref" gorm:"column:ref"`
	ActorUserID *uint     `json:"actorUserId" gorm:"column:actor_user_id"`
}

type Receipt struct {
	ID              uint    `json:"id" gorm:"column:id"`
	OrgID           uint    `json:"orgId" gorm:"column:org_id"`
	ReceiptNo       string  `json:"receiptNo" gorm:"column:receipt_no"`
	Kind            string  `json:"kind" gorm:"column:kind"`
	Origin          string  `json:"origin" gorm:"column:origin"`
	DepotID         uint    `json:"depotId" gorm:"column:depot_id"`
	SicilID         uint    `json:"sicilId" gorm:"column:sicil_id"`
	SicilName       string  `json:"sicilName" gorm:"column:sicil_name"`
	Date            Date    `json:"date" gorm:"column:receipt_date"`
	Method          string  `json:"method" gorm:"column:method"`
	Amount          Money   `json:"amount" gorm:"column:amount"`
	Currency        string  `json:"currency" gorm:"column:currency"`
	BankAccountID   *uint   `json:"bankAccountId,omitempty" gorm:"column:bank_account_id"`
	Description     string  `json:"description,omitempty" gorm:"column:description"`
	InstrumentID    *uint   `json:"instrumentId" gorm:"column:instrument_id"`
	InstrumentNo    *string `json:"instrumentNo" gorm:"column:instrument_no"`
	JournalEntryID  uint    `json:"journalEntryId" gorm:"column:journal_entry_id"`
	VoidedByEntryID *uint   `json:"voidedByEntryId" gorm:"column:voided_by_entry_id"`
}

type CashVoucher struct {
	ID              uint   `json:"id" gorm:"column:id"`
	VoucherNo       string `json:"voucherNo" gorm:"column:voucher_no"`
	Kind            string `json:"kind" gorm:"column:kind"`
	DepotID         uint   `json:"depotId" gorm:"column:depot_id"`
	Date            Date   `json:"date" gorm:"column:voucher_date"`
	AccountID       uint   `json:"accountId" gorm:"column:account_id"`
	Amount          Money  `json:"amount" gorm:"column:amount"`
	Description     string `json:"description" gorm:"column:description"`
	JournalEntryID  uint   `json:"journalEntryId" gorm:"column:journal_entry_id"`
	VoidedByEntryID *uint  `json:"voidedByEntryId" gorm:"column:voided_by_entry_id"`
}

type CashCount struct {
	ID             uint      `json:"id" gorm:"column:id"`
	DepotID        uint      `json:"depotId" gorm:"column:depot_id"`
	CountedAmount  Money     `json:"countedAmount" gorm:"column:counted_amount"`
	PostDifference bool      `json:"postDifference" gorm:"-"`
	Note           string    `json:"note,omitempty" gorm:"column:note"`
	CountedAt      time.Time `json:"countedAt" gorm:"column:counted_at"`
	BookBalance    Money     `json:"bookBalance" gorm:"column:book_balance"`
	Difference     Money     `json:"difference" gorm:"column:difference"`
	JournalEntryID *uint     `json:"journalEntryId" gorm:"column:journal_entry_id"`
	CountedBy      *uint     `json:"countedBy" gorm:"column:counted_by"`
}

type OpeningBalance struct {
	AccountID      uint   `json:"accountId" gorm:"column:account_id"`
	DepotID        uint   `json:"depotId" gorm:"column:depot_id"`
	Year           int    `json:"year" gorm:"column:year"`
	Debit          Money  `json:"debit" gorm:"column:debit"`
	Credit         Money  `json:"credit" gorm:"column:credit"`
	JournalEntryID uint   `json:"journalEntryId" gorm:"column:journal_entry_id"`
	Source         string `json:"source" gorm:"column:source"`
}

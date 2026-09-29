package models

type BalanceRow struct {
	AccountID    uint    `json:"accountId" gorm:"column:account_id"`
	AccountCode  string  `json:"accountCode" gorm:"column:account_code"`
	AccountName  string  `json:"accountName" gorm:"column:account_name"`
	SicilID      *uint   `json:"sicilId" gorm:"column:sicil_id"`
	SicilTipi    *string `json:"sicilTipi" gorm:"column:sicil_tipi"`
	City         *string `json:"city" gorm:"column:city"`
	DepotID      *uint   `json:"depotId" gorm:"column:depot_id"`
	DebitTotal   Money   `json:"debitTotal" gorm:"column:debit_total"`
	CreditTotal  Money   `json:"creditTotal" gorm:"column:credit_total"`
	Balance      Money   `json:"balance" gorm:"column:balance"`
	Overdue      Money   `json:"overdue" gorm:"column:overdue"`
	LastMoveDate *Date   `json:"lastMoveDate" gorm:"column:last_move_date"`
}

type StatementLine struct {
	EntryID        *uint      `json:"entryId" gorm:"column:entry_id"`
	EntryDate      Date       `json:"entryDate" gorm:"column:entry_date"`
	DepotID        *uint      `json:"depotId" gorm:"column:depot_id"`
	DocumentNo     *string    `json:"documentNo" gorm:"column:document_no"`
	SourceType     SourceType `json:"sourceType" gorm:"column:source_type"`
	SourceID       *string    `json:"sourceId" gorm:"column:source_id"`
	Subtype        *string    `json:"subtype" gorm:"column:subtype"`
	DueDate        *Date      `json:"dueDate" gorm:"column:due_date"`
	Description    string     `json:"description" gorm:"column:description"`
	Debit          Money      `json:"debit" gorm:"column:debit"`
	Credit         Money      `json:"credit" gorm:"column:credit"`
	RunningBalance Money      `json:"runningBalance" gorm:"-"`
	ResetsBalance  bool       `json:"resetsBalance" gorm:"column:resets_balance"`
}

type DepotTotal struct {
	DepotID uint  `json:"depotId"`
	Debit   Money `json:"debit"`
	Credit  Money `json:"credit"`
	Balance Money `json:"balance"`
}

type Statement struct {
	AccountID      uint            `json:"accountId"`
	From           Date            `json:"from"`
	To             Date            `json:"to"`
	BroughtForward []StatementLine `json:"broughtForward"`
	Lines          []StatementLine `json:"lines"`
	DepotTotals    []DepotTotal    `json:"depotTotals"`
	ClosingBalance Money           `json:"closingBalance"`
}

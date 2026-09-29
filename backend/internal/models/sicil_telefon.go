package models

import "time"

// TelefonTipi is the phone type; the frontend translates the codes.
type TelefonTipi string

const (
	TelefonTipiCep   TelefonTipi = "CEP"
	TelefonTipiEv    TelefonTipi = "EV"
	TelefonTipiIs    TelefonTipi = "IS"
	TelefonTipiFaks  TelefonTipi = "FAKS"
	TelefonTipiDiger TelefonTipi = "DIGER"
)

func ValidTelefonTipi(t TelefonTipi) bool {
	switch t {
	case TelefonTipiCep, TelefonTipiEv, TelefonTipiIs, TelefonTipiFaks, TelefonTipiDiger:
		return true
	}
	return false
}

// SicilTelefon is a typed phone number of a Sicil (deleted with it via FK cascade).
type SicilTelefon struct {
	ID        uint        `gorm:"primaryKey" json:"id"`
	SicilID   uint        `gorm:"not null;index" json:"sicil_id"`
	Tipi      TelefonTipi `gorm:"size:16;not null;default:'CEP'" json:"tipi"`
	Numara    string      `gorm:"size:32;not null" json:"numara"`
	CreatedAt time.Time   `json:"created_at"`
	UpdatedAt time.Time   `json:"updated_at"`
}

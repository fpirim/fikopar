package models

import "time"

// SicilTipi is the account-card type; the frontend translates the codes.
type SicilTipi string

const (
	SicilTipiFirma   SicilTipi = "FIRMA"
	SicilTipiMusteri SicilTipi = "MUSTERI"
	SicilTipiBanka   SicilTipi = "BANKA"
)

func ValidSicilTipi(t SicilTipi) bool {
	switch t {
	case SicilTipiFirma, SicilTipiMusteri, SicilTipiBanka:
		return true
	}
	return false
}

// Sicil is an account card (company/customer/bank); HesapNo is unique per organization.
type Sicil struct {
	ID             uint           `gorm:"primaryKey" json:"id"`
	OrganizationID uint           `gorm:"not null;index;uniqueIndex:idx_hesap_org_no" json:"organization_id"`
	Organization   *Organization  `json:"organization,omitempty"`
	HesapNo        string         `gorm:"size:64;not null;uniqueIndex:idx_hesap_org_no" json:"hesap_no"`
	HesapAdi       string         `gorm:"size:255;not null" json:"hesap_adi"`
	Adres          string         `gorm:"type:text" json:"adres"`
	Ilce           string         `gorm:"size:128" json:"ilce"`
	Sehir          string         `gorm:"size:128" json:"sehir"`
	VDairesi       string         `gorm:"size:128" json:"v_dairesi"`
	VNo            string         `gorm:"size:32" json:"v_no"`
	TCNo           string         `gorm:"size:16" json:"tc_no"`
	Gun            string         `gorm:"size:32" json:"gun"`
	Iskonto        float64        `gorm:"not null;default:0" json:"iskonto"`
	SicilTipi      SicilTipi      `gorm:"size:16;not null;default:'FIRMA'" json:"sicil_tipi"`
	Telefonlar     []SicilTelefon `gorm:"foreignKey:SicilID;constraint:OnDelete:CASCADE" json:"telefonlar"`
	CreatedAt      time.Time      `json:"created_at"`
	UpdatedAt      time.Time      `json:"updated_at"`
}

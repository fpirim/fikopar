package models

import (
	"database/sql/driver"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/jackc/pgx/v5/pgtype"
)

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
	// Created by migrations/0001 and 0005, not by AutoMigrate.
	CreditLimit *Money        `gorm:"-:migration;column:credit_limit" json:"credit_limit"`
	Contacts    SicilContacts `gorm:"-:migration;column:contacts" json:"contacts"`
	Emails      StringList    `gorm:"-:migration;column:emails" json:"emails"`
	Version     int           `gorm:"-:migration;column:version;default:1" json:"version"`
	// SalesRepUserID is a company member; SalesRepName is read with the card.
	SalesRepUserID *uint     `gorm:"-:migration;column:sales_rep_user_id" json:"sales_rep_user_id"`
	SalesRepName   *string   `gorm:"-:migration;->;column:sales_rep_name" json:"sales_rep_name"`
	CreatedAt      time.Time `json:"created_at"`
	UpdatedAt      time.Time `json:"updated_at"`
}

// SicilContact is a contact person; the first one is shown on the statement card.
type SicilContact struct {
	Name  string `json:"name"`
	Role  string `json:"role,omitempty"`
	Phone string `json:"phone,omitempty"`
	Email string `json:"email,omitempty"`
}

// SicilContacts is stored as jsonb.
type SicilContacts []SicilContact

func (c SicilContacts) Value() (driver.Value, error) {
	if c == nil {
		return "[]", nil
	}
	b, err := json.Marshal(c)
	return string(b), err
}

func (c *SicilContacts) Scan(src any) error {
	var b []byte
	switch v := src.(type) {
	case nil:
		*c = SicilContacts{}
		return nil
	case string:
		b = []byte(v)
	case []byte:
		b = v
	default:
		return fmt.Errorf("contacts: cannot scan %T", src)
	}
	out := SicilContacts{}
	if err := json.Unmarshal(b, &out); err != nil {
		return err
	}
	*c = out
	return nil
}

// StringList is a Postgres text[].
type StringList []string

var arrayQuote = strings.NewReplacer(`\`, `\\`, `"`, `\"`)

func (l StringList) Value() (driver.Value, error) {
	parts := make([]string, len(l))
	for i, s := range l {
		parts[i] = `"` + arrayQuote.Replace(s) + `"`
	}
	return "{" + strings.Join(parts, ",") + "}", nil
}

func (l *StringList) Scan(src any) error {
	if src == nil {
		*l = StringList{}
		return nil
	}
	switch src.(type) {
	case string, []byte:
	default:
		return fmt.Errorf("string list: cannot scan %T", src)
	}
	var out []string
	if err := pgtype.NewMap().SQLScanner(&out).Scan(src); err != nil {
		return err
	}
	*l = append(StringList{}, out...)
	return nil
}

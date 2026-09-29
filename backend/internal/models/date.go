package models

import (
	"database/sql/driver"
	"encoding/json"
	"fmt"
	"time"
)

const DateLayout = "2006-01-02"

// Date is a calendar date without time zone, "2006-01-02" on the wire.
type Date struct{ time.Time }

// ParseDate reads yyyy-mm-dd; a time part after a space or "T" is ignored.
func ParseDate(s string) (Date, error) {
	if len(s) > len(DateLayout) && (s[len(DateLayout)] == ' ' || s[len(DateLayout)] == 'T') {
		s = s[:len(DateLayout)]
	}
	t, err := time.Parse(DateLayout, s)
	if err != nil {
		return Date{}, err
	}
	return Date{t}, nil
}

func NewDate(year int, month time.Month, day int) Date {
	return Date{time.Date(year, month, day, 0, 0, 0, 0, time.UTC)}
}

func (d Date) String() string { return d.Format(DateLayout) }

func (d Date) MarshalJSON() ([]byte, error) { return json.Marshal(d.String()) }

func (d *Date) UnmarshalJSON(b []byte) error {
	var s string
	if err := json.Unmarshal(b, &s); err != nil {
		return err
	}
	v, err := ParseDate(s)
	if err != nil {
		return err
	}
	*d = v
	return nil
}

func (d *Date) Scan(src any) error {
	switch v := src.(type) {
	case time.Time:
		*d = NewDate(v.Year(), v.Month(), v.Day())
		return nil
	case string:
		p, err := ParseDate(v[:min(len(v), 10)])
		*d = p
		return err
	case []byte:
		return d.Scan(string(v))
	}
	return fmt.Errorf("date: cannot scan %T", src)
}

func (d Date) Value() (driver.Value, error) { return d.String(), nil }

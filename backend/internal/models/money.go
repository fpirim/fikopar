package models

import (
	"database/sql/driver"
	"encoding/json"
	"errors"
	"fmt"
	"regexp"
	"strconv"
	"strings"
)

// Money is an amount in cents; on the wire it is a decimal string with two fraction digits.
type Money int64

var moneyPattern = regexp.MustCompile(`^-?\d{1,16}(\.\d{1,2})?$`)

var ErrInvalidMoney = errors.New("invalid money")

func ParseMoney(s string) (Money, error) {
	s = strings.TrimSpace(s)
	if !moneyPattern.MatchString(s) {
		return 0, ErrInvalidMoney
	}
	neg := strings.HasPrefix(s, "-")
	s = strings.TrimPrefix(s, "-")
	whole, frac, _ := strings.Cut(s, ".")
	for len(frac) < 2 {
		frac += "0"
	}
	n, err := strconv.ParseInt(whole+frac, 10, 64)
	if err != nil {
		return 0, ErrInvalidMoney
	}
	if neg {
		n = -n
	}
	return Money(n), nil
}

func (m Money) String() string {
	sign := ""
	v := int64(m)
	if v < 0 {
		sign = "-"
		v = -v
	}
	return fmt.Sprintf("%s%d.%02d", sign, v/100, v%100)
}

func (m Money) Abs() Money {
	if m < 0 {
		return -m
	}
	return m
}

func (m Money) MarshalJSON() ([]byte, error) { return json.Marshal(m.String()) }

func (m *Money) UnmarshalJSON(b []byte) error {
	var s string
	if err := json.Unmarshal(b, &s); err != nil {
		return ErrInvalidMoney
	}
	v, err := ParseMoney(s)
	if err != nil {
		return err
	}
	*m = v
	return nil
}

func (m *Money) Scan(src any) error {
	switch v := src.(type) {
	case nil:
		*m = 0
		return nil
	case int64:
		*m = Money(v * 100)
		return nil
	case string:
		return m.scanText(v)
	case []byte:
		return m.scanText(string(v))
	}
	return fmt.Errorf("money: cannot scan %T", src)
}

func (m *Money) scanText(s string) error {
	whole, frac, _ := strings.Cut(s, ".")
	if len(frac) > 2 {
		frac = frac[:2]
	}
	v, err := ParseMoney(strings.TrimRight(whole+"."+frac, "."))
	if err != nil {
		return fmt.Errorf("money: cannot scan %q", s)
	}
	*m = v
	return nil
}

func (m Money) Value() (driver.Value, error) { return m.String(), nil }

// TrimDecimal renders a numeric without trailing zeros, e.g. "20.0000" → "20".
func TrimDecimal(s string) string {
	if !strings.Contains(s, ".") {
		return s
	}
	s = strings.TrimRight(s, "0")
	return strings.TrimSuffix(s, ".")
}

package models

import (
	"database/sql/driver"
	"encoding/json"
	"errors"
	"fmt"
	"math/big"
	"regexp"
	"strconv"
	"strings"
)

// Dec4 is a decimal with four fraction digits: quantities and unit prices. On the wire it is a
// string without trailing zeros ("12.5", "-3").
type Dec4 int64

const dec4Scale = 10000

var (
	dec4Pattern       = regexp.MustCompile(`^-?\d{1,14}(\.\d{1,4})?$`)
	ErrInvalidDecimal = errors.New("invalid decimal")
)

// ParseDec4 reads a signed decimal with up to four fraction digits.
func ParseDec4(s string) (Dec4, error) {
	s = strings.TrimSpace(s)
	if !dec4Pattern.MatchString(s) {
		return 0, ErrInvalidDecimal
	}
	neg := strings.HasPrefix(s, "-")
	whole, frac, _ := strings.Cut(strings.TrimPrefix(s, "-"), ".")
	frac += strings.Repeat("0", 4-len(frac))
	n, err := strconv.ParseInt(whole+frac, 10, 64)
	if err != nil {
		return 0, ErrInvalidDecimal
	}
	if neg {
		n = -n
	}
	return Dec4(n), nil
}

// ParseQuantity reads an unsigned Dec4 (Quantity, Money4).
func ParseQuantity(s string) (Dec4, error) {
	d, err := ParseDec4(s)
	if err != nil || d < 0 || strings.HasPrefix(strings.TrimSpace(s), "-") {
		return 0, ErrInvalidDecimal
	}
	return d, nil
}

func (d Dec4) String() string {
	sign := ""
	v := int64(d)
	if v < 0 {
		sign = "-"
		v = -v
	}
	s := fmt.Sprintf("%s%d", sign, v/dec4Scale)
	if frac := v % dec4Scale; frac != 0 {
		s += strings.TrimRight(fmt.Sprintf(".%04d", frac), "0")
	}
	return s
}

func (d Dec4) IsNonNegative() bool { return d >= 0 }

// Times multiplies two Dec4 values into Money, rounded half away from zero.
func (d Dec4) Times(o Dec4) Money {
	p := new(big.Int).Mul(big.NewInt(int64(d)), big.NewInt(int64(o)))
	neg := p.Sign() < 0
	p.Abs(p)
	q, r := new(big.Int).QuoRem(p, big.NewInt(1_000_000), new(big.Int))
	if r.Cmp(big.NewInt(500_000)) >= 0 {
		q.Add(q, big.NewInt(1))
	}
	if neg {
		q.Neg(q)
	}
	return Money(q.Int64())
}

func (d Dec4) MarshalJSON() ([]byte, error) { return json.Marshal(d.String()) }

func (d *Dec4) UnmarshalJSON(b []byte) error {
	var s string
	if err := json.Unmarshal(b, &s); err != nil {
		return ErrInvalidDecimal
	}
	v, err := ParseDec4(s)
	if err != nil {
		return err
	}
	*d = v
	return nil
}

func (d *Dec4) Scan(src any) error {
	switch v := src.(type) {
	case nil:
		*d = 0
		return nil
	case int64:
		*d = Dec4(v * dec4Scale)
		return nil
	case string:
		return d.scanText(v)
	case []byte:
		return d.scanText(string(v))
	}
	return fmt.Errorf("dec4: cannot scan %T", src)
}

func (d *Dec4) scanText(s string) error {
	whole, frac, _ := strings.Cut(s, ".")
	if len(frac) > 4 {
		frac = frac[:4]
	}
	v, err := ParseDec4(strings.TrimRight(whole+"."+frac, "."))
	if err != nil {
		return fmt.Errorf("dec4: cannot scan %q", s)
	}
	*d = v
	return nil
}

func (d Dec4) Value() (driver.Value, error) { return d.String(), nil }

// PercentChange is (cur − prev) / prev in percent with one decimal; nil when prev is zero.
func PercentChange(prev, cur Dec4) *string {
	if prev == 0 {
		return nil
	}
	s := strconv.FormatFloat(float64(cur-prev)/float64(prev)*100, 'f', 1, 64)
	if s == "-0.0" {
		s = "0.0"
	}
	return &s
}

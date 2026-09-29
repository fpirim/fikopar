package models

import (
	"encoding/json"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestParseMoney(t *testing.T) {
	for in, want := range map[string]Money{"0": 0, "12": 1200, "12.5": 1250, "-0.05": -5, " 1250.00 ": 125000, "9999999999999999.99": 999999999999999999} {
		got, err := ParseMoney(in)
		require.NoError(t, err, in)
		assert.Equal(t, want, got, in)
	}
	for _, in := range []string{"", "1.234", "1,00", "abc", "--1", "1e3", "12345678901234567"} {
		_, err := ParseMoney(in)
		assert.ErrorIs(t, err, ErrInvalidMoney, in)
	}
}

func TestMoneyStringAndJSON(t *testing.T) {
	assert.Equal(t, "-1234.05", Money(-123405).String())
	assert.Equal(t, "0.00", Money(0).String())
	assert.Equal(t, Money(5), Money(-5).Abs())

	b, err := json.Marshal(struct{ A Money }{A: 1050})
	require.NoError(t, err)
	assert.JSONEq(t, `{"A":"10.50"}`, string(b))

	var v struct{ A Money }
	require.NoError(t, json.Unmarshal([]byte(`{"A":"7.10"}`), &v))
	assert.Equal(t, Money(710), v.A)
	assert.Error(t, json.Unmarshal([]byte(`{"A":7.1}`), &v), "money is a string on the wire")
	assert.Error(t, json.Unmarshal([]byte(`{"A":"7.123"}`), &v))
}

func TestMoneyScanAndValue(t *testing.T) {
	var m Money
	for src, want := range map[any]Money{"12.34": 1234, "12.3400": 1234, "-3": -300, int64(4): 400} {
		require.NoError(t, m.Scan(src))
		assert.Equal(t, want, m)
	}
	require.NoError(t, m.Scan([]byte("0.10")))
	assert.Equal(t, Money(10), m)
	require.NoError(t, m.Scan(nil))
	assert.Equal(t, Money(0), m)
	assert.Error(t, m.Scan(1.5))
	assert.Error(t, m.Scan("x"))
	v, err := Money(-1).Value()
	require.NoError(t, err)
	assert.Equal(t, "-0.01", v)
}

func TestTrimDecimal(t *testing.T) {
	assert.Equal(t, "20", TrimDecimal("20.0000"))
	assert.Equal(t, "18.5", TrimDecimal("18.5000"))
	assert.Equal(t, "7", TrimDecimal("7"))
}

func TestDate(t *testing.T) {
	d, err := ParseDate("2026-02-28")
	require.NoError(t, err)
	assert.Equal(t, "2026-02-28", d.String())
	_, err = ParseDate("2026-02-30")
	assert.Error(t, err)

	b, err := json.Marshal(d)
	require.NoError(t, err)
	assert.Equal(t, `"2026-02-28"`, string(b))
	var back Date
	require.NoError(t, json.Unmarshal(b, &back))
	assert.Equal(t, d, back)
	assert.Error(t, json.Unmarshal([]byte(`"28.02.2026"`), &back))
	assert.Error(t, json.Unmarshal([]byte(`20260228`), &back))

	var s Date
	require.NoError(t, s.Scan(time.Date(2026, 3, 1, 23, 0, 0, 0, time.FixedZone("x", 3*3600))))
	assert.Equal(t, "2026-03-01", s.String())
	require.NoError(t, s.Scan("2026-03-02T00:00:00Z"))
	assert.Equal(t, "2026-03-02", s.String())
	require.NoError(t, s.Scan([]byte("2026-03-03")))
	assert.Equal(t, "2026-03-03", s.String())
	assert.Error(t, s.Scan(42))
	v, err := s.Value()
	require.NoError(t, err)
	assert.Equal(t, "2026-03-03", v)
}

func TestSideAndAccountType(t *testing.T) {
	assert.Equal(t, Credit, Debit.Opposite())
	assert.Equal(t, Debit, Credit.Opposite())
	s, ok := AccountType("LIABILITY").NormalSide()
	assert.True(t, ok)
	assert.Equal(t, Credit, s)
	_, ok = AccountType("X").NormalSide()
	assert.False(t, ok)
}

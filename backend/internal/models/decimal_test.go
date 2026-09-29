package models

import (
	"encoding/json"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestParseDec4(t *testing.T) {
	for in, want := range map[string]Dec4{"0": 0, "12": 120000, "12.5": 125000, "-3": -30000, "0.0001": 1, " 7.25 ": 72500,
		"99999999999999.9999": 999999999999999999} {
		got, err := ParseDec4(in)
		require.NoError(t, err, in)
		assert.Equal(t, want, got, in)
	}
	for _, in := range []string{"", "1.23456", "1,5", "x", "--1", "1e3", "123456789012345"} {
		_, err := ParseDec4(in)
		assert.ErrorIs(t, err, ErrInvalidDecimal, in)
	}
}

func TestDec4StringJSONAndScan(t *testing.T) {
	assert.Equal(t, "12.5", Dec4(125000).String())
	assert.Equal(t, "-3", Dec4(-30000).String())
	assert.Equal(t, "0", Dec4(0).String())
	assert.Equal(t, "0.0001", Dec4(1).String())
	assert.Equal(t, "-0.05", Dec4(-500).String())

	b, err := json.Marshal(struct{ Q Dec4 }{Q: 125000})
	require.NoError(t, err)
	assert.JSONEq(t, `{"Q":"12.5"}`, string(b))
	var v struct{ Q Dec4 }
	require.NoError(t, json.Unmarshal([]byte(`{"Q":"1.25"}`), &v))
	assert.Equal(t, Dec4(12500), v.Q)
	assert.Error(t, json.Unmarshal([]byte(`{"Q":1.25}`), &v))

	var d Dec4
	require.NoError(t, d.Scan("-12.5000"))
	assert.Equal(t, Dec4(-125000), d)
	require.NoError(t, d.Scan([]byte("3")))
	assert.Equal(t, Dec4(30000), d)
	require.NoError(t, d.Scan(int64(4)))
	assert.Equal(t, Dec4(40000), d)
	require.NoError(t, d.Scan(nil))
	assert.Equal(t, Dec4(0), d)
	assert.Error(t, d.Scan(1.5))
	val, err := Dec4(125000).Value()
	require.NoError(t, err)
	assert.Equal(t, "12.5", val)
}

func TestDec4Value(t *testing.T) {
	// difference × unit cost, rounded half away from zero to cents
	assert.Equal(t, Money(-3704), Dec4(-30000).Times(Dec4(123456)))
	assert.Equal(t, Money(1), Dec4(10000).Times(Dec4(50)))
	assert.Equal(t, Money(0), Dec4(10000).Times(Dec4(49)))
	assert.Equal(t, Money(-1), Dec4(-10000).Times(Dec4(50)))
	assert.True(t, Dec4(1).IsNonNegative())
	assert.False(t, Dec4(-1).IsNonNegative())
}

func TestDec4Unsigned(t *testing.T) {
	_, err := ParseQuantity("-1")
	assert.ErrorIs(t, err, ErrInvalidDecimal)
	q, err := ParseQuantity("2.5")
	require.NoError(t, err)
	assert.Equal(t, Dec4(25000), q)
}

func TestPercentChange(t *testing.T) {
	assert.Equal(t, "6.9", *PercentChange(Dec4(1000000), Dec4(1069000)))
	assert.Equal(t, "-50.0", *PercentChange(Dec4(20000), Dec4(10000)))
	assert.Nil(t, PercentChange(Dec4(0), Dec4(10000)))
}

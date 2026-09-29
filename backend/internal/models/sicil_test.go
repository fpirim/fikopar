package models

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestStringList_RoundTripsThePostgresArrayText(t *testing.T) {
	v, err := StringList{"a@x.com", `q"uote\\`}.Value()
	require.NoError(t, err)
	var l StringList
	require.NoError(t, l.Scan(v))
	assert.Equal(t, StringList{"a@x.com", `q"uote\\`}, l)
	require.NoError(t, l.Scan([]byte("{}")))
	assert.Equal(t, StringList{}, l)
	require.NoError(t, l.Scan(nil))
	assert.Equal(t, StringList{}, l)
	assert.Error(t, l.Scan(42))
}

func TestSicilContacts_JSONColumn(t *testing.T) {
	v, err := SicilContacts{{Name: "A", Phone: "1"}}.Value()
	require.NoError(t, err)
	var c SicilContacts
	require.NoError(t, c.Scan(v))
	assert.Equal(t, SicilContacts{{Name: "A", Phone: "1"}}, c)
	require.NoError(t, c.Scan([]byte(`[]`)))
	assert.Equal(t, SicilContacts{}, c)
	require.NoError(t, c.Scan(nil))
	assert.Equal(t, SicilContacts{}, c)
	assert.Error(t, c.Scan(42))
	v, err = SicilContacts(nil).Value()
	require.NoError(t, err)
	assert.Equal(t, "[]", v)
}

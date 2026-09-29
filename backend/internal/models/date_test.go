package models

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestParseDate_IgnoresTimePart(t *testing.T) {
	for _, s := range []string{"2026-03-01", "2026-03-01 10:30:00", "2026-03-01T23:59:59Z"} {
		d, err := ParseDate(s)
		require.NoError(t, err, s)
		assert.Equal(t, "2026-03-01", d.String(), s)
	}
	for _, s := range []string{"", "2026-3-1", "2026-03-01x", "2026-13-01 00:00:00"} {
		_, err := ParseDate(s)
		assert.Error(t, err, s)
	}
}

package services

import (
	"encoding/base64"
	"encoding/json"
	"strconv"

	"github.com/fpirim/fikopar/backend/internal/apierror"
)

func itoa(i int) string { return strconv.Itoa(i) }

// encodeCursor makes an opaque keyset cursor from any JSON-encodable position.
func encodeCursor(v any) string {
	b, _ := json.Marshal(v)
	return base64.RawURLEncoding.EncodeToString(b)
}

// decodeCursorInto reads a cursor from encodeCursor; empty leaves v untouched and reports false.
func decodeCursorInto(cursor string, v any) (bool, error) {
	if cursor == "" {
		return false, nil
	}
	b, err := base64.RawURLEncoding.DecodeString(cursor)
	if err != nil || json.Unmarshal(b, v) != nil {
		return false, apierror.WithField(apierror.InvalidRequest, "cursor")
	}
	return true, nil
}

// decodeCursor reads a string cursor.
func decodeCursor(cursor string) (string, error) {
	var s string
	_, err := decodeCursorInto(cursor, &s)
	return s, err
}

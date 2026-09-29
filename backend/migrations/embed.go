// Package migrations holds the ordered SQL schema applied at startup; draft/ is not embedded.
package migrations

import "embed"

//go:embed *.sql
var FS embed.FS

package services

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestNormalizeCode_FoldsTurkishLettersToASCII(t *testing.T) {
	for in, want := range map[string]string{
		"ısı": "ISI", "IŞIK": "ISIK", "şalter": "SALTER", "Şalter": "SALTER", "İğne-ÜÇ": "IGNEUC",
		"göğüs": "GOGUS", "Çığ 12": "CIG12", "abc.123": "ABC123", "--": "", "çöp": "COP",
	} {
		assert.Equal(t, want, NormalizeCode(in), in)
	}
}

func TestFoldWords_FoldsTurkishLettersToASCII(t *testing.T) {
	words, norms := foldWords("ÇIĞ şalter İğne")
	assert.Equal(t, []string{"cig", "salter", "igne"}, words)
	assert.Equal(t, []string{"CIG", "SALTER", "IGNE"}, norms)
}

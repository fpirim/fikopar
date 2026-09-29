//go:build integration

package repository_test

import (
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/testutil"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func seedOrgs(t *testing.T) (uint, uint) {
	t.Helper()
	orgRepo := repository.NewOrganizationRepository(testDB)
	a := &models.Organization{Name: "OrgA", Slug: "org-a"}
	b := &models.Organization{Name: "OrgB", Slug: "org-b"}
	require.NoError(t, orgRepo.Create(a))
	require.NoError(t, orgRepo.Create(b))
	return a.ID, b.ID
}

func TestSicilRepository_CRUD(t *testing.T) {
	testutil.TruncateAll(t, testDB)
	orgA, _ := seedOrgs(t)
	repo := repository.NewSicilRepository(testDB)

	c := &models.Sicil{
		OrganizationID: orgA, HesapNo: "320.C.001", HesapAdi: "Ceylan", SicilTipi: models.SicilTipiFirma,
	}
	require.NoError(t, repo.Create(c))
	assert.NotZero(t, c.ID)

	got, err := repo.GetByID(c.ID)
	require.NoError(t, err)
	assert.Equal(t, "Ceylan", got.HesapAdi)

	require.NoError(t, repo.UpdateFields(c.ID, map[string]any{"hesap_adi": "Ceylan A.Ş."}))
	reloaded, err := repo.GetByID(c.ID)
	require.NoError(t, err)
	assert.Equal(t, "Ceylan A.Ş.", reloaded.HesapAdi)

	require.NoError(t, repo.Delete(c.ID))
	_, err = repo.GetByID(c.ID)
	assert.Error(t, err)
}

func TestSicilRepository_UniqueHesapNoPerOrg(t *testing.T) {
	testutil.TruncateAll(t, testDB)
	orgA, orgB := seedOrgs(t)
	repo := repository.NewSicilRepository(testDB)

	require.NoError(t, repo.Create(&models.Sicil{OrganizationID: orgA, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma}))

	// Same hesap_no within the same org → conflict.
	err := repo.Create(&models.Sicil{OrganizationID: orgA, HesapNo: "001", HesapAdi: "Y", SicilTipi: models.SicilTipiFirma})
	assert.Error(t, err, "duplicate hesap_no within an org must be rejected")

	// Same hesap_no in a different org → allowed.
	err = repo.Create(&models.Sicil{OrganizationID: orgB, HesapNo: "001", HesapAdi: "Z", SicilTipi: models.SicilTipiFirma})
	require.NoError(t, err, "same hesap_no in a different org must be accepted")
}

func TestSicilRepository_GridScopedByOrg(t *testing.T) {
	testutil.TruncateAll(t, testDB)
	orgA, orgB := seedOrgs(t)
	repo := repository.NewSicilRepository(testDB)

	require.NoError(t, repo.Create(&models.Sicil{OrganizationID: orgA, HesapNo: "A1", HesapAdi: "AlphaA", SicilTipi: models.SicilTipiFirma}))
	require.NoError(t, repo.Create(&models.Sicil{OrganizationID: orgA, HesapNo: "A2", HesapAdi: "BetaA", SicilTipi: models.SicilTipiFirma}))
	require.NoError(t, repo.Create(&models.Sicil{OrganizationID: orgB, HesapNo: "B1", HesapAdi: "AlphaB", SicilTipi: models.SicilTipiFirma}))

	resp, err := repo.ListForGrid(repository.GridRequest{StartRow: 0, EndRow: 100}, orgA)
	require.NoError(t, err)
	assert.Equal(t, int64(2), resp.TotalCount, "only 2 cards for OrgA")
	rows, _ := resp.Rows.(*[]models.Sicil)
	require.NotNil(t, rows)
	for _, r := range *rows {
		assert.Equal(t, orgA, r.OrganizationID)
	}
}

func TestSicilRepository_GridSearch(t *testing.T) {
	testutil.TruncateAll(t, testDB)
	orgA, _ := seedOrgs(t)
	repo := repository.NewSicilRepository(testDB)

	require.NoError(t, repo.Create(&models.Sicil{OrganizationID: orgA, HesapNo: "320.C.001", HesapAdi: "Ceylan", Sehir: "İSTANBUL", SicilTipi: models.SicilTipiFirma}))
	require.NoError(t, repo.Create(&models.Sicil{OrganizationID: orgA, HesapNo: "320.A.001", HesapAdi: "Ankara Banka", Sehir: "ANKARA", SicilTipi: models.SicilTipiBanka}))

	resp, err := repo.ListForGrid(repository.GridRequest{StartRow: 0, EndRow: 100, Search: "ceylan"}, orgA)
	require.NoError(t, err)
	assert.Equal(t, int64(1), resp.TotalCount)

	resp, err = repo.ListForGrid(repository.GridRequest{StartRow: 0, EndRow: 100, Search: "ANKARA"}, orgA)
	require.NoError(t, err)
	assert.Equal(t, int64(1), resp.TotalCount)
}

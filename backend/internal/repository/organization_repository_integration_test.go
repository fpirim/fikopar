//go:build integration

package repository_test

import (
	"testing"

	"fikopar/backend/internal/models"
	"fikopar/backend/internal/repository"
	"fikopar/backend/internal/testutil"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestOrgRepository_CRUD(t *testing.T) {
	testutil.TruncateAll(t, testDB)
	repo := repository.NewOrganizationRepository(testDB)

	o := &models.Organization{Name: "Acme", Slug: "acme"}
	require.NoError(t, repo.Create(o))
	assert.NotZero(t, o.ID)

	got, err := repo.GetByID(o.ID)
	require.NoError(t, err)
	assert.Equal(t, "acme", got.Slug)

	got.Name = "Acme Inc"
	require.NoError(t, repo.Update(got))
	reloaded, err := repo.GetByID(o.ID)
	require.NoError(t, err)
	assert.Equal(t, "Acme Inc", reloaded.Name)

	require.NoError(t, repo.Delete(o.ID))
	_, err = repo.GetByID(o.ID)
	assert.Error(t, err)
}

func TestOrgRepository_UniqueSlugConflict(t *testing.T) {
	testutil.TruncateAll(t, testDB)
	repo := repository.NewOrganizationRepository(testDB)

	require.NoError(t, repo.Create(&models.Organization{Name: "A", Slug: "dup"}))
	err := repo.Create(&models.Organization{Name: "B", Slug: "dup"})
	assert.Error(t, err, "duplicate slug must violate the unique index")
}

func TestOrgRepository_Grid(t *testing.T) {
	testutil.TruncateAll(t, testDB)
	repo := repository.NewOrganizationRepository(testDB)

	require.NoError(t, repo.Create(&models.Organization{Name: "Alpha", Slug: "alpha"}))
	require.NoError(t, repo.Create(&models.Organization{Name: "Beta", Slug: "beta"}))
	require.NoError(t, repo.Create(&models.Organization{Name: "Gamma", Slug: "gamma"}))

	resp, err := repo.ListForGrid(repository.GridRequest{
		StartRow:  0,
		EndRow:    2,
		SortModel: []repository.SortModel{{ColID: "name", Sort: "asc"}},
	})
	require.NoError(t, err)
	assert.Equal(t, int64(3), resp.TotalCount, "total should be all rows when unfiltered")

	rows, ok := resp.Rows.(*[]models.Organization)
	require.True(t, ok)
	assert.Len(t, *rows, 2, "page size should cap returned rows")
	assert.Equal(t, "Alpha", (*rows)[0].Name, "ascending sort by name")
}

// TestOrgRepository_GridSearch covers the free-text Search term, which matches
// across name and slug (OR ILIKE), case-insensitively.
func TestOrgRepository_GridSearch(t *testing.T) {
	testutil.TruncateAll(t, testDB)
	repo := repository.NewOrganizationRepository(testDB)

	require.NoError(t, repo.Create(&models.Organization{Name: "Acme Corp", Slug: "acme"}))
	require.NoError(t, repo.Create(&models.Organization{Name: "Beta LLC", Slug: "beta-co"}))
	require.NoError(t, repo.Create(&models.Organization{Name: "Gamma", Slug: "gamma"}))

	cases := []struct {
		term string
		want int64
	}{
		{"acme", 1}, // name + slug
		{"BETA", 1}, // case-insensitive name match
		{"-co", 1},  // slug fragment (beta-co)
		{"a", 3},    // appears in all three names
		{"zzzz", 0}, // no match
	}
	for _, c := range cases {
		resp, err := repo.ListForGrid(repository.GridRequest{StartRow: 0, EndRow: 100, Search: c.term})
		require.NoError(t, err, c.term)
		assert.Equal(t, c.want, resp.TotalCount, "search %q", c.term)
	}
}

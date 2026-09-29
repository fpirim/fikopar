//go:build integration

package repository_test

import (
	"fmt"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/testutil"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestUserRepository_CRUD(t *testing.T) {
	testutil.TruncateAll(t)
	repo := repository.NewUserRepository(testDB)

	u := &models.User{Email: "a@b.com", Password: "h", Name: "Alice", Role: models.RoleUser}
	require.NoError(t, repo.Create(u))
	assert.NotZero(t, u.ID)

	got, err := repo.GetByID(u.ID)
	require.NoError(t, err)
	assert.Equal(t, "Alice", got.Name)

	byEmail, err := repo.GetByEmail("a@b.com")
	require.NoError(t, err)
	assert.Equal(t, u.ID, byEmail.ID)

	got.Name = "Alice2"
	require.NoError(t, repo.Update(got))
	reloaded, err := repo.GetByID(u.ID)
	require.NoError(t, err)
	assert.Equal(t, "Alice2", reloaded.Name)

	require.NoError(t, repo.Delete(u.ID))
	_, err = repo.GetByID(u.ID)
	assert.Error(t, err)
}

func TestUserRepository_CountAdmins(t *testing.T) {
	testutil.TruncateAll(t)
	repo := repository.NewUserRepository(testDB)

	require.NoError(t, repo.Create(&models.User{Email: "admin1@x.com", Password: "h", Role: models.RoleAdmin}))
	require.NoError(t, repo.Create(&models.User{Email: "admin2@x.com", Password: "h", Role: models.RoleAdmin}))
	require.NoError(t, repo.Create(&models.User{Email: "user@x.com", Password: "h", Role: models.RoleUser}))

	count, err := repo.CountAdmins()
	require.NoError(t, err)
	assert.Equal(t, int64(2), count)
}

func TestUserRepository_SetOrganizations(t *testing.T) {
	testutil.TruncateAll(t)
	userRepo := repository.NewUserRepository(testDB)
	orgRepo := repository.NewOrganizationRepository(testDB)

	o1 := &models.Organization{Name: "O1", Slug: "o1"}
	o2 := &models.Organization{Name: "O2", Slug: "o2"}
	require.NoError(t, orgRepo.Create(o1))
	require.NoError(t, orgRepo.Create(o2))

	u := &models.User{Email: "m@x.com", Password: "h", Role: models.RoleUser}
	require.NoError(t, userRepo.Create(u))
	require.NoError(t, userRepo.SetOrganizations(u.ID, []uint{o1.ID, o2.ID}))

	got, err := userRepo.GetByID(u.ID)
	require.NoError(t, err)
	assert.Len(t, got.Organizations, 2)
}

// TestUserRepository_GridTotalCountWithFilter guards the fix where TotalCount
// was computed before filters were applied.
func TestUserRepository_GridTotalCountWithFilter(t *testing.T) {
	testutil.TruncateAll(t)
	repo := repository.NewUserRepository(testDB)

	// 3 users match "acme", 7 do not.
	for i := 0; i < 3; i++ {
		require.NoError(t, repo.Create(&models.User{
			Email: fmt.Sprintf("user%d@acme.com", i), Password: "h", Name: "Acme", Role: models.RoleUser,
		}))
	}
	for i := 0; i < 7; i++ {
		require.NoError(t, repo.Create(&models.User{
			Email: fmt.Sprintf("user%d@other.com", i), Password: "h", Name: "Other", Role: models.RoleUser,
		}))
	}

	resp, err := repo.ListForGrid(repository.GridRequest{
		StartRow: 0,
		EndRow:   100,
		FilterModel: repository.FilterModel{
			"email": {FilterType: "text", Type: "contains", Filter: "acme"},
		},
	})
	require.NoError(t, err)
	assert.Equal(t, int64(3), resp.TotalCount, "TotalCount must reflect the filtered set, not all rows")

	rows, ok := resp.Rows.(*[]models.User)
	require.True(t, ok)
	assert.Len(t, *rows, 3)
}

// TestUserRepository_GridFilterVariants exercises the startsWith/endsWith/equals
// filter branches in addition to the contains branch covered above.
func TestUserRepository_GridFilterVariants(t *testing.T) {
	testutil.TruncateAll(t)
	repo := repository.NewUserRepository(testDB)
	for _, e := range []string{"alice@acme.com", "bob@acme.com", "carol@other.com"} {
		require.NoError(t, repo.Create(&models.User{Email: e, Password: "h", Role: models.RoleUser}))
	}

	cases := []struct {
		typ, filter string
		want        int
	}{
		{"startsWith", "alice", 1},
		{"endsWith", "other.com", 1},
		{"equals", "bob@acme.com", 1},
		{"notContains", "acme", 1},
	}
	for _, c := range cases {
		resp, err := repo.ListForGrid(repository.GridRequest{
			StartRow: 0, EndRow: 100,
			FilterModel: repository.FilterModel{"email": {Type: c.typ, Filter: c.filter}},
		})
		require.NoError(t, err, c.typ)
		assert.Equal(t, int64(c.want), resp.TotalCount, "filter %s", c.typ)
	}
}

// TestUserRepository_GridSearch covers the free-text Search term which matches
// across name, email and role (OR ILIKE), case-insensitively.
func TestUserRepository_GridSearch(t *testing.T) {
	testutil.TruncateAll(t)
	repo := repository.NewUserRepository(testDB)
	require.NoError(t, repo.Create(&models.User{Email: "alice@acme.com", Name: "Alice", Password: "h", Role: models.RoleUser}))
	require.NoError(t, repo.Create(&models.User{Email: "bob@other.com", Name: "Bob", Password: "h", Role: models.RoleAdmin}))
	require.NoError(t, repo.Create(&models.User{Email: "carol@x.com", Name: "Carol", Password: "h", Role: models.RoleUser}))

	cases := []struct {
		term string
		want int64
	}{
		{"alice", 1},      // name + email
		{"ACME", 1},       // case-insensitive email match
		{"admin", 1},      // role match (bob)
		{"@", 3},          // all emails contain @
		{"nomatchxyz", 0}, // nothing
	}
	for _, c := range cases {
		resp, err := repo.ListForGrid(repository.GridRequest{StartRow: 0, EndRow: 100, Search: c.term})
		require.NoError(t, err, c.term)
		assert.Equal(t, c.want, resp.TotalCount, "search %q", c.term)
	}
}

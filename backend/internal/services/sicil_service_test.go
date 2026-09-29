package services

import (
	"errors"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/repository/mocks"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
	"gorm.io/gorm"
)

func newSicilService() (*SicilService, *mocks.SicilRepository) {
	repo := &mocks.SicilRepository{}
	return NewSicilService(repo), repo
}

func memberClaims(orgID uint) *Claims {
	return &Claims{UserID: 1, Email: "u@x", Role: string(models.RoleUser), OrganizationIDs: []uint{orgID}}
}
func adminClaims() *Claims {
	return &Claims{UserID: 1, Email: "a@x", Role: string(models.RoleAdmin)}
}

// --- AssertCanAccessOrg -------------------------------------------------

func TestAssertCanAccessOrg_AdminBypass(t *testing.T) {
	require.NoError(t, AssertCanAccessOrg(adminClaims(), 999))
}

func TestAssertCanAccessOrg_MemberAllowed(t *testing.T) {
	require.NoError(t, AssertCanAccessOrg(memberClaims(7), 7))
}

func TestAssertCanAccessOrg_NonMemberForbidden(t *testing.T) {
	err := AssertCanAccessOrg(memberClaims(7), 9)
	require.ErrorIs(t, err, apierror.Forbidden)
}

func TestAssertCanAccessOrg_NilClaims(t *testing.T) {
	err := AssertCanAccessOrg(nil, 1)
	require.ErrorIs(t, err, apierror.Unauthorized)
}

// --- Create -------------------------------------------------------------

func TestCreate_ForbiddenForNonMember(t *testing.T) {
	svc, repo := newSicilService()
	_, err := svc.Create(memberClaims(1), &models.Sicil{
		OrganizationID: 2, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
	})
	require.ErrorIs(t, err, apierror.Forbidden)
	repo.AssertNotCalled(t, "Create", mock.Anything)
}

func TestCreate_RequiresHesapNo(t *testing.T) {
	svc, _ := newSicilService()
	_, err := svc.Create(memberClaims(1), &models.Sicil{
		OrganizationID: 1, HesapNo: "  ", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
	})
	require.ErrorIs(t, err, apierror.HesapNoRequired)
}

func TestCreate_RequiresHesapAdi(t *testing.T) {
	svc, _ := newSicilService()
	_, err := svc.Create(memberClaims(1), &models.Sicil{
		OrganizationID: 1, HesapNo: "001", HesapAdi: "", SicilTipi: models.SicilTipiFirma,
	})
	require.ErrorIs(t, err, apierror.HesapAdiRequired)
}

func TestCreate_RejectsInvalidSicilTipi(t *testing.T) {
	svc, _ := newSicilService()
	_, err := svc.Create(memberClaims(1), &models.Sicil{
		OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: "GARIP",
	})
	require.ErrorIs(t, err, apierror.InvalidSicilTipi)
}

func TestCreate_RejectsIskontoOutOfRange(t *testing.T) {
	svc, _ := newSicilService()
	_, err := svc.Create(memberClaims(1), &models.Sicil{
		OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma, Iskonto: 150,
	})
	require.ErrorIs(t, err, apierror.InvalidIskonto)
}

func TestCreate_DefaultsSicilTipiToFirma(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("Create", mock.Anything).Return(nil)

	card, err := svc.Create(memberClaims(1), &models.Sicil{
		OrganizationID: 1, HesapNo: "001", HesapAdi: "X",
	})
	require.NoError(t, err)
	assert.Equal(t, models.SicilTipiFirma, card.SicilTipi)
}

func TestCreate_DuplicateMapsToHesapNoExists(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("Create", mock.Anything).Return(errors.New("duplicate key value"))

	_, err := svc.Create(memberClaims(1), &models.Sicil{
		OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
	})
	require.ErrorIs(t, err, apierror.HesapNoExists)
}

// --- Update -------------------------------------------------------------

func TestUpdate_NotFound(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(5)).Return(nil, gorm.ErrRecordNotFound)

	_, err := svc.Update(memberClaims(1), 5, map[string]any{"hesap_adi": "Y"})
	require.ErrorIs(t, err, apierror.SicilNotFound)
}

func TestUpdate_ForbiddenForOutsideOrg(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(5)).Return(&models.Sicil{
		ID: 5, OrganizationID: 99, HesapNo: "001", HesapAdi: "Eski", SicilTipi: models.SicilTipiFirma,
	}, nil)

	_, err := svc.Update(memberClaims(1), 5, map[string]any{"hesap_adi": "Yeni"})
	require.ErrorIs(t, err, apierror.Forbidden)
	repo.AssertNotCalled(t, "UpdateFields", mock.Anything, mock.Anything)
}

func TestUpdate_IgnoresOrganizationIDChange(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(5)).Return(&models.Sicil{
		ID: 5, OrganizationID: 1, HesapNo: "001", HesapAdi: "Eski", SicilTipi: models.SicilTipiFirma,
	}, nil).Once()
	repo.On("UpdateFields", uint(5), mock.MatchedBy(func(fields map[string]any) bool {
		_, hasOrg := fields["organization_id"]
		return !hasOrg && fields["hesap_adi"] == "Yeni"
	})).Return(nil).Once()
	repo.On("GetByID", uint(5)).Return(&models.Sicil{ID: 5, OrganizationID: 1, HesapAdi: "Yeni"}, nil).Once()

	updated, err := svc.Update(memberClaims(1), 5, map[string]any{
		"hesap_adi":       "Yeni",
		"organization_id": uint(2), // attacker tries to move it to another org
	})
	require.NoError(t, err)
	assert.Equal(t, uint(1), updated.OrganizationID, "organization_id must stay immutable")
}

func TestUpdate_RejectsInvalidSicilTipi(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(5)).Return(&models.Sicil{
		ID: 5, OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
	}, nil)

	_, err := svc.Update(memberClaims(1), 5, map[string]any{"sicil_tipi": "XYZ"})
	require.ErrorIs(t, err, apierror.InvalidSicilTipi)
	repo.AssertNotCalled(t, "UpdateFields", mock.Anything, mock.Anything)
}

func TestUpdate_DuplicateMapsToHesapNoExists(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(5)).Return(&models.Sicil{
		ID: 5, OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
	}, nil).Once()
	repo.On("UpdateFields", uint(5), mock.Anything).Return(errors.New("UNIQUE constraint failed")).Once()

	_, err := svc.Update(memberClaims(1), 5, map[string]any{"hesap_no": "002"})
	require.ErrorIs(t, err, apierror.HesapNoExists)
}

// --- Delete -------------------------------------------------------------

func TestDelete_ForbiddenForOutsideOrg(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(7)).Return(&models.Sicil{ID: 7, OrganizationID: 9}, nil)

	err := svc.Delete(memberClaims(1), 7)
	require.ErrorIs(t, err, apierror.Forbidden)
	repo.AssertNotCalled(t, "Delete", mock.Anything)
}

func TestDelete_AdminBypassesMembership(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(7)).Return(&models.Sicil{ID: 7, OrganizationID: 9}, nil)
	repo.On("Delete", uint(7)).Return(nil)

	require.NoError(t, svc.Delete(adminClaims(), 7))
	repo.AssertCalled(t, "Delete", uint(7))
}

// --- ListForGrid --------------------------------------------------------

func TestListForGrid_ForbiddenForOutsideOrg(t *testing.T) {
	svc, repo := newSicilService()
	_, err := svc.ListForGrid(memberClaims(1), repository.GridRequest{}, 99)
	require.ErrorIs(t, err, apierror.Forbidden)
	repo.AssertNotCalled(t, "ListForGrid", mock.Anything, mock.Anything)
}

// --- Telefonlar ---------------------------------------------------------

func TestCreate_AcceptsTelefonlarList(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("Create", mock.Anything).Return(nil)

	card, err := svc.Create(memberClaims(1), &models.Sicil{
		OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
		Telefonlar: []models.SicilTelefon{
			{Tipi: models.TelefonTipiCep, Numara: "+90 555 111 22 33"},
			{Tipi: models.TelefonTipiIs, Numara: "+90 212 444 55 66"},
		},
	})
	require.NoError(t, err)
	require.Len(t, card.Telefonlar, 2)
}

func TestCreate_RejectsInvalidTelefonTipi(t *testing.T) {
	svc, _ := newSicilService()
	_, err := svc.Create(memberClaims(1), &models.Sicil{
		OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
		Telefonlar: []models.SicilTelefon{{Tipi: "GARIP", Numara: "+90"}},
	})
	require.ErrorIs(t, err, apierror.InvalidTelefonTipi)
}

func TestCreate_RejectsEmptyTelefonNumara(t *testing.T) {
	svc, _ := newSicilService()
	_, err := svc.Create(memberClaims(1), &models.Sicil{
		OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
		Telefonlar: []models.SicilTelefon{{Tipi: models.TelefonTipiCep, Numara: "  "}},
	})
	require.ErrorIs(t, err, apierror.TelefonNumaraRequired)
}

func TestUpdate_TelefonlarReplaceAll(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(5)).Return(&models.Sicil{
		ID: 5, OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
	}, nil).Once()
	repo.On("UpdateFields", uint(5), mock.Anything).Return(nil).Once()
	repo.On("ReplaceTelefonlar", uint(5), mock.MatchedBy(func(ts []models.SicilTelefon) bool {
		return len(ts) == 1 && ts[0].Tipi == models.TelefonTipiEv && ts[0].Numara == "+90 312 000 00 00"
	})).Return(nil).Once()
	repo.On("GetByID", uint(5)).Return(&models.Sicil{
		ID: 5, OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
		Telefonlar: []models.SicilTelefon{{Tipi: models.TelefonTipiEv, Numara: "+90 312 000 00 00"}},
	}, nil).Once()

	updated, err := svc.Update(memberClaims(1), 5, map[string]any{
		"hesap_adi": "X",
		"telefonlar": []any{
			map[string]any{"tipi": "EV", "numara": "+90 312 000 00 00"},
		},
	})
	require.NoError(t, err)
	require.Len(t, updated.Telefonlar, 1)
}

func TestUpdate_NoTelefonlarKey_KeepsExisting(t *testing.T) {
	svc, repo := newSicilService()
	existing := &models.Sicil{
		ID: 5, OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
		Telefonlar: []models.SicilTelefon{{Tipi: models.TelefonTipiCep, Numara: "+90 555 111 22 33"}},
	}
	repo.On("GetByID", uint(5)).Return(existing, nil)
	repo.On("UpdateFields", uint(5), mock.Anything).Return(nil)

	_, err := svc.Update(memberClaims(1), 5, map[string]any{"hesap_adi": "Yeni"})
	require.NoError(t, err)
	repo.AssertNotCalled(t, "ReplaceTelefonlar", mock.Anything, mock.Anything)
}

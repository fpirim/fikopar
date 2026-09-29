package services

import (
	"errors"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
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

// --- Create -------------------------------------------------------------

func TestCreate_RequiresHesapNo(t *testing.T) {
	svc, _ := newSicilService()
	_, err := svc.Create(1, &models.Sicil{
		HesapNo: "  ", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
	})
	require.ErrorIs(t, err, apierror.HesapNoRequired)
}

func TestCreate_RequiresHesapAdi(t *testing.T) {
	svc, _ := newSicilService()
	_, err := svc.Create(1, &models.Sicil{
		HesapNo: "001", HesapAdi: "", SicilTipi: models.SicilTipiFirma,
	})
	require.ErrorIs(t, err, apierror.HesapAdiRequired)
}

func TestCreate_RejectsInvalidSicilTipi(t *testing.T) {
	svc, _ := newSicilService()
	_, err := svc.Create(1, &models.Sicil{
		HesapNo: "001", HesapAdi: "X", SicilTipi: "GARIP",
	})
	require.ErrorIs(t, err, apierror.InvalidSicilTipi)
}

func TestCreate_RejectsIskontoOutOfRange(t *testing.T) {
	svc, _ := newSicilService()
	_, err := svc.Create(1, &models.Sicil{
		HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma, Iskonto: 150,
	})
	require.ErrorIs(t, err, apierror.InvalidIskonto)
}

func TestCreate_DefaultsSicilTipiToFirma(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("Create", mock.Anything).Return(nil)

	card, err := svc.Create(1, &models.Sicil{
		HesapNo: "001", HesapAdi: "X",
	})
	require.NoError(t, err)
	assert.Equal(t, models.SicilTipiFirma, card.SicilTipi)
}

func TestCreate_DuplicateMapsToHesapNoExists(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("Create", mock.Anything).Return(errors.New("duplicate key value"))

	_, err := svc.Create(1, &models.Sicil{
		HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
	})
	require.ErrorIs(t, err, apierror.HesapNoExists)
}

// --- Update -------------------------------------------------------------

func TestUpdate_NotFound(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(1), uint(5)).Return(nil, gorm.ErrRecordNotFound)

	_, err := svc.Update(1, 5, map[string]any{"hesap_adi": "Y"})
	require.ErrorIs(t, err, apierror.SicilNotFound)
}

func TestUpdate_IgnoresOrganizationIDChange(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(1), uint(5)).Return(&models.Sicil{
		ID: 5, OrganizationID: 1, HesapNo: "001", HesapAdi: "Eski", SicilTipi: models.SicilTipiFirma,
	}, nil).Once()
	repo.On("UpdateFields", uint(1), uint(5), mock.MatchedBy(func(fields map[string]any) bool {
		_, hasOrg := fields["organization_id"]
		return !hasOrg && fields["hesap_adi"] == "Yeni"
	})).Return(nil).Once()
	repo.On("GetByID", uint(1), uint(5)).Return(&models.Sicil{ID: 5, OrganizationID: 1, HesapAdi: "Yeni"}, nil).Once()

	updated, err := svc.Update(1, 5, map[string]any{
		"hesap_adi":       "Yeni",
		"organization_id": uint(2), // attacker tries to move it to another org
	})
	require.NoError(t, err)
	assert.Equal(t, uint(1), updated.OrganizationID, "organization_id must stay immutable")
}

func TestUpdate_RejectsInvalidSicilTipi(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(1), uint(5)).Return(&models.Sicil{
		ID: 5, OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
	}, nil)

	_, err := svc.Update(1, 5, map[string]any{"sicil_tipi": "XYZ"})
	require.ErrorIs(t, err, apierror.InvalidSicilTipi)
	repo.AssertNotCalled(t, "UpdateFields", mock.Anything, mock.Anything, mock.Anything)
}

func TestUpdate_DuplicateMapsToHesapNoExists(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(1), uint(5)).Return(&models.Sicil{
		ID: 5, OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
	}, nil).Once()
	repo.On("UpdateFields", uint(1), uint(5), mock.Anything).Return(errors.New("UNIQUE constraint failed")).Once()

	_, err := svc.Update(1, 5, map[string]any{"hesap_no": "002"})
	require.ErrorIs(t, err, apierror.HesapNoExists)
}

// --- Delete -------------------------------------------------------------

func TestDelete_NotFoundInOrg(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("Delete", uint(1), uint(7)).Return(gorm.ErrRecordNotFound)

	require.ErrorIs(t, svc.Delete(1, 7), apierror.SicilNotFound)
}

func TestDelete_ReferencedCardMapsToHasPostings(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("Delete", uint(1), uint(7)).Return(errors.New(`ERROR: update or delete on table "sicils" violates foreign key constraint (SQLSTATE 23503)`))

	require.ErrorIs(t, svc.Delete(1, 7), apierror.SicilHasPostings)
}

func TestGetByID_NotFoundInOrg(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(1), uint(7)).Return(nil, gorm.ErrRecordNotFound)

	_, err := svc.GetByID(1, 7)
	require.ErrorIs(t, err, apierror.SicilNotFound)
}

// --- Telefonlar ---------------------------------------------------------

func TestCreate_AcceptsTelefonlarList(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("Create", mock.Anything).Return(nil)

	card, err := svc.Create(1, &models.Sicil{
		HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
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
	_, err := svc.Create(1, &models.Sicil{
		HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
		Telefonlar: []models.SicilTelefon{{Tipi: "GARIP", Numara: "+90"}},
	})
	require.ErrorIs(t, err, apierror.InvalidTelefonTipi)
}

func TestCreate_RejectsEmptyTelefonNumara(t *testing.T) {
	svc, _ := newSicilService()
	_, err := svc.Create(1, &models.Sicil{
		HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
		Telefonlar: []models.SicilTelefon{{Tipi: models.TelefonTipiCep, Numara: "  "}},
	})
	require.ErrorIs(t, err, apierror.TelefonNumaraRequired)
}

func TestUpdate_TelefonlarReplaceAll(t *testing.T) {
	svc, repo := newSicilService()
	repo.On("GetByID", uint(1), uint(5)).Return(&models.Sicil{
		ID: 5, OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
	}, nil).Once()
	repo.On("UpdateFields", uint(1), uint(5), mock.Anything).Return(nil).Once()
	repo.On("ReplaceTelefonlar", uint(5), mock.MatchedBy(func(ts []models.SicilTelefon) bool {
		return len(ts) == 1 && ts[0].Tipi == models.TelefonTipiEv && ts[0].Numara == "+90 312 000 00 00"
	})).Return(nil).Once()
	repo.On("GetByID", uint(1), uint(5)).Return(&models.Sicil{
		ID: 5, OrganizationID: 1, HesapNo: "001", HesapAdi: "X", SicilTipi: models.SicilTipiFirma,
		Telefonlar: []models.SicilTelefon{{Tipi: models.TelefonTipiEv, Numara: "+90 312 000 00 00"}},
	}, nil).Once()

	updated, err := svc.Update(1, 5, map[string]any{
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
	repo.On("GetByID", uint(1), uint(5)).Return(existing, nil)
	repo.On("UpdateFields", uint(1), uint(5), mock.Anything).Return(nil)

	_, err := svc.Update(1, 5, map[string]any{"hesap_adi": "Yeni"})
	require.NoError(t, err)
	repo.AssertNotCalled(t, "ReplaceTelefonlar", mock.Anything, mock.Anything)
}

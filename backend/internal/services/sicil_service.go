package services

import (
	"errors"
	"strings"

	"fikopar/backend/internal/apierror"
	"fikopar/backend/internal/models"
	"fikopar/backend/internal/repository"

	"gorm.io/gorm"
)

type SicilService struct {
	repo repository.SicilRepository
}

func NewSicilService(repo repository.SicilRepository) *SicilService {
	return &SicilService{repo: repo}
}

// AssertCanAccessOrg allows admins everywhere and users only in their JWT's organizations.
func AssertCanAccessOrg(claims *Claims, organizationID uint) error {
	if claims == nil {
		return apierror.Unauthorized
	}
	if claims.Role == string(models.RoleAdmin) {
		return nil
	}
	for _, id := range claims.OrganizationIDs {
		if id == organizationID {
			return nil
		}
	}
	return apierror.Forbidden
}

func validateSicil(card *models.Sicil) error {
	card.HesapNo = strings.TrimSpace(card.HesapNo)
	card.HesapAdi = strings.TrimSpace(card.HesapAdi)
	if card.HesapNo == "" {
		return apierror.HesapNoRequired
	}
	if card.HesapAdi == "" {
		return apierror.HesapAdiRequired
	}
	if card.SicilTipi == "" {
		card.SicilTipi = models.SicilTipiFirma
	}
	if !models.ValidSicilTipi(card.SicilTipi) {
		return apierror.InvalidSicilTipi
	}
	if card.Iskonto < 0 || card.Iskonto > 100 {
		return apierror.InvalidIskonto
	}
	return validateTelefonlar(card.Telefonlar)
}

func validateTelefonlar(ts []models.SicilTelefon) error {
	for i := range ts {
		ts[i].Numara = strings.TrimSpace(ts[i].Numara)
		if ts[i].Tipi == "" {
			ts[i].Tipi = models.TelefonTipiCep
		}
		if !models.ValidTelefonTipi(ts[i].Tipi) {
			return apierror.InvalidTelefonTipi
		}
		if ts[i].Numara == "" {
			return apierror.TelefonNumaraRequired
		}
	}
	return nil
}

func (s *SicilService) Create(claims *Claims, card *models.Sicil) (*models.Sicil, error) {
	if err := AssertCanAccessOrg(claims, card.OrganizationID); err != nil {
		return nil, err
	}
	if err := validateSicil(card); err != nil {
		return nil, err
	}
	card.ID = 0
	card.Organization = nil
	if err := s.repo.Create(card); err != nil {
		if isDuplicate(err) {
			return nil, apierror.HesapNoExists
		}
		return nil, err
	}
	return card, nil
}

func (s *SicilService) GetByID(claims *Claims, id uint) (*models.Sicil, error) {
	card, err := s.repo.GetByID(id)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, apierror.SicilNotFound
		}
		return nil, err
	}
	if err := AssertCanAccessOrg(claims, card.OrganizationID); err != nil {
		return nil, err
	}
	return card, nil
}

// organization_id is deliberately absent: moving a card to another org would bypass authorization.
var allowedSicilUpdateFields = map[string]bool{
	"hesap_no":   true,
	"hesap_adi":  true,
	"adres":      true,
	"ilce":       true,
	"sehir":      true,
	"v_dairesi":  true,
	"v_no":       true,
	"tc_no":      true,
	"gun":        true,
	"iskonto":    true,
	"sicil_tipi": true,
}

func (s *SicilService) Update(claims *Claims, id uint, updates map[string]any) (*models.Sicil, error) {
	card, err := s.repo.GetByID(id)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, apierror.SicilNotFound
		}
		return nil, err
	}
	if err := AssertCanAccessOrg(claims, card.OrganizationID); err != nil {
		return nil, err
	}

	clean := filterSicilUpdates(updates)
	telefonlar, hasTelefonlar, err := validateSicilUpdate(card, clean, updates)
	if err != nil {
		return nil, err
	}

	if err := s.repo.UpdateFields(id, clean); err != nil {
		if isDuplicate(err) {
			return nil, apierror.HesapNoExists
		}
		return nil, err
	}
	if hasTelefonlar {
		if err := s.repo.ReplaceTelefonlar(id, telefonlar); err != nil {
			return nil, err
		}
	}
	return s.repo.GetByID(id)
}

func filterSicilUpdates(updates map[string]any) map[string]any {
	clean := make(map[string]any, len(updates))
	for k, v := range updates {
		if allowedSicilUpdateFields[k] {
			clean[k] = v
		}
	}
	return clean
}

// Validates on a copy of the card and writes trimmed HesapNo/HesapAdi back into clean.
func validateSicilUpdate(card *models.Sicil, clean, updates map[string]any) ([]models.SicilTelefon, bool, error) {
	preview := *card
	applySicilUpdates(&preview, clean)
	telefonlar, hasTelefonlar, err := parseTelefonlarUpdate(updates)
	if err != nil {
		return nil, false, err
	}
	if hasTelefonlar {
		preview.Telefonlar = telefonlar
	}
	if err := validateSicil(&preview); err != nil {
		return nil, false, err
	}
	if _, ok := clean["hesap_no"]; ok {
		clean["hesap_no"] = preview.HesapNo
	}
	if _, ok := clean["hesap_adi"]; ok {
		clean["hesap_adi"] = preview.HesapAdi
	}
	return telefonlar, hasTelefonlar, nil
}

// An absent "telefonlar" key leaves the phone list untouched.
func parseTelefonlarUpdate(updates map[string]any) ([]models.SicilTelefon, bool, error) {
	raw, ok := updates["telefonlar"]
	if !ok {
		return nil, false, nil
	}
	arr, ok := raw.([]any)
	if !ok {
		return nil, true, apierror.InvalidRequest
	}
	out := make([]models.SicilTelefon, 0, len(arr))
	for _, it := range arr {
		obj, ok := it.(map[string]any)
		if !ok {
			return nil, true, apierror.InvalidRequest
		}
		t := models.SicilTelefon{}
		if tipi, ok := obj["tipi"].(string); ok {
			t.Tipi = models.TelefonTipi(tipi)
		}
		if num, ok := obj["numara"].(string); ok {
			t.Numara = num
		}
		out = append(out, t)
	}
	return out, true, nil
}

func applySicilUpdates(card *models.Sicil, updates map[string]any) {
	if v, ok := updates["hesap_no"].(string); ok {
		card.HesapNo = v
	}
	if v, ok := updates["hesap_adi"].(string); ok {
		card.HesapAdi = v
	}
	if v, ok := updates["sicil_tipi"].(string); ok {
		card.SicilTipi = models.SicilTipi(v)
	}
	if v, ok := updates["iskonto"].(float64); ok {
		card.Iskonto = v
	}
}

func (s *SicilService) Delete(claims *Claims, id uint) error {
	card, err := s.repo.GetByID(id)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return apierror.SicilNotFound
		}
		return err
	}
	if err := AssertCanAccessOrg(claims, card.OrganizationID); err != nil {
		return err
	}
	return s.repo.Delete(id)
}

func (s *SicilService) ListForGrid(claims *Claims, req repository.GridRequest, organizationID uint) (*repository.GridResponse, error) {
	if err := AssertCanAccessOrg(claims, organizationID); err != nil {
		return nil, err
	}
	return s.repo.ListForGrid(req, organizationID)
}

package services

import (
	"encoding/json"
	"errors"
	"regexp"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"

	"gorm.io/gorm"
)

type SicilService struct {
	repo repository.SicilRepository
	orgs repository.OrganizationRepository
}

func NewSicilService(repo repository.SicilRepository, orgs repository.OrganizationRepository) *SicilService {
	return &SicilService{repo: repo, orgs: orgs}
}

// salesRep checks that the card's salesperson is a member of the company.
func (s *SicilService) salesRep(organizationID uint, userID *uint) error {
	if userID == nil {
		return nil
	}
	ok, err := s.orgs.IsMember(*userID, organizationID)
	if err != nil {
		return err
	}
	if !ok {
		return apierror.UnknownRef(apierror.UserNotFound, "sales_rep_user_id")
	}
	return nil
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
	if card.Iskonto < 0 || card.Iskonto > 100 || card.Iskonto2 < 0 || card.Iskonto2 > 100 {
		return apierror.InvalidIskonto
	}
	if err := validateCardLists(card); err != nil {
		return err
	}
	return validateTelefonlar(card.Telefonlar)
}

var emailAddr = regexp.MustCompile(`^[^@\s]+@[^@\s]+\.[^@\s]+$`)

// validateCardLists trims contacts and emails and defaults them to empty lists.
func validateCardLists(card *models.Sicil) error {
	if card.CreditLimit != nil && *card.CreditLimit < 0 {
		return apierror.WithField(apierror.InvalidRequest, "credit_limit")
	}
	if card.Emails == nil {
		card.Emails = models.StringList{}
	}
	for i, e := range card.Emails {
		card.Emails[i] = strings.TrimSpace(e)
		if !emailAddr.MatchString(card.Emails[i]) {
			return apierror.WithField(apierror.InvalidRequest, "emails")
		}
	}
	if card.Contacts == nil {
		card.Contacts = models.SicilContacts{}
	}
	for i := range card.Contacts {
		c := &card.Contacts[i]
		c.Name, c.Role, c.Phone, c.Email = strings.TrimSpace(c.Name), strings.TrimSpace(c.Role), strings.TrimSpace(c.Phone), strings.TrimSpace(c.Email)
		if c.Name == "" || (c.Email != "" && !emailAddr.MatchString(c.Email)) {
			return apierror.WithField(apierror.InvalidRequest, "contacts")
		}
	}
	return nil
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

// Org membership is checked by middleware before any of these run; every query is scoped by org.
func (s *SicilService) Create(organizationID uint, card *models.Sicil) (*models.Sicil, error) {
	card.OrganizationID = organizationID
	if err := validateSicil(card); err != nil {
		return nil, err
	}
	if err := s.salesRep(organizationID, card.SalesRepUserID); err != nil {
		return nil, err
	}
	card.ID = 0
	card.Version = 1
	card.Organization = nil
	card.SalesRepName = nil
	if err := s.repo.Create(card); err != nil {
		if isDuplicate(err) {
			return nil, apierror.HesapNoExists
		}
		return nil, err
	}
	if card.SalesRepUserID != nil {
		return s.repo.GetByID(organizationID, card.ID)
	}
	return card, nil
}

func (s *SicilService) GetByID(organizationID, id uint) (*models.Sicil, error) {
	card, err := s.repo.GetByID(organizationID, id)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, apierror.SicilNotFound
		}
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
	"iskonto2":   true,
	"sicil_tipi": true,
}

// Update applies a partial update at the version the client read (If-Match).
func (s *SicilService) Update(organizationID, id uint, version int, updates map[string]any) (*models.Sicil, error) {
	card, err := s.GetByID(organizationID, id)
	if err != nil {
		return nil, err
	}
	if card.Version != version {
		return nil, apierror.VersionConflict
	}

	clean := filterSicilUpdates(updates)
	telefonlar, hasTelefonlar, err := validateSicilUpdate(card, clean, updates)
	if err != nil {
		return nil, err
	}
	if raw, ok := updates["sales_rep_user_id"]; ok {
		var rep *uint
		if err := remarshal(raw, &rep); err != nil {
			return nil, apierror.WithField(apierror.InvalidRequest, "sales_rep_user_id")
		}
		if err := s.salesRep(organizationID, rep); err != nil {
			return nil, err
		}
		clean["sales_rep_user_id"] = rep
	}
	var phones *[]models.SicilTelefon
	if hasTelefonlar {
		phones = &telefonlar
	}
	ok, err := s.repo.Update(organizationID, id, version, clean, phones)
	switch {
	case err != nil && isDuplicate(err):
		return nil, apierror.HesapNoExists
	case err != nil:
		return nil, err
	case !ok:
		return nil, apierror.VersionConflict
	}
	return s.repo.GetByID(organizationID, id)
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
	if err := parseCardListUpdates(&preview, updates, clean); err != nil {
		return nil, false, err
	}
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
	if _, ok := clean["contacts"]; ok {
		clean["contacts"] = preview.Contacts
	}
	if _, ok := clean["emails"]; ok {
		clean["emails"] = preview.Emails
	}
	return telefonlar, hasTelefonlar, nil
}

// parseCardListUpdates reads credit_limit, emails and contacts from the raw JSON map into the
// preview card and puts typed values into clean.
func parseCardListUpdates(preview *models.Sicil, updates, clean map[string]any) error {
	if raw, ok := updates["credit_limit"]; ok {
		var limit *models.Money
		if raw != nil {
			str, isStr := raw.(string)
			m, err := models.ParseMoney(str)
			if !isStr || err != nil {
				return apierror.WithField(apierror.InvalidRequest, "credit_limit")
			}
			limit = &m
		}
		preview.CreditLimit = limit
		clean["credit_limit"] = limit
	}
	if raw, ok := updates["emails"]; ok {
		var emails models.StringList
		if err := remarshal(raw, &emails); err != nil {
			return apierror.WithField(apierror.InvalidRequest, "emails")
		}
		preview.Emails = emails
		clean["emails"] = emails
	}
	if raw, ok := updates["contacts"]; ok {
		var contacts models.SicilContacts
		if err := remarshal(raw, &contacts); err != nil {
			return apierror.WithField(apierror.InvalidRequest, "contacts")
		}
		preview.Contacts = contacts
		clean["contacts"] = contacts
	}
	return nil
}

func remarshal(v any, out any) error {
	b, err := json.Marshal(v)
	if err != nil {
		return err
	}
	return json.Unmarshal(b, out)
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
	if v, ok := updates["iskonto2"].(float64); ok {
		card.Iskonto2 = v
	}
}

func (s *SicilService) Delete(organizationID, id uint) error {
	err := s.repo.Delete(organizationID, id)
	switch {
	case errors.Is(err, gorm.ErrRecordNotFound):
		return apierror.SicilNotFound
	case repository.IsForeignKeyViolation(err):
		return apierror.SicilHasPostings
	}
	return err
}

func (s *SicilService) ListForGrid(organizationID uint, req repository.GridRequest) (*repository.GridResponse, error) {
	return s.repo.ListForGrid(req, organizationID)
}

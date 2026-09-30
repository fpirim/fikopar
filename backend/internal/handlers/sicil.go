package handlers

import (
	"net/http"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

type SicilHandler struct {
	svc     *services.SicilService
	exports *ReportHandler
}

func NewSicilHandler(svc *services.SicilService, exports *ReportHandler) *SicilHandler {
	return &SicilHandler{svc: svc, exports: exports}
}

type telefonPayload struct {
	Tipi   models.TelefonTipi `json:"tipi"`
	Numara string             `json:"numara"`
}

type createSicilRequest struct {
	HesapNo        string               `json:"hesap_no"`
	HesapAdi       string               `json:"hesap_adi"`
	Adres          string               `json:"adres"`
	Ilce           string               `json:"ilce"`
	Sehir          string               `json:"sehir"`
	VDairesi       string               `json:"v_dairesi"`
	VNo            string               `json:"v_no"`
	TCNo           string               `json:"tc_no"`
	Gun            string               `json:"gun"`
	Iskonto        float64              `json:"iskonto"`
	Iskonto2       float64              `json:"iskonto2"`
	SicilTipi      models.SicilTipi     `json:"sicil_tipi"`
	Telefonlar     []telefonPayload     `json:"telefonlar"`
	CreditLimit    *models.Money        `json:"credit_limit"`
	Contacts       models.SicilContacts `json:"contacts"`
	Emails         models.StringList    `json:"emails"`
	SalesRepUserID *uint                `json:"sales_rep_user_id"`
}

func (h *SicilHandler) Grid(c echo.Context) error {
	var req repository.GridRequest
	if err := c.Bind(&req); err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	if accepts(c, mimeXLSX) {
		return h.exports.SicilGridXLSX(c, req)
	}
	resp, err := h.svc.ListForGrid(orgID(c), req)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, resp)
}

func (h *SicilHandler) Create(c echo.Context) error {
	var req createSicilRequest
	if err := c.Bind(&req); err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	telefonlar := make([]models.SicilTelefon, 0, len(req.Telefonlar))
	for _, t := range req.Telefonlar {
		telefonlar = append(telefonlar, models.SicilTelefon{Tipi: t.Tipi, Numara: t.Numara})
	}
	card := &models.Sicil{
		HesapNo:        req.HesapNo,
		HesapAdi:       req.HesapAdi,
		Adres:          req.Adres,
		Ilce:           req.Ilce,
		Sehir:          req.Sehir,
		VDairesi:       req.VDairesi,
		VNo:            req.VNo,
		TCNo:           req.TCNo,
		Gun:            req.Gun,
		Iskonto:        req.Iskonto,
		Iskonto2:       req.Iskonto2,
		SicilTipi:      req.SicilTipi,
		Telefonlar:     telefonlar,
		CreditLimit:    req.CreditLimit,
		Contacts:       req.Contacts,
		Emails:         req.Emails,
		SalesRepUserID: req.SalesRepUserID,
	}
	created, err := h.svc.Create(orgID(c), card)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, created.Version)
	return c.JSON(http.StatusCreated, created)
}

func (h *SicilHandler) Get(c echo.Context) error {
	id, err := pathID(c, "sicilId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	card, err := h.svc.GetByID(orgID(c), id)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, card.Version)
	return c.JSON(http.StatusOK, card)
}

func (h *SicilHandler) Update(c echo.Context) error {
	id, err := pathID(c, "sicilId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	var updates map[string]any
	if err := c.Bind(&updates); err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	card, err := h.svc.Update(orgID(c), id, version, updates)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, card.Version)
	return c.JSON(http.StatusOK, card)
}

func (h *SicilHandler) Delete(c echo.Context) error {
	id, err := pathID(c, "sicilId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	if err := h.svc.Delete(orgID(c), id); err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, map[string]string{"code": "OK_SICIL_DELETED"})
}

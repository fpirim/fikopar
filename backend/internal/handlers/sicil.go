package handlers

import (
	"net/http"
	"strconv"

	"fikopar/backend/internal/apierror"
	"fikopar/backend/internal/models"
	"fikopar/backend/internal/repository"
	"fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

type SicilHandler struct {
	svc *services.SicilService
}

func NewSicilHandler(svc *services.SicilService) *SicilHandler {
	return &SicilHandler{svc: svc}
}

type telefonPayload struct {
	Tipi   models.TelefonTipi `json:"tipi"`
	Numara string             `json:"numara"`
}

type createSicilRequest struct {
	OrganizationID uint             `json:"organization_id"`
	HesapNo        string           `json:"hesap_no"`
	HesapAdi       string           `json:"hesap_adi"`
	Adres          string           `json:"adres"`
	Ilce           string           `json:"ilce"`
	Sehir          string           `json:"sehir"`
	VDairesi       string           `json:"v_dairesi"`
	VNo            string           `json:"v_no"`
	TCNo           string           `json:"tc_no"`
	Gun            string           `json:"gun"`
	Iskonto        float64          `json:"iskonto"`
	SicilTipi      models.SicilTipi `json:"sicil_tipi"`
	Telefonlar     []telefonPayload `json:"telefonlar"`
}

type gridSicilRequest struct {
	repository.GridRequest
	OrganizationID uint `json:"organization_id"`
}

func (h *SicilHandler) Grid(c echo.Context) error {
	var req gridSicilRequest
	if err := c.Bind(&req); err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	if req.OrganizationID == 0 {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	claims := getClaims(c)
	resp, err := h.svc.ListForGrid(claims, req.GridRequest, req.OrganizationID)
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
	if req.OrganizationID == 0 {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	claims := getClaims(c)
	telefonlar := make([]models.SicilTelefon, 0, len(req.Telefonlar))
	for _, t := range req.Telefonlar {
		telefonlar = append(telefonlar, models.SicilTelefon{Tipi: t.Tipi, Numara: t.Numara})
	}
	card := &models.Sicil{
		OrganizationID: req.OrganizationID,
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
		SicilTipi:      req.SicilTipi,
		Telefonlar:     telefonlar,
	}
	created, err := h.svc.Create(claims, card)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusCreated, created)
}

func (h *SicilHandler) Get(c echo.Context) error {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return apierror.Respond(c, apierror.InvalidID)
	}
	card, err := h.svc.GetByID(getClaims(c), uint(id))
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, card)
}

func (h *SicilHandler) Update(c echo.Context) error {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return apierror.Respond(c, apierror.InvalidID)
	}
	var updates map[string]any
	if err := c.Bind(&updates); err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	card, err := h.svc.Update(getClaims(c), uint(id), updates)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, card)
}

func (h *SicilHandler) Delete(c echo.Context) error {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return apierror.Respond(c, apierror.InvalidID)
	}
	if err := h.svc.Delete(getClaims(c), uint(id)); err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, map[string]string{"code": "OK_SICIL_DELETED"})
}

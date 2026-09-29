package handlers

import (
	"fikopar/backend/internal/apierror"
	"fikopar/backend/internal/repository"
	"fikopar/backend/internal/services"
	"net/http"
	"path/filepath"
	"strconv"

	"github.com/labstack/echo/v4"
)

type OrganizationHandler struct {
	orgService *services.OrganizationService
}

func NewOrganizationHandler(orgService *services.OrganizationService) *OrganizationHandler {
	return &OrganizationHandler{orgService: orgService}
}

type createOrgRequest struct {
	Name    string  `json:"name"`
	Slug    string  `json:"slug"`
	LogoURL *string `json:"logo_url"`
}

func (h *OrganizationHandler) List(c echo.Context) error {
	orgs, err := h.orgService.GetAll()
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, orgs)
}

func (h *OrganizationHandler) Create(c echo.Context) error {
	var req createOrgRequest
	if err := c.Bind(&req); err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	org, err := h.orgService.Create(req.Name, req.Slug, req.LogoURL)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusCreated, org)
}

func (h *OrganizationHandler) Update(c echo.Context) error {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return apierror.Respond(c, apierror.InvalidID)
	}
	org, err := h.orgService.GetByID(uint(id))
	if err != nil {
		return apierror.Respond(c, apierror.OrgNotFound)
	}
	var req createOrgRequest
	if err := c.Bind(&req); err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	if err := h.orgService.Update(org, req.Name, req.Slug, req.LogoURL); err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, org)
}

func (h *OrganizationHandler) Delete(c echo.Context) error {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return apierror.Respond(c, apierror.InvalidID)
	}
	if err := h.orgService.Delete(uint(id)); err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, map[string]string{"code": "OK_ORG_DELETED"})
}

func (h *OrganizationHandler) Grid(c echo.Context) error {
	var req repository.GridRequest
	if err := c.Bind(&req); err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	resp, err := h.orgService.ListForGrid(req)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, resp)
}

func (h *OrganizationHandler) UploadLogo(c echo.Context) error {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return apierror.Respond(c, apierror.InvalidID)
	}
	org, err := h.orgService.GetByID(uint(id))
	if err != nil {
		return apierror.Respond(c, apierror.OrgNotFound)
	}

	file, err := c.FormFile("logo")
	if err != nil {
		return apierror.Respond(c, apierror.NoFileUploaded)
	}
	if file.Size > maxUploadSize {
		return apierror.Respond(c, apierror.FileTooLarge)
	}

	src, err := file.Open()
	if err != nil {
		return apierror.Respond(c, apierror.FileOpen)
	}
	defer func() { _ = src.Close() }()

	if err := h.orgService.UploadLogo(org, file.Filename, src); err != nil {
		return apierror.Respond(c, err)
	}

	return c.JSON(http.StatusOK, org)
}

func (h *OrganizationHandler) DeleteLogo(c echo.Context) error {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return apierror.Respond(c, apierror.InvalidID)
	}
	org, err := h.orgService.GetByID(uint(id))
	if err != nil {
		return apierror.Respond(c, apierror.OrgNotFound)
	}

	if err := h.orgService.DeleteLogo(org); err != nil {
		return apierror.Respond(c, err)
	}

	return c.JSON(http.StatusOK, org)
}

func (h *OrganizationHandler) GetLogo(c echo.Context) error {
	fileName := filepath.Base(c.Param("filename"))
	return c.File(filepath.Join("uploads/logos", fileName))
}

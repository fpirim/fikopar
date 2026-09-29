package handlers

import (
	"fikopar/backend/internal/apierror"
	"fikopar/backend/internal/models"
	"fikopar/backend/internal/repository"
	"fikopar/backend/internal/services"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

const maxUploadSize = 2 * 1024 * 1024

type AdminHandler struct {
	userService *services.UserService
	userRepo    repository.UserRepository
}

func NewAdminHandler(userService *services.UserService, userRepo repository.UserRepository) *AdminHandler {
	return &AdminHandler{userService: userService, userRepo: userRepo}
}

type createUserRequest struct {
	Email           string      `json:"email"`
	Password        string      `json:"password"`
	Name            string      `json:"name"`
	Role            models.Role `json:"role"`
	AvatarURL       *string     `json:"avatar_url"`
	OrganizationIDs []uint      `json:"organization_ids"`
}

func (h *AdminHandler) ListUsers(c echo.Context) error {
	users, err := h.userRepo.GetAll()
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, users)
}

func (h *AdminHandler) CreateUser(c echo.Context) error {
	var req createUserRequest
	if err := c.Bind(&req); err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	if req.Role == "" {
		req.Role = models.RoleUser
	}
	user, err := h.userService.CreateUser(req.Email, req.Password, req.Name, req.Role, req.OrganizationIDs, req.AvatarURL)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusCreated, user)
}

func (h *AdminHandler) UpdateUser(c echo.Context) error {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return apierror.Respond(c, apierror.InvalidID)
	}
	user, err := h.userRepo.GetByID(uint(id))
	if err != nil {
		return apierror.Respond(c, apierror.UserNotFound)
	}

	var req createUserRequest
	if err := c.Bind(&req); err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}

	updates := map[string]interface{}{
		"name":       req.Name,
		"email":      req.Email,
		"password":   req.Password,
		"role":       string(req.Role),
		"avatar_url": req.AvatarURL,
	}
	if err := h.userService.UpdateUser(user, updates); err != nil {
		return apierror.Respond(c, err)
	}

	if req.OrganizationIDs != nil {
		if err := h.userService.SetUserOrganizations(user.ID, req.OrganizationIDs); err != nil {
			return apierror.Respond(c, err)
		}
	}

	return c.JSON(http.StatusOK, user)
}

func (h *AdminHandler) DeleteUser(c echo.Context) error {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return apierror.Respond(c, apierror.InvalidID)
	}

	claims := getClaims(c)
	if err := h.userService.DeleteUser(uint(id), claims.UserID); err != nil {
		return apierror.Respond(c, err)
	}

	return c.JSON(http.StatusOK, map[string]string{"code": "OK_USER_DELETED"})
}

func (h *AdminHandler) UploadUserAvatar(c echo.Context) error {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return apierror.Respond(c, apierror.InvalidID)
	}
	user, err := h.userRepo.GetByID(uint(id))
	if err != nil {
		return apierror.Respond(c, apierror.UserNotFound)
	}

	file, err := c.FormFile("avatar")
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

	if err := h.userService.UploadAvatar(user, file.Filename, src); err != nil {
		return apierror.Respond(c, err)
	}

	return c.JSON(http.StatusOK, user)
}

func (h *AdminHandler) DeleteUserAvatar(c echo.Context) error {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return apierror.Respond(c, apierror.InvalidID)
	}
	user, err := h.userRepo.GetByID(uint(id))
	if err != nil {
		return apierror.Respond(c, apierror.UserNotFound)
	}

	if err := h.userService.DeleteAvatar(user); err != nil {
		return apierror.Respond(c, err)
	}

	return c.JSON(http.StatusOK, user)
}

func (h *AdminHandler) ListUsersGrid(c echo.Context) error {
	var req repository.GridRequest
	if err := c.Bind(&req); err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	resp, err := h.userService.ListForGrid(req)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, resp)
}

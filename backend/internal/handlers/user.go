package handlers

import (
	"fikopar/backend/internal/apierror"
	"fikopar/backend/internal/repository"
	"fikopar/backend/internal/services"
	"net/http"
	"path/filepath"

	"github.com/labstack/echo/v4"
)

type UserHandler struct {
	userRepo    repository.UserRepository
	userService *services.UserService
	authService *services.AuthService
}

func NewUserHandler(userRepo repository.UserRepository, userService *services.UserService, authService *services.AuthService) *UserHandler {
	return &UserHandler{userRepo: userRepo, userService: userService, authService: authService}
}

func getClaims(c echo.Context) *services.Claims {
	return c.Get("user").(*services.Claims)
}

func (h *UserHandler) Me(c echo.Context) error {
	claims := getClaims(c)
	user, err := h.userRepo.GetByID(claims.UserID)
	if err != nil {
		return apierror.Respond(c, apierror.UserNotFound)
	}
	return c.JSON(http.StatusOK, user)
}

type updateProfileRequest struct {
	Name     string `json:"name"`
	Email    string `json:"email"`
	Password string `json:"password,omitempty"`
}

func (h *UserHandler) UpdateMe(c echo.Context) error {
	claims := getClaims(c)
	user, err := h.userRepo.GetByID(claims.UserID)
	if err != nil {
		return apierror.Respond(c, apierror.UserNotFound)
	}

	var req updateProfileRequest
	if err := c.Bind(&req); err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}

	updates := map[string]interface{}{
		"name":     req.Name,
		"email":    req.Email,
		"password": req.Password,
	}
	if err := h.userService.UpdateUser(user, updates); err != nil {
		return apierror.Respond(c, err)
	}

	return c.JSON(http.StatusOK, user)
}

func (h *UserHandler) UploadAvatar(c echo.Context) error {
	claims := getClaims(c)
	user, err := h.userRepo.GetByID(claims.UserID)
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

func (h *UserHandler) DeleteMyAvatar(c echo.Context) error {
	claims := getClaims(c)
	user, err := h.userRepo.GetByID(claims.UserID)
	if err != nil {
		return apierror.Respond(c, apierror.UserNotFound)
	}
	if err := h.userService.DeleteAvatar(user); err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, user)
}

func (h *UserHandler) GetAvatar(c echo.Context) error {
	fileName := filepath.Base(c.Param("filename"))
	return c.File(filepath.Join("uploads/avatars", fileName))
}

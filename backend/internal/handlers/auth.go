package handlers

import (
	"fikopar/backend/internal/apierror"
	"fikopar/backend/internal/repository"
	"fikopar/backend/internal/services"
	"net/http"

	"github.com/labstack/echo/v4"
)

type AuthHandler struct {
	userRepo    repository.UserRepository
	authService *services.AuthService
}

func NewAuthHandler(userRepo repository.UserRepository, authService *services.AuthService) *AuthHandler {
	return &AuthHandler{userRepo: userRepo, authService: authService}
}

type loginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type authResponse struct {
	Token string `json:"token"`
	User  any    `json:"user"`
}

func (h *AuthHandler) Login(c echo.Context) error {
	var req loginRequest
	if err := c.Bind(&req); err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}

	user, err := h.userRepo.GetByEmail(req.Email)
	if err != nil {
		return apierror.Respond(c, apierror.InvalidCredentials)
	}

	if !h.authService.CheckPassword(user.Password, req.Password) {
		return apierror.Respond(c, apierror.InvalidCredentials)
	}

	token, err := h.authService.GenerateToken(user)
	if err != nil {
		return apierror.Respond(c, apierror.TokenGeneration)
	}

	return c.JSON(http.StatusOK, authResponse{Token: token, User: user})
}

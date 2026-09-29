// Package apierror defines stable error codes (e.g. ERR_SLUG_EXISTS); the frontend owns the text.
package apierror

import (
	"errors"
	"log/slog"
	"net/http"

	"github.com/labstack/echo/v4"
)

// APIError is an error code plus its HTTP status.
type APIError struct {
	Code   string
	Status int
}

func (e *APIError) Error() string { return e.Code }

var (
	InvalidRequest        = &APIError{"ERR_INVALID_REQUEST", http.StatusBadRequest}
	InvalidID             = &APIError{"ERR_INVALID_ID", http.StatusBadRequest}
	UserNotFound          = &APIError{"ERR_USER_NOT_FOUND", http.StatusNotFound}
	OrgNotFound           = &APIError{"ERR_ORG_NOT_FOUND", http.StatusNotFound}
	InvalidCredentials    = &APIError{"ERR_INVALID_CREDENTIALS", http.StatusUnauthorized}
	TokenGeneration       = &APIError{"ERR_TOKEN_GENERATION", http.StatusInternalServerError}
	NoFileUploaded        = &APIError{"ERR_NO_FILE_UPLOADED", http.StatusBadRequest}
	FileTooLarge          = &APIError{"ERR_FILE_TOO_LARGE", http.StatusBadRequest}
	FileOpen              = &APIError{"ERR_FILE_OPEN", http.StatusInternalServerError}
	MissingToken          = &APIError{"ERR_MISSING_TOKEN", http.StatusUnauthorized}
	InvalidTokenFormat    = &APIError{"ERR_INVALID_TOKEN_FORMAT", http.StatusUnauthorized}
	InvalidToken          = &APIError{"ERR_INVALID_TOKEN", http.StatusUnauthorized}
	Unauthorized          = &APIError{"ERR_UNAUTHORIZED", http.StatusUnauthorized}
	Forbidden             = &APIError{"ERR_FORBIDDEN", http.StatusForbidden}
	SlugInvalid           = &APIError{"ERR_SLUG_INVALID", http.StatusBadRequest}
	SlugExists            = &APIError{"ERR_SLUG_EXISTS", http.StatusConflict}
	EmailExists           = &APIError{"ERR_EMAIL_EXISTS", http.StatusConflict}
	CannotDeleteSelf      = &APIError{"ERR_CANNOT_DELETE_SELF", http.StatusBadRequest}
	CannotDeleteLastAdmin = &APIError{"ERR_CANNOT_DELETE_LAST_ADMIN", http.StatusBadRequest}
	HesapNoRequired       = &APIError{"ERR_HESAP_NO_REQUIRED", http.StatusBadRequest}
	HesapAdiRequired      = &APIError{"ERR_HESAP_ADI_REQUIRED", http.StatusBadRequest}
	HesapNoExists         = &APIError{"ERR_HESAP_NO_EXISTS", http.StatusConflict}
	SicilNotFound         = &APIError{"ERR_SICIL_NOT_FOUND", http.StatusNotFound}
	InvalidSicilTipi      = &APIError{"ERR_INVALID_SICIL_TIPI", http.StatusBadRequest}
	InvalidTelefonTipi    = &APIError{"ERR_INVALID_TELEFON_TIPI", http.StatusBadRequest}
	TelefonNumaraRequired = &APIError{"ERR_TELEFON_NUMARA_REQUIRED", http.StatusBadRequest}
	InvalidIskonto        = &APIError{"ERR_INVALID_ISKONTO", http.StatusBadRequest}
	Internal              = &APIError{"ERR_INTERNAL", http.StatusInternalServerError}
)

// Respond maps unexpected errors to ERR_INTERNAL so internals never leak.
func Respond(c echo.Context, err error) error {
	var apiErr *APIError
	if errors.As(err, &apiErr) {
		return c.JSON(apiErr.Status, map[string]string{"code": apiErr.Code})
	}
	slog.Error("unhandled error", "err", err, "path", c.Path())
	return c.JSON(Internal.Status, map[string]string{"code": Internal.Code})
}

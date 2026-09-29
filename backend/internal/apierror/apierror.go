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

	PreconditionRequired     = &APIError{"ERR_PRECONDITION_REQUIRED", http.StatusPreconditionRequired}
	VersionConflict          = &APIError{"ERR_VERSION_CONFLICT", http.StatusPreconditionFailed}
	IdempotencyKeyRequired   = &APIError{"ERR_IDEMPOTENCY_KEY_REQUIRED", http.StatusBadRequest}
	IdempotencyKeyReused     = &APIError{"ERR_IDEMPOTENCY_KEY_REUSED", http.StatusUnprocessableEntity}
	IdempotencyInProgress    = &APIError{"ERR_IDEMPOTENCY_IN_PROGRESS", http.StatusConflict}
	InvalidStateTransition   = &APIError{"ERR_INVALID_STATE_TRANSITION", http.StatusConflict}
	DepotNotFound            = &APIError{"ERR_DEPOT_NOT_FOUND", http.StatusNotFound}
	DepotRequired            = &APIError{"ERR_DEPOT_REQUIRED", http.StatusBadRequest}
	NumberSeriesBackwards    = &APIError{"ERR_NUMBER_SERIES_BACKWARDS", http.StatusUnprocessableEntity}
	AccountNotFound          = &APIError{"ERR_ACCOUNT_NOT_FOUND", http.StatusNotFound}
	AccountCodeExists        = &APIError{"ERR_ACCOUNT_CODE_EXISTS", http.StatusConflict}
	AccountInactive          = &APIError{"ERR_ACCOUNT_INACTIVE", http.StatusUnprocessableEntity}
	CurrencyMismatch         = &APIError{"ERR_CURRENCY_MISMATCH", http.StatusUnprocessableEntity}
	EntryNotFound            = &APIError{"ERR_ENTRY_NOT_FOUND", http.StatusNotFound}
	EntryUnbalanced          = &APIError{"ERR_ENTRY_UNBALANCED", http.StatusUnprocessableEntity}
	EntryTooFewLines         = &APIError{"ERR_ENTRY_TOO_FEW_LINES", http.StatusUnprocessableEntity}
	EntryAlreadyReversed     = &APIError{"ERR_ENTRY_ALREADY_REVERSED", http.StatusConflict}
	EntryOwnedBySource       = &APIError{"ERR_ENTRY_OWNED_BY_SOURCE", http.StatusConflict}
	AlreadyCarriedForward    = &APIError{"ERR_ALREADY_CARRIED_FORWARD", http.StatusConflict}
	InvalidAmount            = &APIError{"ERR_INVALID_AMOUNT", http.StatusBadRequest}
	SicilHasPostings         = &APIError{"ERR_SICIL_HAS_POSTINGS", http.StatusConflict}
	ReceiptNotFound          = &APIError{"ERR_RECEIPT_NOT_FOUND", http.StatusNotFound}
	ReceiptAlreadyVoided     = &APIError{"ERR_RECEIPT_ALREADY_VOIDED", http.StatusConflict}
	ReceiptOwnedByInstrument = &APIError{"ERR_RECEIPT_OWNED_BY_INSTRUMENT", http.StatusConflict}
	BankAccountRequired      = &APIError{"ERR_BANK_ACCOUNT_REQUIRED", http.StatusUnprocessableEntity}
	AccountRoleMissing       = &APIError{"ERR_ACCOUNT_ROLE_MISSING", http.StatusUnprocessableEntity}
	CarryForwardBlocked      = &APIError{"ERR_CARRY_FORWARD_BLOCKED", http.StatusUnprocessableEntity}
	CashVoucherNotFound      = &APIError{"ERR_CASH_VOUCHER_NOT_FOUND", http.StatusNotFound}
	CashVoucherAlreadyVoided = &APIError{"ERR_CASH_VOUCHER_ALREADY_VOIDED", http.StatusConflict}
	// The contract has no ERR_DEPOT_CODE_EXISTS; see docs/review/phase-1-contract-issues.md.
	DepotCodeExists = &APIError{"ERR_INVALID_REQUEST", http.StatusConflict}
)

// Detailed adds the contract's optional detail and field to a code.
type Detailed struct {
	Err    *APIError
	Detail string
	Field  string
}

func (e *Detailed) Error() string { return e.Err.Code + ": " + e.Detail }
func (e *Detailed) Unwrap() error { return e.Err }

func WithDetail(err *APIError, detail string) error { return &Detailed{Err: err, Detail: detail} }
func WithField(err *APIError, field string) error   { return &Detailed{Err: err, Field: field} }

// Respond maps unexpected errors to ERR_INTERNAL so internals never leak.
func Respond(c echo.Context, err error) error {
	var d *Detailed
	if errors.As(err, &d) {
		body := map[string]string{"code": d.Err.Code}
		if d.Detail != "" {
			body["detail"] = d.Detail
		}
		if d.Field != "" {
			body["field"] = d.Field
		}
		return c.JSON(d.Err.Status, body)
	}
	var apiErr *APIError
	if errors.As(err, &apiErr) {
		return c.JSON(apiErr.Status, map[string]string{"code": apiErr.Code})
	}
	slog.Error("unhandled error", "err", err, "path", c.Path())
	return c.JSON(Internal.Status, map[string]string{"code": Internal.Code})
}

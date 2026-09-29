package repository

import (
	"context"
	"errors"
	"strconv"
	"strings"

	"github.com/jackc/pgx/v5/pgconn"
	"gorm.io/gorm"
)

// Store opens transactions for the ledger-side repositories. Q runs queries on either the
// pool or an open transaction; a transaction carried in the context is reused so an outer
// caller (idempotency) can wrap a service call in its own transaction.
type Store struct{ db *gorm.DB }

type Q struct{ db *gorm.DB }

type txKey struct{}

func NewStore(db *gorm.DB) *Store { return &Store{db: db} }

func (s *Store) InTx(ctx context.Context, fn func(q *Q) error) error {
	if tx, ok := ctx.Value(txKey{}).(*gorm.DB); ok {
		return fn(&Q{db: tx})
	}
	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error { return fn(&Q{db: tx}) })
}

func (s *Store) Read(ctx context.Context) *Q {
	if tx, ok := ctx.Value(txKey{}).(*gorm.DB); ok {
		return &Q{db: tx}
	}
	return &Q{db: s.db.WithContext(ctx)}
}

// WithTx returns a context whose InTx calls join q's transaction.
func WithTx(ctx context.Context, q *Q) context.Context { return context.WithValue(ctx, txKey{}, q.db) }

func pgCode(err error) string {
	var pgErr *pgconn.PgError
	if errors.As(err, &pgErr) {
		return pgErr.Code
	}
	return ""
}

func IsUniqueViolation(err error) bool {
	return pgCode(err) == "23505" || (err != nil && strings.Contains(strings.ToLower(err.Error()), "duplicate key"))
}

func IsForeignKeyViolation(err error) bool {
	return pgCode(err) == "23503" || (err != nil && strings.Contains(err.Error(), "SQLSTATE 23503"))
}

func IsNotFound(err error) bool { return errors.Is(err, gorm.ErrRecordNotFound) }

// first scans exactly one row or returns gorm.ErrRecordNotFound.
func first[T any](db *gorm.DB, sql string, args ...any) (*T, error) {
	var rows []T
	if err := db.Raw(sql, args...).Scan(&rows).Error; err != nil {
		return nil, err
	}
	if len(rows) == 0 {
		return nil, gorm.ErrRecordNotFound
	}
	return &rows[0], nil
}

func list[T any](db *gorm.DB, sql string, args ...any) ([]T, error) {
	rows := []T{}
	err := db.Raw(sql, args...).Scan(&rows).Error
	return rows, err
}

func uintText(id uint) string { return strconv.FormatUint(uint64(id), 10) }

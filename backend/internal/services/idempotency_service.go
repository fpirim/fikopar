package services

import (
	"bytes"
	"context"
	"crypto/sha256"
	"encoding/json"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type IdempotencyService struct {
	store *repository.Store
}

func NewIdempotencyService(store *repository.Store) *IdempotencyService {
	return &IdempotencyService{store: store}
}

type IdempotentResult struct {
	Status   int
	Body     []byte
	Replayed bool
}

// Do runs op at most once per (org, key). The key is claimed in its own short transaction so a
// concurrent duplicate sees IN_PROGRESS; op and the stored response then commit together.
func (s *IdempotencyService) Do(ctx context.Context, orgID uint, key, endpoint string, body []byte,
	op func(ctx context.Context) (int, any, error)) (*IdempotentResult, error) {
	h := sha256.New()
	h.Write([]byte(endpoint))
	h.Write([]byte{0})
	h.Write(body)
	hash := h.Sum(nil)

	var (
		claimed bool
		rec     *repository.IdempotencyRecord
	)
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		var err error
		claimed, rec, err = q.ClaimIdempotencyKey(orgID, key, endpoint, hash)
		return err
	})
	if err != nil {
		return nil, err
	}
	if !claimed {
		return replay(rec, endpoint, hash)
	}

	var result IdempotentResult
	err = s.store.InTx(ctx, func(q *repository.Q) error {
		status, resp, err := op(repository.WithTx(ctx, q))
		if err != nil {
			return err
		}
		out, err := json.Marshal(resp)
		if err != nil {
			return err
		}
		result = IdempotentResult{Status: status, Body: out}
		return q.CompleteIdempotencyKey(orgID, key, status, out)
	})
	if err != nil {
		_ = s.store.InTx(context.WithoutCancel(ctx), func(q *repository.Q) error { return q.ReleaseIdempotencyKey(orgID, key) })
		return nil, err
	}
	return &result, nil
}

func replay(rec *repository.IdempotencyRecord, endpoint string, hash []byte) (*IdempotentResult, error) {
	if rec.Status != "COMPLETED" || rec.ResponseCode == nil || rec.ResponseBody == nil {
		return nil, apierror.IdempotencyInProgress
	}
	if rec.Endpoint != endpoint || !bytes.Equal(rec.RequestHash, hash) {
		return nil, apierror.IdempotencyKeyReused
	}
	return &IdempotentResult{Status: *rec.ResponseCode, Body: []byte(*rec.ResponseBody), Replayed: true}, nil
}

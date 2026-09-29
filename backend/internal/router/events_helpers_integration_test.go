//go:build integration

package router_test

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"os"
	"path/filepath"
	"sync"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/santhosh-tekuri/jsonschema/v6"
	"github.com/stretchr/testify/require"
)

const schemaBase = "https://fikopar.fpirim.me/schemas/events/"

// validateEvent checks an envelope against api/events/<type>.schema.json.
func validateEvent(t *testing.T, env []byte) {
	t.Helper()
	dir := filepath.Join("..", "..", "..", "api", "events")
	c := jsonschema.NewCompiler()
	entries, err := os.ReadDir(dir)
	require.NoError(t, err)
	for _, e := range entries {
		raw, err := os.ReadFile(filepath.Join(dir, e.Name()))
		require.NoError(t, err)
		doc, err := jsonschema.UnmarshalJSON(bytes.NewReader(raw))
		require.NoError(t, err)
		require.NoError(t, c.AddResource(schemaBase+e.Name(), doc))
	}
	c.AssertFormat()
	var head struct {
		Type string `json:"type"`
	}
	require.NoError(t, json.Unmarshal(env, &head))
	sch, err := c.Compile(schemaBase + head.Type + ".schema.json")
	require.NoError(t, err)
	inst, err := jsonschema.UnmarshalJSON(bytes.NewReader(env))
	require.NoError(t, err)
	require.NoError(t, sch.Validate(inst), string(env))
}

// recorder captures delivered envelopes; failFirst makes its first delivery fail.
type recorder struct {
	mu        sync.Mutex
	envelopes [][]byte
	failFirst bool
	failed    bool
}

func (r *recorder) Name() string          { return "test-recorder" }
func (r *recorder) Handles(_ string) bool { return true }

func (r *recorder) Handle(_ context.Context, _ *repository.Q, env services.Envelope) error {
	r.mu.Lock()
	defer r.mu.Unlock()
	if r.failFirst && !r.failed {
		r.failed = true
		return errors.New("consumer down")
	}
	b, err := json.Marshal(env)
	if err != nil {
		return err
	}
	r.envelopes = append(r.envelopes, b)
	return nil
}

func (r *recorder) byType(t *testing.T, typ string) [][]byte {
	t.Helper()
	r.mu.Lock()
	defer r.mu.Unlock()
	var out [][]byte
	for _, e := range r.envelopes {
		var h struct {
			Type string `json:"type"`
		}
		require.NoError(t, json.Unmarshal(e, &h))
		if h.Type == typ {
			out = append(out, e)
		}
	}
	return out
}

func (a *testApp) relay(t *testing.T) int {
	t.Helper()
	n, err := a.app.Relay.RunOnce(context.Background())
	require.NoError(t, err)
	return n
}

func decodeBytes[T any](t *testing.T, b []byte) T {
	t.Helper()
	var v T
	require.NoError(t, json.Unmarshal(b, &v), string(b))
	return v
}

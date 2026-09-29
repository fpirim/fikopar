#!/bin/sh
# Applies the drafts to a throwaway postgres:16 and runs the invariant checks.
set -e
cd "$(dirname "$0")/.."
docker rm -f fikopar-ddl >/dev/null 2>&1 || true
docker run -d --name fikopar-ddl -e POSTGRES_PASSWORD=x postgres:16 >/dev/null
trap 'docker rm -f fikopar-ddl >/dev/null' EXIT
until docker exec fikopar-ddl pg_isready -U postgres >/dev/null 2>&1; do sleep 1; done
sleep 1
cat check/prelude.sql 000*.sql check/invariants.sql |
  docker exec -i fikopar-ddl psql -U postgres -v ON_ERROR_STOP=1 -q -t

#!/bin/sh
# Applies the promoted migrations (../../0*.sql), then the draft still to be promoted (0006_migration), to a
# throwaway postgres:16 and runs the invariant checks. MUTATE=M1..M4 applies one mutation of ../0012_documents
# (check/mutations.sh).
set -e
cd "$(dirname "$0")/.."
name=fikopar-ddl-$$
docker run -d --name "$name" -e POSTGRES_PASSWORD=x postgres:16 >/dev/null
trap 'docker rm -f "$name" >/dev/null' EXIT
until docker exec "$name" pg_isready -U postgres >/dev/null 2>&1; do sleep 1; done
sleep 1
case "${MUTATE:-}" in
  '') docs=$(cat ../0012_documents.sql) ;;
  M1) docs=$(grep -v -- '-- M1$' ../0012_documents.sql) ;;
  M2) docs=$(sed 's/AND ol.qty < (SELECT/AND false AND ol.qty < (SELECT/' ../0012_documents.sql) ;;
  M3) docs=$(sed 's/AND l.reserved + NEW.qty <= l.on_hand;/;/' ../0012_documents.sql) ;;
  M4) docs=$(sed 's/IF o.net_total < (SELECT/IF false AND o.net_total < (SELECT/' ../0012_documents.sql) ;;
  *) echo "unknown mutation $MUTATE" >&2; exit 2 ;;
esac
{ cat check/prelude.sql
  for f in ../0*.sql; do
    if [ "$f" = ../0012_documents.sql ]; then printf '%s\n' "$docs"; else cat "$f"; fi
  done
  cat 0006_migration.sql check/invariants.sql check/documents.sql; } |
  docker exec -i "$name" psql -U postgres -v ON_ERROR_STOP=1 -q -t

#!/bin/sh
# Each mutation removes one guard from 0004_documents; the checks must fail. Then the unmutated run must pass.
cd "$(dirname "$0")"
status=0
for m in M1 M2 M3 M4; do
  if MUTATE=$m ./run.sh >${TMPDIR:-/tmp}/fikopar-mutation-$m.log 2>&1; then
    echo "$m survived"; status=1
  else
    echo "$m killed: $(grep -m1 -o 'missed: [^(]*' ${TMPDIR:-/tmp}/fikopar-mutation-$m.log || tail -1 ${TMPDIR:-/tmp}/fikopar-mutation-$m.log)"
  fi
done
./run.sh >/dev/null && echo "unmutated: ok" || status=1
exit $status

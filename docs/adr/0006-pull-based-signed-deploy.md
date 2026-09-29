# 6. Signed, digest-pinned deploy hook

Status: Proposed · 2026-09-29

## Context
The repository is public and CI runs on GitHub-hosted runners. Giving the workflow SSH keys or network
access to the server would put production credentials one malicious PR away.

## Decision
The release workflow builds and pushes `ghcr.io/fpirim/fikopar`, then POSTs `{image, digest, version}`
to a small hook on the server with an `HMAC-SHA256` signature over the body and a timestamp. The hook
checks the signature and freshness, accepts only `ghcr.io/fpirim/fikopar@sha256:…`, and updates the
service to that exact digest. The workflow then polls `/healthz` until it reports the new version.

## Consequences
CI holds only one secret that can do nothing but request a known image by digest. Tags cannot be
swapped under a running deploy. The hook is one more small service to maintain, and a failed smoke
check needs a manual (or scripted) rollback to the previous digest.

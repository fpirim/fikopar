# Monitoring

Grafana dashboard, Prometheus scrape config and alert rules for the backend's
`/metrics` endpoint (echoprometheus, `subsystem=fikopar`, plus Go/process collectors).

| File | Use |
|---|---|
| `prometheus-fikopar.yml` | scrape job to merge into `prometheus.yml` |
| `fikopar-dashboard.json` | Grafana dashboard (import or file provisioning) |
| `fikopar-alert-rules.yml` | Prometheus rules (`rule_files`, needs Alertmanager for delivery) |
| `grafana-provisioning-fikopar-alerts.yml` | the same alerts as Grafana-managed rules |

Wire-up order: add the scrape job and confirm the `fikopar` target is UP, then
import the dashboard, then load one of the two alert files.

Dashboard variables: `datasource`, `job` (default `fikopar`), `instance`, `env`,
and optional `loki` / `loki_selector` for the error-log row.

## Alerts

All rules match `env="prod"`; thresholds are starting points.

| Alert | Condition | for | Severity |
|---|---|---|---|
| FikoparDown | `up == 0` | 2m | critical |
| FikoparTargetMissing | no prod target | 5m | critical |
| FikoparHighErrorRate | 5xx > 5% | 5m | warning |
| FikoparCriticalErrorRate | 5xx > 20% | 5m | critical |
| FikoparHighLatencyP95 | p95 > 1s | 10m | warning |
| FikoparHighMemory | RSS > 400MiB | 15m | warning |
| FikoparGoroutineLeak | goroutines > 1000 | 15m | warning |

## Metrics

- `fikopar_requests_total{code,method,host,url}`
- `fikopar_request_duration_seconds_*`, `fikopar_request_size_bytes_*`, `fikopar_response_size_bytes_*`
- Go and process collectors (`go_goroutines`, `process_resident_memory_bytes`, ...)

`url` is the route template (e.g. `/api/admin/users/:id`), so cardinality stays bounded.

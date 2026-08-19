#!/usr/bin/env bash
set -euo pipefail

sed -i 's/^RSGZGL_OPS_ADMIN_RESET_PASSWORD=.*/RSGZGL_OPS_ADMIN_RESET_PASSWORD=false/' /opt/rsgzgl-ops/app.env

USER=$(grep '^USERNAME=' /opt/rsgzgl-ops/credentials.txt | cut -d= -f2-)
PASS=$(grep '^PASSWORD=' /opt/rsgzgl-ops/credentials.txt | cut -d= -f2-)
rm -f /tmp/ops-cj.txt
curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt -X POST http://127.0.0.1:18090/login \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode "username=${USER}" \
  --data-urlencode "password=${PASS}" \
  -o /tmp/ops-login.html -w 'login:%{http_code}\n'

add_target() {
  local name="$1"
  local url="$2"
  curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt -X POST http://127.0.0.1:18090/api/monitor/targets \
    -H 'Content-Type: application/json' \
    -d "{\"name\":\"${name}\",\"url\":\"${url}\",\"timeoutMs\":5000,\"enabled\":true}"
  echo
}

add_target 'demo 租户' 'http://127.0.0.1:18081/actuator/health'
add_target 'pq 租户' 'http://127.0.0.1:18082/actuator/health'
curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt -X POST http://127.0.0.1:18090/api/monitor/collect >/dev/null
curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt http://127.0.0.1:18090/api/monitor/overview -o /tmp/ops-overview.json
python3 - <<'PY'
import json
data=json.load(open("/tmp/ops-overview.json", encoding="utf-8"))
latest=data.get("latest") or {}
print("overall:", latest.get("overall"))
print("host:", latest.get("hostname"))
probes={p.get("targetId"): p for p in (latest.get("probes") or [])}
print("targets:")
for t in data.get("targets") or []:
    p=probes.get(t.get("id")) or {}
    print(" -", t.get("name"), t.get("url"), p.get("status"), p.get("message"), str(p.get("latencyMs")) + "ms")
PY
echo "=== public local ==="
curl -s -o /dev/null -w 'local_health:%{http_code}\n' http://127.0.0.1:18090/actuator/health
ss -lntp | grep 18090 || true
echo "=== credentials ==="
cat /opt/rsgzgl-ops/credentials.txt

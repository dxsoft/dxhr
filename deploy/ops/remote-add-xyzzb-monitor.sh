#!/usr/bin/env bash
set -euo pipefail

ENV=/opt/rsgzgl-ops/app.env

update_env() {
  local key="$1"
  local value="$2"
  if grep -q "^${key}=" "$ENV"; then
    sed -i "s|^${key}=.*|${key}=${value}|" "$ENV"
  else
    echo "${key}=${value}" >> "$ENV"
  fi
}

update_env RSGZGL_OPS_MONITOR_UNITS "rsgzgl,rsgzgl@demo,rsgzgl@pq,rsgzgl@xyzzb"
update_env RSGZGL_OPS_MONITOR_CERT_HOSTS "renshi.dxsoft.cn,pq.dxsoft.cn,shpr.dxsoft.cn,xyzzb.dxsoft.cn"

systemctl restart rsgzgl-ops
for i in $(seq 1 25); do
  code=$(curl -s -o /tmp/ops-h.json -w '%{http_code}' http://127.0.0.1:18090/actuator/health || echo 000)
  echo "ops try${i}:${code}"
  if [[ "$code" == "200" ]]; then
    break
  fi
  sleep 2
done

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

add_target 'xyzzb 租户' 'http://127.0.0.1:18083/actuator/health'
curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt -X POST http://127.0.0.1:18090/api/monitor/collect >/dev/null
curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt http://127.0.0.1:18090/api/monitor/overview -o /tmp/ops-overview.json
python3 - <<'PY'
import json
data=json.load(open("/tmp/ops-overview.json", encoding="utf-8"))
latest=data.get("latest") or {}
print("overall:", latest.get("overall"))
print("services:")
for s in latest.get("services") or []:
    print(" -", s.get("name"), s.get("status"), s.get("message"))
print("targets:")
probes={p.get("targetId"): p for p in (latest.get("probes") or [])}
for t in data.get("targets") or []:
    p=probes.get(t.get("id")) or {}
    print(" -", t.get("name"), t.get("url"), p.get("status"), p.get("message"))
print("runtimes:")
for r in latest.get("runtimes") or []:
    print(" -", r.get("name"), r.get("status"), r.get("dbStatus"), r.get("message"))
print("certs:")
for c in latest.get("certs") or []:
    print(" -", c.get("host"), c.get("status"), c.get("daysRemaining"))
PY
grep MONITOR "$ENV"

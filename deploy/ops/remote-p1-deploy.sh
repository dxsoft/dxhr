#!/usr/bin/env bash
set -euo pipefail

APP=/opt/rsgzgl
OPS=/opt/rsgzgl-ops
STAMP=$(date +%Y%m%d%H%M%S)

echo "==> backup and install rsgzgl jar"
cp -a "$APP/app.jar" "$APP/app.jar.bak.$STAMP"
install -o rsgzgl -g rsgzgl -m 644 /tmp/rsgzgl-app-new.jar "$APP/app.jar"

echo "==> restart instances"
systemctl restart rsgzgl rsgzgl@demo rsgzgl@pq
wait_health() {
  local port="$1"
  local i code
  for i in $(seq 1 40); do
    code=$(curl -s -o /tmp/h.json -w '%{http_code}' "http://127.0.0.1:${port}/actuator/health" || echo 000)
    echo "health ${port} try${i}:${code}"
    if [ "$code" = "200" ]; then
      return 0
    fi
    sleep 3
  done
  return 1
}
wait_health 8080
wait_health 18081
wait_health 18082

echo "==> runtime loopback"
for port in 8080 18081 18082; do
  echo "--- ${port} ---"
  curl -s -o /tmp/rt.json -w "code:%{http_code}\n" "http://127.0.0.1:${port}/internal/runtime"
  python3 -c "import json; d=json.load(open('/tmp/rt.json')); print(d.get('dbStatus'), 'heap', d.get('heapUsedBytes'), '/', d.get('heapMaxBytes'), 'hikari', d.get('hikariActive'), d.get('hikariMax'), 'tomcat', d.get('tomcatBusy'), d.get('tomcatMax'))"
done

echo "==> backup and install ops jar"
cp -a "$OPS/app.jar" "$OPS/app.jar.bak.$STAMP"
install -o rsgzgl -g rsgzgl -m 644 /tmp/rsgzgl-ops-app-new.jar "$OPS/app.jar"
systemctl restart rsgzgl-ops
for i in $(seq 1 25); do
  code=$(curl -s -o /tmp/ops-h.json -w '%{http_code}' http://127.0.0.1:18090/actuator/health || echo 000)
  echo "ops try${i}:${code}"
  if [ "$code" = "200" ]; then
    break
  fi
  sleep 2
done

USER=$(grep '^USERNAME=' "$OPS/credentials.txt" | cut -d= -f2-)
PASS=$(grep '^PASSWORD=' "$OPS/credentials.txt" | cut -d= -f2-)
rm -f /tmp/ops-cj.txt
curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt -X POST http://127.0.0.1:18090/login \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode "username=${USER}" \
  --data-urlencode "password=${PASS}" -o /tmp/ops-login.html
curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt -X POST http://127.0.0.1:18090/api/monitor/collect >/dev/null
curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt http://127.0.0.1:18090/api/monitor/overview -o /tmp/ops-overview.json
python3 - <<'PY'
import json
data=json.load(open("/tmp/ops-overview.json", encoding="utf-8"))
latest=data.get("latest") or {}
print("overall:", latest.get("overall"))
print("runtimes:")
for r in latest.get("runtimes") or []:
    print(" -", r.get("name"), r.get("status"), r.get("dbStatus"),
          "heap", r.get("heapUsedBytes"), "/", r.get("heapMaxBytes"),
          "pool", r.get("hikariActive"), "/", r.get("hikariMax"),
          "tomcat", r.get("tomcatBusy"), "/", r.get("tomcatMax"),
          r.get("message"))
PY
echo "==> DONE"

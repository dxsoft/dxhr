#!/usr/bin/env bash
set -euo pipefail
APP=/opt/rsgzgl
STAMP=$(date +%Y%m%d%H%M%S)
echo "==> backup and install rsgzgl jar"
cp -a "$APP/app.jar" "$APP/app.jar.bak.$STAMP"
install -o rsgzgl -g rsgzgl -m 644 /tmp/rsgzgl-app-new.jar "$APP/app.jar"
echo "==> restart pq tenant"
systemctl restart rsgzgl@pq
for i in $(seq 1 40); do
  code=$(curl -s -o /tmp/pq-h.json -w '%{http_code}' http://127.0.0.1:18082/actuator/health || echo 000)
  echo "health 18082 try${i}:${code}"
  if [ "$code" = "200" ]; then
    break
  fi
  sleep 3
done
curl -s http://127.0.0.1:18082/actuator/health
echo
curl -s http://127.0.0.1:18082/internal/runtime | python3 -c "import json,sys; d=json.load(sys.stdin); print('db', d.get('dbStatus'), 'heap', d.get('heapUsedBytes'))"
echo "==> DONE pq"

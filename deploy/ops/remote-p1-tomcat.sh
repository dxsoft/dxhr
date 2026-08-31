#!/usr/bin/env bash
set -euo pipefail
APP=/opt/rsgzgl
cp -a "$APP/app.jar" "$APP/app.jar.bak.$(date +%Y%m%d%H%M%S)"
install -o rsgzgl -g rsgzgl -m 644 /tmp/rsgzgl-app-new.jar "$APP/app.jar"
systemctl restart rsgzgl rsgzgl@demo rsgzgl@pq
for port in 8080 18081 18082; do
  for i in $(seq 1 40); do
    code=$(curl -s -o /tmp/h.json -w '%{http_code}' "http://127.0.0.1:${port}/actuator/health" || echo 000)
    if [ "$code" = "200" ]; then
      echo "health ${port} OK"
      break
    fi
    sleep 3
  done
  echo "--- runtime ${port} ---"
  curl -s "http://127.0.0.1:${port}/internal/runtime" | python3 -c "import json,sys; d=json.load(sys.stdin); print('db',d.get('dbStatus'),'heap',d.get('heapUsedBytes'),'tomcat',d.get('tomcatBusy'),d.get('tomcatCurrent'),d.get('tomcatMax'),'hikari',d.get('hikariActive'),d.get('hikariMax'))"
done

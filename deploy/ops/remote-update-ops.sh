#!/usr/bin/env bash
set -euo pipefail
OPS=/opt/rsgzgl-ops
cp -a "$OPS/app.jar" "$OPS/app.jar.bak.$(date +%Y%m%d%H%M%S)"
install -o rsgzgl -g rsgzgl -m 644 /tmp/rsgzgl-ops-app-new.jar "$OPS/app.jar"
systemctl restart rsgzgl-ops
for i in $(seq 1 25); do
  code=$(curl -s -o /tmp/ops-h.json -w '%{http_code}' http://127.0.0.1:18090/actuator/health || echo 000)
  echo "try${i}:${code}"
  if [ "$code" = "200" ]; then
    cat /tmp/ops-h.json
    echo
    break
  fi
  sleep 2
done
systemctl is-active rsgzgl-ops

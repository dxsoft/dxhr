#!/usr/bin/env bash
set -euo pipefail
cp -a /opt/rsgzgl-ops/app.jar "/opt/rsgzgl-ops/app.jar.bak.$(date +%Y%m%d%H%M%S)"
install -o rsgzgl -g rsgzgl -m 644 /tmp/rsgzgl-ops-app-new.jar /opt/rsgzgl-ops/app.jar
if ! grep -q '^RSGZGL_OPS_MONITOR_UNITS=' /opt/rsgzgl-ops/app.env; then
  echo 'RSGZGL_OPS_MONITOR_UNITS=rsgzgl,rsgzgl@demo,rsgzgl@pq' >> /opt/rsgzgl-ops/app.env
fi
if ! grep -q '^RSGZGL_OPS_MONITOR_CERT_HOSTS=' /opt/rsgzgl-ops/app.env; then
  echo 'RSGZGL_OPS_MONITOR_CERT_HOSTS=renshi.dxsoft.cn,pq.dxsoft.cn,shpr.dxsoft.cn' >> /opt/rsgzgl-ops/app.env
fi
systemctl restart rsgzgl-ops
for i in $(seq 1 30); do
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

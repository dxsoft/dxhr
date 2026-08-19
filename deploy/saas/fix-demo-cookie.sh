#!/usr/bin/env bash
set -euo pipefail
sed -i 's/^RSGZGL_SESSION_COOKIE_SECURE=.*/RSGZGL_SESSION_COOKIE_SECURE=false/' /opt/rsgzgl/tenants/demo/app.env
sed -i 's/^RSGZGL_FORWARD_HEADERS_STRATEGY=.*/RSGZGL_FORWARD_HEADERS_STRATEGY=none/' /opt/rsgzgl/tenants/demo/app.env
systemctl restart 'rsgzgl@demo'
for i in $(seq 1 30); do
  if curl -sf -m 3 http://127.0.0.1:18081/actuator/health | grep -q UP; then
    echo health_ok
    break
  fi
  sleep 2
done
PW=$(grep '^ADMIN_PASSWORD=' /opt/rsgzgl/tenants/demo/credentials.txt | cut -d= -f2-)
rm -f /tmp/c.txt /tmp/h.txt
curl -s -c /tmp/c.txt -b /tmp/c.txt -X POST 'http://127.0.0.1:18081/login' \
  --data-urlencode "username=admin" \
  --data-urlencode "password=${PW}" \
  -D /tmp/h.txt -o /dev/null -w "login_http:%{http_code}\n"
echo "--- set-cookie ---"
grep -i set-cookie /tmp/h.txt || true
echo "--- jar ---"
cat /tmp/c.txt || true
echo "--- me ---"
curl -s -b /tmp/c.txt http://127.0.0.1:18081/api/auth/me
echo

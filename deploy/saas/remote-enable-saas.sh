#!/usr/bin/env bash
set -euo pipefail
DEPLOY=/tmp/rsgzgl-saas-deploy
APP=/opt/rsgzgl

echo "==> backup and update jar"
cp -a "$APP/app.jar" "$APP/app.jar.bak.$(date +%Y%m%d%H%M%S)"
cp -f "$DEPLOY/app.jar" "$APP/app.jar"
chown rsgzgl:rsgzgl "$APP/app.jar"

echo "==> install saas scripts"
mkdir -p "$APP/saas"
cp -a "$DEPLOY/saas/." "$APP/saas/"
cp -f "$DEPLOY/gzjsgl.sql" "$APP/saas/gzjsgl.sql"
chmod +x "$APP/saas/provision-tenant.sh"

JAVA_BIN=$(command -v java)
sed "s|__JAVA_BIN__|${JAVA_BIN}|g; s|__APP_ROOT__|/opt/rsgzgl|g" "$APP/saas/rsgzgl@.service" > /etc/systemd/system/rsgzgl@.service

if ! grep -q '^RSGZGL_LICENSE_ISSUE_ENABLED=' "$APP/app.env"; then
  echo 'RSGZGL_LICENSE_ISSUE_ENABLED=false' >> "$APP/app.env"
else
  sed -i 's/^RSGZGL_LICENSE_ISSUE_ENABLED=.*/RSGZGL_LICENSE_ISSUE_ENABLED=false/' "$APP/app.env"
fi
if ! grep -q '^RSGZGL_FORWARD_HEADERS_STRATEGY=' "$APP/app.env"; then
  echo 'RSGZGL_FORWARD_HEADERS_STRATEGY=none' >> "$APP/app.env"
fi
if ! grep -q '^RSGZGL_SESSION_COOKIE_SECURE=' "$APP/app.env"; then
  echo 'RSGZGL_SESSION_COOKIE_SECURE=false' >> "$APP/app.env"
fi

systemctl daemon-reload
systemctl restart rsgzgl
for i in $(seq 1 30); do
  if curl -sf -m 3 http://127.0.0.1:8080/actuator/health | grep -q UP; then
    echo "primary health OK"
    break
  fi
  sleep 2
done
curl -s -m 5 http://127.0.0.1:8080/actuator/health || true
echo

echo "==> provision demo tenant"
DB_PASSWORD=$(grep '^RSGZGL_DB_PASSWORD=' "$APP/app.env" | cut -d= -f2-)
HMAC=$(grep '^RSGZGL_LICENSE_HMAC_SECRET=' "$APP/app.env" | cut -d= -f2- || true)
if [ -z "${HMAC}" ]; then
  HMAC="dxsoft-rsgzgl-license-dev-secret"
  if ! grep -q '^RSGZGL_LICENSE_HMAC_SECRET=' "$APP/app.env"; then
    echo "RSGZGL_LICENSE_HMAC_SECRET=${HMAC}" >> "$APP/app.env"
  fi
fi

export TENANT=demo
export PORT=18081
export MYSQL_HOST=127.0.0.1
export MYSQL_PORT=3307
export DB_ROOT_PASSWORD="$DB_PASSWORD"
export DB_PASSWORD="saas_demo_db_$(openssl rand -hex 4)"
export ADMIN_PASSWORD="DemoAdmin@$(openssl rand -hex 3)"
export HMAC_SECRET="$HMAC"
export SCHEMA_SQL="$APP/saas/gzjsgl.sql"
export APP_ROOT="$APP"

mkdir -p "$APP/tenants"
bash "$APP/saas/provision-tenant.sh"
{
  echo "PORT=${PORT}"
  echo "ADMIN_USERNAME=admin"
  echo "ADMIN_PASSWORD=${ADMIN_PASSWORD}"
  echo "DB_NAME=gzjsgl_demo"
  echo "URL=http://101.201.76.253:18081"
} > "$APP/tenants/demo/credentials.txt"
chmod 600 "$APP/tenants/demo/credentials.txt"
chown -R rsgzgl:rsgzgl "$APP/tenants/demo"

systemctl enable --now "rsgzgl@demo"
for i in $(seq 1 45); do
  if curl -sf -m 3 http://127.0.0.1:18081/actuator/health | grep -q UP; then
    echo "demo health OK"
    break
  fi
  sleep 2
done
systemctl is-active rsgzgl
systemctl is-active "rsgzgl@demo" || systemctl status "rsgzgl@demo" --no-pager -l | head -n 50
curl -s -m 5 http://127.0.0.1:18081/actuator/health || true
echo
echo "==> DONE"
echo "primary: http://101.201.76.253:8080"
echo "demo:    http://101.201.76.253:18081"
echo "demo credentials file: $APP/tenants/demo/credentials.txt"
cat "$APP/tenants/demo/credentials.txt"

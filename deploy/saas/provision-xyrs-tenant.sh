#!/usr/bin/env bash
# 将 xyrs.dxsoft.cn 开通为独立 SaaS 租户（从主库 gzjsgl 复制业务数据）
# 在服务器上执行：sudo bash provision-xyrs-tenant.sh
set -euo pipefail

TENANT=xyrs
PORT=18084
SOURCE_DB=gzjsgl
DB_NAME=gzjsgl_${TENANT}
DB_USER=rsgzgl_${TENANT}
APP_ROOT=/opt/rsgzgl
TENANT_HOME="${APP_ROOT}/tenants/${TENANT}"
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3307
DOMAIN=xyrs.dxsoft.cn
EMAIL=support@dxsoft.cn

die() { echo "ERROR: $*" >&2; exit 1; }
[[ "${EUID}" -eq 0 ]] || die "请使用 root 运行"

DB_ROOT_PASSWORD="${DB_ROOT_PASSWORD:-}"
[[ -n "$DB_ROOT_PASSWORD" ]] || DB_ROOT_PASSWORD="$(grep '^RSGZGL_DB_PASSWORD=' "${APP_ROOT}/app.env" | cut -d= -f2-)"
[[ -n "$DB_ROOT_PASSWORD" ]] || die "请设置 DB_ROOT_PASSWORD 或确保 ${APP_ROOT}/app.env 含 RSGZGL_DB_PASSWORD"

PRIMARY_ENV="${APP_ROOT}/app.env"
[[ -f "$PRIMARY_ENV" ]] || die "缺少 ${PRIMARY_ENV}"
HMAC_SECRET="$(grep '^RSGZGL_LICENSE_HMAC_SECRET=' "$PRIMARY_ENV" | cut -d= -f2-)"
ADMIN_PASSWORD="$(grep '^RSGZGL_ADMIN_PASSWORD=' "$PRIMARY_ENV" | cut -d= -f2-)"
[[ -n "$HMAC_SECRET" && -n "$ADMIN_PASSWORD" ]] || die "无法从主实例读取 HMAC / 管理员密码"

DB_PASSWORD="${DB_PASSWORD:-$(openssl rand -hex 8)}"
[[ -f "${APP_ROOT}/app.jar" ]] || die "缺少 ${APP_ROOT}/app.jar"

echo "==> 创建库 ${DB_NAME} / 用户 ${DB_USER}"
MYSQL_PWD="${DB_ROOT_PASSWORD}" mysql -h "${MYSQL_HOST}" -P "${MYSQL_PORT}" -uroot <<SQL
CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${DB_USER}'@'%' IDENTIFIED BY '${DB_PASSWORD}';
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
ALTER USER '${DB_USER}'@'%' IDENTIFIED BY '${DB_PASSWORD}';
ALTER USER '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'%';
GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;
SQL

echo "==> 从 ${SOURCE_DB} 复制数据到 ${DB_NAME}"
MYSQL_PWD="${DB_ROOT_PASSWORD}" mysqldump -h "${MYSQL_HOST}" -P "${MYSQL_PORT}" -uroot \
  --single-transaction --routines --triggers --set-gtid-purged=OFF "${SOURCE_DB}" \
  | MYSQL_PWD="${DB_ROOT_PASSWORD}" mysql -h "${MYSQL_HOST}" -P "${MYSQL_PORT}" -uroot "${DB_NAME}"

mkdir -p "${TENANT_HOME}/logs"
cat > "${TENANT_HOME}/app.env" <<EOF
PORT=${PORT}
RSGZGL_DB_URL=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_0900_ai_ci&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true
RSGZGL_DB_USERNAME=${DB_USER}
RSGZGL_DB_PASSWORD=${DB_PASSWORD}
RSGZGL_ADMIN_USERNAME=admin
RSGZGL_ADMIN_PASSWORD=${ADMIN_PASSWORD}
RSGZGL_ADMIN_RESET_PASSWORD=false
RSGZGL_ADMIN_DISPLAY_NAME=系统管理员
RSGZGL_SECURITY_INITIALIZE_SCHEMA=true
RSGZGL_LICENSE_ISSUE_ENABLED=false
RSGZGL_LICENSE_HMAC_SECRET=${HMAC_SECRET}
RSGZGL_FORWARD_HEADERS_STRATEGY=framework
RSGZGL_SESSION_COOKIE_SECURE=true
RSGZGL_SESSION_COOKIE_SAME_SITE=lax
RSGZGL_UKEY_ENABLED=true
EOF

cat > "${TENANT_HOME}/credentials.txt" <<EOF
PORT=${PORT}
ADMIN_USERNAME=admin
ADMIN_PASSWORD=${ADMIN_PASSWORD}
DB_NAME=${DB_NAME}
DB_USER=${DB_USER}
DB_PASSWORD=${DB_PASSWORD}
URL=https://${DOMAIN}
EOF

chown -R rsgzgl:rsgzgl "${TENANT_HOME}"
chmod 640 "${TENANT_HOME}/app.env" "${TENANT_HOME}/credentials.txt"

systemctl daemon-reload
systemctl enable "rsgzgl@${TENANT}"
systemctl restart "rsgzgl@${TENANT}"

echo "==> 等待健康检查"
for i in $(seq 1 40); do
  code=$(curl -s -o /tmp/xyrs-h.json -w '%{http_code}' "http://127.0.0.1:${PORT}/actuator/health" || echo 000)
  echo "try${i}:${code}"
  if [[ "$code" == "200" ]]; then
    cat /tmp/xyrs-h.json
    echo
    break
  fi
  sleep 3
done

echo "==> 更新 Nginx ${DOMAIN} -> ${PORT}"
SSL_OPTS=""
if [[ -f /etc/letsencrypt/options-ssl-nginx.conf ]]; then
  SSL_OPTS="include /etc/letsencrypt/options-ssl-nginx.conf;"
fi
SSL_DH=""
if [[ -f /etc/letsencrypt/ssl-dhparams.pem ]]; then
  SSL_DH="ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;"
fi

cat > "/etc/nginx/conf.d/${DOMAIN}.conf" <<EOF
upstream rsgzgl_xyrs {
    server 127.0.0.1:${PORT};
    keepalive 16;
}

server {
    listen 80;
    server_name ${DOMAIN};
    client_max_body_size 512m;
    location / {
        proxy_pass http://rsgzgl_xyrs;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header Connection "";
        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;
    }
}
EOF

nginx -t
systemctl reload nginx

if [[ ! -f "/etc/letsencrypt/live/${DOMAIN}/fullchain.pem" ]]; then
  certbot --nginx -d "${DOMAIN}" --non-interactive --agree-tos -m "${EMAIL}" --redirect || true
fi

if [[ -f "/etc/letsencrypt/live/${DOMAIN}/fullchain.pem" ]]; then
  cat > "/etc/nginx/conf.d/${DOMAIN}.conf" <<EOF
upstream rsgzgl_xyrs {
    server 127.0.0.1:${PORT};
    keepalive 16;
}

server {
    listen 80;
    server_name ${DOMAIN};
    return 301 https://\$host\$request_uri;
}

server {
    listen 443 ssl http2;
    server_name ${DOMAIN};

    ssl_certificate     /etc/letsencrypt/live/${DOMAIN}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/${DOMAIN}/privkey.pem;
    ${SSL_OPTS}
    ${SSL_DH}

    client_max_body_size 512m;

    location / {
        proxy_pass http://rsgzgl_xyrs;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header Connection "";
        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;
    }
}
EOF
  nginx -t
  systemctl reload nginx
fi

curl -sk "https://${DOMAIN}/actuator/health" || true
echo
echo "DONE: https://${DOMAIN}/ -> ${PORT}"
echo "credentials: ${TENANT_HOME}/credentials.txt"

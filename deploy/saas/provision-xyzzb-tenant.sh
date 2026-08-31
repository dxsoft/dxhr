#!/usr/bin/env bash
# 将 xyzzb.dxsoft.cn 开通为独立 SaaS 租户（从 pq 库复制业务数据）
# 在服务器上执行：sudo bash provision-xyzzb-tenant.sh
set -euo pipefail

TENANT=xyzzb
PORT=18083
SOURCE_DB=gzjsgl_pq
DB_NAME=gzjsgl_${TENANT}
DB_USER=rsgzgl_${TENANT}
APP_ROOT=/opt/rsgzgl
TENANT_HOME="${APP_ROOT}/tenants/${TENANT}"
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3307
DOMAIN=xyzzb.dxsoft.cn
EMAIL=support@dxsoft.cn

die() { echo "ERROR: $*" >&2; exit 1; }
[[ "${EUID}" -eq 0 ]] || die "请使用 root 运行"

DB_ROOT_PASSWORD="${DB_ROOT_PASSWORD:-}"
[[ -n "$DB_ROOT_PASSWORD" ]] || DB_ROOT_PASSWORD="$(grep '^RSGZGL_DB_PASSWORD=' "${APP_ROOT}/app.env" | cut -d= -f2-)"
[[ -n "$DB_ROOT_PASSWORD" ]] || die "请设置 DB_ROOT_PASSWORD 或确保 ${APP_ROOT}/app.env 含 RSGZGL_DB_PASSWORD"

PQ_ENV="${APP_ROOT}/tenants/pq/app.env"
[[ -f "$PQ_ENV" ]] || die "缺少 ${PQ_ENV}"
HMAC_SECRET="$(grep '^RSGZGL_LICENSE_HMAC_SECRET=' "$PQ_ENV" | cut -d= -f2-)"
ADMIN_PASSWORD="$(grep '^RSGZGL_ADMIN_PASSWORD=' "$PQ_ENV" | cut -d= -f2-)"
[[ -n "$HMAC_SECRET" && -n "$ADMIN_PASSWORD" ]] || die "无法从 pq 租户读取 HMAC / 管理员密码"

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
RSGZGL_DB_URL=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_0900_ai_ci&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
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
  code=$(curl -s -o /tmp/xyzzb-h.json -w '%{http_code}' "http://127.0.0.1:${PORT}/actuator/health" || echo 000)
  echo "try${i}:${code}"
  if [[ "$code" == "200" ]]; then
    cat /tmp/xyzzb-h.json
    echo
    break
  fi
  sleep 3
done

echo "==> 更新 Nginx ${DOMAIN} -> ${PORT}"
SSL_OPTS=""
[[ -f /etc/letsencrypt/options-ssl-nginx.conf ]] && SSL_OPTS="include /etc/letsencrypt/options-ssl-nginx.conf;"
SSL_DH=""
[[ -f /etc/letsencrypt/ssl-dhparams.pem ]] && SSL_DH="ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;"

if [[ -f "/etc/letsencrypt/live/${DOMAIN}/fullchain.pem" ]]; then
  cat > "/etc/nginx/conf.d/${DOMAIN}.conf" <<EOF
upstream rsgzgl_xyzzb {
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
        proxy_pass http://rsgzgl_xyzzb;
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
else
  cat > "/etc/nginx/conf.d/${DOMAIN}.conf" <<EOF
upstream rsgzgl_xyzzb {
    server 127.0.0.1:${PORT};
    keepalive 16;
}

server {
    listen 80;
    server_name ${DOMAIN};
    client_max_body_size 512m;
    location / {
        proxy_pass http://rsgzgl_xyzzb;
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
  nginx -t && systemctl reload nginx
  if certbot --nginx -d "${DOMAIN}" --non-interactive --agree-tos -m "${EMAIL}" --redirect; then
    cat > "/etc/nginx/conf.d/${DOMAIN}.conf" <<EOF
upstream rsgzgl_xyzzb {
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
        proxy_pass http://rsgzgl_xyzzb;
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
  else
    echo "WARN: certbot failed, HTTP still works"
  fi
fi

nginx -t
systemctl reload nginx

echo "DONE: rsgzgl@${TENANT} on :${PORT}, https://${DOMAIN}/"

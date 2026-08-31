#!/usr/bin/env bash
# 开通一个托管租户目录 + MySQL 库（不启动业务；不覆盖已有 app.env）
# 用法：
#   export TENANT=acme PORT=18081 DB_ROOT_PASSWORD=... DB_PASSWORD=... ADMIN_PASSWORD=... HMAC_SECRET=...
#   sudo -E bash provision-tenant.sh
set -euo pipefail

die() { echo "ERROR: $*" >&2; exit 1; }

[[ "${EUID}" -eq 0 ]] || die "请使用 root: sudo -E bash provision-tenant.sh"

TENANT="${TENANT:-}"
[[ -n "$TENANT" ]] || die "请设置 TENANT（字母数字下划线，如 acme）"
echo "$TENANT" | grep -Eq '^[a-zA-Z0-9_-]+$' || die "TENANT 仅允许字母数字 _ -"

PORT="${PORT:-18081}"
DB_NAME="gzjsgl_${TENANT}"
DB_USER="rsgzgl_${TENANT}"
DB_PASSWORD="${DB_PASSWORD:-}"
DB_ROOT_PASSWORD="${DB_ROOT_PASSWORD:-}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-}"
HMAC_SECRET="${HMAC_SECRET:-}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
APP_ROOT="${APP_ROOT:-/opt/rsgzgl}"
TENANT_HOME="${APP_ROOT}/tenants/${TENANT}"
SHARED_JAR="${APP_ROOT}/app.jar"

[[ -n "$DB_PASSWORD" ]] || die "请设置 DB_PASSWORD"
[[ -n "$ADMIN_PASSWORD" ]] || die "请设置 ADMIN_PASSWORD"
[[ -n "$HMAC_SECRET" ]] || die "请设置 HMAC_SECRET（须与 ops 签发密钥一致）"
[[ -f "$SHARED_JAR" ]] || die "缺少 ${SHARED_JAR}，请先运行 deploy/linux/install.sh"

if ! id -u rsgzgl >/dev/null 2>&1; then
  useradd --system --home "${APP_ROOT}" --shell /usr/sbin/nologin rsgzgl || true
fi

mkdir -p "${TENANT_HOME}/logs"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ ! -f "${TENANT_HOME}/app.env" ]]; then
  {
    echo "PORT=${PORT}"
    echo "RSGZGL_DB_URL=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_0900_ai_ci&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
    echo "RSGZGL_DB_USERNAME=${DB_USER}"
    echo "RSGZGL_DB_PASSWORD=${DB_PASSWORD}"
    echo "RSGZGL_ADMIN_USERNAME=admin"
    echo "RSGZGL_ADMIN_PASSWORD=${ADMIN_PASSWORD}"
    echo "RSGZGL_ADMIN_RESET_PASSWORD=false"
    echo "RSGZGL_ADMIN_DISPLAY_NAME=系统管理员"
    echo "RSGZGL_SECURITY_INITIALIZE_SCHEMA=true"
    echo "RSGZGL_LICENSE_ISSUE_ENABLED=false"
    echo "RSGZGL_LICENSE_HMAC_SECRET=${HMAC_SECRET}"
    echo "RSGZGL_FORWARD_HEADERS_STRATEGY=none"
    echo "RSGZGL_SESSION_COOKIE_SECURE=false"
    echo "RSGZGL_SESSION_COOKIE_SAME_SITE=lax"
    echo "RSGZGL_UKEY_ENABLED=true"
  } > "${TENANT_HOME}/app.env"
  echo "==> 已生成 ${TENANT_HOME}/app.env"
else
  echo "==> 保留已有 ${TENANT_HOME}/app.env"
fi

chown -R rsgzgl:rsgzgl "${TENANT_HOME}"
chmod 640 "${TENANT_HOME}/app.env"

echo "==> 创建 MySQL 库 ${DB_NAME} / 用户 ${DB_USER}"
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

SCHEMA_SQL="${SCHEMA_SQL:-}"
if [[ -z "$SCHEMA_SQL" && -f "${SCRIPT_DIR}/../../gzjsgl.sql" ]]; then
  SCHEMA_SQL="$(cd "${SCRIPT_DIR}/../.." && pwd)/gzjsgl.sql"
fi
if [[ -n "$SCHEMA_SQL" && -f "$SCHEMA_SQL" ]]; then
  echo "==> 导入业务库结构 ${SCHEMA_SQL} -> ${DB_NAME}"
  MYSQL_PWD="${DB_ROOT_PASSWORD}" mysql -h "${MYSQL_HOST}" -P "${MYSQL_PORT}" -uroot --default-character-set=utf8mb4 \
    "${DB_NAME}" < "${SCHEMA_SQL}"
else
  echo "==> 未找到 gzjsgl.sql，请稍后手工导入业务库结构到 ${DB_NAME}"
fi

UNIT_SRC="${SCRIPT_DIR}/rsgzgl@.service"
if [[ -f "$UNIT_SRC" ]]; then
  JAVA_BIN="$(command -v java || true)"
  [[ -n "$JAVA_BIN" ]] || die "未找到 java"
  sed "s|__JAVA_BIN__|${JAVA_BIN}|g; s|__APP_ROOT__|${APP_ROOT}|g" "$UNIT_SRC" \
    > /etc/systemd/system/rsgzgl@.service
  systemctl daemon-reload
  echo "==> 已安装 systemd 模板 rsgzgl@.service"
fi

echo
echo "开通完成。下一步："
echo "  sudo systemctl enable --now rsgzgl@${TENANT}"
echo "  curl -s http://127.0.0.1:${PORT}/actuator/health"
echo "  配置 Nginx 域名 → 127.0.0.1:${PORT}（见 nginx-tenant.conf.example）"
echo "  浏览器导入该客户授权包；可选导入 UKey 绑定包"

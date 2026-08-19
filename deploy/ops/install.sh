#!/usr/bin/env bash
# 在服务器上安装 rsgzgl-ops（监控 + 制锁签发）
# 用法：把 app.jar、本脚本、rsgzgl-ops.service、app.env.example 放在同一目录后
#   sudo bash install.sh
set -euo pipefail

APP_NAME=rsgzgl-ops
APP_HOME=/opt/rsgzgl-ops
APP_USER=rsgzgl
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

die() { echo "ERROR: $*" >&2; exit 1; }

need_root() {
  if [[ "${EUID}" -ne 0 ]]; then
    die "请使用 root 或 sudo 运行: sudo bash install.sh"
  fi
}

find_java21() {
  if [[ -d /opt/rsgzgl ]]; then
    local from_unit
    from_unit="$(systemctl show -p ExecStart --value rsgzgl 2>/dev/null | awk '{print $1}' || true)"
    if [[ -x "${from_unit}" ]]; then
      echo "${from_unit}"
      return 0
    fi
  fi
  if command -v java >/dev/null 2>&1; then
    local ver
    ver="$(java -version 2>&1 | head -n1 || true)"
    if echo "$ver" | grep -Eq '"?21[\. "]'; then
      command -v java
      return 0
    fi
  fi
  for candidate in /usr/lib/jvm/java-21-openjdk/bin/java \
                   /usr/lib/jvm/java-21-openjdk-amd64/bin/java \
                   /usr/lib/jvm/jre-21/bin/java; do
    if [[ -x "$candidate" ]]; then
      echo "$candidate"
      return 0
    fi
  done
  return 1
}

pick_files() {
  if [[ -f "${SCRIPT_DIR}/app.jar" ]]; then
    JAR_SRC="${SCRIPT_DIR}/app.jar"
  else
    local found
    found="$(ls -1 "${SCRIPT_DIR}"/rsgzgl-ops-*.jar 2>/dev/null | head -n1 || true)"
    [[ -n "$found" ]] || die "未找到 jar，请把 rsgzgl-ops jar 放在 ${SCRIPT_DIR}"
    JAR_SRC="$found"
  fi
  SERVICE_SRC="${SCRIPT_DIR}/rsgzgl-ops.service"
  ENV_EXAMPLE="${SCRIPT_DIR}/app.env.example"
  [[ -f "$SERVICE_SRC" ]] || die "缺少 rsgzgl-ops.service"
  [[ -f "$ENV_EXAMPLE" ]] || die "缺少 app.env.example"
}

copy_hmac_if_present() {
  local src="/opt/rsgzgl/app.env"
  if [[ -f "$src" ]] && grep -q '^RSGZGL_LICENSE_HMAC_SECRET=' "$src"; then
    local secret
    secret="$(grep '^RSGZGL_LICENSE_HMAC_SECRET=' "$src" | head -n1 | cut -d= -f2-)"
    if [[ -n "$secret" ]]; then
      if grep -q '^RSGZGL_LICENSE_HMAC_SECRET=' "${APP_HOME}/app.env"; then
        sed -i "s|^RSGZGL_LICENSE_HMAC_SECRET=.*|RSGZGL_LICENSE_HMAC_SECRET=${secret}|" "${APP_HOME}/app.env"
      else
        echo "RSGZGL_LICENSE_HMAC_SECRET=${secret}" >> "${APP_HOME}/app.env"
      fi
    fi
  fi
}

main() {
  need_root
  pick_files
  JAVA_BIN="$(find_java21)" || die "未找到 Java 21"
  echo "==> 使用 Java: ${JAVA_BIN}"

  if ! id -u "${APP_USER}" >/dev/null 2>&1; then
    useradd --system --home /opt/rsgzgl --shell /usr/sbin/nologin "${APP_USER}" || true
  fi
  mkdir -p "${APP_HOME}/logs" "${APP_HOME}/data"
  cp -f "${JAR_SRC}" "${APP_HOME}/app.jar"

  if [[ ! -f "${APP_HOME}/app.env" ]]; then
    cp -f "${ENV_EXAMPLE}" "${APP_HOME}/app.env"
    local pass
    pass="$(openssl rand -base64 18 | tr -d '/+=' | head -c 18)"
    sed -i "s|^RSGZGL_OPS_ADMIN_PASSWORD=.*|RSGZGL_OPS_ADMIN_PASSWORD=${pass}|" "${APP_HOME}/app.env"
    sed -i "s|^RSGZGL_OPS_ADMIN_RESET_PASSWORD=.*|RSGZGL_OPS_ADMIN_RESET_PASSWORD=true|" "${APP_HOME}/app.env"
    copy_hmac_if_present
    {
      echo "URL=http://$(hostname -I | awk '{print $1}'):18090"
      echo "USERNAME=ops"
      echo "PASSWORD=${pass}"
    } > "${APP_HOME}/credentials.txt"
    chmod 600 "${APP_HOME}/credentials.txt"
    echo "==> 已生成 ${APP_HOME}/credentials.txt"
  else
    echo "==> 保留已有 ${APP_HOME}/app.env"
    copy_hmac_if_present
  fi

  chown -R "${APP_USER}:${APP_USER}" "${APP_HOME}"
  chmod 640 "${APP_HOME}/app.env"
  chmod 755 "${APP_HOME}"
  chmod 644 "${APP_HOME}/app.jar"

  sed "s|__JAVA_BIN__|${JAVA_BIN}|g" "${SERVICE_SRC}" > /etc/systemd/system/rsgzgl-ops.service
  systemctl daemon-reload
  systemctl enable --now rsgzgl-ops

  firewall-cmd --permanent --add-port=18090/tcp 2>/dev/null || true
  firewall-cmd --reload 2>/dev/null || true

  echo
  echo "安装完成"
  echo "  状态: sudo systemctl status rsgzgl-ops"
  echo "  健康: curl -s http://127.0.0.1:18090/actuator/health"
  echo "  登录: http://服务器IP:18090  （账号见 ${APP_HOME}/credentials.txt）"
}

main "$@"

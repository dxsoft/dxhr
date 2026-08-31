#!/usr/bin/env bash
# rsgzgl Linux 简化安装（保留本机 MySQL，仅需 JRE 21）
# 用法：
#   1. 本机打包: mvn -DskipTests package
#   2. 上传到服务器同一目录：
#        rsgzgl-0.1.0-SNAPSHOT.jar  (或改名 app.jar)
#        install.sh
#        rsgzgl.service
#        app.env.example
#   3. SSH 登录后:
#        sudo bash install.sh
#
set -euo pipefail

APP_NAME=rsgzgl
APP_HOME=/opt/rsgzgl
APP_USER=rsgzgl
JAR_SRC=""
SERVICE_SRC=""
ENV_EXAMPLE=""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

die() { echo "ERROR: $*" >&2; exit 1; }

need_root() {
  if [[ "${EUID}" -ne 0 ]]; then
    die "请使用 root 或 sudo 运行: sudo bash install.sh"
  fi
}

find_java21() {
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

install_jre21() {
  echo "==> 安装 JRE 21 ..."
  if command -v apt-get >/dev/null 2>&1; then
    apt-get update -y
    apt-get install -y openjdk-21-jre-headless
  elif command -v dnf >/dev/null 2>&1; then
    dnf install -y java-21-openjdk-headless
  elif command -v yum >/dev/null 2>&1; then
    yum install -y java-21-openjdk-headless
  else
    die "无法自动安装 JRE，请先手工安装 Java 21 JRE"
  fi
}

pick_jar() {
  if [[ -f "${SCRIPT_DIR}/app.jar" ]]; then
    JAR_SRC="${SCRIPT_DIR}/app.jar"
  elif [[ -f "${SCRIPT_DIR}/rsgzgl-0.1.0-SNAPSHOT.jar" ]]; then
    JAR_SRC="${SCRIPT_DIR}/rsgzgl-0.1.0-SNAPSHOT.jar"
  else
    local found
    found="$(ls -1 "${SCRIPT_DIR}"/rsgzgl-*.jar 2>/dev/null | head -n1 || true)"
    [[ -n "$found" ]] || die "未找到 jar，请把打包好的 jar 放在 ${SCRIPT_DIR}"
    JAR_SRC="$found"
  fi
  SERVICE_SRC="${SCRIPT_DIR}/rsgzgl.service"
  ENV_EXAMPLE="${SCRIPT_DIR}/app.env.example"
  [[ -f "$SERVICE_SRC" ]] || die "缺少 rsgzgl.service"
  [[ -f "$ENV_EXAMPLE" ]] || die "缺少 app.env.example"
}

main() {
  need_root
  pick_jar

  if ! find_java21 >/dev/null; then
    install_jre21
  fi
  JAVA_BIN="$(find_java21)" || die "未找到 Java 21"
  echo "==> 使用 Java: ${JAVA_BIN}"
  "${JAVA_BIN}" -version

  echo "==> 创建用户与目录 ${APP_HOME}"
  if ! id -u "${APP_USER}" >/dev/null 2>&1; then
    useradd --system --home "${APP_HOME}" --shell /usr/sbin/nologin "${APP_USER}" || true
  fi
  mkdir -p "${APP_HOME}/logs"
  cp -f "${JAR_SRC}" "${APP_HOME}/app.jar"

  if [[ ! -f "${APP_HOME}/app.env" ]]; then
    cp -f "${ENV_EXAMPLE}" "${APP_HOME}/app.env"
    echo "==> 已生成 ${APP_HOME}/app.env ，请编辑数据库密码与管理员密码后再启动"
    echo "    nano ${APP_HOME}/app.env"
  else
    echo "==> 保留已有 ${APP_HOME}/app.env"
  fi

  chown -R "${APP_USER}:${APP_USER}" "${APP_HOME}"
  chmod 640 "${APP_HOME}/app.env"
  chmod 755 "${APP_HOME}"
  chmod 644 "${APP_HOME}/app.jar"

  # 写入实际 java 路径
  sed "s|__JAVA_BIN__|${JAVA_BIN}|g" "${SERVICE_SRC}" > /etc/systemd/system/rsgzgl.service

  systemctl daemon-reload
  systemctl enable rsgzgl

  echo
  echo "安装完成（尚未强制启动，便于你先改 app.env）"
  echo "下一步："
  echo "  1) 编辑配置:  sudo nano ${APP_HOME}/app.env"
  echo "  2) 启动服务:  sudo systemctl start rsgzgl"
  echo "  3) 查看状态:  sudo systemctl status rsgzgl"
  echo "  4) 健康检查:  curl -s http://127.0.0.1:8080/actuator/health"
  echo "  5) 浏览器访问: http://服务器IP:8080"
}

main "$@"

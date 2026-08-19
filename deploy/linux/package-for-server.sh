#!/usr/bin/env bash
# 在开发机执行：打包并把部署文件拷到 dist/linux-install/
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT="${ROOT}/dist/linux-install"
cd "${ROOT}"
mvn -DskipTests package
rm -rf "${OUT}"
mkdir -p "${OUT}"
cp -f "${ROOT}/target/rsgzgl-0.1.0-SNAPSHOT.jar" "${OUT}/app.jar"
cp -f "${ROOT}/deploy/linux/install.sh" "${OUT}/"
cp -f "${ROOT}/deploy/linux/rsgzgl.service" "${OUT}/"
cp -f "${ROOT}/deploy/linux/app.env.example" "${OUT}/"
if [[ -d "${ROOT}/deploy/saas" ]]; then
  cp -a "${ROOT}/deploy/saas" "${OUT}/saas"
fi
chmod +x "${OUT}/install.sh"
echo "已生成安装包目录: ${OUT}"
echo "上传示例:"
echo "  scp -r ${OUT} user@SERVER:/tmp/rsgzgl-install"
echo "服务器执行:"
echo "  cd /tmp/rsgzgl-install && sudo bash install.sh"
echo "托管 SaaS 见: ${OUT}/saas/README.md"

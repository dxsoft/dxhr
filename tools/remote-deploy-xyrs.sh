#!/usr/bin/env bash
set -euo pipefail
APP=/opt/rsgzgl
STAMP=$(date +%Y%m%d%H%M%S)

echo "==> backup and install jar"
cp -a "$APP/app.jar" "$APP/app.jar.bak.$STAMP"
install -o rsgzgl -g rsgzgl -m 644 /tmp/rsgzgl-app-new.jar "$APP/app.jar"
ls -la "$APP/app.jar"

echo "==> restart services"
systemctl restart rsgzgl rsgzgl@demo rsgzgl@pq rsgzgl@xyzzb

for spec in "primary:8080" "demo:18081" "pq:18082" "xyzzb:18083"; do
  name=${spec%%:*}
  port=${spec##*:}
  for i in $(seq 1 40); do
    code=$(curl -s -o "/tmp/h-${port}.json" -w '%{http_code}' "http://127.0.0.1:${port}/actuator/health" || echo 000)
    if [ "$code" = "200" ]; then
      echo "${name} (${port}): UP try${i}"
      break
    fi
    if [ "$i" -eq 40 ]; then
      echo "${name} (${port}): FAILED"
      exit 1
    fi
    sleep 3
  done
done

echo "==> configure nginx for xyrs.dxsoft.cn"
sed -i 's/\r$//' /tmp/setup-xyrs-https.sh
bash /tmp/setup-xyrs-https.sh

echo "==> verify xyrs domain"
curl -sk "https://xyrs.dxsoft.cn/actuator/health"
echo
echo | openssl s_client -connect xyrs.dxsoft.cn:443 -servername xyrs.dxsoft.cn 2>/dev/null | openssl x509 -noout -subject 2>/dev/null || true
echo "==> DONE"

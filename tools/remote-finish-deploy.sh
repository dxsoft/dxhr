#!/usr/bin/env bash
set -euo pipefail

wait_health() {
  local name="$1"
  local port="$2"
  local i code
  for i in $(seq 1 45); do
    code=$(curl -s -o "/tmp/h-${port}.json" -w '%{http_code}' "http://127.0.0.1:${port}/actuator/health" || echo 000)
    if [ "$code" = "200" ]; then
      echo "${name} port ${port}: UP at try${i}"
      return 0
    fi
    echo "${name} port ${port}: waiting try${i} code=${code}"
    sleep 4
  done
  echo "${name} port ${port}: FAILED"
  return 1
}

FAILED=0
wait_health primary 8080 || FAILED=1
wait_health demo 18081 || FAILED=1
wait_health pq 18082 || FAILED=1
wait_health xyzzb 18083 || FAILED=1

systemctl is-active rsgzgl rsgzgl@demo rsgzgl@pq rsgzgl@xyzzb

if [ "$FAILED" -ne 0 ]; then
  journalctl -u rsgzgl@xyzzb -n 30 --no-pager || true
  exit 1
fi

echo "==> repair xyzzb hisbase sid chain"
if mysql -uroot -pdx262105 -e "USE gzjsgl_xyzzb" 2>/dev/null; then
  mysql -uroot -pdx262105 gzjsgl_xyzzb < /tmp/rebuild-hisbase-sid-chain.sql
  mysql -uroot -pdx262105 gzjsgl_xyzzb -e "
    SELECT 'tip_sid_empty' AS metric, COUNT(*) AS cnt FROM hisbase WHERE TRIM(dwbm)='001' AND sid='';
    SELECT 'tip_sid_null' AS metric, COUNT(*) AS cnt FROM hisbase WHERE TRIM(dwbm)='001' AND sid IS NULL;
  "
fi

echo "==> DONE"

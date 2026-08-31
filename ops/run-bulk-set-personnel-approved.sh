#!/usr/bin/env bash
set -euo pipefail
PASS=$(grep ^RSGZGL_DB_PASSWORD= /opt/rsgzgl/app.env | cut -d= -f2- | tr -d '\r')
MYSQL=(mysql -h127.0.0.1 -P3307 -uroot -p"$PASS" --default-character-set=utf8mb4 gzjsgl)

echo "=== before (non-approved counts) ==="
"${MYSQL[@]}" -N -e "SELECT CONCAT('dryzwbh draft/other=',COUNT(*)) FROM dryzwbh WHERE TRIM(COALESCE(bbz,'')) <> '审批通过';"
"${MYSQL[@]}" -N -e "SELECT CONCAT('dndkh draft/other=',COUNT(*)) FROM dndkh WHERE TRIM(COALESCE(bbz,'')) <> '审批通过';"
"${MYSQL[@]}" -N -e "SELECT CONCAT('dxl draft/other=',COUNT(*)) FROM dxl WHERE TRIM(COALESCE(bbz,'')) <> '审批通过';"

echo "=== updating bbz to 审批通过 ==="
"${MYSQL[@]}" < /tmp/bulk-set-personnel-approved.sql

echo "=== actor legacy backfill ==="
"${MYSQL[@]}" < /tmp/migrate-personnel-approval-actors-legacy.sql

echo "=== after ==="
"${MYSQL[@]}" -N -e "SELECT CONCAT('dryjbxx=',COUNT(*)) FROM dryjbxx WHERE TRIM(bbz)='审批通过';"
"${MYSQL[@]}" -N -e "SELECT CONCAT('dryzwbh=',COUNT(*)) FROM dryzwbh WHERE TRIM(bbz)='审批通过';"
"${MYSQL[@]}" -N -e "SELECT CONCAT('dryzwbh_shr=',COUNT(*)) FROM dryzwbh WHERE shr IS NOT NULL;"
"${MYSQL[@]}" -N -e "SELECT CONCAT('dxl=',COUNT(*)) FROM dxl WHERE TRIM(bbz)='审批通过';"
"${MYSQL[@]}" -N -e "SELECT CONCAT('dndkh=',COUNT(*)) FROM dndkh WHERE TRIM(bbz)='审批通过';"
echo done

run_tenant() {
  local name="$1"
  local env_file="$2"
  local user pass url db
  user=$(grep ^RSGZGL_DB_USERNAME= "$env_file" | cut -d= -f2- | tr -d '\r')
  pass=$(grep ^RSGZGL_DB_PASSWORD= "$env_file" | cut -d= -f2- | tr -d '\r')
  url=$(grep ^RSGZGL_DB_URL= "$env_file" | cut -d= -f2- | tr -d '\r')
  db=$(echo "$url" | sed -n 's#.*://[^/]*/\([^?]*\).*#\1#p')
  echo "=== tenant $name ($db) ==="
  mysql -h127.0.0.1 -P3307 -u"$user" -p"$pass" --default-character-set=utf8mb4 "$db" < /tmp/bulk-set-personnel-approved.sql
  mysql -h127.0.0.1 -P3307 -u"$user" -p"$pass" --default-character-set=utf8mb4 "$db" < /tmp/migrate-personnel-approval-actors-legacy.sql
  mysql -h127.0.0.1 -P3307 -u"$user" -p"$pass" "$db" -N -e "SELECT CONCAT('dryjbxx=',COUNT(*)) FROM dryjbxx WHERE TRIM(bbz)='审批通过';"
}

run_tenant demo /opt/rsgzgl/tenants/demo/app.env
run_tenant pq /opt/rsgzgl/tenants/pq/app.env

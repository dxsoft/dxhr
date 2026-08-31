#!/usr/bin/env bash
set -euo pipefail

MIGRATION_SQL="${1:-/tmp/migrate-personnel-approval-actors.sql}"
if [ ! -f "$MIGRATION_SQL" ]; then
  echo "Migration file not found: $MIGRATION_SQL"
  exit 1
fi

load_db_config() {
  local env_file="$1"
  RSGZGL_DB_URL=$(grep '^RSGZGL_DB_URL=' "$env_file" | head -1 | cut -d= -f2- | tr -d '\r')
  RSGZGL_DB_USERNAME=$(grep '^RSGZGL_DB_USERNAME=' "$env_file" | head -1 | cut -d= -f2- | tr -d '\r')
  RSGZGL_DB_PASSWORD=$(grep '^RSGZGL_DB_PASSWORD=' "$env_file" | head -1 | cut -d= -f2- | tr -d '\r')
}

run_for_env() {
  local name="$1"
  local env_file="$2"
  if [ ! -f "$env_file" ]; then
    echo "skip $name: no env file"
    return 0
  fi
  load_db_config "$env_file"
  local url="${RSGZGL_DB_URL:-}"
  local user="${RSGZGL_DB_USERNAME:-}"
  local pass="${RSGZGL_DB_PASSWORD:-}"
  if [ -z "$url" ] || [ -z "$user" ] || [ -z "$pass" ]; then
    echo "skip $name: incomplete db config"
    return 0
  fi
  local host port db
  host=$(echo "$url" | sed -n 's#.*://\([^:/?]*\).*#\1#p')
  port=$(echo "$url" | sed -n 's#.*://[^:]*:\([0-9][0-9]*\)/.*#\1#p')
  db=$(echo "$url" | sed -n 's#.*://[^/]*/\([^?]*\).*#\1#p')
  host=${host:-127.0.0.1}
  port=${port:-3306}

  echo ""
  echo "========== $name ($db @ $host:$port) audit backfill =========="
  mysql -h"$host" -P"$port" -u"$user" -p"$pass" "$db" < "$MIGRATION_SQL"
  LEGACY_SQL="$(dirname "$MIGRATION_SQL")/migrate-personnel-approval-actors-legacy.sql"
  if [ -f "$LEGACY_SQL" ]; then
    echo "========== $name legacy fallback =========="
    mysql -h"$host" -P"$port" -u"$user" -p"$pass" "$db" < "$LEGACY_SQL"
  fi
  mysql -h"$host" -P"$port" -u"$user" -p"$pass" "$db" -N -e "
SELECT CONCAT('dryjbxx approved shr=', COUNT(*)) FROM dryjbxx WHERE TRIM(COALESCE(bbz,''))='审批通过' AND shr IS NOT NULL;
SELECT CONCAT('dryzwbh approved shr=', COUNT(*)) FROM dryzwbh WHERE TRIM(COALESCE(bbz,''))='审批通过' AND shr IS NOT NULL;
SELECT CONCAT('dxl approved shr=', COUNT(*)) FROM dxl WHERE TRIM(COALESCE(bbz,''))='审批通过' AND shr IS NOT NULL;
SELECT CONCAT('dndkh approved shr=', COUNT(*)) FROM dndkh WHERE TRIM(COALESCE(bbz,''))='审批通过' AND shr IS NOT NULL;
SELECT CONCAT('hjxx approved shr=', COUNT(*)) FROM hjxx WHERE TRIM(COALESCE(bbz,''))='审批通过' AND shr IS NOT NULL;
SELECT CONCAT('jx approved shr=', COUNT(*)) FROM jx WHERE TRIM(COALESCE(bbz,''))='审批通过' AND shr IS NOT NULL;
"
}

run_for_env "main" "/opt/rsgzgl/app.env"
run_for_env "demo" "/opt/rsgzgl/tenants/demo/app.env"
run_for_env "pq" "/opt/rsgzgl/tenants/pq/app.env"

echo ""
echo "done"

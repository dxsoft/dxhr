#!/usr/bin/env bash
# Rebuild hisbase/hisbaseb sid chains and normalize NULL tips to '' for a tenant database.
# Usage: ./normalize-hisbase-sid-chain.sh [database_name]
# Example: ./normalize-hisbase-sid-chain.sh gzjsgl_xyzzb

set -euo pipefail

DB_NAME="${1:-gzjsgl}"
MYSQL="${MYSQL_BIN:-mysql}"
ROOT_PW="${MYSQL_ROOT_PASSWORD:-dx262105}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="${SCRIPT_DIR}/../../tools/rebuild-hisbase-sid-chain.sql"

echo "==> Rebuilding sid chains on database: ${DB_NAME}"
"${MYSQL}" -uroot -p"${ROOT_PW}" "${DB_NAME}" < "${SQL_FILE}"
echo "==> Done."

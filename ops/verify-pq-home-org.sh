#!/usr/bin/env bash
set -euo pipefail
mysql -h 127.0.0.1 -P 3307 -u rsgzgl_pq -psaas_pq_db_a1d933a6 gzjsgl_pq <<'SQL'
SHOW COLUMNS FROM app_user LIKE 'home_organization_code';
SELECT code, name, data_scope FROM app_role WHERE code IN ('UNIT_ADMIN','001','ADMIN') ORDER BY code;
SELECT COUNT(*) AS unit_admin_perms FROM app_role_permission rp JOIN app_role r ON r.id = rp.role_id WHERE r.code = 'UNIT_ADMIN';
SELECT u.username, u.home_organization_code, GROUP_CONCAT(r.code ORDER BY r.code) AS roles
FROM app_user u
LEFT JOIN app_user_role ur ON ur.user_id = u.id
LEFT JOIN app_role r ON r.id = ur.role_id
GROUP BY u.id, u.username, u.home_organization_code
ORDER BY u.username;
SQL

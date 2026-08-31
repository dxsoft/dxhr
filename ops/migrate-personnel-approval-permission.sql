-- Grant personnel approval permission to ADMIN role only.
-- Safe to run multiple times (INSERT IGNORE).

INSERT IGNORE INTO app_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM app_role r
JOIN app_permission p ON p.code = 'PERSONNEL_APPROVAL_WRITE'
WHERE r.code = 'ADMIN';

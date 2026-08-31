-- Grant level-promotion permissions to roles that already have payroll read/write.
-- Safe to run multiple times (INSERT IGNORE).

INSERT IGNORE INTO app_role_permission (role_id, permission_id)
SELECT rp.role_id, p_new.id
FROM app_role_permission rp
JOIN app_permission p_old ON p_old.id = rp.permission_id AND p_old.code = 'PAYROLL_READ'
JOIN app_permission p_new ON p_new.code = 'LEVEL_PROMOTION_READ';

INSERT IGNORE INTO app_role_permission (role_id, permission_id)
SELECT rp.role_id, p_new.id
FROM app_role_permission rp
JOIN app_permission p_old ON p_old.id = rp.permission_id AND p_old.code = 'PAYROLL_WRITE'
JOIN app_permission p_new ON p_new.code = 'LEVEL_PROMOTION_WRITE';

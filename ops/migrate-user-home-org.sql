-- Migrate to user-level data scope (all_organizations + home_organization_code).
-- Roles keep only functional permissions; data scope is bound to users.
-- Run once after deploying the user data-scope feature.

-- 0. Ensure user-level columns exist (app startup also adds these).
-- ALTER TABLE app_user ADD COLUMN home_organization_code VARCHAR(20) NULL;
-- ALTER TABLE app_user ADD COLUMN all_organizations TINYINT(1) NOT NULL DEFAULT 0;

-- 1. Ensure UNIT_ADMIN role exists (app startup also seeds this).
INSERT INTO app_role (code, name, data_scope)
SELECT 'UNIT_ADMIN', '单位管理员', 'CUSTOM'
WHERE NOT EXISTS (SELECT 1 FROM app_role WHERE code = 'UNIT_ADMIN');

INSERT IGNORE INTO app_role_permission (role_id, permission_id)
SELECT ua.id, rp.permission_id
FROM app_role ua
JOIN app_role legacy ON legacy.code = '001'
JOIN app_role_permission rp ON rp.role_id = legacy.id
WHERE ua.code = 'UNIT_ADMIN';

-- 2. Users with ADMIN or ALL-scope roles get all_organizations=1.
UPDATE app_user u
SET all_organizations = 1,
    home_organization_code = NULL
WHERE EXISTS (
    SELECT 1
    FROM app_user_role ur
    JOIN app_role r ON r.id = ur.role_id
    WHERE ur.user_id = u.id
      AND (r.code = 'ADMIN' OR r.data_scope = 'ALL')
);

-- 3. Derive home organization from shortest prefix root in legacy role org scope.
UPDATE app_user u
JOIN (
    SELECT ur.user_id,
           MIN(ros.organization_code) AS home_code
    FROM app_user_role ur
    JOIN app_role r ON r.id = ur.role_id
    JOIN app_role_org_scope ros ON ros.role_id = r.id
    WHERE r.data_scope = 'CUSTOM'
      AND r.code NOT IN ('UNIT_ADMIN')
      AND NOT EXISTS (
          SELECT 1
          FROM app_role_org_scope ros2
          WHERE ros2.role_id = r.id
            AND ros2.organization_code <> ros.organization_code
            AND ros.organization_code LIKE CONCAT(ros2.organization_code, '%')
      )
    GROUP BY ur.user_id
) src ON src.user_id = u.id
SET u.home_organization_code = src.home_code
WHERE u.all_organizations = 0
  AND (u.home_organization_code IS NULL OR TRIM(u.home_organization_code) = '');

-- 4. Assign UNIT_ADMIN to users who had unit-specific custom roles.
INSERT IGNORE INTO app_user_role (user_id, role_id)
SELECT DISTINCT ur.user_id, ua.id
FROM app_user_role ur
JOIN app_role r ON r.id = ur.role_id
JOIN app_role ua ON ua.code = 'UNIT_ADMIN'
WHERE r.data_scope = 'CUSTOM'
  AND r.code NOT IN ('UNIT_ADMIN', 'ADMIN')
  AND EXISTS (
      SELECT 1 FROM app_role_org_scope ros WHERE ros.role_id = r.id
  );

-- 5. Optional: remove legacy per-unit role bindings after verification.
-- DELETE ur FROM app_user_role ur
-- JOIN app_role r ON r.id = ur.role_id
-- WHERE r.data_scope = 'CUSTOM'
--   AND r.code NOT IN ('UNIT_ADMIN', 'ADMIN')
--   AND EXISTS (SELECT 1 FROM app_role_org_scope ros WHERE ros.role_id = r.id);

-- Verification:
-- SELECT u.username, u.all_organizations, u.home_organization_code, GROUP_CONCAT(r.code ORDER BY r.code) AS roles
-- FROM app_user u
-- LEFT JOIN app_user_role ur ON ur.user_id = u.id
-- LEFT JOIN app_role r ON r.id = ur.role_id
-- GROUP BY u.id, u.username, u.all_organizations, u.home_organization_code
-- ORDER BY u.username;

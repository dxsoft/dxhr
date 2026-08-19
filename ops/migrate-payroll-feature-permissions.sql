-- Grant fine-grained payroll feature permissions to roles that already have PAYROLL_READ/WRITE.
-- Safe to run multiple times (INSERT IGNORE).
-- Application startup also runs equivalent migration via SecuritySchemaInitializer.migratePayrollFeaturePermissions().

INSERT IGNORE INTO app_role_permission (role_id, permission_id)
SELECT rp.role_id, p_new.id
FROM app_role_permission rp
JOIN app_permission p_old ON p_old.id = rp.permission_id AND p_old.code = 'PAYROLL_READ'
JOIN app_permission p_new ON p_new.code = 'NORMAL_PROMOTION_READ';

INSERT IGNORE INTO app_role_permission (role_id, permission_id)
SELECT rp.role_id, p_new.id
FROM app_role_permission rp
JOIN app_permission p_old ON p_old.id = rp.permission_id AND p_old.code = 'PAYROLL_WRITE'
JOIN app_permission p_new ON p_new.code = 'NORMAL_PROMOTION_WRITE';

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

INSERT IGNORE INTO app_role_permission (role_id, permission_id)
SELECT rp.role_id, p_new.id
FROM app_role_permission rp
JOIN app_permission p_old ON p_old.id = rp.permission_id AND p_old.code = 'PAYROLL_READ'
JOIN app_permission p_new ON p_new.code IN (
    'POSITION_CHANGE_PROMOTION_READ',
    'NEW_PERSONNEL_SALARY_READ',
    'REGULARIZATION_READ',
    'EDUCATION_PROMOTION_READ',
    'TEACHING_ALLOWANCE_ADJUSTMENT_READ',
    'FLOATING_TO_FIXED_READ',
    'OTHER_PAYROLL_CHANGE_READ',
    'REGULARIZATION_HIGH_GRADE_READ',
    'WAGE_REFORM_2006_READ',
    'PROSECUTION_ALLOWANCE_ADJUSTMENT_READ',
    'JUDICIAL_ALLOWANCE_ADJUSTMENT_READ',
    'POLICE_ALLOWANCE_ADJUSTMENT_READ',
    'SUPERVISION_ALLOWANCE_ADJUSTMENT_READ',
    'POLICE_RANK_CHANGE_PROMOTION_READ',
    'PROSECUTION_RANK_CHANGE_PROMOTION_READ',
    'JUDICIAL_RANK_CHANGE_PROMOTION_READ',
    'SUPERVISION_RANK_CHANGE_PROMOTION_READ',
    'INTERN_SALARY_CHANGE_READ',
    'BASIC_SALARY_STANDARD_ADJUSTMENT_READ',
    'CIVIL_ALLOWANCE_STANDARD_ADJUSTMENT_READ',
    'PERFORMANCE_STANDARD_ADJUSTMENT_READ',
    'PERFORMANCE_RATIO_ADJUSTMENT_READ',
    'ALLOWANCE_RECALCULATION_READ',
    'MONTHLY_AVERAGE_SALARY_READ',
    'SALARY_STANDARD_ADJUSTMENT_READ'
);

INSERT IGNORE INTO app_role_permission (role_id, permission_id)
SELECT rp.role_id, p_new.id
FROM app_role_permission rp
JOIN app_permission p_old ON p_old.id = rp.permission_id AND p_old.code = 'PAYROLL_WRITE'
JOIN app_permission p_new ON p_new.code IN (
    'POSITION_CHANGE_PROMOTION_WRITE',
    'NEW_PERSONNEL_SALARY_WRITE',
    'REGULARIZATION_WRITE',
    'EDUCATION_PROMOTION_WRITE',
    'TEACHING_ALLOWANCE_ADJUSTMENT_WRITE',
    'FLOATING_TO_FIXED_WRITE',
    'OTHER_PAYROLL_CHANGE_WRITE',
    'REGULARIZATION_HIGH_GRADE_WRITE',
    'WAGE_REFORM_2006_WRITE',
    'PROSECUTION_ALLOWANCE_ADJUSTMENT_WRITE',
    'JUDICIAL_ALLOWANCE_ADJUSTMENT_WRITE',
    'POLICE_ALLOWANCE_ADJUSTMENT_WRITE',
    'SUPERVISION_ALLOWANCE_ADJUSTMENT_WRITE',
    'POLICE_RANK_CHANGE_PROMOTION_WRITE',
    'PROSECUTION_RANK_CHANGE_PROMOTION_WRITE',
    'JUDICIAL_RANK_CHANGE_PROMOTION_WRITE',
    'SUPERVISION_RANK_CHANGE_PROMOTION_WRITE',
    'INTERN_SALARY_CHANGE_WRITE',
    'BASIC_SALARY_STANDARD_ADJUSTMENT_WRITE',
    'CIVIL_ALLOWANCE_STANDARD_ADJUSTMENT_WRITE',
    'PERFORMANCE_STANDARD_ADJUSTMENT_WRITE',
    'PERFORMANCE_RATIO_ADJUSTMENT_WRITE',
    'ALLOWANCE_RECALCULATION_WRITE',
    'MONTHLY_AVERAGE_SALARY_WRITE',
    'SALARY_STANDARD_ADJUSTMENT_WRITE'
);

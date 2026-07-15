package com.dxsoft.rsgzgl.security;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class SecuritySchemaInitializer {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final boolean initializeSchema;
    private final boolean resetAdminPassword;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminDisplayName;

    SecuritySchemaInitializer(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${rsgzgl.security.initialize-schema:true}") boolean initializeSchema,
            @Value("${rsgzgl.security.admin.reset-password:false}") boolean resetAdminPassword,
            @Value("${rsgzgl.security.admin.username:admin}") String adminUsername,
            @Value("${rsgzgl.security.admin.password:}") String adminPassword,
            @Value("${rsgzgl.security.admin.display-name:系统管理员}") String adminDisplayName) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.initializeSchema = initializeSchema;
        this.resetAdminPassword = resetAdminPassword;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminDisplayName = adminDisplayName;
        initialize();
    }

    private void initialize() {
        if (!initializeSchema) {
            return;
        }
        createTables();
        seedPermissions();
        seedMenus();
        ensureAnnualAssessmentMenu();
        ensureRankAllowanceMenus();
        seedAdmin();
    }

    private void createTables() {
        List.of(
                """
                CREATE TABLE IF NOT EXISTS app_user (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(80) NOT NULL UNIQUE,
                    password_hash VARCHAR(120) NOT NULL,
                    display_name VARCHAR(80) NOT NULL,
                    enabled TINYINT(1) NOT NULL DEFAULT 1,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_role (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    code VARCHAR(80) NOT NULL UNIQUE,
                    name VARCHAR(80) NOT NULL,
                    data_scope VARCHAR(20) NOT NULL DEFAULT 'CUSTOM'
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_permission (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    code VARCHAR(120) NOT NULL UNIQUE,
                    name VARCHAR(120) NOT NULL,
                    category VARCHAR(40) NOT NULL
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_user_role (
                    user_id BIGINT NOT NULL,
                    role_id BIGINT NOT NULL,
                    PRIMARY KEY (user_id, role_id)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_role_permission (
                    role_id BIGINT NOT NULL,
                    permission_id BIGINT NOT NULL,
                    PRIMARY KEY (role_id, permission_id)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_role_org_scope (
                    role_id BIGINT NOT NULL,
                    organization_code CHAR(9) NOT NULL,
                    PRIMARY KEY (role_id, organization_code)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_security_audit_log (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    actor_username VARCHAR(80) NOT NULL,
                    action VARCHAR(80) NOT NULL,
                    target_type VARCHAR(40) NOT NULL,
                    target_id VARCHAR(80) NOT NULL,
                    summary VARCHAR(500) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_menu (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    code VARCHAR(80) NOT NULL UNIQUE,
                    title VARCHAR(80) NOT NULL,
                    path VARCHAR(120) NOT NULL,
                    permission_code VARCHAR(120) NOT NULL,
                    sort_order INT NOT NULL DEFAULT 0,
                    enabled TINYINT(1) NOT NULL DEFAULT 1
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_record_marker (
                    table_name VARCHAR(80) NOT NULL,
                    record_id VARCHAR(80) NOT NULL,
                    marker VARCHAR(40) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (table_name, record_id, marker)
                )
                """).forEach(jdbcTemplate::execute);
    }

    private void seedPermissions() {
        upsertPermission("ORG_READ", "单位查询", "ORGANIZATION");
        upsertPermission("ORG_WRITE", "单位信息维护", "ORGANIZATION");
        upsertPermission("PERSONNEL_READ", "人员信息查询", "PERSONNEL");
        upsertPermission("PERSONNEL_WRITE", "人员信息维护", "PERSONNEL");
        upsertPermission("PAYROLL_READ", "工资试算查询", "PAYROLL");
        upsertPermission("PAYROLL_WRITE", "工资变动维护", "PAYROLL");
        upsertPermission("AUDIT_READ", "工资批量对账", "PAYROLL");
        upsertPermission("STANDARD_READ", "工资标准查询", "PAYROLL");
        upsertPermission("STANDARD_WRITE", "工资标准维护", "PAYROLL");
        upsertPermission("SECURITY_ADMIN", "权限管理", "SYSTEM");
        upsertPermission("DATA_EXCHANGE_READ", "数据交换", "DATA");
        upsertPermission("REPORT_READ", "报表打印与查询统计", "REPORT");
        upsertPermission("SYSTEM_CONFIG", "系统初始化与基础设置", "SYSTEM");
        upsertPermission("OPERATION_LOG_READ", "上机日志查询", "SYSTEM");
        upsertPermission("DATA_MAINTENANCE", "数据维护", "SYSTEM");
        upsertPermission("HELP_READ", "系统帮助", "SYSTEM");
    }

    private void seedMenus() {
        upsertMenu("PERSONNEL", "人员管理", "#personnel", "PERSONNEL_READ", 10);
        disableMenu("PERSONNEL_MAINTENANCE");
        upsertMenu("PERSONNEL_STATISTICS", "人员与工资统计", "#personnel-statistics", "PERSONNEL_READ", 12);
        upsertMenu("RETIREMENT_DUE_QUERY", "已达退休年龄人员", "#retirement-due-query", "PERSONNEL_READ", 13);
        upsertMenu("PERSONNEL_COMPREHENSIVE_QUERY", "人员信息综合查询", "#personnel-comprehensive-query", "PERSONNEL_READ", 14);
        upsertMenu("ANNUAL_ASSESSMENT_MANAGEMENT", "年度考核管理", "#annual-assessment-management", "PERSONNEL_READ", 14);
        disableMenu("ANNUAL_ASSESSMENTS");
        disableMenu("ANNUAL_ASSESSMENT_BATCH");
        disableMenu("ASSESSMENT_SUMMARY");
        upsertMenu("CHANGED_PERSONNEL", "变动人员信息", "#changed-personnel", "PERSONNEL_READ", 17);
        upsertMenu("POSITION_HISTORY", "任职岗位信息", "#position-history", "PERSONNEL_READ", 18);
        upsertMenu("EDUCATION_HISTORY", "学历信息", "#education-history", "PERSONNEL_READ", 19);
        upsertMenu("PAYROLL", "工资试算", "#payroll", "PAYROLL_READ", 20);
        upsertMenu("PAYROLL_HISTORY", "工资变动历史", "#payroll-history", "PAYROLL_READ", 25);
        upsertMenu("TEACHING_ALLOWANCE_ADJUSTMENT", "调整教护龄津贴", "#teaching-allowance-adjustment", "PAYROLL_READ", 28);
        upsertMenu("PROSECUTION_ALLOWANCE_ADJUSTMENT", "调整检察津贴", "#prosecution-allowance-adjustment", "PAYROLL_READ", 281);
        upsertMenu("JUDICIAL_ALLOWANCE_ADJUSTMENT", "调整审判津贴", "#judicial-allowance-adjustment", "PAYROLL_READ", 282);
        upsertMenu("POLICE_ALLOWANCE_ADJUSTMENT", "调整警衔津贴", "#police-allowance-adjustment", "PAYROLL_READ", 283);
        upsertMenu("SUPERVISION_ALLOWANCE_ADJUSTMENT", "调整监察津贴", "#supervision-allowance-adjustment", "PAYROLL_READ", 284);
        upsertMenu("POLICE_RANK_CHANGE_PROMOTION", "警衔工资晋升", "#police-rank-change-promotion", "PAYROLL_READ", 285);
        upsertMenu("PROSECUTION_RANK_CHANGE_PROMOTION", "检察官等级变化晋升", "#prosecution-rank-change-promotion", "PAYROLL_READ", 286);
        upsertMenu("JUDICIAL_RANK_CHANGE_PROMOTION", "法官等级变化晋升", "#judicial-rank-change-promotion", "PAYROLL_READ", 287);
        upsertMenu("SUPERVISION_RANK_CHANGE_PROMOTION", "监察等级变化晋升", "#supervision-rank-change-promotion", "PAYROLL_READ", 288);
        upsertMenu("NORMAL_PROMOTION", "正常档次/薪级晋升", "#normal-promotion", "PAYROLL_READ", 29);
        upsertMenu("LEVEL_PROMOTION", "级别晋升", "#level-promotion", "PAYROLL_READ", 30);
        upsertMenu("POSITION_CHANGE_PROMOTION", "职务变化晋升", "#position-change-promotion", "PAYROLL_READ", 31);
        upsertMenu("EDUCATION_PROMOTION", "学历晋升", "#education-promotion", "PAYROLL_READ", 32);
        upsertMenu("REGULARIZATION", "转正定级", "#regularization", "PAYROLL_READ", 33);
        upsertMenu("FLOATING_TO_FIXED", "浮动转固定", "#floating-to-fixed", "PAYROLL_READ", 34);
        upsertMenu("INTERN_SALARY_CHANGE", "见习工资变动", "#intern-salary-change", "PAYROLL_READ", 35);
        upsertMenu("NEW_PERSONNEL_SALARY", "新增人员确定工资", "#new-personnel-salary", "PAYROLL_READ", 36);
        upsertMenu("OTHER_PAYROLL_CHANGE", "其它情况工资变动", "#other-payroll-change", "PAYROLL_READ", 37);
        upsertMenu("SALARY_STANDARD_ADJUSTMENT", "2024.07调标", "#salary-standard-adjustment", "PAYROLL_READ", 38);
        upsertMenu("BASIC_SALARY_STANDARD_ADJUSTMENT", "调整基本工资标准", "#basic-salary-standard-adjustment", "PAYROLL_READ", 381);
        upsertMenu("CIVIL_ALLOWANCE_STANDARD_ADJUSTMENT", "调整公务员津贴补贴", "#civil-allowance-standard-adjustment", "PAYROLL_READ", 382);
        upsertMenu("PERFORMANCE_STANDARD_ADJUSTMENT", "调整绩效工资标准", "#performance-standard-adjustment", "PAYROLL_READ", 383);
        upsertMenu("PERFORMANCE_RATIO_ADJUSTMENT", "调整绩效比例", "#performance-ratio-adjustment", "PAYROLL_READ", 384);
        upsertMenu("ALLOWANCE_RECALCULATION", "重算津补贴", "#allowance-recalculation", "PAYROLL_READ", 39);
        upsertMenu("REFORM_LEVEL_ROLLING", "级别滚动晋升", "#reform-level-rolling", "PAYROLL_READ", 40);
        upsertMenu("REGULARIZATION_HIGH_GRADE", "转正高定档次薪级", "#regularization-high-grade", "PAYROLL_READ", 41);
        upsertMenu("MONTHLY_AVERAGE_SALARY", "月平均工资计算", "#monthly-average-salary", "PAYROLL_READ", 42);
        upsertMenu("WAGE_REFORM_2006", "2006年工资套改", "#wage-reform-2006", "PAYROLL_READ", 43);
        upsertMenu("AUDIT", "批量对账", "#audit", "AUDIT_READ", 35);
        upsertMenu("BASIC_STANDARDS", "基本工资标准", "#basic-standards", "STANDARD_READ", 40);
        upsertMenu("INTERN_SALARY_STANDARDS", "见习工资标准", "#intern-salary-standards", "STANDARD_READ", 45);
        upsertMenu("ALLOWANCE_STANDARDS", "津贴补贴标准", "#allowance-standards", "STANDARD_READ", 50);
        upsertMenu("RANK_ALLOWANCE_STANDARDS", "警衔津贴标准", "#rank-allowance-standards", "STANDARD_READ", 55);
        upsertMenu("PROSECUTION_ALLOWANCE_STANDARDS", "检察津贴标准", "#rank-allowance-standards/jc", "STANDARD_READ", 56);
        upsertMenu("JUDICIAL_ALLOWANCE_STANDARDS", "审判津贴标准", "#rank-allowance-standards/sp", "STANDARD_READ", 57);
        upsertMenu("RETAINED_ALLOWANCE_STANDARDS", "保留福补标准", "#retained-allowance-standards", "STANDARD_READ", 60);
        upsertMenu("YEAR_ALLOWANCE_STANDARDS", "年补贴标准", "#year-allowance-standards", "STANDARD_READ", 65);
        upsertMenu("RURAL_TEACHER_ALLOWANCE_STANDARDS", "农村学校教师补贴", "#year-allowance-standards", "STANDARD_READ", 66);
        upsertMenu("WAGE_REFORM_STANDARDS", "2006套改标准", "#wage-reform-standards", "STANDARD_READ", 70);
        upsertMenu("OTHER_ALLOWANCE_STANDARDS", "其他补贴标准", "#other-allowance-standards", "STANDARD_READ", 75);
        upsertMenu("ORGANIZATION_MAINTENANCE", "单位信息维护", "#organization-maintenance", "ORG_READ", 80);
        upsertMenu("LOCAL_POLICY_CONFIG", "本地工资政策", "#local-policy-config", "SYSTEM_CONFIG", 82);
        upsertMenu("DICTIONARY_MAINTENANCE", "设置常用值", "#dictionary-maintenance", "SYSTEM_CONFIG", 85);
        disableMenu("RAISE_GRADE_STANDARDS");
        disableMenu("PERSONNEL_STRUCTURE_SUMMARY");
        upsertMenu("SECURITY", "权限管理", "#security", "SECURITY_ADMIN", 90);
        upsertMenu("OPERATION_LOG", "上机日志", "#operation-log", "OPERATION_LOG_READ", 91);
        upsertMenu("DATA_MAINTENANCE", "数据维护", "#data-maintenance", "DATA_MAINTENANCE", 92);
        upsertMenu("SYSTEM_HELP", "系统帮助", "#system-help", "HELP_READ", 93);
        upsertMenu("SYSTEM_SETUP", "系统初始化与导入", "#system-setup", "SYSTEM_CONFIG", 94);
        upsertMenu("PAYROLL_CHANGE_REGISTER_REPORT", "工资变动花名册", "#payroll-change-register-report", "REPORT_READ", 100);
        upsertMenu("PAYROLL_CHANGE_APPROVAL_REPORT", "工资变动审批表", "#payroll-change-approval-report", "REPORT_READ", 101);
        upsertMenu("WAGE_REFORM_2006_PUBLIC_NOTICE_REPORT", "2006套改公示表", "#wage-reform-2006-public-notice-report", "REPORT_READ", 102);
        upsertMenu("PERSONNEL_INFORMATION_COLLECTION_REPORT", "人员信息采集表", "#personnel-information-collection-report", "REPORT_READ", 103);
        upsertMenu("PERSONNEL_INFORMATION_REGISTRATION_REPORT", "人员信息登记表", "#personnel-information-registration-report", "REPORT_READ", 104);
        upsertMenu("DATA_EXCHANGE", "数据交换", "#data-exchange", "DATA_EXCHANGE_READ", 105);
        upsertMenu("LEGACY_INFO_MAINTENANCE", "VFP-信息维护（待迁移）", "#legacy-info", "PERSONNEL_READ", 110, false);
        upsertMenu("LEGACY_PAYROLL_CHANGE", "VFP-工资变动（待迁移）", "#legacy-payroll-change", "PAYROLL_READ", 120, false);
        upsertMenu("LEGACY_DATA_EXCHANGE", "VFP-数据交换（已由数据交换替代）", "#legacy-data-exchange", "DATA_EXCHANGE_READ", 130, false);
        upsertMenu("LEGACY_REPORT_PRINT", "VFP-报表打印（已由报表菜单替代）", "#legacy-report-print", "REPORT_READ", 140, false);
        upsertMenu("LEGACY_QUERY_STATISTICS", "VFP-查询统计（已由统计/查询菜单替代）", "#legacy-query-statistics", "REPORT_READ", 150, false);
        upsertMenu("LEGACY_INITIAL_SETTINGS", "VFP-初始设置（已由标准/配置菜单替代）", "#legacy-initial-settings", "SYSTEM_CONFIG", 160, false);
        upsertMenu("LEGACY_SYSTEM_MAINTENANCE", "VFP-系统维护（待迁移）", "#legacy-system-maintenance", "SECURITY_ADMIN", 170, false);
        upsertMenu("LEGACY_HELP", "VFP-系统帮助（已由系统帮助替代）", "#legacy-help", "HELP_READ", 180, false);
    }

    private void seedAdmin() {
        jdbcTemplate.update("""
                INSERT INTO app_role (code, name, data_scope)
                SELECT 'ADMIN', '系统管理员', 'ALL'
                WHERE NOT EXISTS (SELECT 1 FROM app_role WHERE code = 'ADMIN')
                """);
        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_user WHERE username = ?",
                Integer.class,
                adminUsername);
        if ((userCount == null || userCount == 0) && adminPassword != null && !adminPassword.isBlank()) {
            jdbcTemplate.update("""
                    INSERT INTO app_user (username, password_hash, display_name, enabled)
                    VALUES (?, ?, ?, 1)
                    """, adminUsername, passwordEncoder.encode(adminPassword), adminDisplayName);
        } else if (resetAdminPassword && adminPassword != null && !adminPassword.isBlank()) {
            jdbcTemplate.update("""
                    UPDATE app_user
                    SET password_hash = ?, display_name = ?, enabled = 1
                    WHERE username = ?
                    """, passwordEncoder.encode(adminPassword), adminDisplayName, adminUsername);
        }
        jdbcTemplate.update("""
                INSERT IGNORE INTO app_user_role (user_id, role_id)
                SELECT u.id, r.id
                FROM app_user u, app_role r
                WHERE u.username = ? AND r.code = 'ADMIN'
                """, adminUsername);
        jdbcTemplate.update("""
                INSERT IGNORE INTO app_role_permission (role_id, permission_id)
                SELECT r.id, p.id
                FROM app_role r, app_permission p
                WHERE r.code = 'ADMIN'
                """);
    }

    private void upsertPermission(String code, String name, String category) {
        jdbcTemplate.update("""
                INSERT INTO app_permission (code, name, category)
                SELECT ?, ?, ?
                WHERE NOT EXISTS (SELECT 1 FROM app_permission WHERE code = ?)
                """, code, name, category, code);
    }

    private void upsertMenu(String code, String title, String path, String permissionCode, int sortOrder) {
        upsertMenu(code, title, path, permissionCode, sortOrder, true);
    }

    private void upsertMenu(String code, String title, String path, String permissionCode, int sortOrder, boolean enabled) {
        jdbcTemplate.update("""
                INSERT INTO app_menu (code, title, path, permission_code, sort_order, enabled)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    title = VALUES(title),
                    path = VALUES(path),
                    permission_code = VALUES(permission_code),
                    sort_order = VALUES(sort_order),
                    enabled = VALUES(enabled)
                """, code, title, path, permissionCode, sortOrder, enabled ? 1 : 0);
    }

    private void disableMenu(String code) {
        jdbcTemplate.update("UPDATE app_menu SET enabled = 0 WHERE code = ?", code);
    }

    private void ensureAnnualAssessmentMenu() {
        upsertMenu("ANNUAL_ASSESSMENT_MANAGEMENT", "年度考核管理", "#annual-assessment-management", "PERSONNEL_READ", 14);
        disableMenu("ANNUAL_ASSESSMENTS");
        disableMenu("ANNUAL_ASSESSMENT_BATCH");
        disableMenu("ASSESSMENT_SUMMARY");
        jdbcTemplate.update("""
                UPDATE app_menu
                SET title = '年度考核管理',
                    path = '#annual-assessment-management',
                    enabled = 1
                WHERE code = 'ANNUAL_ASSESSMENT_MANAGEMENT'
                """);
    }

    private void ensureRankAllowanceMenus() {
        upsertMenu("PROSECUTION_ALLOWANCE_ADJUSTMENT", "调整检察津贴", "#prosecution-allowance-adjustment", "PAYROLL_READ", 281);
        upsertMenu("JUDICIAL_ALLOWANCE_ADJUSTMENT", "调整审判津贴", "#judicial-allowance-adjustment", "PAYROLL_READ", 282);
        upsertMenu("POLICE_ALLOWANCE_ADJUSTMENT", "调整警衔津贴", "#police-allowance-adjustment", "PAYROLL_READ", 283);
        upsertMenu("SUPERVISION_ALLOWANCE_ADJUSTMENT", "调整监察津贴", "#supervision-allowance-adjustment", "PAYROLL_READ", 284);
        upsertMenu("POLICE_RANK_CHANGE_PROMOTION", "警衔工资晋升", "#police-rank-change-promotion", "PAYROLL_READ", 285);
        upsertMenu("PROSECUTION_RANK_CHANGE_PROMOTION", "检察官等级变化晋升", "#prosecution-rank-change-promotion", "PAYROLL_READ", 286);
        upsertMenu("JUDICIAL_RANK_CHANGE_PROMOTION", "法官等级变化晋升", "#judicial-rank-change-promotion", "PAYROLL_READ", 287);
        upsertMenu("SUPERVISION_RANK_CHANGE_PROMOTION", "监察等级变化晋升", "#supervision-rank-change-promotion", "PAYROLL_READ", 288);
        jdbcTemplate.update("""
                UPDATE app_menu
                SET enabled = 1
                WHERE code IN (
                    'PROSECUTION_ALLOWANCE_ADJUSTMENT',
                    'JUDICIAL_ALLOWANCE_ADJUSTMENT',
                    'POLICE_ALLOWANCE_ADJUSTMENT',
                    'SUPERVISION_ALLOWANCE_ADJUSTMENT',
                    'POLICE_RANK_CHANGE_PROMOTION',
                    'PROSECUTION_RANK_CHANGE_PROMOTION',
                    'JUDICIAL_RANK_CHANGE_PROMOTION',
                    'SUPERVISION_RANK_CHANGE_PROMOTION'
                )
                """);
    }
}

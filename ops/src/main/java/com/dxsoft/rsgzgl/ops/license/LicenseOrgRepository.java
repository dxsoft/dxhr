package com.dxsoft.rsgzgl.ops.license;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class LicenseOrgRepository {

    private final JdbcTemplate jdbcTemplate;

    LicenseOrgRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    List<LicenseOrgRow> findAll(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return jdbcTemplate.query("""
                    SELECT * FROM license_org ORDER BY organization_code
                    """, this::mapRow);
        }
        String like = "%" + keyword.trim() + "%";
        return jdbcTemplate.query("""
                SELECT * FROM license_org
                WHERE organization_code LIKE ? OR name LIKE ? OR short_name LIKE ?
                ORDER BY organization_code
                """, this::mapRow, like, like, like);
    }

    LicenseOrgRow findByCode(String code) {
        List<LicenseOrgRow> rows = jdbcTemplate.query(
                "SELECT * FROM license_org WHERE organization_code = ?",
                this::mapRow,
                code);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    List<LicenseOrgRow> findByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", codes.stream().map(c -> "?").toList());
        return jdbcTemplate.query(
                "SELECT * FROM license_org WHERE organization_code IN (" + placeholders + ") ORDER BY organization_code",
                this::mapRow,
                codes.toArray());
    }

    void upsert(LicenseOrgRequest request) {
        String code = request.organizationCode().trim();
        LicenseOrgRow existing = findByCode(code);
        if (existing == null) {
            jdbcTemplate.update("""
                    INSERT INTO license_org (
                        organization_code, name, short_name, property, category, payroll_category,
                        allowance_standard, personnel_quota, establishment_count, actual_count,
                        organization_level, system_category, performance_allowance_enabled, performance_category,
                        performance_ratio, year_allowance_category, finance_source, housing_fund_withheld, pension_withheld,
                        city, supervisor
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    code,
                    request.name().trim(),
                    empty(request.shortName()),
                    empty(request.property()),
                    empty(request.category()),
                    empty(request.payrollCategory()),
                    empty(request.allowanceStandard()),
                    request.personnelQuota(),
                    request.establishmentCount(),
                    request.actualCount(),
                    empty(request.organizationLevel()),
                    empty(request.systemCategory()),
                    request.performanceAllowanceEnabled(),
                    request.performanceCategory(),
                    empty(request.performanceRatio()),
                    request.yearAllowanceCategory(),
                    empty(request.financeSource()),
                    empty(request.housingFundWithheld()),
                    empty(request.pensionWithheld()),
                    empty(request.city()),
                    empty(request.supervisor()));
        } else {
            jdbcTemplate.update("""
                    UPDATE license_org SET
                        name = ?, short_name = ?, property = ?, category = ?, payroll_category = ?,
                        allowance_standard = ?, personnel_quota = ?, establishment_count = ?, actual_count = ?,
                        organization_level = ?, system_category = ?, performance_allowance_enabled = ?, performance_category = ?,
                        performance_ratio = ?, year_allowance_category = ?, finance_source = ?, housing_fund_withheld = ?,
                        pension_withheld = ?, city = ?, supervisor = ?
                    WHERE organization_code = ?
                    """,
                    request.name().trim(),
                    empty(request.shortName()),
                    empty(request.property()),
                    empty(request.category()),
                    empty(request.payrollCategory()),
                    empty(request.allowanceStandard()),
                    request.personnelQuota(),
                    request.establishmentCount(),
                    request.actualCount(),
                    empty(request.organizationLevel()),
                    empty(request.systemCategory()),
                    request.performanceAllowanceEnabled(),
                    request.performanceCategory(),
                    empty(request.performanceRatio()),
                    request.yearAllowanceCategory(),
                    empty(request.financeSource()),
                    empty(request.housingFundWithheld()),
                    empty(request.pensionWithheld()),
                    empty(request.city()),
                    empty(request.supervisor()),
                    code);
        }
    }

    void delete(String code) {
        jdbcTemplate.update("DELETE FROM license_org WHERE organization_code = ?", code);
    }

    void insertIssueLog(
            String actor,
            String subjectCode,
            String subjectName,
            String expiresAt,
            int orgCount,
            String fingerprint,
            String summary) {
        jdbcTemplate.update("""
                INSERT INTO license_issue_log
                (actor_username, subject_code, subject_name, expires_at, organization_count, fingerprint, summary)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, actor, subjectCode, subjectName, expiresAt, orgCount, fingerprint, summary);
    }

    List<IssueLogRow> recentLogs(int limit) {
        return jdbcTemplate.query("""
                SELECT id, actor_username, subject_code, subject_name, expires_at, organization_count,
                       fingerprint, summary, created_at
                FROM license_issue_log
                ORDER BY id DESC
                LIMIT ?
                """, (rs, rowNum) -> new IssueLogRow(
                rs.getLong("id"),
                rs.getString("actor_username"),
                rs.getString("subject_code"),
                rs.getString("subject_name"),
                rs.getString("expires_at"),
                rs.getInt("organization_count"),
                rs.getString("fingerprint"),
                rs.getString("summary"),
                rs.getTimestamp("created_at").toLocalDateTime()), Math.max(1, Math.min(limit, 200)));
    }

    /** subject_code → 最近一次签发时间（ISO 本地时间字符串） */
    Map<String, String> latestIssueAtBySubject() {
        List<Map.Entry<String, String>> rows = jdbcTemplate.query("""
                SELECT subject_code, MAX(created_at) AS last_issued_at
                FROM license_issue_log
                WHERE subject_code IS NOT NULL AND TRIM(subject_code) <> ''
                GROUP BY subject_code
                """, (rs, rowNum) -> {
            String code = rs.getString("subject_code");
            java.sql.Timestamp ts = rs.getTimestamp("last_issued_at");
            return Map.entry(
                    code == null ? "" : code.trim(),
                    ts == null ? "" : ts.toLocalDateTime().toString().replace('T', ' '));
        });
        Map<String, String> map = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, String> row : rows) {
            if (!row.getKey().isEmpty()) {
                map.put(row.getKey(), row.getValue());
            }
        }
        return map;
    }

    private LicenseOrgRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new LicenseOrgRow(
                rs.getLong("id"),
                rs.getString("organization_code"),
                rs.getString("name"),
                rs.getString("short_name"),
                rs.getString("property"),
                rs.getString("category"),
                rs.getString("payroll_category"),
                rs.getString("allowance_standard"),
                (Integer) rs.getObject("personnel_quota"),
                (Integer) rs.getObject("establishment_count"),
                (Integer) rs.getObject("actual_count"),
                rs.getString("organization_level"),
                rs.getString("system_category"),
                (Integer) rs.getObject("performance_allowance_enabled"),
                (Integer) rs.getObject("performance_category"),
                rs.getString("performance_ratio"),
                (Integer) rs.getObject("year_allowance_category"),
                rs.getString("finance_source"),
                rs.getString("housing_fund_withheld"),
                rs.getString("pension_withheld"),
                rs.getString("city"),
                rs.getString("supervisor"),
                null,
                null);
    }

    private static String empty(String value) {
        return value == null ? "" : value.trim();
    }

    record LicenseOrgRow(
            Long id,
            String organizationCode,
            String name,
            String shortName,
            String property,
            String category,
            String payrollCategory,
            String allowanceStandard,
            Integer personnelQuota,
            Integer establishmentCount,
            Integer actualCount,
            String organizationLevel,
            String systemCategory,
            Integer performanceAllowanceEnabled,
            Integer performanceCategory,
            String performanceRatio,
            Integer yearAllowanceCategory,
            String financeSource,
            String housingFundWithheld,
            String pensionWithheld,
            String city,
            String supervisor,
            Boolean issued,
            String lastIssuedAt
    ) {
        LicenseOrgRow withIssueStatus(boolean issuedFlag, String issuedAt) {
            return new LicenseOrgRow(
                    id, organizationCode, name, shortName, property, category, payrollCategory,
                    allowanceStandard, personnelQuota, establishmentCount, actualCount,
                    organizationLevel, systemCategory, performanceAllowanceEnabled, performanceCategory,
                    performanceRatio, yearAllowanceCategory, financeSource, housingFundWithheld, pensionWithheld,
                    city, supervisor, issuedFlag, issuedAt);
        }

        LicenseOrganization toLicenseOrganization() {
            return new LicenseOrganization(
                    organizationCode,
                    name,
                    shortName,
                    property,
                    category,
                    payrollCategory,
                    allowanceStandard,
                    personnelQuota,
                    establishmentCount,
                    actualCount,
                    organizationLevel,
                    systemCategory,
                    performanceAllowanceEnabled,
                    performanceCategory,
                    performanceRatio,
                    yearAllowanceCategory,
                    financeSource,
                    housingFundWithheld,
                    pensionWithheld);
        }
    }

    record IssueLogRow(
            Long id,
            String actorUsername,
            String subjectCode,
            String subjectName,
            String expiresAt,
            int organizationCount,
            String fingerprint,
            String summary,
            java.time.LocalDateTime createdAt
    ) {
    }
}

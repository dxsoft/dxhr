package com.dxsoft.rsgzgl.statistics;

import com.dxsoft.rsgzgl.common.SqlText;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class StatisticsRepository {

    private static final RowMapper<RetirementDueCandidate> RETIREMENT_DUE_CANDIDATE_MAPPER = (rs, rowNum) -> new RetirementDueCandidate(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("xb")),
            SqlText.trim(rs.getString("csny")),
            SqlText.trim(rs.getString("position_code")),
            SqlText.trim(rs.getString("position_name")),
            rs.getInt("yctxsj"));

    private final NamedParameterJdbcTemplate jdbcTemplate;

    StatisticsRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    PersonnelSummaryStatistics personnelSummary(OrganizationScope scope, String organizationCode) {
        if (scope.noneScope()) {
            return new PersonnelSummaryStatistics(0, 0, 0, 0);
        }
        MapSqlParameterSource parameters = scopedParameters(scope, organizationCode);
        Long organizationCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM dwbm o
                WHERE (:allOrganizations = TRUE OR o.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR o.dwbm = :organizationCode)
                """, parameters, Long.class);
        Long activePersonnelCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM dryjbxx p
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                """, parameters, Long.class);
        Long changedPersonnelCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM dryjbxxb p
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                """, parameters, Long.class);
        Long probationPersonnelCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM dryjbxx p
                JOIN hisbase h ON h.dwbm = p.dwbm AND h.grbm = p.grbm
                WHERE (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND h.zwbm2 LIKE '%F%'
                  AND (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                """, parameters, Long.class);
        return new PersonnelSummaryStatistics(
                organizationCount == null ? 0 : organizationCount,
                activePersonnelCount == null ? 0 : activePersonnelCount,
                changedPersonnelCount == null ? 0 : changedPersonnelCount,
                probationPersonnelCount == null ? 0 : probationPersonnelCount);
    }

    List<PayrollChangeSummaryStatistics> payrollChangeSummary(
            OrganizationScope scope,
            String organizationCode,
            String year,
            String month,
            List<String> changeTypes) {
        if (scope.noneScope()) {
            return List.of();
        }
        List<String> normalizedTypes = changeTypes == null
                ? List.of()
                : changeTypes.stream()
                        .filter(type -> type != null && !type.isBlank())
                        .map(String::trim)
                        .distinct()
                        .toList();
        String normalizedMonth = normalizeMonth(month);
        MapSqlParameterSource parameters = scopedParameters(scope, organizationCode)
                .addValue("year", year == null || year.isBlank() ? null : year.trim())
                .addValue("month", normalizedMonth)
                .addValue("changeTypesEmpty", normalizedTypes.isEmpty())
                .addValue("changeTypes", normalizedTypes.isEmpty() ? List.of("__NONE__") : normalizedTypes);
        return jdbcTemplate.query("""
                SELECT CASE
                           WHEN TRIM(h.jslb) REGEXP '^[?？]+$' THEN '未知类别'
                           ELSE TRIM(h.jslb)
                       END AS change_type,
                       CONCAT(h.jsnf, LPAD(TRIM(h.jsyf), 2, '0')) AS period,
                       COUNT(*) AS change_count,
                       COUNT(DISTINCT CONCAT(h.dwbm, '-', h.grbm)) AS personnel_count
                FROM hisbase h
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (:year IS NULL OR h.jsnf = :year)
                  AND (:month IS NULL OR LPAD(TRIM(h.jsyf), 2, '0') = :month)
                  AND (:changeTypesEmpty = TRUE OR CASE
                           WHEN TRIM(h.jslb) REGEXP '^[?？]+$' THEN '未知类别'
                           ELSE TRIM(h.jslb)
                       END IN (:changeTypes))
                  AND h.jslb IS NOT NULL
                  AND TRIM(h.jslb) <> ''
                GROUP BY change_type, h.jsnf, h.jsyf
                ORDER BY h.jsnf DESC, h.jsyf DESC, change_type
                LIMIT 500
                """, parameters, (rs, rowNum) -> new PayrollChangeSummaryStatistics(
                rs.getString("change_type"),
                rs.getString("period"),
                rs.getLong("change_count"),
                rs.getLong("personnel_count")));
    }

    List<String> payrollChangeTypes(OrganizationScope scope, String organizationCode, String year, String month) {
        if (scope.noneScope()) {
            return List.of();
        }
        String normalizedMonth = normalizeMonth(month);
        MapSqlParameterSource parameters = scopedParameters(scope, organizationCode)
                .addValue("year", year == null || year.isBlank() ? null : year.trim())
                .addValue("month", normalizedMonth);
        return jdbcTemplate.query("""
                SELECT DISTINCT CASE
                           WHEN TRIM(h.jslb) REGEXP '^[?？]+$' THEN '未知类别'
                           ELSE TRIM(h.jslb)
                       END AS change_type
                FROM hisbase h
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (:year IS NULL OR h.jsnf = :year)
                  AND (:month IS NULL OR LPAD(TRIM(h.jsyf), 2, '0') = :month)
                  AND h.jslb IS NOT NULL
                  AND TRIM(h.jslb) <> ''
                ORDER BY change_type
                """, parameters, (rs, rowNum) -> rs.getString("change_type"));
    }

    private static String normalizeMonth(String month) {
        if (month == null || month.isBlank()) {
            return null;
        }
        String digits = month.trim().replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        try {
            int value = Integer.parseInt(digits);
            if (value < 1 || value > 12) {
                return null;
            }
            return String.format("%02d", value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    List<RetirementDueCandidate> findRetirementDueCandidates(
            OrganizationScope scope,
            String organizationCode,
            String keyword,
            String maleBirthUpperBound,
            String femaleBirthUpperBound) {
        if (scope.noneScope()) {
            return List.of();
        }
        String trimmedOrganization = organizationCode == null || organizationCode.isBlank() ? null : organizationCode.trim();
        String maleUpper = emptyToNull(maleBirthUpperBound);
        String femaleUpper = emptyToNull(femaleBirthUpperBound);
        if (maleUpper == null || femaleUpper == null) {
            return List.of();
        }
        MapSqlParameterSource parameters = scopedParameters(scope, trimmedOrganization)
                .addValue("maleBirthUpper", maleUpper)
                .addValue("femaleBirthUpper", femaleUpper)
                .addValue("keyword", keyword == null || keyword.isBlank() ? null : keyword.trim())
                .addValue("keywordLike", keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%");
        // tip 用一次物化派生表，避免对每人做 hisbase 相关子查询；出生年月按性别预筛，显著缩小候选集。
        return jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.xb, p.csny, p.yctxsj,
                       COALESCE(
                           NULLIF(TRIM(h.zwbm2), ''),
                           NULLIF(TRIM(p.zjbm), '')
                       ) AS position_code,
                       COALESCE(
                           NULLIF(TRIM(h.zwgw2), ''),
                           NULLIF(TRIM(p.xrzw), '')
                       ) AS position_name
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                LEFT JOIN (
                    SELECT dwbm, grbm, MAX(id) AS id
                    FROM hisbase
                    WHERE sid IS NULL OR sid = ''
                    GROUP BY dwbm, grbm
                ) tip ON tip.dwbm = p.dwbm AND tip.grbm = p.grbm
                LEFT JOIN hisbase h ON h.id = tip.id
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND REPLACE(REPLACE(COALESCE(NULLIF(TRIM(p.csny), ''), '000000'), '.', ''), '-', '') >= '190001'
                  AND (
                        CASE
                          WHEN TRIM(p.xb) IN ('男', '1') OR UPPER(TRIM(p.xb)) IN ('M', 'MALE')
                            THEN REPLACE(REPLACE(COALESCE(NULLIF(TRIM(p.csny), ''), '000000'), '.', ''), '-', '')
                                 <= :maleBirthUpper
                          ELSE REPLACE(REPLACE(COALESCE(NULLIF(TRIM(p.csny), ''), '000000'), '.', ''), '-', '')
                               <= :femaleBirthUpper
                        END
                      )
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR p.zjbm LIKE :keywordLike
                       OR p.xrzw LIKE :keywordLike
                       OR h.zwbm2 LIKE :keywordLike
                       OR h.zwgw2 LIKE :keywordLike)
                ORDER BY p.dwbm, p.grbm
                """, parameters, RETIREMENT_DUE_CANDIDATE_MAPPER);
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private MapSqlParameterSource scopedParameters(OrganizationScope scope, String organizationCode) {
        String trimmedOrganizationCode = organizationCode == null || organizationCode.isBlank() ? null : organizationCode.trim();
        return new MapSqlParameterSource()
                .addValue("allOrganizations", scope.all())
                .addValue("organizationCodes", scope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : scope.organizationCodes())
                .addValue("organizationCode", trimmedOrganizationCode);
    }
}

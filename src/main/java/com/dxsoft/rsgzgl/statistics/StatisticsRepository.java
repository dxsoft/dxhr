package com.dxsoft.rsgzgl.statistics;

import com.dxsoft.rsgzgl.common.SqlText;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.List;
import java.util.Optional;
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

    List<PayrollChangeSummaryStatistics> payrollChangeSummary(OrganizationScope scope, String organizationCode, String year) {
        if (scope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = scopedParameters(scope, organizationCode)
                .addValue("year", year == null || year.isBlank() ? null : year.trim());
        return jdbcTemplate.query("""
                SELECT CONCAT(h.jsnf, h.jsyf) AS period,
                       COUNT(*) AS change_count,
                       COUNT(DISTINCT CONCAT(h.dwbm, '-', h.grbm)) AS personnel_count
                FROM hisbase h
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (:year IS NULL OR h.jsnf = :year)
                GROUP BY h.jsnf, h.jsyf
                ORDER BY h.jsnf DESC, h.jsyf DESC
                LIMIT 24
                """, parameters, (rs, rowNum) -> new PayrollChangeSummaryStatistics(
                rs.getString("period"),
                rs.getLong("change_count"),
                rs.getLong("personnel_count")));
    }

    List<RetirementDueCandidate> findRetirementDueCandidates(OrganizationScope scope, String organizationCode, String keyword) {
        if (scope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = scopedParameters(scope, organizationCode)
                .addValue("keyword", keyword == null || keyword.isBlank() ? null : keyword.trim())
                .addValue("keywordLike", keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%");
        return jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.xb, p.csny, p.yctxsj,
                       z.zwbm AS position_code, z.xzzw AS position_name
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                LEFT JOIN dryzwbh z ON z.dwbm = p.dwbm AND z.grbm = p.grbm AND z.xrzwbz = '1'
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND REPLACE(COALESCE(NULLIF(TRIM(p.csny), ''), '000000'), '.', '') >= '190001'
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR z.xzzw LIKE :keywordLike
                       OR z.zwbm LIKE :keywordLike)
                ORDER BY p.dwbm, p.grbm
                """, parameters, RETIREMENT_DUE_CANDIDATE_MAPPER);
    }

    private MapSqlParameterSource scopedParameters(OrganizationScope scope, String organizationCode) {
        String trimmedOrganizationCode = organizationCode == null || organizationCode.isBlank() ? null : organizationCode.trim();
        return new MapSqlParameterSource()
                .addValue("allOrganizations", scope.all())
                .addValue("organizationCodes", scope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : scope.organizationCodes())
                .addValue("organizationCode", trimmedOrganizationCode);
    }
}

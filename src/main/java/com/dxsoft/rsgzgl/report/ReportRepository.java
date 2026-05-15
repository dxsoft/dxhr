package com.dxsoft.rsgzgl.report;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.SqlText;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class ReportRepository {

    private static final RowMapper<ReportTypeOption> REPORT_TYPE_MAPPER = (rs, rowNum) -> new ReportTypeOption(
            SqlText.trim(rs.getString("lbbm")),
            SqlText.trim(rs.getString("cname")),
            SqlText.trim(rs.getString("ctitle")),
            SqlText.trim(rs.getString("cfilename")),
            SqlText.trim(rs.getString("rpttype")),
            SqlText.trim(rs.getString("bblb")),
            SqlText.trim(rs.getString("dyclb")),
            SqlText.trim(rs.getString("cdefault")));

    private static final RowMapper<PayrollChangeRegisterRow> PAYROLL_CHANGE_REGISTER_MAPPER = (rs, rowNum) -> new PayrollChangeRegisterRow(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("jsnf")),
            SqlText.trim(rs.getString("jsyf")),
            SqlText.trim(rs.getString("jslb")),
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            SqlText.trim(rs.getString("jbgzjb2")),
            SqlText.trim(rs.getString("zwgzdc2")),
            rs.getInt("zwgzse2"),
            rs.getInt("jbgzse2"),
            rs.getInt("jsdjgz2"),
            rs.getInt("dfbt2"),
            rs.getInt("blfb2"),
            rs.getInt("jxjt"),
            rs.getBigDecimal("njbt"),
            rs.getInt("pgbc"),
            rs.getInt("hj2"));

    private final NamedParameterJdbcTemplate jdbc;

    ReportRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    List<ReportTypeOption> findReportTypes(String category, PageRequest pageRequest) {
        return jdbc.query("""
                SELECT lbbm, cname, ctitle, cfilename, rpttype, bblb, dyclb, cdefault
                FROM rptinfo
                WHERE (:category IS NULL OR bblb = :category OR dyclb = :category)
                ORDER BY lbbm
                LIMIT :limit OFFSET :offset
                """, new MapSqlParameterSource()
                .addValue("category", emptyToNull(category))
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset()), REPORT_TYPE_MAPPER);
    }

    long countReportTypes(String category) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM rptinfo
                WHERE (:category IS NULL OR bblb = :category OR dyclb = :category)
                """, new MapSqlParameterSource("category", emptyToNull(category)), Long.class);
        return count == null ? 0 : count;
    }

    List<PayrollChangeRegisterRow> findPayrollChangeCandidates(
            OrganizationScope scope,
            String organizationFilter,
            String reportTypeCode,
            String year,
            String keyword,
            PageRequest pageRequest) {
        if (scope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = candidateParameters(scope, organizationFilter, reportTypeCode, year, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbc.query("""
                SELECT h.id, h.dwbm, dw.dwmc, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                       h.zwbm2, h.zwgw2, h.jbgzjb2, h.zwgzdc2,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.blfb2,
                       h.jxjt, h.njbt, h.pgbc, h.hj2
                FROM hisbase h
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR h.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:year IS NULL OR h.jsnf = :year)
                  AND (:reportText IS NULL OR :isAllRegister = TRUE
                       OR (:isSalaryLevel = TRUE AND h.jslb LIKE '%薪级%')
                       OR (:isStepPromotion = TRUE AND h.jslb NOT LIKE '%薪级%' AND (h.jslb LIKE '%档%' OR h.jslb LIKE '%正常晋升%'))
                       OR (:isGradePromotion = TRUE AND h.jslb LIKE '%级别%')
                       OR (:isPositionChange = TRUE AND (h.jslb LIKE '%职务%' OR h.jslb LIKE '%岗位%' OR h.jslb LIKE '%职级%'))
                       OR (:isAllowanceChange = TRUE AND (h.jslb LIKE '%津贴%' OR h.jslb LIKE '%补贴%' OR h.jslb LIKE '%绩效%'))
                       OR (:isRegularization = TRUE AND (h.jslb LIKE '%转正%' OR h.jslb LIKE '%见习%'))
                       OR (:isTransfer = TRUE AND (h.jslb LIKE '%调入%' OR h.jslb LIKE '%新进%'))
                       OR h.jslb LIKE :reportTextLike)
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike
                       OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY h.dwbm, h.grbm, h.jsnf DESC, h.jsyf DESC, h.jslb
                LIMIT :limit OFFSET :offset
                """, params, PAYROLL_CHANGE_REGISTER_MAPPER);
    }

    long countPayrollChangeCandidates(
            OrganizationScope scope,
            String organizationFilter,
            String reportTypeCode,
            String year,
            String keyword) {
        if (scope.noneScope()) {
            return 0;
        }
        MapSqlParameterSource params = candidateParameters(scope, organizationFilter, reportTypeCode, year, keyword);
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM hisbase h
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR h.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:year IS NULL OR h.jsnf = :year)
                  AND (:reportText IS NULL OR :isAllRegister = TRUE
                       OR (:isSalaryLevel = TRUE AND h.jslb LIKE '%薪级%')
                       OR (:isStepPromotion = TRUE AND h.jslb NOT LIKE '%薪级%' AND (h.jslb LIKE '%档%' OR h.jslb LIKE '%正常晋升%'))
                       OR (:isGradePromotion = TRUE AND h.jslb LIKE '%级别%')
                       OR (:isPositionChange = TRUE AND (h.jslb LIKE '%职务%' OR h.jslb LIKE '%岗位%' OR h.jslb LIKE '%职级%'))
                       OR (:isAllowanceChange = TRUE AND (h.jslb LIKE '%津贴%' OR h.jslb LIKE '%补贴%' OR h.jslb LIKE '%绩效%'))
                       OR (:isRegularization = TRUE AND (h.jslb LIKE '%转正%' OR h.jslb LIKE '%见习%'))
                       OR (:isTransfer = TRUE AND (h.jslb LIKE '%调入%' OR h.jslb LIKE '%新进%'))
                       OR h.jslb LIKE :reportTextLike)
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike
                       OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                """, params, Long.class);
        return count == null ? 0 : count;
    }

    private MapSqlParameterSource candidateParameters(
            OrganizationScope scope,
            String organizationFilter,
            String reportTypeCode,
            String year,
            String keyword) {
        String reportText = reportTypeText(reportTypeCode);
        String normalized = reportText == null ? "" : reportText;
        return parameters(scope, organizationFilter, null, keyword)
                .addValue("year", emptyToNull(year))
                .addValue("reportText", emptyToNull(reportText))
                .addValue("reportTextLike", reportText == null ? null : "%" + reportText + "%")
                .addValue("isAllRegister", normalized.contains("全体工作人员工资花名册") || normalized.contains("工作人员工资花名册"))
                .addValue("isSalaryLevel", normalized.contains("薪级"))
                .addValue("isStepPromotion", normalized.contains("档次") || normalized.contains("晋档"))
                .addValue("isGradePromotion", normalized.contains("级别滚动") || normalized.contains("正常级别") || normalized.contains("晋级"))
                .addValue("isPositionChange", normalized.contains("职务") || normalized.contains("职级"))
                .addValue("isAllowanceChange", normalized.contains("津贴") || normalized.contains("补贴") || normalized.contains("绩效"))
                .addValue("isRegularization", normalized.contains("转正") || normalized.contains("见习"))
                .addValue("isTransfer", normalized.contains("调入") || normalized.contains("新进"));
    }

    private String reportTypeText(String reportTypeCode) {
        String code = emptyToNull(reportTypeCode);
        if (code == null) {
            return null;
        }
        return jdbc.queryForList("""
                        SELECT CONCAT(cname, ' ', ctitle, ' ', bblb, ' ', dyclb)
                        FROM rptinfo
                        WHERE lbbm = :code
                        LIMIT 1
                        """, new MapSqlParameterSource("code", code), String.class)
                .stream()
                .findFirst()
                .map(SqlText::trim)
                .orElse(null);
    }

    List<PayrollChangeRegisterRow> findPayrollChangeRegister(
            OrganizationScope scope,
            String organizationFilter,
            String period,
            String keyword,
            PageRequest pageRequest) {
        if (scope.noneScope()) {
            return List.of();
        }
        return jdbc.query("""
                SELECT h.id, h.dwbm, dw.dwmc, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                       h.zwbm2, h.zwgw2, h.jbgzjb2, h.zwgzdc2,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.blfb2,
                       h.jxjt, h.njbt, h.pgbc, h.hj2
                FROM hisbase h
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR h.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:period IS NULL OR CONCAT(h.jsnf, h.jsyf) = :period)
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike
                       OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY h.dwbm, h.grbm, h.jsnf DESC, h.jsyf DESC, h.jslb
                LIMIT :limit OFFSET :offset
                """, parameters(scope, organizationFilter, period, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset()), PAYROLL_CHANGE_REGISTER_MAPPER);
    }

    long countPayrollChangeRegister(OrganizationScope scope, String organizationFilter, String period, String keyword) {
        if (scope.noneScope()) {
            return 0;
        }
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM hisbase h
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR h.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:period IS NULL OR CONCAT(h.jsnf, h.jsyf) = :period)
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike
                       OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                """, parameters(scope, organizationFilter, period, keyword), Long.class);
        return count == null ? 0 : count;
    }

    private MapSqlParameterSource parameters(OrganizationScope scope, String organizationFilter, String period, String keyword) {
        String trimmedOrganization = emptyToNull(organizationFilter);
        String trimmedKeyword = emptyToNull(keyword);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", scope.all())
                .addValue("organizationCodes", scope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : scope.organizationCodes())
                .addValue("organizationFilter", trimmedOrganization)
                .addValue("organizationFilterLike", trimmedOrganization == null ? null : "%" + trimmedOrganization + "%")
                .addValue("period", emptyToNull(period))
                .addValue("keyword", trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null ? null : "%" + trimmedKeyword + "%");
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

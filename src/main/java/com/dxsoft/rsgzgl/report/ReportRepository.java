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

    private static final RowMapper<WageReform2006PublicNoticeRow> WAGE_REFORM_2006_PUBLIC_NOTICE_MAPPER = (rs, rowNum) -> new WageReform2006PublicNoticeRow(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("cjgzny")),
            rs.getInt("xlnx"),
            rs.getInt("zdgznx"),
            rs.getInt("kjnx"),
            rs.getInt("tgnx"),
            SqlText.trim(rs.getString("zwbm")),
            SqlText.trim(rs.getString("zwmc")),
            SqlText.trim(rs.getString("rzsj")),
            rs.getInt("rznx"),
            rs.getInt("zwkjnx"),
            SqlText.trim(rs.getString("zwbm1")),
            SqlText.trim(rs.getString("zwmc1")),
            SqlText.trim(rs.getString("rzsj1")),
            rs.getInt("rznx1"),
            rs.getInt("zwkjnx1"),
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("xl")),
            SqlText.trim(rs.getString("tgzwbm")),
            SqlText.trim(rs.getString("tgzw")),
            SqlText.trim(rs.getString("tgjb")),
            SqlText.trim(rs.getString("tgdc")),
            rs.getInt("gddc"),
            rs.getInt("dddc"),
            rs.getInt("gdjb"),
            rs.getInt("ddjb"),
            SqlText.trim(rs.getString("remark")));

    private static final RowMapper<PayrollChangeRegisterRow> PAYROLL_CHANGE_REGISTER_MAPPER = (rs, rowNum) -> new PayrollChangeRegisterRow(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("jsnf")),
            SqlText.trim(rs.getString("jsyf")),
            SqlText.trim(rs.getString("jslb")),
            SqlText.trim(rs.getString("before_zwbm2")),
            SqlText.trim(rs.getString("before_zwgw2")),
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

    List<ReportTypeOption> findReportTypes(String category, String reportType, PageRequest pageRequest) {
        return jdbc.query("""
                SELECT lbbm, cname, ctitle, cfilename, rpttype, bblb, dyclb, cdefault
                FROM rptinfo
                WHERE (:category IS NULL
                       OR bblb = :category
                       OR dyclb = :category
                       OR (:category = '审批表'
                           AND (cname LIKE '%审批表%' OR ctitle LIKE '%审批表%' OR lbmc LIKE '%审批%'))
                       OR (:category = '花名册'
                           AND (cname LIKE '%花名册%' OR ctitle LIKE '%花名册%' OR bblb LIKE '%花名册%')))
                  AND (:reportType IS NULL OR TRIM(rpttype) = :reportType)
                ORDER BY lbbm ASC, cname ASC
                LIMIT :limit OFFSET :offset
                """, new MapSqlParameterSource()
                .addValue("category", emptyToNull(category))
                .addValue("reportType", emptyToNull(reportType))
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset()), REPORT_TYPE_MAPPER);
    }

    long countReportTypes(String category, String reportType) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM rptinfo
                WHERE (:category IS NULL
                       OR bblb = :category
                       OR dyclb = :category
                       OR (:category = '审批表'
                           AND (cname LIKE '%审批表%' OR ctitle LIKE '%审批表%' OR lbmc LIKE '%审批%'))
                       OR (:category = '花名册'
                           AND (cname LIKE '%花名册%' OR ctitle LIKE '%花名册%' OR bblb LIKE '%花名册%')))
                  AND (:reportType IS NULL OR TRIM(rpttype) = :reportType)
                """, new MapSqlParameterSource()
                .addValue("category", emptyToNull(category))
                .addValue("reportType", emptyToNull(reportType)), Long.class);
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
                       prev.zwbm2 AS before_zwbm2, prev.zwgw2 AS before_zwgw2,
                       h.zwbm2, h.zwgw2, h.jbgzjb2, h.zwgzdc2,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.blfb2,
                       h.jxjt, h.njbt, h.pgbc, h.hj2
                FROM (
                    SELECT h.id, h.dwbm, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                           h.zwbm2, h.zwgw2, h.jbgzjb2, h.zwgzdc2,
                           h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.blfb2,
                           h.jxjt, h.njbt, h.pgbc, h.hj2
                    FROM hisbase h
                    LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                    WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                      AND (:organizationFilter IS NULL OR h.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                      AND (:year IS NULL OR h.jsnf = :year)
                      AND (:reportText IS NULL OR :isAllRegister = TRUE
                           OR (:isNormalStepOrSalary = TRUE AND h.jslb IN ('正常档次', '正常晋档', '正常薪级'))
                           OR (:isGradePromotion = TRUE AND h.jslb IN ('正常级别', '级别滚动'))
                           OR (:isPositionChange = TRUE AND (
                                h.jslb IN ('职务变化', '职级晋升')
                                OR h.jslb LIKE '职务%'
                                OR h.jslb LIKE '职级晋升%'
                           ))
                           OR (:isEducationChange = TRUE AND (h.jslb = '学历变化' OR h.jslb LIKE '学历%'))
                           OR (:isAllowanceChange = TRUE AND (
                                h.jslb IN ('津贴变化', '教护津贴', '警衔津贴', '审判津贴', '检察津贴')
                                OR h.jslb LIKE '%津贴%'
                                OR h.jslb LIKE '%补贴%'
                                OR h.jslb LIKE '%绩效%'
                           ))
                           OR (:isInternSalary = TRUE AND (h.jslb = '见习工资' OR h.jslb LIKE '见习%'))
                           OR (:isRegularization = TRUE AND (h.jslb = '转正定级' OR h.jslb LIKE '转正%'))
                           OR (:isHighGradeRegularization = TRUE AND (
                                h.jslb LIKE '%高定%'
                                OR h.jslb = '转正定级'
                                OR h.jslb LIKE '转正%'
                           ))
                           OR (:isTransfer = TRUE AND (
                                h.jslb IN ('调入定资', '新进工资')
                                OR h.jslb LIKE '调入%'
                                OR h.jslb LIKE '新进%'
                           ))
                           OR (:isOtherChange = TRUE AND (h.jslb = '其它情况' OR h.jslb LIKE '其它%' OR h.jslb LIKE '其他%'))
                           OR (:isDemobilized = TRUE AND (h.jslb = '转业定资' OR h.jslb LIKE '转业%'))
                           OR (:isVeteran = TRUE AND (h.jslb = '退伍定资' OR h.jslb LIKE '退伍%'))
                           OR (:isRewardPromotion = TRUE AND (h.jslb = '奖励晋升' OR h.jslb LIKE '奖励%'))
                           OR (:isPenaltyDemotion = TRUE AND (h.jslb = '降资处分' OR h.jslb LIKE '降资%'))
                           OR (:isStandardAdjust = TRUE AND (
                                h.jslb IN ('调标晋升', '工资调标', '调整标准')
                                OR h.jslb LIKE '调标%'
                           ))
                           OR (:isFloatingFixed = TRUE AND (h.jslb LIKE '%浮动%' OR h.jslb LIKE '浮动%'))
                           OR (:isWageReform = TRUE AND (h.jslb LIKE '%套改%' OR h.jslb LIKE '套改%'))
                           OR (:isRankChange = TRUE AND (
                                h.jslb IN ('警衔变化', '法官等级', '检察等级', '监察等级')
                                OR h.jslb LIKE '警衔变化%'
                                OR h.jslb LIKE '法官等级%'
                                OR h.jslb LIKE '检察等级%'
                                OR h.jslb LIKE '监察等级%'
                           ))
                           OR (:useReportTextFallback = TRUE AND h.jslb LIKE :reportTextLike))
                      AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike
                           OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike
                           OR CONCAT(h.dwbm, '-', h.grbm) LIKE :keywordLike
                           OR CONCAT(h.dwbm, h.grbm) LIKE :keywordLike)
                    ORDER BY h.dwbm, h.grbm, h.jsnf DESC, h.jsyf DESC, h.jslb
                    LIMIT :limit OFFSET :offset
                ) h
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                LEFT JOIN hisbase prev ON prev.sid = h.id
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
                       OR (:isNormalStepOrSalary = TRUE AND h.jslb IN ('正常档次', '正常晋档', '正常薪级'))
                       OR (:isGradePromotion = TRUE AND h.jslb IN ('正常级别', '级别滚动'))
                       OR (:isPositionChange = TRUE AND (
                            h.jslb IN ('职务变化', '职级晋升')
                            OR h.jslb LIKE '职务%'
                            OR h.jslb LIKE '职级晋升%'
                       ))
                       OR (:isEducationChange = TRUE AND (h.jslb = '学历变化' OR h.jslb LIKE '学历%'))
                       OR (:isAllowanceChange = TRUE AND (
                            h.jslb IN ('津贴变化', '教护津贴', '警衔津贴', '审判津贴', '检察津贴')
                            OR h.jslb LIKE '%津贴%'
                            OR h.jslb LIKE '%补贴%'
                            OR h.jslb LIKE '%绩效%'
                       ))
                       OR (:isInternSalary = TRUE AND (h.jslb = '见习工资' OR h.jslb LIKE '见习%'))
                       OR (:isRegularization = TRUE AND (h.jslb = '转正定级' OR h.jslb LIKE '转正%'))
                       OR (:isHighGradeRegularization = TRUE AND (
                            h.jslb LIKE '%高定%'
                            OR h.jslb = '转正定级'
                            OR h.jslb LIKE '转正%'
                       ))
                       OR (:isTransfer = TRUE AND (
                            h.jslb IN ('调入定资', '新进工资')
                            OR h.jslb LIKE '调入%'
                            OR h.jslb LIKE '新进%'
                       ))
                       OR (:isOtherChange = TRUE AND (h.jslb = '其它情况' OR h.jslb LIKE '其它%' OR h.jslb LIKE '其他%'))
                       OR (:isDemobilized = TRUE AND (h.jslb = '转业定资' OR h.jslb LIKE '转业%'))
                       OR (:isVeteran = TRUE AND (h.jslb = '退伍定资' OR h.jslb LIKE '退伍%'))
                       OR (:isRewardPromotion = TRUE AND (h.jslb = '奖励晋升' OR h.jslb LIKE '奖励%'))
                       OR (:isPenaltyDemotion = TRUE AND (h.jslb = '降资处分' OR h.jslb LIKE '降资%'))
                       OR (:isStandardAdjust = TRUE AND (
                            h.jslb IN ('调标晋升', '工资调标', '调整标准')
                            OR h.jslb LIKE '调标%'
                       ))
                       OR (:isFloatingFixed = TRUE AND (h.jslb LIKE '%浮动%' OR h.jslb LIKE '浮动%'))
                       OR (:isWageReform = TRUE AND (h.jslb LIKE '%套改%' OR h.jslb LIKE '套改%'))
                       OR (:isRankChange = TRUE AND (
                            h.jslb IN ('警衔变化', '法官等级', '检察等级', '监察等级')
                            OR h.jslb LIKE '警衔变化%'
                            OR h.jslb LIKE '法官等级%'
                            OR h.jslb LIKE '检察等级%'
                            OR h.jslb LIKE '监察等级%'
                       ))
                       OR (:useReportTextFallback = TRUE AND h.jslb LIKE :reportTextLike))
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike
                       OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike
                       OR CONCAT(h.dwbm, '-', h.grbm) LIKE :keywordLike
                       OR CONCAT(h.dwbm, h.grbm) LIKE :keywordLike)
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
        boolean isAllRegister = normalized.contains("全体工作人员工资花名册") || normalized.contains("工作人员工资花名册");
        boolean isHighGradeRegularization = normalized.contains("高定");
        boolean isEducationChange = normalized.contains("学历");
        boolean isWageReform = normalized.contains("套改");
        boolean isFloatingFixed = normalized.contains("浮动");
        boolean isOtherChange = normalized.contains("其它情况") || normalized.contains("其他情况");
        boolean isDemobilized = normalized.contains("转业");
        boolean isVeteran = normalized.contains("退伍");
        boolean isRewardPromotion = normalized.contains("奖励晋升");
        boolean isPenaltyDemotion = normalized.contains("降资");
        boolean isStandardAdjust = normalized.contains("调标");
        boolean isRankChange = (normalized.contains("警衔变化")
                || normalized.contains("法官等级")
                || normalized.contains("检察官等级")
                || normalized.contains("监察等级")
                || normalized.contains("等级变化"))
                && !normalized.contains("津贴");
        boolean isAllowanceChange = (normalized.contains("津贴") || normalized.contains("补贴") || normalized.contains("绩效"))
                && !isRankChange;
        boolean isNormalStepOrSalary = (normalized.contains("薪级")
                || normalized.contains("档次")
                || normalized.contains("晋档")
                || normalized.contains("正常晋升"))
                && !isHighGradeRegularization;
        boolean isGradePromotion = (normalized.contains("级别滚动") || normalized.contains("正常级别") || normalized.contains("晋级"))
                && !isRewardPromotion;
        boolean isPositionChange = (normalized.contains("职务") || normalized.contains("职级")) && !isWageReform;
        boolean isInternSalary = normalized.contains("见习");
        boolean isRegularization = normalized.contains("转正") && !isHighGradeRegularization;
        boolean isTransfer = normalized.contains("调入") || normalized.contains("新进");
        boolean typedFilter = isAllRegister || isNormalStepOrSalary || isGradePromotion || isPositionChange
                || isEducationChange || isAllowanceChange || isInternSalary || isRegularization
                || isHighGradeRegularization || isTransfer || isOtherChange || isDemobilized || isVeteran
                || isRewardPromotion || isPenaltyDemotion || isStandardAdjust || isFloatingFixed
                || isWageReform || isRankChange;
        return parameters(scope, organizationFilter, null, keyword)
                .addValue("year", emptyToNull(year))
                .addValue("reportText", emptyToNull(reportText))
                .addValue("reportTextLike", reportText == null ? null : "%" + reportText + "%")
                .addValue("isAllRegister", isAllRegister)
                .addValue("isNormalStepOrSalary", isNormalStepOrSalary)
                .addValue("isGradePromotion", isGradePromotion)
                .addValue("isPositionChange", isPositionChange)
                .addValue("isEducationChange", isEducationChange)
                .addValue("isAllowanceChange", isAllowanceChange)
                .addValue("isInternSalary", isInternSalary)
                .addValue("isRegularization", isRegularization)
                .addValue("isHighGradeRegularization", isHighGradeRegularization)
                .addValue("isTransfer", isTransfer)
                .addValue("isOtherChange", isOtherChange)
                .addValue("isDemobilized", isDemobilized)
                .addValue("isVeteran", isVeteran)
                .addValue("isRewardPromotion", isRewardPromotion)
                .addValue("isPenaltyDemotion", isPenaltyDemotion)
                .addValue("isStandardAdjust", isStandardAdjust)
                .addValue("isFloatingFixed", isFloatingFixed)
                .addValue("isWageReform", isWageReform)
                .addValue("isRankChange", isRankChange)
                .addValue("useReportTextFallback", emptyToNull(reportText) != null && !typedFilter);
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
                       prev.zwbm2 AS before_zwbm2, prev.zwgw2 AS before_zwgw2,
                       h.zwbm2, h.zwgw2, h.jbgzjb2, h.zwgzdc2,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.blfb2,
                       h.jxjt, h.njbt, h.pgbc, h.hj2
                FROM hisbase h
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                LEFT JOIN hisbase prev ON prev.sid = h.id
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR h.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:period IS NULL OR CONCAT(h.jsnf, h.jsyf) = :period)
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike
                       OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike
                       OR prev.zwgw2 LIKE :keywordLike)
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

    List<WageReform2006PublicNoticeRow> findWageReform2006PublicNoticeRows(
            OrganizationScope scope,
            String organizationFilter,
            String keyword,
            PageRequest pageRequest) {
        if (scope.noneScope()) {
            return List.of();
        }
        return jdbc.query("""
                SELECT t.id, t.dwbm, dw.dwmc, t.grbm,
                       COALESCE(NULLIF(TRIM(p.xm), ''), NULLIF(TRIM(pb.xm), ''), '') AS xm,
                       t.cjgzny, t.xlnx, t.zdgznx, t.kjnx, t.tgnx,
                       t.zwbm, t.zwmc, t.rzsj, t.rznx, t.zwkjnx,
                       t.zwbm1, t.zwmc1, t.rzsj1, t.rznx1, t.zwkjnx1,
                       t.xlbm, t.xl, t.tgzwbm, t.tgzw, t.tgjb, t.tgdc,
                       t.gddc, t.dddc, t.gdjb, t.ddjb, t.remark
                FROM dtgxx t
                LEFT JOIN dryjbxx p ON p.dwbm = t.dwbm AND p.grbm = t.grbm
                LEFT JOIN dryjbxxb pb ON pb.dwbm = t.dwbm AND pb.grbm = t.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = t.dwbm
                WHERE (:allOrganizations = TRUE OR t.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR t.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:keyword IS NULL OR t.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike OR pb.xm LIKE :keywordLike
                       OR t.zwmc LIKE :keywordLike OR t.zwmc1 LIKE :keywordLike
                       OR t.tgzw LIKE :keywordLike OR t.remark LIKE :keywordLike)
                ORDER BY t.dwbm, t.grbm, t.id DESC
                LIMIT :limit OFFSET :offset
                """, parameters(scope, organizationFilter, null, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset()), WAGE_REFORM_2006_PUBLIC_NOTICE_MAPPER);
    }

    long countWageReform2006PublicNoticeRows(OrganizationScope scope, String organizationFilter, String keyword) {
        if (scope.noneScope()) {
            return 0;
        }
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM dtgxx t
                LEFT JOIN dryjbxx p ON p.dwbm = t.dwbm AND p.grbm = t.grbm
                LEFT JOIN dryjbxxb pb ON pb.dwbm = t.dwbm AND pb.grbm = t.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = t.dwbm
                WHERE (:allOrganizations = TRUE OR t.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR t.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:keyword IS NULL OR t.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike OR pb.xm LIKE :keywordLike
                       OR t.zwmc LIKE :keywordLike OR t.zwmc1 LIKE :keywordLike
                       OR t.tgzw LIKE :keywordLike OR t.remark LIKE :keywordLike)
                """, parameters(scope, organizationFilter, null, keyword), Long.class);
        return count == null ? 0 : count;
    }

    private static final RowMapper<PersonnelReportCandidateRow> PERSONNEL_REPORT_CANDIDATE_MAPPER = (rs, rowNum) -> new PersonnelReportCandidateRow(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("xb")),
            SqlText.trim(rs.getString("csny")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("xrzw")));

    List<PersonnelReportCandidateRow> findPersonnelReportCandidates(
            OrganizationScope scope,
            String organizationFilter,
            String keyword,
            PageRequest pageRequest) {
        if (scope.noneScope()) {
            return List.of();
        }
        return jdbc.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.xb, p.csny, p.ryfl, p.xrzw
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR p.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:keyword IS NULL OR p.xm LIKE :keywordLike OR p.grbm LIKE :keywordLike OR p.sfzh LIKE :keywordLike
                       OR p.xrzw LIKE :keywordLike)
                ORDER BY p.dwbm, p.grbm
                LIMIT :limit OFFSET :offset
                """, parameters(scope, organizationFilter, null, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset()), PERSONNEL_REPORT_CANDIDATE_MAPPER);
    }

    long countPersonnelReportCandidates(OrganizationScope scope, String organizationFilter, String keyword) {
        if (scope.noneScope()) {
            return 0;
        }
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR p.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:keyword IS NULL OR p.xm LIKE :keywordLike OR p.grbm LIKE :keywordLike OR p.sfzh LIKE :keywordLike
                       OR p.xrzw LIKE :keywordLike)
                """, parameters(scope, organizationFilter, null, keyword), Long.class);
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

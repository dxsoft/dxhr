package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.SensitiveData;
import com.dxsoft.rsgzgl.common.SqlText;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PersonnelRepository {

    private static final List<TablePair> PERSONNEL_CHANGE_TABLE_PAIRS = List.of(
            new TablePair("dxl", "dxlb"),
            new TablePair("dryzwbh", "dryzwbhb"),
            new TablePair("dndkh", "dndkhb"),
            new TablePair("dtgxx", "dtgxxb"),
            new TablePair("tgqgz2006", "tgqgz2006b"),
            new TablePair("jx", "jxb"),
            new TablePair("jfjs", "jfjsb"),
            new TablePair("jytgyb", "jytgybb"),
            new TablePair("jytgzzbf", "jytgzzbfb"),
            new TablePair("hjxx", "hjxxb"),
            new TablePair("djxgz", "djxgzb")
    );

    private static final RowMapper<PersonnelComprehensiveQueryRecord> COMPREHENSIVE_QUERY_MAPPER = (rs, rowNum) -> new PersonnelComprehensiveQueryRecord(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SensitiveData.maskIdCard(rs.getString("sfzh")),
            SqlText.trim(rs.getString("xb")),
            SqlText.trim(rs.getString("csny")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("dwsx")),
            SqlText.trim(rs.getString("gwfl")),
            SqlText.trim(rs.getString("cjgzny")),
            SqlText.trim(rs.getString("zzny")),
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("zgxl")),
            rs.getInt("gznx"),
            SqlText.trim(rs.getString("appointment_position_code")),
            SqlText.trim(rs.getString("appointment_position_name")),
            SqlText.trim(rs.getString("appointment_start")),
            payrollPeriod(rs.getString("jsnf"), rs.getString("jsyf")),
            SqlText.trim(rs.getString("payroll_position_code")),
            SqlText.trim(rs.getString("payroll_position_name")),
            SqlText.trim(rs.getString("grade_level")),
            SqlText.trim(rs.getString("grade_step")),
            rs.getObject("total_salary") == null ? null : rs.getInt("total_salary"));

    private static String payrollPeriod(String year, String month) {
        String normalizedYear = SqlText.trim(year);
        String normalizedMonth = SqlText.trim(month);
        if (normalizedYear.isBlank()) {
            return "";
        }
        if (normalizedMonth.isBlank()) {
            return normalizedYear;
        }
        return normalizedYear + (normalizedMonth.length() == 1 ? "0" + normalizedMonth : normalizedMonth);
    }

    private static final RowMapper<PersonnelInformationCollectionShell> COLLECTION_SHELL_MAPPER = (rs, rowNum) -> new PersonnelInformationCollectionShell(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("dwbz")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("sfzh")),
            SqlText.trim(rs.getString("xb")),
            SqlText.trim(rs.getString("csny")),
            SqlText.trim(rs.getString("mz")),
            SqlText.trim(rs.getString("zzmm")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("dwsx")),
            SqlText.trim(rs.getString("gwfl")),
            SqlText.trim(rs.getString("cjgzny")),
            SqlText.trim(rs.getString("zzny")),
            SqlText.trim(rs.getString("jrny")),
            SqlText.trim(rs.getString("jrfs")),
            rs.getInt("gznx"),
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("zgxl")),
            SqlText.trim(rs.getString("zwjb")),
            SqlText.trim(rs.getString("zjbm")),
            SqlText.trim(rs.getString("xrzw")),
            SqlText.trim(rs.getString("srny")),
            SqlText.trim(rs.getString("dah")),
            SqlText.trim(rs.getString("ydwzw")),
            SqlText.trim(rs.getString("yzwrzsj")),
            SqlText.trim(rs.getString("bgdwjc")),
            SqlText.trim(rs.getString("txsj")),
            SqlText.trim(rs.getString("jhlqsny")),
            rs.getInt("zdjhlnx"));

    private static final RowMapper<PersonnelInformationCollectionPayrollSnapshot> COLLECTION_PAYROLL_MAPPER = (rs, rowNum) -> new PersonnelInformationCollectionPayrollSnapshot(
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
            rs.getInt("hj2"));

    private static final RowMapper<PersonnelSummary> SUMMARY_MAPPER = (rs, rowNum) -> new PersonnelSummary(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SensitiveData.maskIdCard(rs.getString("sfzh")),
            SqlText.trim(rs.getString("xb")),
            SqlText.trim(rs.getString("csny")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("dwsx")),
            SqlText.trim(rs.getString("gwfl")),
            SqlText.trim(rs.getString("xrzw")),
            SqlText.trim(rs.getString("zjbm")),
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            SqlText.trim(rs.getString("bbz")),
            false,
            false,
            null
    );

    private static final RowMapper<PersonnelDetail> DETAIL_MAPPER = (rs, rowNum) -> new PersonnelDetail(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SensitiveData.maskIdCard(rs.getString("sfzh")),
            SqlText.trim(rs.getString("xb")),
            SqlText.trim(rs.getString("csny")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("dwsx")),
            SqlText.trim(rs.getString("gwfl")),
            SqlText.trim(rs.getString("cjgzny")),
            SqlText.trim(rs.getString("zzny")),
            SqlText.trim(rs.getString("zwjb")),
            SqlText.trim(rs.getString("zjbm")),
            SqlText.trim(rs.getString("xrzw")),
            SqlText.trim(rs.getString("srny")),
            rs.getInt("gznx"),
            SqlText.trim(rs.getString("zgxl")),
            SqlText.trim(rs.getString("txsj"))
    );


    private static int attachmentCount(java.sql.ResultSet rs) throws java.sql.SQLException {
        try {
            return rs.getInt("attachment_count");
        } catch (java.sql.SQLException ignored) {
            return 0;
        }
    }

    private static final RowMapper<PersonnelMaintenanceRecord> MAINTENANCE_MAPPER = (rs, rowNum) -> new PersonnelMaintenanceRecord(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("dwbz")),
            SqlText.trim(rs.getString("org_gzczbz")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("sfzh")),
            SqlText.trim(rs.getString("xb")),
            SqlText.trim(rs.getString("csny")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("dwsx")),
            SqlText.trim(rs.getString("gwfl")),
            SqlText.trim(rs.getString("cjgzny")),
            SqlText.trim(rs.getString("zzny")),
            rs.getInt("gznx"),
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("zgxl")),
            SqlText.trim(rs.getString("zwjb")),
            SqlText.trim(rs.getString("zjbm")),
            SqlText.trim(rs.getString("xrzw")),
            SqlText.trim(rs.getString("srny")),
            SqlText.trim(rs.getString("mz")),
            SqlText.trim(rs.getString("zzmm")),
            SqlText.trim(rs.getString("dah")),
            SqlText.trim(rs.getString("jrny")),
            SqlText.trim(rs.getString("jrfs")),
            SqlText.trim(rs.getString("bbz")),
            SqlText.trim(rs.getString("tc")),
            SqlText.trim(rs.getString("org_dwsx")),
            rs.getBoolean("jkjs"),
            readApprovalActorFields(rs).submittedBy(),
            readApprovalActorFields(rs).submittedAt(),
            readApprovalActorFields(rs).approvedBy(),
            readApprovalActorFields(rs).approvedAt());

    private static final RowMapper<PositionRecord> POSITION_MAPPER = (rs, rowNum) -> new PositionRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xrzwbm")),
            SqlText.trim(rs.getString("xrzw")),
            SqlText.trim(rs.getString("zwjb")),
            SqlText.trim(rs.getString("zjbm")),
            SqlText.trim(rs.getString("zwbm")),
            SqlText.trim(rs.getString("xzzw")),
            SqlText.trim(rs.getString("srny")),
            rs.getInt("kjnx"),
            SqlText.trim(rs.getString("xrzwbz")),
            SqlText.trim(rs.getString("jsbz")),
            SqlText.trim(rs.getString("zwlb")),
            readOptionalInteger(rs, "linked_award_id"),
            normalizeSubrecordApproval(rs.getString("bbz")),
            rs.getBoolean("app_created"),
            readApprovalActorFields(rs).submittedBy(),
            readApprovalActorFields(rs).submittedAt(),
            readApprovalActorFields(rs).approvedBy(),
            readApprovalActorFields(rs).approvedAt(),
            rs.getInt("attachment_count")
    );

    private static final RowMapper<PersonnelPositionHistoryRecord> POSITION_HISTORY_MAPPER = (rs, rowNum) -> new PersonnelPositionHistoryRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("xrzwbm")),
            SqlText.trim(rs.getString("xrzw")),
            SqlText.trim(rs.getString("zwjb")),
            SqlText.trim(rs.getString("zjbm")),
            SqlText.trim(rs.getString("zwbm")),
            SqlText.trim(rs.getString("xzzw")),
            SqlText.trim(rs.getString("srny")),
            rs.getInt("kjnx"),
            SqlText.trim(rs.getString("xrzwbz")),
            SqlText.trim(rs.getString("jsbz"))
    );

    private static final RowMapper<EducationRecord> EDUCATION_MAPPER = (rs, rowNum) -> new EducationRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("xl")),
            SqlText.trim(rs.getString("byyx")),
            SqlText.trim(rs.getString("rxsj")),
            SqlText.trim(rs.getString("bysj")),
            rs.getInt("xz"),
            SqlText.trim(rs.getString("xllb")),
            SqlText.trim(rs.getString("bz")),
            normalizeSubrecordApproval(rs.getString("bbz")),
            rs.getBoolean("app_created"),
            readApprovalActorFields(rs).submittedBy(),
            readApprovalActorFields(rs).submittedAt(),
            readApprovalActorFields(rs).approvedBy(),
            readApprovalActorFields(rs).approvedAt(),
            attachmentCount(rs)
    );

    private static final RowMapper<PersonnelEducationHistoryRecord> EDUCATION_HISTORY_MAPPER = (rs, rowNum) -> new PersonnelEducationHistoryRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("xl")),
            SqlText.trim(rs.getString("byyx")),
            SqlText.trim(rs.getString("rxsj")),
            SqlText.trim(rs.getString("bysj")),
            rs.getInt("xz"),
            SqlText.trim(rs.getString("xllb")),
            SqlText.trim(rs.getString("bz"))
    );

    private static final RowMapper<AssessmentRecord> ASSESSMENT_MAPPER = (rs, rowNum) -> new AssessmentRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("khnd")),
            SqlText.trim(rs.getString("khjg")),
            normalizeSubrecordApproval(rs.getString("bbz")),
            rs.getBoolean("app_created"),
            readApprovalActorFields(rs).submittedBy(),
            readApprovalActorFields(rs).submittedAt(),
            readApprovalActorFields(rs).approvedBy(),
            readApprovalActorFields(rs).approvedAt(),
            attachmentCount(rs)
    );

    private static final RowMapper<AwardRecord> AWARD_MAPPER = (rs, rowNum) -> new AwardRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("hjmc")),
            SqlText.trim(rs.getString("sjdw")),
            SqlText.trim(rs.getString("jllx")),
            SqlText.trim(rs.getString("hjsj")),
            SqlText.trim(rs.getString("tqyjjssj")),
            SqlText.trim(rs.getString("qtqk")),
            rs.getInt("jldc"),
            rs.getInt("jljb"),
            normalizeSubrecordApproval(rs.getString("bbz")),
            readApprovalActorFields(rs).submittedBy(),
            readApprovalActorFields(rs).submittedAt(),
            readApprovalActorFields(rs).approvedBy(),
            readApprovalActorFields(rs).approvedAt(),
            attachmentCount(rs)
    );

    private static final RowMapper<RankRecord> RANK_MAPPER = (rs, rowNum) -> new RankRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("jx")),
            SqlText.trim(rs.getString("sysj")),
            SqlText.trim(rs.getString("syyy")),
            SqlText.trim(rs.getString("rmwh")),
            rs.getInt("xrjxbz"),
            SqlText.trim(rs.getString("lb")),
            normalizeSubrecordApproval(rs.getString("bbz")),
            readApprovalActorFields(rs).submittedBy(),
            readApprovalActorFields(rs).submittedAt(),
            readApprovalActorFields(rs).approvedBy(),
            readApprovalActorFields(rs).approvedAt(),
            attachmentCount(rs)
    );

    private static final RowMapper<AnnualAssessmentRecord> ANNUAL_ASSESSMENT_MAPPER = (rs, rowNum) -> new AnnualAssessmentRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("khnd")),
            SqlText.trim(rs.getString("khjg"))
    );

    private static final RowMapper<AnnualAssessmentSummaryRecord> ANNUAL_ASSESSMENT_SUMMARY_MAPPER = (rs, rowNum) -> new AnnualAssessmentSummaryRecord(
            SqlText.trim(rs.getString("khnd")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("khjg")),
            rs.getLong("personnel_count")
    );

    private static final RowMapper<BatchAssessmentEntryRow> BATCH_ASSESSMENT_ENTRY_MAPPER = (rs, rowNum) -> new BatchAssessmentEntryRow(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("dwsx")),
            SqlText.trim(rs.getString("xrzw")),
            SqlText.trim(rs.getString("khnd")),
            rs.getObject("assessment_id") == null ? null : rs.getInt("assessment_id"),
            SqlText.trim(rs.getString("assessment_result")),
            SqlText.trim(rs.getString("assessment_approval_status")),
            null
    );

    private static final RowMapper<ChangedPersonnelRecord> CHANGED_PERSONNEL_MAPPER = (rs, rowNum) -> new ChangedPersonnelRecord(
            rs.getObject("uid") == null ? null : rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SensitiveData.maskIdCard(rs.getString("sfzh")),
            SqlText.trim(rs.getString("xb")),
            SqlText.trim(rs.getString("csny")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("dwsx")),
            SqlText.trim(rs.getString("gwfl")),
            null,
            null,
            null,
            null,
            null,
            null,
            SqlText.trim(rs.getString("zjbm")),
            SqlText.trim(rs.getString("xrzw")),
            null,
            null,
            null,
            SqlText.trim(rs.getString("bz"))
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    PersonnelRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    List<PersonnelSummary> findAll(OrganizationScope organizationScope, String organizationFilter, String keyword,
            String sort, String direction, PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = parameters(organizationScope, organizationFilter, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.sfzh, p.xb, p.csny,
                       p.ryfl, p.dwsx, p.gwfl, p.xrzw, p.zjbm, h.zwbm2, h.zwgw2, p.bbz
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                LEFT JOIN hisbase h ON h.id = (
                    SELECT h2.id
                    FROM hisbase h2
                    WHERE h2.dwbm = p.dwbm
                      AND h2.grbm = p.grbm
                      AND (h2.sid IS NULL OR TRIM(h2.sid) = '')
                    ORDER BY COALESCE(h2.jsnf, '') DESC, COALESCE(h2.jsyf, '') DESC, h2.id DESC
                    LIMIT 1
                )
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND (:keyword IS NULL OR p.xm LIKE :keywordLike OR p.grbm LIKE :keywordLike OR p.sfzh LIKE :keywordLike)
                ORDER BY %s
                LIMIT :limit OFFSET :offset
                """.formatted(organizationDescendantFilter("p"), personnelSummaryOrderBy(sort, direction)), params, SUMMARY_MAPPER);
    }

    private String personnelSummaryOrderBy(String sort, String direction) {
        String column = switch (sort == null ? "" : sort) {
            case "personCode" -> "p.grbm";
            case "name" -> "p.xm";
            case "organizationCode" -> "p.dwbm";
            case "gender" -> "p.xb";
            case "birthYearMonth" -> "p.csny";
            case "currentPosition" -> "p.xrzw";
            case "appointmentPosition" -> "h.zwgw2";
            default -> null;
        };
        if (column == null) {
            return "p.dwbm, p.grbm";
        }
        String order = "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";
        return column + " " + order + ", p.dwbm, p.grbm";
    }

    long countAll(OrganizationScope organizationScope, String organizationFilter, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND (:keyword IS NULL OR p.xm LIKE :keywordLike OR p.grbm LIKE :keywordLike OR p.sfzh LIKE :keywordLike)
                """.formatted(organizationDescendantFilter("p")), parameters(organizationScope, organizationFilter, keyword), Long.class);
        return count == null ? 0 : count;
    }

    List<PersonnelComprehensiveQueryRecord> findComprehensiveQueries(
            OrganizationScope organizationScope,
            PersonnelComprehensiveQueryCriteria criteria,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = comprehensiveQueryParameters(organizationScope, criteria)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        // 分页列表只需为 LIMIT 行解析 tip/任职；相关子查询在索引有序扫描下对首页更快。
        return jdbcTemplate.query(comprehensiveQuerySql(true) + """
                ORDER BY p.dwbm, p.grbm
                LIMIT :limit OFFSET :offset
                """, params, COMPREHENSIVE_QUERY_MAPPER);
    }

    long countComprehensiveQueries(OrganizationScope organizationScope, PersonnelComprehensiveQueryCriteria criteria) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        MapSqlParameterSource params = comprehensiveQueryParameters(organizationScope, criteria);
        // 无 tip/任职过滤条件时，COUNT 不必关联 hisbase/dryzwbh（原先全员相关子查询可达十余秒）。
        boolean joinTipTables = comprehensiveQueryNeedsTipTables(criteria);
        String sql = joinTipTables
                ? """
                SELECT COUNT(DISTINCT p.uid)
                """ + comprehensiveQueryCountFromSql() + comprehensiveQueryWhereSql(true)
                : """
                SELECT COUNT(*)
                FROM dryjbxx p
                """ + comprehensiveQueryWhereSql(false);
        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
        return count == null ? 0 : count;
    }

    PersonnelComprehensiveQueryOptions findComprehensiveQueryOptions() {
        List<PersonnelComprehensiveQueryOptions.CodeNameOption> personnelCategories = jdbcTemplate.query("""
                SELECT TRIM(ryfl) AS code, TRIM(ryfl) AS name
                FROM dryjbxx
                WHERE TRIM(COALESCE(ryfl, '')) <> ''
                GROUP BY TRIM(ryfl)
                ORDER BY COUNT(*) DESC, TRIM(ryfl)
                """, (rs, rowNum) -> new PersonnelComprehensiveQueryOptions.CodeNameOption(
                SqlText.trim(rs.getString("code")),
                SqlText.trim(rs.getString("name"))));
        List<PersonnelComprehensiveQueryOptions.CodeNameOption> organizationTypes = jdbcTemplate.query("""
                SELECT RIGHT(TRIM(bm), 2) AS code, TRIM(mc) AS name
                FROM dmb
                WHERE bm LIKE '008%'
                  AND CHAR_LENGTH(TRIM(bm)) = 5
                  AND sfsy = 1
                ORDER BY bm
                """, (rs, rowNum) -> new PersonnelComprehensiveQueryOptions.CodeNameOption(
                SqlText.trim(rs.getString("code")),
                SqlText.trim(rs.getString("name"))));
        List<PersonnelComprehensiveQueryOptions.CodeNameOption> postCategories = jdbcTemplate.query("""
                SELECT TRIM(gwfl) AS code, TRIM(gwfl) AS name
                FROM dryjbxx
                WHERE TRIM(COALESCE(gwfl, '')) <> ''
                GROUP BY TRIM(gwfl)
                ORDER BY COUNT(*) DESC, TRIM(gwfl)
                """, (rs, rowNum) -> new PersonnelComprehensiveQueryOptions.CodeNameOption(
                SqlText.trim(rs.getString("code")),
                SqlText.trim(rs.getString("name"))));
        List<PersonnelComprehensiveQueryOptions.CodeNameOption> educations = jdbcTemplate.query("""
                SELECT SUBSTRING(TRIM(bm), 4) AS code, TRIM(mc) AS name
                FROM dmb
                WHERE bm LIKE '002%'
                  AND CHAR_LENGTH(TRIM(bm)) = 5
                  AND sfsy = 1
                ORDER BY bm
                """, (rs, rowNum) -> new PersonnelComprehensiveQueryOptions.CodeNameOption(
                SqlText.trim(rs.getString("code")),
                SqlText.trim(rs.getString("name"))));
        List<PersonnelComprehensiveQueryOptions.CodeNameOption> positions = jdbcTemplate.query("""
                SELECT code, name
                FROM (
                    SELECT code,
                           name,
                           usage_count,
                           ROW_NUMBER() OVER (PARTITION BY code ORDER BY usage_count DESC, name) AS rn
                    FROM (
                        SELECT TRIM(z.zwbm) AS code,
                               COALESCE(NULLIF(TRIM(z.xzzw), ''), TRIM(z.zwbm)) AS name,
                               COUNT(*) AS usage_count
                        FROM dryzwbh z
                        WHERE TRIM(COALESCE(z.zwbm, '')) <> ''
                        GROUP BY TRIM(z.zwbm), COALESCE(NULLIF(TRIM(z.xzzw), ''), TRIM(z.zwbm))
                        UNION ALL
                        SELECT TRIM(h.zwbm2) AS code,
                               COALESCE(NULLIF(TRIM(h.zwgw2), ''), TRIM(h.zwbm2)) AS name,
                               COUNT(*) AS usage_count
                        FROM hisbase h
                        WHERE (h.sid IS NULL OR h.sid = '')
                          AND TRIM(COALESCE(h.zwbm2, '')) <> ''
                        GROUP BY TRIM(h.zwbm2), COALESCE(NULLIF(TRIM(h.zwgw2), ''), TRIM(h.zwbm2))
                    ) source
                ) ranked
                WHERE rn = 1
                ORDER BY code
                LIMIT 800
                """, (rs, rowNum) -> new PersonnelComprehensiveQueryOptions.CodeNameOption(
                SqlText.trim(rs.getString("code")),
                SqlText.trim(rs.getString("name"))));
        return new PersonnelComprehensiveQueryOptions(
                personnelCategories,
                organizationTypes,
                postCategories,
                educations,
                positions);
    }

    private String comprehensiveQuerySql(boolean forPagedList) {
        return """
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.sfzh, p.xb, p.csny,
                       p.ryfl, p.dwsx, p.gwfl, p.cjgzny, p.zzny, p.xlbm, p.zgxl, p.gznx,
                       z.zwbm AS appointment_position_code, z.xzzw AS appointment_position_name, z.srny AS appointment_start,
                       h.jsnf, h.jsyf, h.zwbm2 AS payroll_position_code, h.zwgw2 AS payroll_position_name,
                       h.jbgzjb2 AS grade_level, h.zwgzdc2 AS grade_step, h.hj2 AS total_salary
                """ + (forPagedList ? comprehensiveQueryListFromSql() : comprehensiveQueryCountFromSql())
                + comprehensiveQueryWhereSql(true);
    }

    private String comprehensiveQueryListFromSql() {
        return """
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                LEFT JOIN dryzwbh z ON z.id = (
                    SELECT z2.id
                    FROM dryzwbh z2
                    WHERE z2.dwbm = p.dwbm
                      AND z2.grbm = p.grbm
                      AND z2.xrzwbz = '1'
                    ORDER BY COALESCE(z2.srny, '') DESC, z2.id DESC
                    LIMIT 1
                )
                LEFT JOIN hisbase h ON h.id = (
                    SELECT h2.id
                    FROM hisbase h2
                    WHERE h2.dwbm = p.dwbm
                      AND h2.grbm = p.grbm
                      AND (h2.sid IS NULL OR h2.sid = '')
                    LIMIT 1
                )
                """;
    }

    private String comprehensiveQueryCountFromSql() {
        return """
                FROM dryjbxx p
                LEFT JOIN (
                    SELECT dwbm, grbm, MAX(id) AS id
                    FROM hisbase
                    WHERE sid IS NULL OR sid = ''
                    GROUP BY dwbm, grbm
                ) tip ON tip.dwbm = p.dwbm AND tip.grbm = p.grbm
                LEFT JOIN hisbase h ON h.id = tip.id
                LEFT JOIN dryzwbh z ON z.dwbm = p.dwbm
                      AND z.grbm = p.grbm
                      AND z.xrzwbz = '1'
                """;
    }

    private boolean comprehensiveQueryNeedsTipTables(PersonnelComprehensiveQueryCriteria criteria) {
        String keyword = SqlText.trim(criteria.keyword());
        return (keyword != null && !keyword.isEmpty())
                || emptyToNull(criteria.positionCode()) != null
                || emptyToNull(criteria.positionCodePrefix()) != null
                || emptyToNull(criteria.gradeLevelFrom()) != null
                || emptyToNull(criteria.gradeLevelTo()) != null;
    }

    private String comprehensiveQueryWhereSql(boolean tipTablesJoined) {
        String keywordExtra = tipTablesJoined
                ? """
                       OR z.xzzw LIKE :keywordLike
                       OR z.zwbm LIKE :keywordLike
                       OR h.zwgw2 LIKE :keywordLike
                """
                : "";
        String tipFilters = tipTablesJoined
                ? """
                  AND (:positionCode IS NULL
                       OR TRIM(COALESCE(NULLIF(TRIM(z.zwbm), ''), NULLIF(TRIM(p.zjbm), ''), NULLIF(TRIM(h.zwbm2), ''))) = :positionCode)
                  AND (:positionCodePrefix IS NULL
                       OR LEFT(TRIM(COALESCE(NULLIF(TRIM(z.zwbm), ''), NULLIF(TRIM(p.zjbm), ''), NULLIF(TRIM(h.zwbm2), ''))), 2) = :positionCodePrefix)
                  AND (:gradeLevelFrom IS NULL OR CAST(NULLIF(TRIM(h.jbgzjb2), '') AS UNSIGNED) >= CAST(:gradeLevelFrom AS UNSIGNED))
                  AND (:gradeLevelTo IS NULL OR CAST(NULLIF(TRIM(h.jbgzjb2), '') AS UNSIGNED) <= CAST(:gradeLevelTo AS UNSIGNED))
                """
                : """
                  AND (:positionCode IS NULL)
                  AND (:positionCodePrefix IS NULL)
                  AND (:gradeLevelFrom IS NULL)
                  AND (:gradeLevelTo IS NULL)
                """;
        return """
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL
                       OR p.dwbm = :organizationCode
                       OR p.dwbm LIKE :organizationCodeLike)
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR p.sfzh LIKE :keywordLike
                       OR p.xrzw LIKE :keywordLike
                """ + keywordExtra + """
                       )
                  AND (:gender IS NULL OR TRIM(p.xb) = :gender)
                  AND (:personnelCategory IS NULL OR TRIM(p.ryfl) = :personnelCategory)
                  AND (:organizationType IS NULL OR TRIM(p.dwsx) = :organizationType)
                  AND (:postCategory IS NULL OR TRIM(p.gwfl) = :postCategory)
                  AND (:educationCode IS NULL OR TRIM(p.xlbm) = :educationCode)
                  AND (:birthYearMonthFrom IS NULL
                       OR REPLACE(COALESCE(NULLIF(TRIM(p.csny), ''), '000000'), '.', '') >= :birthYearMonthFrom)
                  AND (:birthYearMonthTo IS NULL
                       OR REPLACE(COALESCE(NULLIF(TRIM(p.csny), ''), '999999'), '.', '') <= :birthYearMonthTo)
                  AND (:workStartYearMonthFrom IS NULL
                       OR REPLACE(COALESCE(NULLIF(TRIM(p.cjgzny), ''), '000000'), '.', '') >= :workStartYearMonthFrom)
                  AND (:workStartYearMonthTo IS NULL
                       OR REPLACE(COALESCE(NULLIF(TRIM(p.cjgzny), ''), '999999'), '.', '') <= :workStartYearMonthTo)
                  AND (:regularizationYearMonthFrom IS NULL
                       OR REPLACE(COALESCE(NULLIF(TRIM(p.zzny), ''), '000000'), '.', '') >= :regularizationYearMonthFrom)
                  AND (:regularizationYearMonthTo IS NULL
                       OR REPLACE(COALESCE(NULLIF(TRIM(p.zzny), ''), '999999'), '.', '') <= :regularizationYearMonthTo)
                """ + tipFilters;
    }

    private MapSqlParameterSource comprehensiveQueryParameters(
            OrganizationScope organizationScope,
            PersonnelComprehensiveQueryCriteria criteria) {
        String keyword = SqlText.trim(criteria.keyword());
        String personnelCategory = SqlText.trim(criteria.personnelCategory());
        String postCategory = SqlText.trim(criteria.postCategory());
        String organizationCode = emptyToNull(criteria.organizationCode());
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationCode", organizationCode)
                .addValue("organizationCodeLike", organizationCode == null ? null : organizationCode + "%")
                .addValue("keyword", keyword == null || keyword.isEmpty() ? null : keyword)
                .addValue("keywordLike", keyword == null || keyword.isEmpty() ? null : "%" + keyword + "%")
                .addValue("gender", emptyToNull(criteria.gender()))
                .addValue("personnelCategory", personnelCategory == null || personnelCategory.isEmpty() ? null : personnelCategory)
                .addValue("personnelCategoryLike", personnelCategory == null || personnelCategory.isEmpty() ? null : "%" + personnelCategory + "%")
                .addValue("organizationType", emptyToNull(criteria.organizationType()))
                .addValue("postCategory", postCategory == null || postCategory.isEmpty() ? null : postCategory)
                .addValue("postCategoryLike", postCategory == null || postCategory.isEmpty() ? null : "%" + postCategory + "%")
                .addValue("educationCode", emptyToNull(criteria.educationCode()))
                .addValue("birthYearMonthFrom", normalizeYearMonthFilter(criteria.birthYearMonthFrom()))
                .addValue("birthYearMonthTo", normalizeYearMonthFilter(criteria.birthYearMonthTo()))
                .addValue("workStartYearMonthFrom", normalizeYearMonthFilter(criteria.workStartYearMonthFrom()))
                .addValue("workStartYearMonthTo", normalizeYearMonthFilter(criteria.workStartYearMonthTo()))
                .addValue("regularizationYearMonthFrom", normalizeYearMonthFilter(criteria.regularizationYearMonthFrom()))
                .addValue("regularizationYearMonthTo", normalizeYearMonthFilter(criteria.regularizationYearMonthTo()))
                .addValue("positionCode", emptyToNull(criteria.positionCode()))
                .addValue("positionCodePrefix", emptyToNull(criteria.positionCodePrefix()))
                .addValue("gradeLevelFrom", emptyToNull(criteria.gradeLevelFrom()))
                .addValue("gradeLevelTo", emptyToNull(criteria.gradeLevelTo()));
    }

    private String normalizeYearMonthFilter(String value) {
        String trimmed = SqlText.trim(value);
        if (trimmed == null || trimmed.isEmpty()) {
            return null;
        }
        String digits = trimmed.replace(".", "");
        if (digits.length() >= 6) {
            return digits.substring(0, 6);
        }
        if (digits.length() == 4) {
            return digits + "01";
        }
        return digits;
    }

    Optional<PersonnelDetail> findByUid(int uid) {
        return jdbcTemplate.query("""
                SELECT p.*, dw.dwmc
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE p.uid = :uid
                """, new MapSqlParameterSource("uid", uid), DETAIL_MAPPER).stream().findFirst();
    }

    Optional<PersonnelInformationCollectionReport> findInformationCollectionReport(int uid) {
        Optional<PersonnelInformationCollectionShell> shell = findInformationCollectionShell(uid);
        if (shell.isEmpty()) {
            return Optional.empty();
        }
        PersonKey key = findKeyByUid(uid).orElseThrow();
        PersonnelInformationCollectionShell basic = shell.get();
        return Optional.of(new PersonnelInformationCollectionReport(
                basic.uid(),
                basic.organizationCode(),
                basic.organizationName(),
                basic.organizationCategory(),
                basic.personCode(),
                basic.name(),
                basic.idCard(),
                basic.gender(),
                basic.birthYearMonth(),
                basic.ethnicity(),
                basic.politicalStatus(),
                basic.personnelCategory(),
                basic.organizationType(),
                basic.postCategory(),
                basic.workStartYearMonth(),
                basic.regularizationYearMonth(),
                basic.entryYearMonth(),
                basic.entryMethod(),
                basic.salaryYears(),
                basic.educationCode(),
                basic.highestEducation(),
                basic.currentPositionLevel(),
                basic.currentRankCode(),
                basic.currentPosition(),
                basic.currentPositionStartYearMonth(),
                basic.archiveNumber(),
                basic.formerUnitPosition(),
                basic.formerUnitAppointmentPeriod(),
                basic.reportOrganizationAbbr(),
                basic.retirementMonth(),
                basic.teachingAllowanceStartMonth(),
                basic.teachingAllowanceYears(),
                findEducation(key),
                findPositions(key),
                findAssessments(key),
                findCurrentPayrollSnapshot(key).orElse(null)));
    }

    public Optional<PersonnelMaintenanceRecord> findMaintenanceByUid(int uid) {
        return jdbcTemplate.query("""
                SELECT p.*, dw.dwmc, dw.dwbz, dw.dwsx AS org_dwsx, dw.gzczbz AS org_gzczbz, COALESCE(dw.jkjs, 0) AS jkjs
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE p.uid = :uid
                """, new MapSqlParameterSource("uid", uid), MAINTENANCE_MAPPER).stream().findFirst();
    }

    int createPersonnel(PersonnelMaintenanceRequest request) {
        MapSqlParameterSource params = maintenanceParameters(request);
        jdbcTemplate.update("""
                INSERT INTO dryjbxx (
                    dwbm, grbm, xm, sfzh, xb, csny, ryfl, dwsx, gwfl, cjgzny, zzny, jrny, jrfs,
                    zdgznx, gznx, jhlqsny, zdjhlnx, xlbm, zgxl, bjglxlnx, tc, txsj, bgdwjc,
                    zwjb, zjbm, xrzw, srny, tgbl, jtbl, fddc, khqk, dynkh, denkh, bbz, bh,
                    gryhzh, spdw, mz, zzmm, fdgd, fdsj, jzgb, ydwzw, yzwrzsj, dah, sfjzgb, yctxsj
                ) VALUES (
                    :organizationCode, :personCode, :name, :idCard, :gender, :birthYearMonth, :personnelCategory, :organizationType, :postCategory,
                    :workStartYearMonth, :regularizationYearMonth, :joinYearMonth, :joinType, 0, :salaryYears, '', 0, :educationCode, :highestEducation, 0, '', '',
                    '', :currentPositionLevel, :currentRankCode, :currentPosition, :currentPositionStartYearMonth, 0, '', '', '', '', '', '', '草稿',
                    '', '', :ethnicity, :politicalStatus, '', '', '', '', '', :archiveNumber, '', 0
                )
                """, params);
        Integer uid = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Integer.class);
        return uid == null ? 0 : uid;
    }

    void updatePersonnel(int uid, PersonnelMaintenanceRequest request) {
        MapSqlParameterSource params = maintenanceParameters(request).addValue("uid", uid);
        jdbcTemplate.update("""
                UPDATE dryjbxx
                SET dwbm = :organizationCode,
                    grbm = :personCode,
                    xm = :name,
                    sfzh = :idCard,
                    xb = :gender,
                    csny = :birthYearMonth,
                    ryfl = :personnelCategory,
                    dwsx = :organizationType,
                    gwfl = :postCategory,
                    cjgzny = :workStartYearMonth,
                    zzny = :regularizationYearMonth,
                    jrny = :joinYearMonth,
                    jrfs = :joinType,
                    gznx = :salaryYears,
                    xlbm = :educationCode,
                    zgxl = :highestEducation,
                    zwjb = :currentPositionLevel,
                    zjbm = :currentRankCode,
                    xrzw = :currentPosition,
                    srny = :currentPositionStartYearMonth,
                    mz = :ethnicity,
                    zzmm = :politicalStatus,
                    dah = :archiveNumber
                WHERE uid = :uid
                """, params);
    }

    void updateApprovalStatus(int uid, String approvalStatus) {
        jdbcTemplate.update("""
                UPDATE dryjbxx
                SET bbz = :approvalStatus
                WHERE uid = :uid
                """, new MapSqlParameterSource("uid", uid).addValue("approvalStatus", approvalStatus));
    }

    void updateMainApprovalSubmit(int uid, String actor, LocalDateTime submittedAt) {
        jdbcTemplate.update("""
                UPDATE dryjbxx
                SET bbz = :approvalStatus,
                    tjr = :actor,
                    tjsj = :submittedAt,
                    shr = NULL,
                    shsj = NULL
                WHERE uid = :uid
                """, new MapSqlParameterSource()
                .addValue("uid", uid)
                .addValue("approvalStatus", PersonnelApprovalStatuses.SUBMITTED)
                .addValue("actor", actor)
                .addValue("submittedAt", Timestamp.valueOf(submittedAt)));
    }

    void updateMainApprovalApprove(int uid, String actor, LocalDateTime approvedAt) {
        jdbcTemplate.update("""
                UPDATE dryjbxx
                SET bbz = :approvalStatus,
                    shr = :actor,
                    shsj = :approvedAt
                WHERE uid = :uid
                """, new MapSqlParameterSource()
                .addValue("uid", uid)
                .addValue("approvalStatus", PersonnelApprovalStatuses.APPROVED)
                .addValue("actor", actor)
                .addValue("approvedAt", Timestamp.valueOf(approvedAt)));
    }

    void updateMainApprovalDraft(int uid) {
        jdbcTemplate.update("""
                UPDATE dryjbxx
                SET bbz = :approvalStatus,
                    tjr = NULL,
                    tjsj = NULL,
                    shr = NULL,
                    shsj = NULL
                WHERE uid = :uid
                """, new MapSqlParameterSource()
                .addValue("uid", uid)
                .addValue("approvalStatus", PersonnelApprovalStatuses.DRAFT));
    }

    void deletePersonnel(int uid) {
        jdbcTemplate.update("DELETE FROM dryjbxx WHERE uid = :uid", new MapSqlParameterSource("uid", uid));
    }

    /**
     * Archives an active person into changed tables (dryjbxxb / hisbaseb).
     * Exposed for cross-package callers such as retirement processing.
     */
    public PersonnelChangeResult archivePersonnelChange(int uid, PersonnelChangeRequest request) {
        return movePersonnelToChanged(uid, request);
    }

    PersonnelChangeResult movePersonnelToChanged(int uid, PersonnelChangeRequest request) {
        PersonnelMaintenanceRecord record = findMaintenanceByUid(uid)
                .orElseThrow(() -> new com.dxsoft.rsgzgl.common.NotFoundException("Personnel record not found: " + uid));
        String organizationCode = record.organizationCode();
        String personCode = record.personCode();
        String changePeriod = normalizedChangePeriod(request.effectivePeriod());
        PersonKey personKey = new PersonKey(organizationCode, personCode);
        jdbcTemplate.update("""
                DELETE FROM dryjbxxb
                WHERE dwbm = :dwbm AND grbm = :grbm
                """, keyParameters(personKey));
        insertCommonColumns("dryjbxx", "dryjbxxb", "uid", "p", "p.uid = :uid", new MapSqlParameterSource("uid", uid));
        jdbcTemplate.update("""
                UPDATE dryjbxxb
                SET bz = :remark,
                    txsj = CASE WHEN :changeType = '退休' THEN :effectivePeriod ELSE txsj END
                WHERE dwbm = :dwbm AND grbm = :grbm
                """, keyParameters(personKey)
                .addValue("changeType", valueOrBlank(request.changeType()))
                .addValue("effectivePeriod", displayChangePeriod(changePeriod))
                .addValue("remark", personnelChangeRemark(request, changePeriod)));

        moveRelatedRecordsToChanged(personKey);

        jdbcTemplate.update("""
                DELETE FROM hisbaseb
                WHERE dwbm = :dwbm AND grbm = :grbm
                """, keyParameters(personKey));
        insertCommonColumns("hisbase", "hisbaseb", null, "h", "h.dwbm = :dwbm AND h.grbm = :grbm", keyParameters(personKey));
        jdbcTemplate.update("""
                UPDATE hisbaseb
                SET jslb = :changeType,
                    jsnf = :year,
                    jsyf = :month,
                    bbz = :marker
                WHERE dwbm = :dwbm AND grbm = :grbm AND (sid IS NULL OR TRIM(sid) = '')
                """, keyParameters(personKey)
                .addValue("changeType", valueOrBlank(request.changeType()))
                .addValue("year", changePeriod.substring(0, 4))
                .addValue("month", changePeriod.substring(4, 6))
                .addValue("marker", "变动"));

        jdbcTemplate.update("DELETE FROM hisbase WHERE dwbm = :dwbm AND grbm = :grbm", keyParameters(personKey));
        jdbcTemplate.update("DELETE FROM dryjbxx WHERE uid = :uid", new MapSqlParameterSource("uid", uid));
        return new PersonnelChangeResult(organizationCode, personCode, record.name(), request.changeType(), "人员变动处理完成");
    }

    PersonnelChangeResult transferPersonnelWithinSystem(int uid, PersonnelChangeRequest request) {
        PersonnelMaintenanceRecord record = findMaintenanceByUid(uid)
                .orElseThrow(() -> new com.dxsoft.rsgzgl.common.NotFoundException("Personnel record not found: " + uid));
        String sourceOrganizationCode = record.organizationCode();
        String sourcePersonCode = record.personCode();
        String targetOrganizationCode = SqlText.trim(request.targetOrganizationCode());
        String targetOrganizationName = SqlText.trim(request.targetOrganizationName());
        if (targetOrganizationName.isBlank()) {
            targetOrganizationName = findOrganizationName(targetOrganizationCode);
        }
        String sourceOrganizationName = SqlText.trim(record.organizationName());
        if (sourceOrganizationName.isBlank()) {
            sourceOrganizationName = findOrganizationName(sourceOrganizationCode);
        }
        String changePeriod = normalizedChangePeriod(request.effectivePeriod());
        String displayPeriod = displayChangePeriod(changePeriod);
        String targetPersonCode = sourcePersonCode;
        if (personKeyExists(targetOrganizationCode, sourcePersonCode)) {
            targetPersonCode = allocatePersonCode(targetOrganizationCode);
        }
        PersonKey sourceKey = new PersonKey(sourceOrganizationCode, sourcePersonCode);
        PersonKey targetKey = new PersonKey(targetOrganizationCode, targetPersonCode);
        rekeyActivePersonnelTables(sourceKey, targetKey);

        String formerUnitText = (sourceOrganizationName.isBlank() ? sourceOrganizationCode : sourceOrganizationName)
                + "（" + sourceOrganizationCode + "）";
        if (!SqlText.trim(record.currentPosition()).isBlank()) {
            formerUnitText = formerUnitText + " " + SqlText.trim(record.currentPosition());
        }
        String transferRemark = personnelTransferRemark(
                request,
                displayPeriod,
                formerUnitText,
                targetOrganizationName,
                targetOrganizationCode);
        // 清空 tc：新增人员确定工资只列出 tc<>已定工资（或已定但 tip 为定资类）的人员。
        // 系统内调动后需在新单位按「调入定资」重新办理，故必须重置，否则查不到。
        jdbcTemplate.update("""
                UPDATE dryjbxx
                SET dwbm = :targetOrganizationCode,
                    grbm = :targetPersonCode,
                    ydwzw = :formerUnitText,
                    yzwrzsj = CASE
                        WHEN :positionStart IS NULL OR TRIM(:positionStart) = '' THEN yzwrzsj
                        ELSE :positionStart
                    END,
                    jrny = :transferPeriod,
                    jrfs = '调动',
                    tc = '',
                    bz = CASE
                        WHEN bz IS NULL OR TRIM(bz) = '' THEN :transferRemark
                        ELSE CONCAT(TRIM(bz), '；', :transferRemark)
                    END
                WHERE uid = :uid
                """, new MapSqlParameterSource()
                .addValue("targetOrganizationCode", targetOrganizationCode)
                .addValue("targetPersonCode", targetPersonCode)
                .addValue("formerUnitText", formerUnitText)
                .addValue("positionStart", valueOrBlank(record.currentPositionStartYearMonth()))
                .addValue("transferPeriod", displayPeriod)
                .addValue("transferRemark", transferRemark)
                .addValue("uid", uid));

        insertPersonnelTransferHistory(
                uid,
                record,
                sourceOrganizationCode,
                sourceOrganizationName,
                sourcePersonCode,
                targetOrganizationCode,
                targetOrganizationName,
                targetPersonCode,
                displayPeriod,
                transferRemark);

        return new PersonnelChangeResult(
                targetOrganizationCode,
                targetPersonCode,
                record.name(),
                "调动",
                "已调往 " + (targetOrganizationName.isBlank() ? targetOrganizationCode : targetOrganizationName)
                        + "，人员仍保留在职，已写入调动履历");
    }

    void insertPersonnelTransferHistory(
            int personUid,
            PersonnelMaintenanceRecord record,
            String sourceOrganizationCode,
            String sourceOrganizationName,
            String sourcePersonCode,
            String targetOrganizationCode,
            String targetOrganizationName,
            String targetPersonCode,
            String transferPeriod,
            String remark) {
        jdbcTemplate.update("""
                INSERT INTO app_personnel_transfer (
                    person_uid, id_card, person_name,
                    source_organization_code, source_organization_name, source_person_code,
                    target_organization_code, target_organization_name, target_person_code,
                    transfer_period, change_type, remark
                ) VALUES (
                    :personUid, :idCard, :personName,
                    :sourceOrganizationCode, :sourceOrganizationName, :sourcePersonCode,
                    :targetOrganizationCode, :targetOrganizationName, :targetPersonCode,
                    :transferPeriod, '调动', :remark
                )
                """, new MapSqlParameterSource()
                .addValue("personUid", personUid)
                .addValue("idCard", valueOrBlank(record.idCard()))
                .addValue("personName", valueOrBlank(record.name()))
                .addValue("sourceOrganizationCode", sourceOrganizationCode)
                .addValue("sourceOrganizationName", valueOrBlank(sourceOrganizationName))
                .addValue("sourcePersonCode", sourcePersonCode)
                .addValue("targetOrganizationCode", targetOrganizationCode)
                .addValue("targetOrganizationName", valueOrBlank(targetOrganizationName))
                .addValue("targetPersonCode", targetPersonCode)
                .addValue("transferPeriod", valueOrBlank(transferPeriod))
                .addValue("remark", truncateText(valueOrBlank(remark), 500)));
    }


    Optional<PersonnelTransferRecord> findTransferById(int id) {
        return jdbcTemplate.query("""
                SELECT id, person_uid, id_card, person_name,
                       source_organization_code, source_organization_name, source_person_code,
                       target_organization_code, target_organization_name, target_person_code,
                       transfer_period, change_type, remark,
                       DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS created_at,
                       (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'app_personnel_transfer' AND att.record_id = id AND att.record_key = '') AS attachment_count
                FROM app_personnel_transfer
                WHERE id = :id
                """, new MapSqlParameterSource("id", id), (rs, rowNum) -> new PersonnelTransferRecord(
                rs.getLong("id"),
                rs.getInt("person_uid"),
                SqlText.trim(rs.getString("id_card")),
                SqlText.trim(rs.getString("person_name")),
                SqlText.trim(rs.getString("source_organization_code")),
                SqlText.trim(rs.getString("source_organization_name")),
                SqlText.trim(rs.getString("source_person_code")),
                SqlText.trim(rs.getString("target_organization_code")),
                SqlText.trim(rs.getString("target_organization_name")),
                SqlText.trim(rs.getString("target_person_code")),
                SqlText.trim(rs.getString("transfer_period")),
                SqlText.trim(rs.getString("change_type")),
                SqlText.trim(rs.getString("remark")),
                SqlText.trim(rs.getString("created_at")),
                attachmentCount(rs)
        )).stream().findFirst();
    }

    List<PersonnelTransferRecord> findTransferHistories(int personUid, String idCard, PersonKey key) {
        String normalizedIdCard = valueOrBlank(idCard);
        return jdbcTemplate.query("""
                SELECT id, person_uid, id_card, person_name,
                       source_organization_code, source_organization_name, source_person_code,
                       target_organization_code, target_organization_name, target_person_code,
                       transfer_period, change_type, remark,
                       DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS created_at,
                       (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'app_personnel_transfer' AND att.record_id = id AND att.record_key = '') AS attachment_count
                FROM app_personnel_transfer
                WHERE person_uid = :personUid
                   OR (:idCard <> '' AND id_card = :idCard)
                   OR (target_organization_code = :dwbm AND target_person_code = :grbm)
                   OR (source_organization_code = :dwbm AND source_person_code = :grbm)
                ORDER BY transfer_period DESC, id DESC
                """, keyParameters(key)
                .addValue("personUid", personUid)
                .addValue("idCard", normalizedIdCard), (rs, rowNum) -> new PersonnelTransferRecord(
                rs.getLong("id"),
                rs.getInt("person_uid"),
                SqlText.trim(rs.getString("id_card")),
                SqlText.trim(rs.getString("person_name")),
                SqlText.trim(rs.getString("source_organization_code")),
                SqlText.trim(rs.getString("source_organization_name")),
                SqlText.trim(rs.getString("source_person_code")),
                SqlText.trim(rs.getString("target_organization_code")),
                SqlText.trim(rs.getString("target_organization_name")),
                SqlText.trim(rs.getString("target_person_code")),
                SqlText.trim(rs.getString("transfer_period")),
                SqlText.trim(rs.getString("change_type")),
                SqlText.trim(rs.getString("remark")),
                SqlText.trim(rs.getString("created_at")),
                attachmentCount(rs)
        ));
    }

    private void rekeyActivePersonnelTables(PersonKey sourceKey, PersonKey targetKey) {
        if (sourceKey.organizationCode().equals(targetKey.organizationCode())
                && sourceKey.personCode().equals(targetKey.personCode())) {
            return;
        }
        List<String> tables = new ArrayList<>();
        tables.add("hisbase");
        for (TablePair pair : PERSONNEL_CHANGE_TABLE_PAIRS) {
            tables.add(pair.activeTable());
        }
        MapSqlParameterSource parameters = keyParameters(sourceKey)
                .addValue("targetDwbm", targetKey.organizationCode())
                .addValue("targetGrbm", targetKey.personCode());
        for (String tableName : tables) {
            if (!tableExists(tableName)) {
                continue;
            }
            jdbcTemplate.update("""
                    UPDATE %s
                    SET dwbm = :targetDwbm,
                        grbm = :targetGrbm
                    WHERE dwbm = :dwbm AND grbm = :grbm
                    """.formatted(quote(tableName)), parameters);
        }
    }

    private boolean personKeyExists(String organizationCode, String personCode) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryjbxx
                WHERE dwbm = :dwbm AND grbm = :grbm
                """, keyParameters(new PersonKey(organizationCode, personCode)), Long.class);
        return count != null && count > 0;
    }

    private String allocatePersonCode(String organizationCode) {
        Integer maxCode = jdbcTemplate.query("""
                SELECT grbm
                FROM dryjbxx
                WHERE dwbm = :dwbm
                """, new MapSqlParameterSource("dwbm", organizationCode), (rs, rowNum) -> {
            try {
                return Integer.parseInt(SqlText.trim(rs.getString("grbm")));
            } catch (NumberFormatException ex) {
                return 0;
            }
        }).stream().max(Integer::compareTo).orElse(0);
        return "%05d".formatted(maxCode + 1);
    }

    private String findOrganizationName(String organizationCode) {
        return jdbcTemplate.query("""
                SELECT dwmc
                FROM dwbm
                WHERE dwbm = :dwbm
                LIMIT 1
                """, new MapSqlParameterSource("dwbm", organizationCode), (rs, rowNum) -> SqlText.trim(rs.getString("dwmc")))
                .stream()
                .findFirst()
                .orElse("");
    }

    private String personnelTransferRemark(
            PersonnelChangeRequest request,
            String displayPeriod,
            String formerUnitText,
            String targetOrganizationName,
            String targetOrganizationCode) {
        String targetText = (targetOrganizationName == null || targetOrganizationName.isBlank()
                ? targetOrganizationCode
                : targetOrganizationName) + "（" + targetOrganizationCode + "）";
        String userRemark = valueOrBlank(request.remark());
        return ("系统内调动 " + displayPeriod
                + " 由" + formerUnitText
                + "调往" + targetText
                + (userRemark.isBlank() ? "" : " " + userRemark)).trim();
    }

    private String truncateText(String value, int maxLength) {
        String text = valueOrBlank(value);
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    PersonnelChangeResult restoreChangedPersonnel(String organizationCode, String personCode) {
        PersonKey personKey = new PersonKey(organizationCode, personCode);
        MapSqlParameterSource key = keyParameters(personKey);
        Map<String, Object> changed = jdbcTemplate.queryForList("""
                SELECT *
                FROM dryjbxxb
                WHERE dwbm = :dwbm AND grbm = :grbm
                LIMIT 1
                """, key).stream().findFirst()
                .orElseThrow(() -> new com.dxsoft.rsgzgl.common.NotFoundException("Changed personnel record not found"));
        String name = SqlText.trim(String.valueOf(changed.getOrDefault("xm", "")));
        jdbcTemplate.update("DELETE FROM dryjbxx WHERE dwbm = :dwbm AND grbm = :grbm", key);
        insertCommonColumns("dryjbxxb", "dryjbxx", "uid", "b", "b.dwbm = :dwbm AND b.grbm = :grbm", key);
        restoreRelatedRecords(personKey);
        jdbcTemplate.update("DELETE FROM hisbase WHERE dwbm = :dwbm AND grbm = :grbm", key);
        insertCommonColumns("hisbaseb", "hisbase", null, "h", "h.dwbm = :dwbm AND h.grbm = :grbm", key);
        jdbcTemplate.update("DELETE FROM hisbaseb WHERE dwbm = :dwbm AND grbm = :grbm", key);
        jdbcTemplate.update("DELETE FROM dryjbxxb WHERE dwbm = :dwbm AND grbm = :grbm", key);
        return new PersonnelChangeResult(organizationCode, personCode, name, "恢复在册", "人员已恢复到在册人员信息");
    }

    Optional<PersonKey> findKeyByUid(int uid) {
        return jdbcTemplate.query("""
                SELECT uid, dwbm, grbm
                FROM dryjbxx
                WHERE uid = :uid
                """, new MapSqlParameterSource("uid", uid), (rs, rowNum) -> new PersonKey(
                SqlText.trim(rs.getString("dwbm")),
                SqlText.trim(rs.getString("grbm"))
        )).stream().findFirst();
    }

    public Optional<Integer> findUidByOrgPerson(String organizationCode, String personCode) {
        if (organizationCode == null || organizationCode.isBlank() || personCode == null || personCode.isBlank()) {
            return Optional.empty();
        }
        List<Integer> rows = jdbcTemplate.query("""
                SELECT uid
                FROM dryjbxx
                WHERE dwbm = :organizationCode AND grbm = :personCode
                LIMIT 1
                """,
                new MapSqlParameterSource()
                        .addValue("organizationCode", organizationCode.trim())
                        .addValue("personCode", personCode.trim()),
                (rs, rowNum) -> rs.getInt("uid"));
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    Optional<PersonKey> findEducationKeyById(int id) {
        return findSubrecordKeyById("dxl", id);
    }

    Optional<PersonKey> findPositionKeyById(int id) {
        return findSubrecordKeyById("dryzwbh", id);
    }

    Optional<PersonKey> findAssessmentKeyById(int id) {
        return findSubrecordKeyById("dndkh", id);
    }

    Optional<PersonKey> findAwardKeyById(int id) {
        return findSubrecordKeyById("hjxx", id);
    }

    Optional<PersonKey> findRankKeyById(int id) {
        return findSubrecordKeyById("jx", id);
    }

    Optional<EducationRecord> findEducationById(int id) {
        return jdbcTemplate.query("""
                SELECT e.id, e.dwbm, e.grbm, e.xlbm, e.xl, e.byyx, e.rxsj, e.bysj, e.xz, e.xllb, e.bz, e.bbz, e.tjr, e.tjsj, e.shr, e.shsj,
                       marker.record_id IS NOT NULL AS app_created,
                       (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'dxl' AND att.record_id = e.id AND att.record_key = '') AS attachment_count
                FROM dxl e
                LEFT JOIN app_record_marker marker ON marker.table_name = 'dxl'
                    AND marker.record_id COLLATE utf8mb4_0900_ai_ci = CAST(e.id AS CHAR) COLLATE utf8mb4_0900_ai_ci
                    AND marker.marker = 'APP_CREATED'
                WHERE e.id = :id
                """, new MapSqlParameterSource("id", id), EDUCATION_MAPPER).stream().findFirst();
    }

    Optional<PositionRecord> findPositionById(int id) {
        return jdbcTemplate.query("""
                SELECT z.id, z.dwbm, z.grbm, z.xrzwbm, z.xrzw, z.zwjb, z.zjbm, z.zwbm, z.xzzw,
                       z.srny, z.kjnx, z.xrzwbz, z.jsbz, z.zwlb, z.linked_award_id, z.bbz, z.tjr, z.tjsj, z.shr, z.shsj,
                       marker.record_id IS NOT NULL AS app_created,
                       (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'dryzwbh' AND att.record_id = z.id AND att.record_key = '') AS attachment_count
                FROM dryzwbh z
                LEFT JOIN app_record_marker marker ON marker.table_name = 'dryzwbh'
                    AND marker.record_id COLLATE utf8mb4_0900_ai_ci = CAST(z.id AS CHAR) COLLATE utf8mb4_0900_ai_ci
                    AND marker.marker = 'APP_CREATED'
                WHERE z.id = :id
                """, new MapSqlParameterSource("id", id), POSITION_MAPPER).stream().findFirst();
    }

    Optional<AssessmentRecord> findAssessmentById(int id) {
        return jdbcTemplate.query("""
                SELECT a.id, a.dwbm, a.grbm, a.khnd, a.khjg, a.bbz, a.tjr, a.tjsj, a.shr, a.shsj,
                       marker.record_id IS NOT NULL AS app_created,
                       (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'dndkh' AND att.record_id = a.id AND att.record_key = '') AS attachment_count
                FROM dndkh a
                LEFT JOIN app_record_marker marker ON marker.table_name = 'dndkh'
                    AND marker.record_id COLLATE utf8mb4_0900_ai_ci = CAST(a.id AS CHAR) COLLATE utf8mb4_0900_ai_ci
                    AND marker.marker = 'APP_CREATED'
                WHERE a.id = :id
                """, new MapSqlParameterSource("id", id), ASSESSMENT_MAPPER).stream().findFirst();
    }

    public Optional<AwardRecord> findAwardById(int id) {
        return jdbcTemplate.query("""
                SELECT a.id, a.dwbm, a.grbm, a.hjmc, a.sjdw, a.jllx, a.hjsj, a.tqyjjssj, a.qtqk, a.jldc, a.jljb, a.bbz, a.tjr, a.tjsj, a.shr, a.shsj,
                       (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'hjxx' AND att.record_id = a.id AND att.record_key = '') AS attachment_count
                FROM hjxx a
                WHERE a.id = :id
                """, new MapSqlParameterSource("id", id), AWARD_MAPPER).stream().findFirst();
    }

    public Optional<RankRecord> findRankById(int id) {
        return jdbcTemplate.query("""
                SELECT r.id, r.dwbm, r.grbm, r.jx, r.sysj, r.syyy, r.rmwh, r.xrjxbz, r.lb, r.bbz, r.tjr, r.tjsj, r.shr, r.shsj,
                       (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'jx' AND att.record_id = r.id AND att.record_key = '') AS attachment_count
                FROM jx r
                WHERE r.id = :id
                """, new MapSqlParameterSource("id", id), RANK_MAPPER).stream().findFirst();
    }

    List<AwardRecord> findAwards(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT a.id, a.dwbm, a.grbm, a.hjmc, a.sjdw, a.jllx, a.hjsj, a.tqyjjssj, a.qtqk, a.jldc, a.jljb, a.bbz, a.tjr, a.tjsj, a.shr, a.shsj,
                       (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'hjxx' AND att.record_id = a.id AND att.record_key = '') AS attachment_count
                FROM hjxx a
                WHERE a.dwbm = :dwbm AND a.grbm = :grbm
                ORDER BY a.hjsj DESC, a.id DESC
                """, keyParameters(key), AWARD_MAPPER);
    }

    List<RankRecord> findRanks(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT r.id, r.dwbm, r.grbm, r.jx, r.sysj, r.syyy, r.rmwh, r.xrjxbz, r.lb, r.bbz, r.tjr, r.tjsj, r.shr, r.shsj,
                       (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'jx' AND att.record_id = r.id AND att.record_key = '') AS attachment_count
                FROM jx r
                WHERE r.dwbm = :dwbm AND r.grbm = :grbm
                ORDER BY r.sysj DESC, r.id DESC
                """, keyParameters(key), RANK_MAPPER);
    }

    void updateSubrecordApprovalStatus(PersonnelSubrecordType type, int id, String approvalStatus) {
        jdbcTemplate.update("""
                UPDATE `%s`
                SET bbz = :approvalStatus
                WHERE id = :id
                """.formatted(type.tableName()),
                new MapSqlParameterSource("id", id).addValue("approvalStatus", approvalStatus));
    }

    void updateSubrecordApprovalSubmit(PersonnelSubrecordType type, int id, String actor, LocalDateTime submittedAt) {
        jdbcTemplate.update("""
                UPDATE `%s`
                SET bbz = :approvalStatus,
                    tjr = :actor,
                    tjsj = :submittedAt,
                    shr = NULL,
                    shsj = NULL
                WHERE id = :id
                """.formatted(type.tableName()),
                new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("approvalStatus", PersonnelApprovalStatuses.SUBMITTED)
                .addValue("actor", actor)
                .addValue("submittedAt", Timestamp.valueOf(submittedAt)));
    }

    void updateSubrecordApprovalApprove(PersonnelSubrecordType type, int id, String actor, LocalDateTime approvedAt) {
        jdbcTemplate.update("""
                UPDATE `%s`
                SET bbz = :approvalStatus,
                    shr = :actor,
                    shsj = :approvedAt
                WHERE id = :id
                """.formatted(type.tableName()),
                new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("approvalStatus", PersonnelApprovalStatuses.APPROVED)
                .addValue("actor", actor)
                .addValue("approvedAt", Timestamp.valueOf(approvedAt)));
    }

    void updateSubrecordApprovalDraft(PersonnelSubrecordType type, int id) {
        jdbcTemplate.update("""
                UPDATE `%s`
                SET bbz = :approvalStatus,
                    tjr = NULL,
                    tjsj = NULL,
                    shr = NULL,
                    shsj = NULL
                WHERE id = :id
                """.formatted(type.tableName()),
                new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("approvalStatus", PersonnelApprovalStatuses.DRAFT));
    }

    List<PositionRecord> findPositions(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT z.id, z.dwbm, z.grbm, z.xrzwbm, z.xrzw, z.zwjb, z.zjbm, z.zwbm, z.xzzw,
                       z.srny, z.kjnx, z.xrzwbz, z.jsbz, z.zwlb, z.linked_award_id, z.bbz, z.tjr, z.tjsj, z.shr, z.shsj,
                       marker.record_id IS NOT NULL AS app_created,
                       (SELECT COUNT(*)
                        FROM app_subrecord_attachment att
                        WHERE att.table_name = 'dryzwbh'
                          AND att.record_id = z.id) AS attachment_count
                FROM dryzwbh z
                LEFT JOIN app_record_marker marker ON marker.table_name = 'dryzwbh'
                    AND marker.record_id COLLATE utf8mb4_0900_ai_ci = CAST(z.id AS CHAR) COLLATE utf8mb4_0900_ai_ci
                    AND marker.marker = 'APP_CREATED'
                WHERE z.dwbm = :dwbm AND z.grbm = :grbm
                ORDER BY CASE WHEN z.xrzwbz = '1' THEN 0 ELSE 1 END, z.srny DESC, z.id DESC
                """, keyParameters(key), POSITION_MAPPER);
    }

    List<PersonnelPositionHistoryRecord> findPositionHistories(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String positionCode,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = personnelHistoryParameters(organizationScope, organizationCode, keyword, positionCode)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT z.id, z.dwbm, dw.dwmc, z.grbm, p.xm, z.xrzwbm, z.xrzw, z.zwjb, z.zjbm,
                       z.zwbm, z.xzzw, z.srny, z.kjnx, z.xrzwbz, z.jsbz
                FROM dryzwbh z
                LEFT JOIN dryjbxx p ON p.dwbm = z.dwbm AND p.grbm = z.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = z.dwbm
                WHERE (:allOrganizations = TRUE OR z.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND (:keyword IS NULL OR z.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike
                       OR z.xrzw LIKE :keywordLike OR z.xzzw LIKE :keywordLike OR z.zwbm = :keyword)
                  AND (:positionCode IS NULL OR TRIM(z.zwbm) = :positionCode OR TRIM(z.xrzwbm) = :positionCode)
                ORDER BY z.dwbm, z.grbm, z.srny DESC, z.id DESC
                LIMIT :limit OFFSET :offset
                """.formatted(organizationDescendantFilter("z")), params, POSITION_HISTORY_MAPPER);
    }

    long countPositionHistories(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String positionCode) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryzwbh z
                LEFT JOIN dryjbxx p ON p.dwbm = z.dwbm AND p.grbm = z.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = z.dwbm
                WHERE (:allOrganizations = TRUE OR z.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND (:keyword IS NULL OR z.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike
                       OR z.xrzw LIKE :keywordLike OR z.xzzw LIKE :keywordLike OR z.zwbm = :keyword)
                  AND (:positionCode IS NULL OR TRIM(z.zwbm) = :positionCode OR TRIM(z.xrzwbm) = :positionCode)
                """.formatted(organizationDescendantFilter("z")), personnelHistoryParameters(organizationScope, organizationCode, keyword, positionCode), Long.class);
        return count == null ? 0 : count;
    }

    List<EducationRecord> findEducation(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT e.id, e.dwbm, e.grbm, e.xlbm, e.xl, e.byyx, e.rxsj, e.bysj, e.xz, e.xllb, e.bz, e.bbz, e.tjr, e.tjsj, e.shr, e.shsj,
                       marker.record_id IS NOT NULL AS app_created,
                       (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'dxl' AND att.record_id = e.id AND att.record_key = '') AS attachment_count
                FROM dxl e
                LEFT JOIN app_record_marker marker ON marker.table_name = 'dxl'
                    AND marker.record_id COLLATE utf8mb4_0900_ai_ci = CAST(e.id AS CHAR) COLLATE utf8mb4_0900_ai_ci
                    AND marker.marker = 'APP_CREATED'
                WHERE e.dwbm = :dwbm AND e.grbm = :grbm
                ORDER BY bysj DESC, xlbm
                """, keyParameters(key), EDUCATION_MAPPER);
    }

    List<PersonnelEducationHistoryRecord> findEducationHistories(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String educationCode,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = personnelHistoryParameters(organizationScope, organizationCode, keyword, null, educationCode)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT e.id, e.dwbm, dw.dwmc, e.grbm, p.xm, e.xlbm, e.xl, e.byyx,
                       e.rxsj, e.bysj, e.xz, e.xllb, e.bz
                FROM dxl e
                LEFT JOIN dryjbxx p ON p.dwbm = e.dwbm AND p.grbm = e.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = e.dwbm
                WHERE (:allOrganizations = TRUE OR e.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND (:keyword IS NULL OR e.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike
                       OR e.xlbm = :keyword OR e.xl LIKE :keywordLike OR e.byyx LIKE :keywordLike OR e.xllb LIKE :keywordLike)
                  AND (:educationCode IS NULL OR TRIM(e.xlbm) = :educationCode)
                ORDER BY e.dwbm, e.grbm, e.bysj DESC, e.xlbm, e.id DESC
                LIMIT :limit OFFSET :offset
                """.formatted(organizationDescendantFilter("e")), params, EDUCATION_HISTORY_MAPPER);
    }

    long countEducationHistories(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String educationCode) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dxl e
                LEFT JOIN dryjbxx p ON p.dwbm = e.dwbm AND p.grbm = e.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = e.dwbm
                WHERE (:allOrganizations = TRUE OR e.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND (:keyword IS NULL OR e.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike
                       OR e.xlbm = :keyword OR e.xl LIKE :keywordLike OR e.byyx LIKE :keywordLike OR e.xllb LIKE :keywordLike)
                  AND (:educationCode IS NULL OR TRIM(e.xlbm) = :educationCode)
                """.formatted(organizationDescendantFilter("e")), personnelHistoryParameters(organizationScope, organizationCode, keyword, null, educationCode), Long.class);
        return count == null ? 0 : count;
    }

    List<ChangedPersonnelRecord> findChangedPersonnel(
            OrganizationScope organizationScope,
            String organizationCode,
            String period,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = changedPersonnelParameters(organizationScope, organizationCode, period, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT b.uid, b.dwbm, dw.dwmc, b.grbm, b.xm, b.sfzh, b.xb, b.csny, b.ryfl, b.dwsx, b.gwfl,
                       b.zjbm, b.xrzw, b.bz
                FROM dryjbxxb b
                LEFT JOIN dwbm dw ON dw.dwbm = b.dwbm
                WHERE (:allOrganizations = TRUE OR b.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND (:period IS NULL
                       OR REPLACE(REPLACE(COALESCE(b.txsj, ''), '.', ''), '-', '') LIKE :periodLike
                       OR REPLACE(REPLACE(COALESCE(b.bz, ''), '.', ''), '-', '') LIKE :periodLike)
                  AND (:keyword IS NULL OR b.grbm LIKE :keywordLike OR b.xm LIKE :keywordLike
                       OR b.sfzh LIKE :keywordLike OR b.xrzw LIKE :keywordLike OR b.bz LIKE :keywordLike)
                ORDER BY b.dwbm, b.grbm
                LIMIT :limit OFFSET :offset
                """.formatted(organizationDescendantFilter("b")), params, CHANGED_PERSONNEL_MAPPER);
    }

    long countChangedPersonnel(OrganizationScope organizationScope, String organizationCode, String period, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryjbxxb b
                LEFT JOIN dwbm dw ON dw.dwbm = b.dwbm
                WHERE (:allOrganizations = TRUE OR b.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND (:period IS NULL
                       OR REPLACE(REPLACE(COALESCE(b.txsj, ''), '.', ''), '-', '') LIKE :periodLike
                       OR REPLACE(REPLACE(COALESCE(b.bz, ''), '.', ''), '-', '') LIKE :periodLike)
                  AND (:keyword IS NULL OR b.grbm LIKE :keywordLike OR b.xm LIKE :keywordLike
                       OR b.sfzh LIKE :keywordLike OR b.xrzw LIKE :keywordLike OR b.bz LIKE :keywordLike)
                """.formatted(organizationDescendantFilter("b")), changedPersonnelParameters(organizationScope, organizationCode, period, keyword), Long.class);
        return count == null ? 0 : count;
    }

    Optional<PersonnelMaintenanceRecord> findChangedMaintenanceByUid(int uid) {
        return jdbcTemplate.query("""
                SELECT p.*, dw.dwmc, dw.dwbz, dw.dwsx AS org_dwsx, dw.gzczbz AS org_gzczbz, COALESCE(dw.jkjs, 0) AS jkjs
                FROM dryjbxxb p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE p.uid = :uid
                """, new MapSqlParameterSource("uid", uid), MAINTENANCE_MAPPER).stream().findFirst();
    }

    Optional<PersonKey> findChangedKeyByUid(int uid) {
        return jdbcTemplate.query("""
                SELECT dwbm, grbm
                FROM dryjbxxb
                WHERE uid = :uid
                """, new MapSqlParameterSource("uid", uid), (rs, rowNum) -> new PersonKey(
                SqlText.trim(rs.getString("dwbm")),
                SqlText.trim(rs.getString("grbm"))
        )).stream().findFirst();
    }

    List<EducationRecord> findChangedEducation(PersonKey key) {
        if (!tableExists("dxlb")) {
            return List.of();
        }
        return jdbcTemplate.query("""
                SELECT e.id, e.dwbm, e.grbm, e.xlbm, e.xl, e.byyx, e.rxsj, e.bysj, e.xz, e.xllb, e.bz,
                       FALSE AS app_created
                FROM dxlb e
                WHERE e.dwbm = :dwbm AND e.grbm = :grbm
                ORDER BY bysj DESC, xlbm
                """, keyParameters(key), EDUCATION_MAPPER);
    }

    List<PositionRecord> findChangedPositions(PersonKey key) {
        if (!tableExists("dryzwbhb")) {
            return List.of();
        }
        return jdbcTemplate.query("""
                SELECT z.id, z.dwbm, z.grbm, z.xrzwbm, z.xrzw, z.zwjb, z.zjbm, z.zwbm, z.xzzw,
                       z.srny, z.kjnx, z.xrzwbz, z.jsbz, FALSE AS app_created, 0 AS attachment_count
                FROM dryzwbhb z
                WHERE z.dwbm = :dwbm AND z.grbm = :grbm
                ORDER BY CASE WHEN z.xrzwbz = '1' THEN 0 ELSE 1 END, z.srny DESC, z.id DESC
                """, keyParameters(key), POSITION_MAPPER);
    }

    List<AssessmentRecord> findChangedAssessments(PersonKey key) {
        if (!tableExists("dndkhb")) {
            return List.of();
        }
        return jdbcTemplate.query("""
                SELECT a.id, a.dwbm, a.grbm, a.khnd, a.khjg, FALSE AS app_created
                FROM dndkhb a
                WHERE a.dwbm = :dwbm AND a.grbm = :grbm
                ORDER BY khnd DESC
                """, keyParameters(key), ASSESSMENT_MAPPER);
    }

    List<Map<String, Object>> findChangedPayrollHistories(PersonKey key) {
        if (!tableExists("hisbaseb")) {
            return List.of();
        }
        return jdbcTemplate.query("""
                SELECT CAST(h.id AS CHAR) AS id,
                       h.jsnf AS calculationYear,
                       h.jsyf AS calculationMonth,
                       h.jslb AS changeType,
                       h.zwgw2 AS positionName,
                       h.jbgzjb2 AS gradeSalaryLevel,
                       h.zwgzdc2 AS positionSalaryGrade,
                       h.xckhndjb AS levelAssessmentStartYear,
                       h.xckhndzw AS stepAssessmentStartYear,
                       h.zwgzse2 AS positionSalary,
                       h.jbgzse2 AS gradeSalary,
                       h.hj2 AS totalAmount,
                       CASE WHEN h.sid IS NULL OR TRIM(h.sid) = '' THEN TRUE ELSE FALSE END AS currentPayroll
                FROM hisbaseb h
                WHERE h.dwbm = :dwbm AND h.grbm = :grbm
                ORDER BY
                    CASE WHEN h.sid IS NULL OR TRIM(h.sid) = '' THEN 0 ELSE 1 END,
                    COALESCE(h.jsnf, '') DESC,
                    LPAD(TRIM(COALESCE(h.jsyf, '')), 2, '0') DESC,
                    h.id DESC
                """, keyParameters(key), (rs, rowNum) -> {
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("id", SqlText.trim(rs.getString("id")));
            row.put("calculationYear", SqlText.trim(rs.getString("calculationYear")));
            row.put("calculationMonth", SqlText.trim(rs.getString("calculationMonth")));
            row.put("changeType", SqlText.trim(rs.getString("changeType")));
            row.put("positionName", SqlText.trim(rs.getString("positionName")));
            row.put("gradeSalaryLevel", SqlText.trim(rs.getString("gradeSalaryLevel")));
            row.put("positionSalaryGrade", SqlText.trim(rs.getString("positionSalaryGrade")));
            row.put("levelAssessmentStartYear", SqlText.trim(rs.getString("levelAssessmentStartYear")));
            row.put("stepAssessmentStartYear", SqlText.trim(rs.getString("stepAssessmentStartYear")));
            row.put("positionSalary", rs.getObject("positionSalary"));
            row.put("gradeSalary", rs.getObject("gradeSalary"));
            row.put("totalAmount", rs.getObject("totalAmount"));
            row.put("currentPayroll", rs.getBoolean("currentPayroll"));
            row.put("appCreated", false);
            return row;
        });
    }

    Map<String, Object> findChangedPersonnelRelatedRecords(PersonKey key) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("currentPayroll", firstTableRow(
                "hisbaseb",
                key,
                "CASE WHEN sid IS NULL OR TRIM(sid) = '' THEN 0 ELSE 1 END, jsnf DESC, jsyf DESC"));
        result.put("awards", tableRows("hjxxb", key, "hjsj DESC, id DESC"));
        result.put("rankRecords", tableRows("jxb", key, "sysj DESC, id DESC"));
        result.put("wageReform", tableRows("dtgxxb", key, "id DESC"));
        result.put("preReformSalary", tableRows("tgqgz2006b", key, "id DESC"));
        result.put("pensionBase", tableRows("jfjsb", key, "nd DESC, id DESC"));
        return result;
    }

    List<AssessmentRecord> findAssessments(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT a.id, a.dwbm, a.grbm, a.khnd, a.khjg, a.bbz, a.tjr, a.tjsj, a.shr, a.shsj,
                       marker.record_id IS NOT NULL AS app_created,
                       (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'dndkh' AND att.record_id = a.id AND att.record_key = '') AS attachment_count
                FROM dndkh a
                LEFT JOIN app_record_marker marker ON marker.table_name = 'dndkh'
                    AND marker.record_id COLLATE utf8mb4_0900_ai_ci = CAST(a.id AS CHAR) COLLATE utf8mb4_0900_ai_ci
                    AND marker.marker = 'APP_CREATED'
                WHERE a.dwbm = :dwbm AND a.grbm = :grbm
                ORDER BY khnd DESC
                """, keyParameters(key), ASSESSMENT_MAPPER);
    }

    int currentAssessmentStartYear(PersonKey key) {
        Map<String, Object> row = firstTableRow(
                "hisbase",
                key,
                "CASE WHEN sid IS NULL OR TRIM(sid) = '' THEN 0 ELSE 1 END, jsnf DESC, jsyf DESC");
        int levelStart = intValue(row.get("xckhndjb"));
        int stepStart = intValue(row.get("xckhndzw"));
        if (levelStart > 0 && stepStart > 0) {
            return Math.min(levelStart, stepStart);
        }
        return Math.max(levelStart, stepStart);
    }

    List<String> findMissingAssessmentYears(PersonKey key, int startYear, int targetYear) {
        if (startYear <= 0 || targetYear <= startYear) {
            return List.of();
        }
        List<String> existing = jdbcTemplate.queryForList("""
                SELECT DISTINCT khnd
                FROM dndkh
                WHERE dwbm = :dwbm AND grbm = :grbm
                  AND khnd BETWEEN :startYear AND :endYear
                """, keyParameters(key)
                .addValue("startYear", String.valueOf(startYear))
                .addValue("endYear", String.valueOf(targetYear - 1)), String.class);
        java.util.Set<String> existingYears = existing.stream()
                .map(SqlText::trim)
                .collect(java.util.stream.Collectors.toSet());
        List<String> missing = new java.util.ArrayList<>();
        for (int year = startYear; year < targetYear; year++) {
            String text = String.valueOf(year);
            if (!existingYears.contains(text)) {
                missing.add(text);
            }
        }
        return missing;
    }

    Map<String, Object> findPersonnelRelatedRecords(PersonKey key) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("currentPayroll", firstTableRow("hisbase", key, "CASE WHEN sid IS NULL OR TRIM(sid) = '' THEN 0 ELSE 1 END, jsnf DESC, jsyf DESC"));
        result.put("awards", findAwards(key));
        result.put("rankRecords", findRanks(key));
        result.put("wageReform", tableRows("dtgxx", key, "id DESC"));
        result.put("preReformSalary", tableRows("tgqgz2006", key, "id DESC"));
        result.put("pensionBase", tableRows("jfjs", key, "nd DESC, id DESC"));
        return result;
    }

    private Optional<PersonnelInformationCollectionShell> findInformationCollectionShell(int uid) {
        return jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, dw.dwbz, p.grbm, p.xm, p.sfzh, p.xb, p.csny,
                       p.mz, p.zzmm, p.ryfl, p.dwsx, p.gwfl, p.cjgzny, p.zzny, p.jrny, p.jrfs,
                       p.gznx, p.xlbm, p.zgxl, p.zwjb, p.zjbm, p.xrzw, p.srny, p.dah,
                       p.ydwzw, p.yzwrzsj, p.bgdwjc, p.txsj, p.jhlqsny, p.zdjhlnx
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE p.uid = :uid
                """, new MapSqlParameterSource("uid", uid), COLLECTION_SHELL_MAPPER).stream().findFirst();
    }

    private Optional<PersonnelInformationCollectionPayrollSnapshot> findCurrentPayrollSnapshot(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT h.jsnf, h.jsyf, h.jslb, h.zwbm2, h.zwgw2, h.jbgzjb2, h.zwgzdc2,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.hj2
                FROM hisbase h
                WHERE h.dwbm = :dwbm AND h.grbm = :grbm
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                ORDER BY h.jsnf DESC, h.jsyf DESC
                LIMIT 1
                """, keyParameters(key), COLLECTION_PAYROLL_MAPPER).stream().findFirst();
    }

    int createEducation(PersonKey key, EducationMaintenanceRequest request) {
        jdbcTemplate.update("""
                INSERT INTO dxl (dwbm, grbm, xlbm, xl, byyx, rxsj, bysj, xz, xllb, bz, bbz)
                VALUES (:dwbm, :grbm, :educationCode, :educationName, :school, :enrollmentDate, :graduationDate, :studyYears, :educationType, :remark, :approvalStatus)
                """, educationParameters(key, request));
        int id = lastInsertId();
        markAppCreated("dxl", id);
        return id;
    }

    void updateEducation(int id, EducationMaintenanceRequest request) {
        jdbcTemplate.update("""
                UPDATE dxl
                SET xlbm = :educationCode, xl = :educationName, byyx = :school, rxsj = :enrollmentDate,
                    bysj = :graduationDate, xz = :studyYears, xllb = :educationType, bz = :remark
                WHERE id = :id
                """, educationParameters(new PersonKey("", ""), request).addValue("id", id));
    }

    void deleteEducation(int id) {
        jdbcTemplate.update("DELETE FROM dxl WHERE id = :id", new MapSqlParameterSource("id", id));
        unmarkAppCreated("dxl", id);
    }

    int createPosition(PersonKey key, PositionMaintenanceRequest request) {
        jdbcTemplate.update("""
                INSERT INTO dryzwbh (dwbm, grbm, xrzwbm, xrzw, zwjb, zjbm, zwbm, xzzw, zwlb, srny, kjnx, xrzwbz, jsbz, linked_award_id, bbz)
                VALUES (:dwbm, :grbm, :currentPositionCode, :currentPosition, :positionLevel, :rankCode, :positionCode, :positionName, :positionChangeReason, :startYearMonth, :intervalYears, :activeFlag, :promotionFlag, :linkedAwardId, :approvalStatus)
                """, positionParameters(key, request));
        int id = lastInsertId();
        markAppCreated("dryzwbh", id);
        return id;
    }

    void updatePosition(int id, PositionMaintenanceRequest request) {
        jdbcTemplate.update("""
                UPDATE dryzwbh
                SET xrzwbm = :currentPositionCode, xrzw = :currentPosition, zwjb = :positionLevel, zjbm = :rankCode,
                    zwbm = :positionCode, xzzw = :positionName, zwlb = :positionChangeReason,
                    srny = :startYearMonth, kjnx = :intervalYears, xrzwbz = :activeFlag, jsbz = :promotionFlag,
                    linked_award_id = :linkedAwardId
                WHERE id = :id
                """, positionParameters(new PersonKey("", ""), request).addValue("id", id));
    }

    void clearOtherActivePositions(PersonKey key, int exceptId) {
        jdbcTemplate.update("""
                UPDATE dryzwbh
                SET xrzwbz = '0'
                WHERE dwbm = :dwbm
                  AND grbm = :grbm
                  AND xrzwbz = '1'
                  AND id <> :exceptId
                """, keyParameters(key).addValue("exceptId", exceptId));
    }

    void deletePosition(int id) {
        jdbcTemplate.update("DELETE FROM dryzwbh WHERE id = :id", new MapSqlParameterSource("id", id));
        unmarkAppCreated("dryzwbh", id);
    }

    int createAssessment(PersonKey key, AssessmentMaintenanceRequest request) {
        jdbcTemplate.update("""
                INSERT INTO dndkh (dwbm, grbm, khnd, khjg, bbz)
                VALUES (:dwbm, :grbm, :year, :result, :approvalStatus)
                """, assessmentParameters(key, request));
        int id = lastInsertId();
        markAppCreated("dndkh", id);
        return id;
    }

    void updateAssessment(int id, AssessmentMaintenanceRequest request) {
        jdbcTemplate.update("""
                UPDATE dndkh
                SET khnd = :year, khjg = :result
                WHERE id = :id
                """, assessmentParameters(new PersonKey("", ""), request).addValue("id", id));
    }

    void deleteAssessment(int id) {
        jdbcTemplate.update("DELETE FROM dndkh WHERE id = :id", new MapSqlParameterSource("id", id));
        unmarkAppCreated("dndkh", id);
    }

    int createAward(PersonKey key, AwardMaintenanceRequest request) {
        jdbcTemplate.update("""
                INSERT INTO hjxx (dwbm, grbm, hjmc, sjdw, jllx, hjsj, tqyjjssj, qtqk, jldc, jljb, bbz)
                VALUES (:dwbm, :grbm, :hjmc, :sjdw, :jllx, :hjsj, :tqyjjssj, :qtqk, :jldc, :jljb, :approvalStatus)
                """, awardParameters(key, request));
        int id = lastInsertId();
        markAppCreated("hjxx", id);
        return id;
    }

    void updateAward(int id, AwardMaintenanceRequest request) {
        jdbcTemplate.update("""
                UPDATE hjxx
                SET hjmc = :hjmc, sjdw = :sjdw, jllx = :jllx, hjsj = :hjsj,
                    tqyjjssj = :tqyjjssj, qtqk = :qtqk, jldc = :jldc, jljb = :jljb
                WHERE id = :id
                """, awardParameters(new PersonKey("", ""), request).addValue("id", id));
    }

    void deleteAward(int id) {
        jdbcTemplate.update("DELETE FROM hjxx WHERE id = :id", new MapSqlParameterSource("id", id));
        unmarkAppCreated("hjxx", id);
    }

    int createRank(PersonKey key, RankMaintenanceRequest request) {
        jdbcTemplate.update("""
                INSERT INTO jx (dwbm, grbm, jx, sysj, syyy, rmwh, xrjxbz, lb, bbz)
                VALUES (:dwbm, :grbm, :jx, :sysj, :syyy, :rmwh, :xrjxbz, :lb, :approvalStatus)
                """, rankParameters(key, request));
        int id = lastInsertId();
        markAppCreated("jx", id);
        return id;
    }

    void updateRank(int id, RankMaintenanceRequest request) {
        jdbcTemplate.update("""
                UPDATE jx
                SET jx = :jx, sysj = :sysj, syyy = :syyy, rmwh = :rmwh, xrjxbz = :xrjxbz, lb = :lb
                WHERE id = :id
                """, rankParameters(new PersonKey("", ""), request).addValue("id", id));
    }

    void deleteRank(int id) {
        jdbcTemplate.update("DELETE FROM jx WHERE id = :id", new MapSqlParameterSource("id", id));
        unmarkAppCreated("jx", id);
    }

    List<BatchAssessmentEntryRow> findBatchAssessmentEntries(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String keyword,
            boolean includeDescendants) {
        if (organizationScope.noneScope() || emptyToNull(year) == null) {
            return List.of();
        }
        MapSqlParameterSource params = batchAssessmentParameters(organizationScope, organizationCode, year, keyword, includeDescendants);
        List<BatchAssessmentEntryRow> rows = jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.ryfl, p.dwsx, p.xrzw,
                       dw.dwbz AS org_dwbz, dw.gzczbz AS org_gzczbz,
                       :year AS khnd, a.id AS assessment_id, a.khjg AS assessment_result, a.bbz AS assessment_approval_status
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                LEFT JOIN dndkh a ON a.dwbm = p.dwbm AND a.grbm = p.grbm AND a.khnd = :year
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND (%s)
                  AND (:keyword IS NULL OR p.xm LIKE :keywordLike OR p.grbm LIKE :keywordLike OR p.sfzh LIKE :keywordLike)
                ORDER BY p.dwbm, p.grbm
                """.formatted(batchOrganizationFilterSql(includeDescendants), batchAssessmentWorkStartedFilterSql()), params, (rs, rowNum) -> {
            BatchAssessmentEntryRow row = BATCH_ASSESSMENT_ENTRY_MAPPER.mapRow(rs, rowNum);
            String defaultResult = defaultAssessmentResultText(
                    row.personnelCategory(),
                    row.organizationType(),
                    SqlText.trim(rs.getString("org_dwbz")),
                    SqlText.trim(rs.getString("org_gzczbz")));
            return new BatchAssessmentEntryRow(
                    row.uid(),
                    row.organizationCode(),
                    row.organizationName(),
                    row.personCode(),
                    row.name(),
                    row.personnelCategory(),
                    row.organizationType(),
                    row.currentPosition(),
                    row.year(),
                    row.assessmentId(),
                    row.result(),
                    row.approvalStatus(),
                    defaultResult);
        });
        return rows;
    }

    Optional<PersonnelSummary> findPersonnelSummary(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.sfzh, p.xb, p.csny,
                       p.ryfl, p.dwsx, p.gwfl, p.xrzw, p.zjbm, h.zwbm2, h.zwgw2, p.bbz
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                LEFT JOIN hisbase h ON h.id = (
                    SELECT h2.id
                    FROM hisbase h2
                    WHERE h2.dwbm = p.dwbm
                      AND h2.grbm = p.grbm
                      AND (h2.sid IS NULL OR TRIM(h2.sid) = '')
                    ORDER BY COALESCE(h2.jsnf, '') DESC, COALESCE(h2.jsyf, '') DESC, h2.id DESC
                    LIMIT 1
                )
                WHERE p.dwbm = :dwbm AND p.grbm = :grbm
                """, keyParameters(key), SUMMARY_MAPPER).stream().findFirst();
    }

    Optional<Integer> findAssessmentId(PersonKey key, String year) {
        List<Integer> ids = jdbcTemplate.query("""
                SELECT id
                FROM dndkh
                WHERE dwbm = :dwbm AND grbm = :grbm AND khnd = :year
                """, keyParameters(key).addValue("year", year), (rs, rowNum) -> rs.getInt("id"));
        return ids.stream().findFirst();
    }

    boolean upsertAssessment(PersonKey key, AssessmentMaintenanceRequest request) {
        Optional<Integer> existingId = findAssessmentId(key, request.year());
        if (existingId.isPresent()) {
            updateAssessment(existingId.get(), request);
            return false;
        }
        createAssessment(key, request);
        return true;
    }

    List<AnnualAssessmentRecord> findAnnualAssessments(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = assessmentParameters(organizationScope, organizationCode, year, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT a.id, a.dwbm, dw.dwmc, a.grbm, p.xm, a.khnd, a.khjg
                FROM dndkh a
                LEFT JOIN dryjbxx p ON p.dwbm = a.dwbm AND p.grbm = a.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = a.dwbm
                WHERE (:allOrganizations = TRUE OR a.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND (:year IS NULL OR a.khnd = :year)
                  AND (:keyword IS NULL OR a.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike OR a.khjg LIKE :keywordLike)
                ORDER BY a.khnd DESC, a.dwbm, a.grbm
                LIMIT :limit OFFSET :offset
                """.formatted(organizationDescendantFilter("a")), params, ANNUAL_ASSESSMENT_MAPPER);
    }

    long countAnnualAssessments(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dndkh a
                LEFT JOIN dryjbxx p ON p.dwbm = a.dwbm AND p.grbm = a.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = a.dwbm
                WHERE (:allOrganizations = TRUE OR a.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND (:year IS NULL OR a.khnd = :year)
                  AND (:keyword IS NULL OR a.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike OR a.khjg LIKE :keywordLike)
                """.formatted(organizationDescendantFilter("a")), assessmentParameters(organizationScope, organizationCode, year, keyword), Long.class);
        return count == null ? 0 : count;
    }

    List<AnnualAssessmentSummaryRecord> findAnnualAssessmentSummary(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String result,
            boolean includeDescendants,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = assessmentSummaryParameters(
                        organizationScope, organizationCode, year, result, includeDescendants)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT a.khnd, a.dwbm, dw.dwmc, a.khjg, COUNT(*) AS personnel_count
                FROM dndkh a
                LEFT JOIN dwbm dw ON dw.dwbm = a.dwbm
                WHERE (:allOrganizations = TRUE OR a.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND (:year IS NULL OR a.khnd = :year)
                  AND (:result IS NULL OR a.khjg = :result)
                GROUP BY a.khnd, a.dwbm, dw.dwmc, a.khjg
                ORDER BY a.khnd DESC, a.dwbm, a.khjg
                LIMIT :limit OFFSET :offset
                """.formatted(summaryOrganizationFilterSql(includeDescendants)), params, ANNUAL_ASSESSMENT_SUMMARY_MAPPER);
    }

    long countAnnualAssessmentSummary(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String result,
            boolean includeDescendants) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM (
                    SELECT 1
                    FROM dndkh a
                    LEFT JOIN dwbm dw ON dw.dwbm = a.dwbm
                    WHERE (:allOrganizations = TRUE OR a.dwbm IN (:organizationCodes))
                      AND (%s)
                      AND (:year IS NULL OR a.khnd = :year)
                      AND (:result IS NULL OR a.khjg = :result)
                    GROUP BY a.khnd, a.dwbm, a.khjg
                ) grouped_assessment
                """.formatted(summaryOrganizationFilterSql(includeDescendants)),
                assessmentSummaryParameters(organizationScope, organizationCode, year, result, includeDescendants),
                Long.class);
        return count == null ? 0 : count;
    }

    private MapSqlParameterSource parameters(OrganizationScope organizationScope, String organizationFilter, String keyword) {
        String trimmedKeyword = SqlText.trim(keyword);
        String trimmedOrganizationFilter = SqlText.trim(organizationFilter);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationFilter", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : trimmedOrganizationFilter)
                .addValue("organizationCodePrefixLike", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : trimmedOrganizationFilter + "%")
                .addValue("keyword", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : "%" + trimmedKeyword + "%");
    }

    private MapSqlParameterSource maintenanceParameters(PersonnelMaintenanceRequest request) {
        return new MapSqlParameterSource()
                .addValue("organizationCode", valueOrBlank(request.organizationCode()))
                .addValue("personCode", valueOrBlank(request.personCode()))
                .addValue("name", valueOrBlank(request.name()))
                .addValue("idCard", valueOrBlank(request.idCard()))
                .addValue("gender", valueOrBlank(request.gender()))
                .addValue("birthYearMonth", valueOrBlank(request.birthYearMonth()))
                .addValue("personnelCategory", valueOrBlank(request.personnelCategory()))
                .addValue("organizationType", valueOrBlank(request.organizationType()))
                .addValue("postCategory", valueOrBlank(request.postCategory()))
                .addValue("workStartYearMonth", valueOrBlank(request.workStartYearMonth()))
                .addValue("regularizationYearMonth", valueOrBlank(request.regularizationYearMonth()))
                .addValue("salaryYears", request.salaryYears() == null ? 0 : request.salaryYears())
                .addValue("educationCode", valueOrBlank(request.educationCode()))
                .addValue("highestEducation", valueOrBlank(request.highestEducation()))
                .addValue("currentPositionLevel", valueOrBlank(request.currentPositionLevel()))
                .addValue("currentRankCode", valueOrBlank(request.currentRankCode()))
                .addValue("currentPosition", valueOrBlank(request.currentPosition()))
                .addValue("currentPositionStartYearMonth", valueOrBlank(request.currentPositionStartYearMonth()))
                .addValue("ethnicity", valueOrBlank(request.ethnicity()))
                .addValue("politicalStatus", valueOrBlank(request.politicalStatus()))
                .addValue("archiveNumber", valueOrBlank(request.archiveNumber()))
                .addValue("joinYearMonth", valueOrBlank(request.joinYearMonth()))
                .addValue("joinType", valueOrBlank(request.joinType()));
    }

    private MapSqlParameterSource educationParameters(PersonKey key, EducationMaintenanceRequest request) {
        return new MapSqlParameterSource()
                .addValue("dwbm", key.organizationCode())
                .addValue("grbm", key.personCode())
                .addValue("educationCode", valueOrBlank(request.educationCode()))
                .addValue("educationName", valueOrBlank(request.educationName()))
                .addValue("school", valueOrBlank(request.school()))
                .addValue("enrollmentDate", valueOrBlank(request.enrollmentDate()))
                .addValue("graduationDate", valueOrBlank(request.graduationDate()))
                .addValue("studyYears", request.studyYears() == null ? 0 : request.studyYears())
                .addValue("educationType", valueOrBlank(request.educationType()))
                .addValue("remark", valueOrBlank(request.remark()))
                .addValue("approvalStatus", PersonnelSubrecordEditPolicy.defaultApprovalStatus());
    }

    private MapSqlParameterSource positionParameters(PersonKey key, PositionMaintenanceRequest request) {
        return new MapSqlParameterSource()
                .addValue("dwbm", key.organizationCode())
                .addValue("grbm", key.personCode())
                .addValue("currentPositionCode", valueOrBlank(request.currentPositionCode()))
                .addValue("currentPosition", valueOrBlank(request.currentPosition()))
                .addValue("positionLevel", valueOrBlank(request.positionLevel()))
                .addValue("rankCode", valueOrBlank(request.rankCode()))
                .addValue("positionCode", valueOrBlank(request.positionCode()))
                .addValue("positionName", valueOrBlank(request.positionName()))
                .addValue("startYearMonth", valueOrBlank(request.startYearMonth()))
                .addValue("intervalYears", request.intervalYears() == null ? 0 : request.intervalYears())
                .addValue("activeFlag", valueOrBlank(request.activeFlag()))
                .addValue("promotionFlag", valueOrBlank(request.promotionFlag()))
                .addValue("positionChangeReason", valueOrBlank(request.positionChangeReason()))
                .addValue("linkedAwardId", request.linkedAwardId())
                .addValue("approvalStatus", PersonnelSubrecordEditPolicy.defaultApprovalStatus());
    }

    private MapSqlParameterSource assessmentParameters(PersonKey key, AssessmentMaintenanceRequest request) {
        return new MapSqlParameterSource()
                .addValue("dwbm", key.organizationCode())
                .addValue("grbm", key.personCode())
                .addValue("year", valueOrBlank(request.year()))
                .addValue("result", valueOrBlank(request.result()))
                .addValue("approvalStatus", PersonnelSubrecordEditPolicy.defaultApprovalStatus());
    }

    private MapSqlParameterSource awardParameters(PersonKey key, AwardMaintenanceRequest request) {
        return new MapSqlParameterSource()
                .addValue("dwbm", key.organizationCode())
                .addValue("grbm", key.personCode())
                .addValue("hjmc", valueOrBlank(request.hjmc()))
                .addValue("sjdw", valueOrBlank(request.sjdw()))
                .addValue("jllx", valueOrBlank(request.jllx()))
                .addValue("hjsj", valueOrBlank(request.hjsj()))
                .addValue("tqyjjssj", valueOrBlank(request.tqyjjssj()))
                .addValue("qtqk", valueOrBlank(request.qtqk()))
                .addValue("jldc", request.jldc() == null ? 0 : request.jldc())
                .addValue("jljb", request.jljb() == null ? 0 : request.jljb())
                .addValue("approvalStatus", PersonnelSubrecordEditPolicy.defaultApprovalStatus());
    }

    private MapSqlParameterSource rankParameters(PersonKey key, RankMaintenanceRequest request) {
        return new MapSqlParameterSource()
                .addValue("dwbm", key.organizationCode())
                .addValue("grbm", key.personCode())
                .addValue("jx", valueOrBlank(request.jx()))
                .addValue("sysj", valueOrBlank(request.sysj()))
                .addValue("syyy", valueOrBlank(request.syyy()))
                .addValue("rmwh", valueOrBlank(request.rmwh()))
                .addValue("xrjxbz", request.xrjxbz() == null ? 0 : request.xrjxbz())
                .addValue("lb", valueOrBlank(request.lb()))
                .addValue("approvalStatus", PersonnelSubrecordEditPolicy.defaultApprovalStatus());
    }

    private int lastInsertId() {
        Integer id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Integer.class);
        return id == null ? 0 : id;
    }

    private int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(value.toString().trim());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private static Integer readOptionalInteger(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Object value = rs.getObject(column);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void markAppCreated(String tableName, Object recordId) {
        jdbcTemplate.update("""
                INSERT IGNORE INTO app_record_marker (table_name, record_id, marker)
                VALUES (:tableName, :recordId, 'APP_CREATED')
                """, new MapSqlParameterSource()
                .addValue("tableName", tableName)
                .addValue("recordId", String.valueOf(recordId)));
    }

    private void unmarkAppCreated(String tableName, Object recordId) {
        jdbcTemplate.update("""
                DELETE FROM app_record_marker
                WHERE table_name = :tableName AND record_id = :recordId AND marker = 'APP_CREATED'
                """, new MapSqlParameterSource()
                .addValue("tableName", tableName)
                .addValue("recordId", String.valueOf(recordId)));
    }

    private String valueOrBlank(String value) {
        String trimmed = SqlText.trim(value);
        return trimmed == null ? "" : trimmed;
    }

    private MapSqlParameterSource keyParameters(PersonKey key) {
        return new MapSqlParameterSource()
                .addValue("dwbm", key.organizationCode())
                .addValue("grbm", key.personCode());
    }

    private Optional<PersonKey> findSubrecordKeyById(String tableName, int id) {
        return jdbcTemplate.query("""
                SELECT dwbm, grbm
                FROM %s
                WHERE id = :id
                """.formatted(tableName), new MapSqlParameterSource("id", id), (rs, rowNum) -> new PersonKey(
                SqlText.trim(rs.getString("dwbm")),
                SqlText.trim(rs.getString("grbm")))).stream().findFirst();
    }

    private MapSqlParameterSource assessmentParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String keyword) {
        String trimmedKeyword = SqlText.trim(keyword);
        String trimmedOrganizationFilter = SqlText.trim(organizationCode);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationFilter", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : trimmedOrganizationFilter)
                .addValue("organizationCodePrefixLike", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : trimmedOrganizationFilter + "%")
                .addValue("year", emptyToNull(year))
                .addValue("keyword", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : "%" + trimmedKeyword + "%");
    }

    private MapSqlParameterSource batchAssessmentParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String keyword,
            boolean includeDescendants) {
        String trimmedKeyword = SqlText.trim(keyword);
        String trimmedOrganizationCode = SqlText.trim(organizationCode);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationCode", trimmedOrganizationCode)
                .addValue("organizationCodeLike", trimmedOrganizationCode == null || trimmedOrganizationCode.isEmpty() ? null : trimmedOrganizationCode + "%")
                .addValue("organizationFilter", trimmedOrganizationCode == null || trimmedOrganizationCode.isEmpty() ? null : trimmedOrganizationCode)
                .addValue("year", SqlText.trim(year))
                .addValue("keyword", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : "%" + trimmedKeyword + "%")
                .addValue("includeDescendants", includeDescendants);
    }

    private String batchOrganizationFilterSql(boolean includeDescendants) {
        return includeDescendants
                ? "(:organizationFilter IS NULL OR p.dwbm LIKE :organizationCodeLike)"
                : "(:organizationFilter IS NULL OR p.dwbm = :organizationCode)";
    }

    private String organizationDescendantFilter(String alias) {
        return "(:organizationFilter IS NULL OR %s.dwbm = :organizationFilter OR %s.dwbm LIKE :organizationCodePrefixLike)"
                .formatted(alias, alias);
    }

    /** 仅考核年度已参加工作（cjgzny，缺省取 jrny）的人员参与年度考核录入。 */
    private String batchAssessmentWorkStartedFilterSql() {
        return """
                (
                  NULLIF(TRIM(p.cjgzny), '') IS NOT NULL
                  OR NULLIF(TRIM(p.jrny), '') IS NOT NULL
                )
                AND LEFT(
                  REPLACE(
                    COALESCE(NULLIF(TRIM(p.cjgzny), ''), NULLIF(TRIM(p.jrny), ''), '000000'),
                    '.', ''
                  ),
                  4
                ) <= :year
                """;
    }

    private String defaultAssessmentResultText(
            String personnelCategory,
            String organizationType,
            String organizationCategory,
            String organizationPayrollCategory) {
        return PersonnelService.defaultAssessmentResultText(
                personnelCategory, organizationType, organizationCategory, organizationPayrollCategory);
    }

    private String defaultAssessmentResultText(String personnelCategory, String organizationType) {
        return defaultAssessmentResultText(personnelCategory, organizationType, null, null);
    }

    private MapSqlParameterSource assessmentSummaryParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String result,
            boolean includeDescendants) {
        String trimmedOrganizationCode = SqlText.trim(organizationCode);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationCode", trimmedOrganizationCode)
                .addValue("organizationCodeLike", trimmedOrganizationCode == null || trimmedOrganizationCode.isEmpty() ? null : trimmedOrganizationCode + "%")
                .addValue("organizationFilter", trimmedOrganizationCode == null || trimmedOrganizationCode.isEmpty() ? null : trimmedOrganizationCode)
                .addValue("includeDescendants", includeDescendants)
                .addValue("year", emptyToNull(year))
                .addValue("result", emptyToNull(result));
    }

    private String summaryOrganizationFilterSql(boolean includeDescendants) {
        return includeDescendants
                ? "(:organizationFilter IS NULL OR a.dwbm LIKE :organizationCodeLike)"
                : "(:organizationFilter IS NULL OR a.dwbm = :organizationCode)";
    }

    private MapSqlParameterSource personnelHistoryParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword) {
        return personnelHistoryParameters(organizationScope, organizationCode, keyword, null, null);
    }

    private MapSqlParameterSource personnelHistoryParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String positionCode) {
        return personnelHistoryParameters(organizationScope, organizationCode, keyword, positionCode, null);
    }

    private MapSqlParameterSource personnelHistoryParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String positionCode,
            String educationCode) {
        String trimmedKeyword = SqlText.trim(keyword);
        String trimmedOrganizationFilter = SqlText.trim(organizationCode);
        String trimmedPositionCode = SqlText.trim(positionCode);
        String trimmedEducationCode = SqlText.trim(educationCode);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationFilter", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : trimmedOrganizationFilter)
                .addValue("organizationCodePrefixLike", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : trimmedOrganizationFilter + "%")
                .addValue("keyword", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : "%" + trimmedKeyword + "%")
                .addValue("positionCode", trimmedPositionCode == null || trimmedPositionCode.isEmpty() ? null : trimmedPositionCode)
                .addValue("educationCode", trimmedEducationCode == null || trimmedEducationCode.isEmpty() ? null : trimmedEducationCode);
    }

    private MapSqlParameterSource changedPersonnelParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String period,
            String keyword) {
        String trimmedKeyword = SqlText.trim(keyword);
        String trimmedOrganizationFilter = SqlText.trim(organizationCode);
        String normalizedPeriod = emptyToNull(period) == null ? null : period.trim().replaceAll("\\D", "");
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationFilter", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : trimmedOrganizationFilter)
                .addValue("organizationCodePrefixLike", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : trimmedOrganizationFilter + "%")
                .addValue("period", normalizedPeriod)
                .addValue("periodLike", normalizedPeriod == null ? null : "%" + normalizedPeriod + "%")
                .addValue("keyword", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : "%" + trimmedKeyword + "%");
    }

    private void insertCommonColumns(
            String sourceTable,
            String targetTable,
            String excludedColumn,
            String sourceAlias,
            String whereClause,
            MapSqlParameterSource parameters) {
        List<TableColumn> sourceColumns = tableColumns(sourceTable);
        List<TableColumn> targetColumns = tableColumns(targetTable);
        Map<String, TableColumn> sourceByName = sourceColumns.stream()
                .collect(java.util.stream.Collectors.toMap(
                        column -> column.name().toLowerCase(Locale.ROOT),
                        column -> column,
                        (left, right) -> left));
        List<TableColumn> commonColumns = new ArrayList<>();
        for (TableColumn targetColumn : targetColumns) {
            if (excludedColumn != null && targetColumn.name().equalsIgnoreCase(excludedColumn)) {
                continue;
            }
            if (sourceByName.containsKey(targetColumn.name().toLowerCase(Locale.ROOT))) {
                commonColumns.add(targetColumn);
            }
        }
        if (commonColumns.isEmpty()) {
            throw new IllegalStateException("No common columns between " + sourceTable + " and " + targetTable);
        }
        String targetColumnSql = commonColumns.stream()
                .map(column -> quote(column.name()))
                .collect(java.util.stream.Collectors.joining(", "));
        String sourceColumnSql = commonColumns.stream()
                .map(column -> sourceExpression(
                        sourceAlias,
                        sourceByName.get(column.name().toLowerCase(Locale.ROOT))))
                .collect(java.util.stream.Collectors.joining(", "));
        jdbcTemplate.update("""
                INSERT INTO %s (%s)
                SELECT %s
                FROM %s %s
                WHERE %s
                """.formatted(quote(targetTable), targetColumnSql, sourceColumnSql, quote(sourceTable), sourceAlias, whereClause), parameters);
    }

    private void moveRelatedRecordsToChanged(PersonKey key) {
        for (TablePair pair : PERSONNEL_CHANGE_TABLE_PAIRS) {
            moveTableRows(pair.activeTable(), pair.changedTable(), key);
        }
    }

    private void restoreRelatedRecords(PersonKey key) {
        for (TablePair pair : PERSONNEL_CHANGE_TABLE_PAIRS) {
            moveTableRows(pair.changedTable(), pair.activeTable(), key);
        }
    }

    private void moveTableRows(String sourceTable, String targetTable, PersonKey key) {
        if (!tableExists(sourceTable) || !tableExists(targetTable)) {
            return;
        }
        MapSqlParameterSource parameters = keyParameters(key);
        jdbcTemplate.update("""
                DELETE FROM %s
                WHERE dwbm = :dwbm AND grbm = :grbm
                """.formatted(quote(targetTable)), parameters);
        insertCommonColumns(
                sourceTable,
                targetTable,
                "id",
                "src",
                "src.dwbm = :dwbm AND src.grbm = :grbm",
                parameters);
        jdbcTemplate.update("""
                DELETE FROM %s
                WHERE dwbm = :dwbm AND grbm = :grbm
                """.formatted(quote(sourceTable)), parameters);
    }

    private List<Map<String, Object>> tableRows(String tableName, PersonKey key, String orderBy) {
        if (!tableExists(tableName)) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT *
                FROM %s
                WHERE dwbm = :dwbm AND grbm = :grbm
                ORDER BY %s
                """.formatted(quote(tableName), orderBy), keyParameters(key));
    }

    private Map<String, Object> firstTableRow(String tableName, PersonKey key, String orderBy) {
        List<Map<String, Object>> rows = tableRows(tableName, key, orderBy);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE LOWER(table_schema) = LOWER(COALESCE(SCHEMA(), DATABASE()))
                  AND LOWER(table_name) = LOWER(:tableName)
                """, new MapSqlParameterSource("tableName", tableName), Integer.class);
        return count != null && count > 0;
    }

    private List<TableColumn> tableColumns(String tableName) {
        return jdbcTemplate.query("""
                SELECT column_name, data_type
                FROM information_schema.columns
                WHERE LOWER(table_schema) = LOWER(COALESCE(SCHEMA(), DATABASE()))
                  AND LOWER(table_name) = LOWER(:tableName)
                ORDER BY ordinal_position
                """, new MapSqlParameterSource("tableName", tableName), (rs, rowNum) -> new TableColumn(
                rs.getString("column_name"),
                rs.getString("data_type")));
    }

    private String sourceExpression(String alias, TableColumn column) {
        String source = alias + "." + quote(column.name());
        if (numericType(column.dataType())) {
            return "COALESCE(" + source + ", 0)";
        }
        if (temporalType(column.dataType())) {
            return "CASE WHEN " + source + " IS NULL OR TRIM(CAST(" + source + " AS CHAR)) = '' THEN NULL ELSE "
                    + source + " END";
        }
        return "COALESCE(" + source + ", '')";
    }

    private boolean temporalType(String dataType) {
        String type = String.valueOf(dataType).toLowerCase(Locale.ROOT);
        return type.contains("timestamp") || type.contains("datetime") || "date".equals(type) || "time".equals(type);
    }

    private boolean numericType(String dataType) {
        return Set.of("int", "integer", "bigint", "smallint", "tinyint", "decimal", "numeric", "float", "double")
                .contains(String.valueOf(dataType).toLowerCase());
    }

    private String quote(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    private String personnelChangeRemark(PersonnelChangeRequest request, String changePeriod) {
        String type = valueOrBlank(request.changeType());
        String period = displayChangePeriod(changePeriod);
        String remark = valueOrBlank(request.remark());
        return (type + (period.isBlank() ? "" : " " + period) + (remark.isBlank() ? "" : " " + remark)).trim();
    }

    private String normalizedChangePeriod(String effectivePeriod) {
        String normalized = valueOrBlank(effectivePeriod).replace(".", "");
        if (normalized.length() >= 6) {
            return normalized.substring(0, 6);
        }
        YearMonth current = YearMonth.now();
        return "%04d%02d".formatted(current.getYear(), current.getMonthValue());
    }

    private String displayChangePeriod(String normalizedPeriod) {
        return normalizedPeriod.substring(0, 4) + "." + normalizedPeriod.substring(4, 6);
    }

    private record TableColumn(String name, String dataType) {
    }

    private record TablePair(String activeTable, String changedTable) {
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String normalizeSubrecordApproval(String value) {
        String normalized = SqlText.trim(value);
        return normalized == null || normalized.isBlank()
                ? PersonnelSubrecordEditPolicy.defaultApprovalStatus()
                : normalized;
    }

    private static LocalDateTime readTimestamp(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static ApprovalActorFields readApprovalActorFields(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new ApprovalActorFields(
                SqlText.trim(rs.getString("tjr")),
                readTimestamp(rs, "tjsj"),
                SqlText.trim(rs.getString("shr")),
                readTimestamp(rs, "shsj"));
    }

    private static final List<String> AUDIT_SUBMIT_ACTIONS = List.of(
            "PERSONNEL_APPROVAL_SUBMIT",
            "PERSONNEL_SUBRECORD_SUBMIT");
    private static final List<String> AUDIT_APPROVE_ACTIONS = List.of(
            "PERSONNEL_APPROVE",
            "PERSONNEL_SUBRECORD_APPROVE");

    private static final RowMapper<PersonnelApprovalTrackingRecord> APPROVAL_TRACKING_BASE_MAPPER = (rs, rowNum) -> {
        ApprovalActorFields actors = readApprovalActorFields(rs);
        return new PersonnelApprovalTrackingRecord(
                rs.getInt("uid"),
                SqlText.trim(rs.getString("record_type")),
                rs.getInt("record_id"),
                SqlText.trim(rs.getString("dwbm")),
                SqlText.trim(rs.getString("dwmc")),
                SqlText.trim(rs.getString("grbm")),
                SqlText.trim(rs.getString("xm")),
                SqlText.trim(rs.getString("summary")),
                SqlText.trim(rs.getString("position_name")),
                SqlText.trim(rs.getString("effective_year_month")),
                rs.getInt("attachment_count"),
                SqlText.trim(rs.getString("approval_status")),
                SqlText.trim(rs.getString("audit_target_type")),
                SqlText.trim(rs.getString("audit_target_id")),
                actors.submittedBy(),
                actors.submittedAt(),
                actors.approvedBy(),
                actors.approvedAt());
    };

    private static RowMapper<PersonnelApprovalTrackingRecord> approvalTrackingAuditMapper(boolean approvedFields) {
        return (rs, rowNum) -> {
            java.sql.Timestamp auditMoment = rs.getTimestamp("audit_moment");
            LocalDateTime moment = auditMoment == null ? null : auditMoment.toLocalDateTime();
            String actor = SqlText.trim(rs.getString("audit_actor"));
            return new PersonnelApprovalTrackingRecord(
                    rs.getInt("uid"),
                    SqlText.trim(rs.getString("record_type")),
                    rs.getInt("record_id"),
                    SqlText.trim(rs.getString("dwbm")),
                    SqlText.trim(rs.getString("dwmc")),
                    SqlText.trim(rs.getString("grbm")),
                    SqlText.trim(rs.getString("xm")),
                    SqlText.trim(rs.getString("summary")),
                    SqlText.trim(rs.getString("position_name")),
                    SqlText.trim(rs.getString("effective_year_month")),
                    rs.getInt("attachment_count"),
                    SqlText.trim(rs.getString("approval_status")),
                    SqlText.trim(rs.getString("audit_target_type")),
                    SqlText.trim(rs.getString("audit_target_id")),
                    approvedFields ? null : actor,
                    approvedFields ? null : moment,
                    approvedFields ? actor : null,
                    approvedFields ? moment : null);
        };
    }

    List<PersonnelApprovalTrackingRecord> findApprovalTracking(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String approvalStatus,
            boolean submittedByMe,
            String submittedByUsername,
            Integer approvedWithinDays,
            String recordType,
            String assessmentYear,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = approvalTrackingParameters(
                organizationScope,
                organizationCode,
                keyword,
                approvalStatus,
                submittedByMe,
                submittedByUsername,
                approvedWithinDays,
                recordType,
                assessmentYear)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT uid, record_type, record_id, dwbm, dwmc, grbm, xm, summary,
                       position_name, effective_year_month, attachment_count,
                       approval_status, audit_target_type, audit_target_id,
                       tjr, tjsj, shr, shsj
                FROM (%s) tracking
                WHERE %s
                %s
                LIMIT :limit OFFSET :offset
                """.formatted(
                approvalTrackingUnionSql(),
                approvalTrackingOuterFilterSql(),
                approvalTrackingStatusUnionOrderBySql(approvalStatus)),
                params,
                APPROVAL_TRACKING_BASE_MAPPER);
    }

    long countApprovalTracking(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String approvalStatus,
            boolean submittedByMe,
            String submittedByUsername,
            Integer approvedWithinDays,
            String recordType,
            String assessmentYear) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        MapSqlParameterSource params = approvalTrackingParameters(
                organizationScope,
                organizationCode,
                keyword,
                approvalStatus,
                submittedByMe,
                submittedByUsername,
                approvedWithinDays,
                recordType,
                assessmentYear);
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM (%s) tracking
                WHERE %s
                """.formatted(approvalTrackingUnionSql(), approvalTrackingOuterFilterSql()), params, Long.class);
        return count == null ? 0 : count;
    }

    Optional<AssessmentApprovalStats> findAssessmentApprovalStats(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            boolean includeDescendants) {
        if (organizationScope.noneScope() || emptyToNull(year) == null) {
            return Optional.empty();
        }
        MapSqlParameterSource params = assessmentSummaryParameters(
                organizationScope, organizationCode, year, null, includeDescendants);
        List<AssessmentResultAggregateRow> rows = jdbcTemplate.query("""
                SELECT TRIM(a.khjg) AS khjg, TRIM(COALESCE(a.bbz, '')) AS bbz, COUNT(*) AS cnt
                FROM dndkh a
                WHERE (:allOrganizations = TRUE OR a.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND a.khnd = :year
                  AND TRIM(COALESCE(a.khjg, '')) <> ''
                GROUP BY TRIM(a.khjg), TRIM(COALESCE(a.bbz, ''))
                """.formatted(summaryOrganizationFilterSql(includeDescendants)),
                params,
                (rs, rowNum) -> new AssessmentResultAggregateRow(
                        SqlText.trim(rs.getString("khjg")),
                        SqlText.trim(rs.getString("bbz")),
                        rs.getInt("cnt")));
        if (rows.isEmpty()) {
            String orgCode = SqlText.trim(organizationCode);
            String orgName = orgCode == null || orgCode.isBlank()
                    ? ""
                    : jdbcTemplate.query("""
                            SELECT dwmc FROM dwbm WHERE dwbm = :code LIMIT 1
                            """,
                            new MapSqlParameterSource("code", orgCode),
                            (rs, rowNum) -> SqlText.trim(rs.getString("dwmc"))).stream().findFirst().orElse("");
            return Optional.of(new AssessmentApprovalStats(
                    orgCode == null ? "" : orgCode,
                    orgName,
                    year,
                    0,
                    0,
                    java.math.BigDecimal.ZERO,
                    0,
                    0,
                    0,
                    0,
                    List.of()));
        }
        java.util.Map<String, Integer> resultCounts = new java.util.LinkedHashMap<>();
        int participantCount = 0;
        int excellentCount = 0;
        int pendingCount = 0;
        int pendingExcellentCount = 0;
        int approvedCount = 0;
        int approvedExcellentCount = 0;
        for (AssessmentResultAggregateRow row : rows) {
            resultCounts.merge(row.result(), row.count(), Integer::sum);
            participantCount += row.count();
            if ("优秀".equals(row.result())) {
                excellentCount += row.count();
            }
            if (PersonnelApprovalStatuses.SUBMITTED.equals(row.approvalStatus())) {
                pendingCount += row.count();
                if ("优秀".equals(row.result())) {
                    pendingExcellentCount += row.count();
                }
            }
            if (PersonnelApprovalStatuses.APPROVED.equals(row.approvalStatus())) {
                approvedCount += row.count();
                if ("优秀".equals(row.result())) {
                    approvedExcellentCount += row.count();
                }
            }
        }
        java.math.BigDecimal excellentRatio = participantCount == 0
                ? java.math.BigDecimal.ZERO
                : java.math.BigDecimal.valueOf(excellentCount)
                        .multiply(java.math.BigDecimal.valueOf(100))
                        .divide(java.math.BigDecimal.valueOf(participantCount), 1, java.math.RoundingMode.HALF_UP);
        String orgCode = SqlText.trim(organizationCode);
        String orgName = "";
        if (orgCode != null && !orgCode.isBlank()) {
            orgName = jdbcTemplate.query("""
                    SELECT dwmc FROM dwbm WHERE dwbm = :code LIMIT 1
                    """,
                    new MapSqlParameterSource("code", orgCode),
                    (rs, rowNum) -> SqlText.trim(rs.getString("dwmc"))).stream().findFirst().orElse("");
        }
        List<AssessmentResultCountItem> counts = resultCounts.entrySet().stream()
                .map(entry -> new AssessmentResultCountItem(entry.getKey(), entry.getValue()))
                .sorted(java.util.Comparator.comparing(AssessmentResultCountItem::result))
                .toList();
        return Optional.of(new AssessmentApprovalStats(
                orgCode == null ? "" : orgCode,
                orgName,
                year,
                participantCount,
                excellentCount,
                excellentRatio,
                pendingCount,
                pendingExcellentCount,
                approvedCount,
                approvedExcellentCount,
                counts));
    }

    private record AssessmentResultAggregateRow(String result, String approvalStatus, int count) {
    }

    private boolean shouldUseAuditDrivenApprovalTracking(
            String approvalStatus,
            boolean submittedByMe,
            Integer approvedWithinDays) {
        if (submittedByMe) {
            return true;
        }
        return PersonnelApprovalStatuses.APPROVED.equals(approvalStatus)
                && approvedWithinDays != null
                && approvedWithinDays > 0;
    }

    private List<PersonnelApprovalTrackingRecord> findApprovalTrackingAuditDriven(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String approvalStatus,
            boolean submittedByMe,
            String submittedByUsername,
            Integer approvedWithinDays,
            PageRequest pageRequest) {
        boolean approvedFields = !submittedByMe;
        MapSqlParameterSource params = approvalTrackingAuditParameters(
                organizationScope,
                organizationCode,
                keyword,
                approvalStatus,
                submittedByMe,
                submittedByUsername,
                approvedWithinDays)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                %s
                SELECT uid, record_type, record_id, dwbm, dwmc, grbm, xm, summary,
                       approval_status, audit_target_type, audit_target_id,
                       audit_actor, audit_moment
                FROM (%s) tracking
                WHERE %s
                ORDER BY audit_moment DESC, dwbm, grbm, record_type, record_id
                LIMIT :limit OFFSET :offset
                """.formatted(
                approvalTrackingAuditLatestCteSql(),
                approvalTrackingAuditDrivenUnionSql(),
                approvalTrackingAuditDrivenFilterSql()),
                params,
                approvalTrackingAuditMapper(approvedFields));
    }

    private long countApprovalTrackingAuditDriven(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String approvalStatus,
            boolean submittedByMe,
            String submittedByUsername,
            Integer approvedWithinDays) {
        MapSqlParameterSource params = approvalTrackingAuditParameters(
                organizationScope,
                organizationCode,
                keyword,
                approvalStatus,
                submittedByMe,
                submittedByUsername,
                approvedWithinDays);
        Long count = jdbcTemplate.queryForObject("""
                %s
                SELECT COUNT(*)
                FROM (%s) tracking
                WHERE %s
                """.formatted(
                approvalTrackingAuditLatestCteSql(),
                approvalTrackingAuditDrivenUnionSql(),
                approvalTrackingAuditDrivenFilterSql()),
                params,
                Long.class);
        return count == null ? 0 : count;
    }

    private String approvalTrackingAuditLatestCteSql() {
        return """
                WITH audit_latest AS (
                    SELECT al.target_type, al.target_id, al.actor_username, al.created_at
                    FROM app_security_audit_log al
                    INNER JOIN (
                        SELECT target_type, target_id, MAX(id) AS max_id
                        FROM app_security_audit_log
                        WHERE action IN (:auditActions)
                          AND (:auditActor IS NULL OR actor_username = :auditActor)
                          AND (:approvedSince IS NULL OR created_at >= :approvedSince)
                        GROUP BY target_type, target_id
                    ) pick ON al.id = pick.max_id
                )
                """;
    }

    private String approvalTrackingAuditDrivenUnionSql() {
        String orgFilter = organizationDescendantFilter("p");
        String statusFilter = "%s.bbz = :approvalStatus";
        return """
                SELECT p.uid AS uid, 'main' AS record_type, p.uid AS record_id,
                       p.dwbm, dw.dwmc, p.grbm, p.xm, '主表基本信息' AS summary,
                       TRIM(COALESCE(p.bbz, '')) AS approval_status,
                       al.target_type AS audit_target_type, al.target_id AS audit_target_id,
                       al.actor_username AS audit_actor, al.created_at AS audit_moment
                FROM audit_latest al
                JOIN dryjbxx p ON al.target_type = 'personnel' AND al.target_id = CAST(p.uid AS CHAR)
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE %s AND (%s)
                UNION ALL
                SELECT p.uid, 'education', e.id, p.dwbm, dw.dwmc, p.grbm, p.xm,
                       TRIM(CONCAT(COALESCE(e.xl, ''), CASE WHEN TRIM(COALESCE(e.byyx, '')) <> ''
                            THEN CONCAT(' / ', e.byyx) ELSE '' END)) AS summary,
                       TRIM(COALESCE(e.bbz, '')) AS approval_status,
                       al.target_type, al.target_id, al.actor_username, al.created_at
                FROM audit_latest al
                JOIN dxl e ON al.target_type = 'dxl' AND al.target_id = CAST(e.id AS CHAR)
                JOIN dryjbxx p ON p.dwbm = e.dwbm AND p.grbm = e.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE %s AND (%s)
                UNION ALL
                SELECT p.uid, 'position', z.id, p.dwbm, dw.dwmc, p.grbm, p.xm,
                       TRIM(COALESCE(NULLIF(TRIM(z.xzzw), ''), z.xrzw, '')) AS summary,
                       TRIM(COALESCE(z.bbz, '')) AS approval_status,
                       al.target_type, al.target_id, al.actor_username, al.created_at
                FROM audit_latest al
                JOIN dryzwbh z ON al.target_type = 'dryzwbh' AND al.target_id = CAST(z.id AS CHAR)
                JOIN dryjbxx p ON p.dwbm = z.dwbm AND p.grbm = z.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE %s AND (%s)
                UNION ALL
                SELECT p.uid, 'assessment', a.id, p.dwbm, dw.dwmc, p.grbm, p.xm,
                       TRIM(CONCAT(COALESCE(a.khnd, ''), CASE WHEN TRIM(COALESCE(a.khjg, '')) <> ''
                            THEN CONCAT(' - ', a.khjg) ELSE '' END)) AS summary,
                       TRIM(COALESCE(a.bbz, a.tjr, a.tjsj, a.shr, a.shsj, '')) AS approval_status,
                       al.target_type, al.target_id, al.actor_username, al.created_at
                FROM audit_latest al
                JOIN dndkh a ON al.target_type = 'dndkh' AND al.target_id = CAST(a.id AS CHAR)
                JOIN dryjbxx p ON p.dwbm = a.dwbm AND p.grbm = a.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE %s AND (%s)
                UNION ALL
                SELECT p.uid, 'award', aw.id, p.dwbm, dw.dwmc, p.grbm, p.xm,
                       TRIM(COALESCE(aw.hjmc, '')) AS summary,
                       TRIM(COALESCE(aw.bbz, '')) AS approval_status,
                       al.target_type, al.target_id, al.actor_username, al.created_at
                FROM audit_latest al
                JOIN hjxx aw ON al.target_type = 'hjxx' AND al.target_id = CAST(aw.id AS CHAR)
                JOIN dryjbxx p ON p.dwbm = aw.dwbm AND p.grbm = aw.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE %s AND (%s)
                UNION ALL
                SELECT p.uid, 'rank', r.id, p.dwbm, dw.dwmc, p.grbm, p.xm,
                       TRIM(COALESCE(r.jx, '')) AS summary,
                       TRIM(COALESCE(r.bbz, '')) AS approval_status,
                       al.target_type, al.target_id, al.actor_username, al.created_at
                FROM audit_latest al
                JOIN jx r ON al.target_type = 'jx' AND al.target_id = CAST(r.id AS CHAR)
                JOIN dryjbxx p ON p.dwbm = r.dwbm AND p.grbm = r.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE %s AND (%s)
                """.formatted(
                statusFilter.formatted("p"), orgFilter,
                statusFilter.formatted("e"), orgFilter,
                statusFilter.formatted("z"), orgFilter,
                statusFilter.formatted("a"), orgFilter,
                statusFilter.formatted("aw"), orgFilter,
                statusFilter.formatted("r"), orgFilter);
    }

    private String approvalTrackingKeywordFilterSql() {
        return """
                (:keyword IS NULL OR grbm LIKE :keywordLike OR xm LIKE :keywordLike
                       OR summary LIKE :keywordLike OR position_name LIKE :keywordLike
                       OR dwmc LIKE :keywordLike OR dwbm LIKE :keywordLike
                       OR CONCAT(dwbm, '-', grbm) LIKE :keywordLike
                       OR CONCAT(dwbm, grbm) LIKE :keywordLike)
                """;
    }

    private String approvalTrackingSubmittedByMeFilterSql() {
        return """
                  AND (:submittedByMe = FALSE OR EXISTS (
                        SELECT 1 FROM app_security_audit_log sub
                        WHERE sub.target_type = tracking.audit_target_type
                          AND sub.target_id = tracking.audit_target_id
                          AND sub.action IN ('PERSONNEL_APPROVAL_SUBMIT', 'PERSONNEL_SUBRECORD_SUBMIT')
                          AND sub.actor_username = :submittedByUsername))
                """;
    }

    private String approvalTrackingAuditDrivenFilterSql() {
        return approvalTrackingKeywordFilterSql() + approvalTrackingSubmittedByMeFilterSql();
    }

    private String approvalTrackingOuterFilterSql() {
        return approvalTrackingKeywordFilterSql() + """
                  AND (:submittedByMe = FALSE OR tjr = :submittedByUsername)
                  AND (:approvedSince IS NULL
                       OR :approvalStatus <> '审批通过'
                       OR shsj >= :approvedSince)
                  AND (:recordType IS NULL OR record_type = :recordType)
                  AND (:assessmentYear IS NULL
                       OR record_type <> 'assessment'
                       OR TRIM(effective_year_month) = :assessmentYear)
                """;
    }

    private String approvalTrackingStatusUnionFilterSql() {
        return approvalTrackingOuterFilterSql();
    }

    private String approvalTrackingStatusUnionOrderBySql(String approvalStatus) {
        if (PersonnelApprovalStatuses.APPROVED.equals(approvalStatus)) {
            return "ORDER BY COALESCE(shsj, '1970-01-01 00:00:00') DESC, dwbm, grbm, record_type, record_id";
        }
        return "ORDER BY COALESCE(tjsj, '1970-01-01 00:00:00') DESC, dwbm, grbm, record_type, record_id";
    }

    private MapSqlParameterSource approvalTrackingAuditParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String approvalStatus,
            boolean submittedByMe,
            String submittedByUsername,
            Integer approvedWithinDays) {
        MapSqlParameterSource base = approvalTrackingParameters(
                organizationScope,
                organizationCode,
                keyword,
                approvalStatus,
                submittedByMe,
                submittedByUsername,
                approvedWithinDays,
                null,
                null);
        List<String> auditActions = submittedByMe ? AUDIT_SUBMIT_ACTIONS : AUDIT_APPROVE_ACTIONS;
        return new MapSqlParameterSource()
                .addValue("allOrganizations", base.getValue("allOrganizations"))
                .addValue("organizationCodes", base.getValue("organizationCodes"))
                .addValue("organizationFilter", base.getValue("organizationFilter"))
                .addValue("organizationCodePrefixLike", base.getValue("organizationCodePrefixLike"))
                .addValue("keyword", base.getValue("keyword"))
                .addValue("keywordLike", base.getValue("keywordLike"))
                .addValue("approvalStatus", base.getValue("approvalStatus"))
                .addValue("submittedByMe", submittedByMe)
                .addValue("submittedByUsername", base.getValue("submittedByUsername"))
                .addValue("auditActions", auditActions)
                .addValue("auditActor", submittedByMe ? base.getValue("submittedByUsername") : null)
                .addValue("approvedSince", submittedByMe ? null : base.getValue("approvedSince"));
    }

    private String approvalTrackingUnionSql() {
        String orgFilter = organizationDescendantFilter("p");
        String statusFilter = "%s.bbz = :approvalStatus";
        String actorColumns = "%s.tjr, %s.tjsj, %s.shr, %s.shsj";
        return """
                SELECT p.uid AS uid, 'main' AS record_type, p.uid AS record_id,
                       p.dwbm, dw.dwmc, p.grbm, p.xm, '主表基本信息' AS summary,
                       '' AS position_name, '' AS effective_year_month, (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'dryjbxx' AND att.record_id = p.uid AND att.record_key = '') AS attachment_count,
                       TRIM(COALESCE(p.bbz, '')) AS approval_status,
                       'personnel' AS audit_target_type, CAST(p.uid AS CHAR) AS audit_target_id,
                       p.tjr, p.tjsj, p.shr, p.shsj
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND %s
                UNION ALL
                SELECT p.uid, 'education', e.id, p.dwbm, dw.dwmc, p.grbm, p.xm,
                       TRIM(CONCAT(COALESCE(e.xl, ''), CASE WHEN TRIM(COALESCE(e.byyx, '')) <> ''
                            THEN CONCAT(' / ', e.byyx) ELSE '' END)) AS summary,
                       '' AS position_name, TRIM(COALESCE(e.bysj, '')) AS effective_year_month, (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'dxl' AND att.record_id = e.id AND att.record_key = '') AS attachment_count,
                       TRIM(COALESCE(e.bbz, '')) AS approval_status,
                       'dxl' AS audit_target_type, CAST(e.id AS CHAR) AS audit_target_id,
                       e.tjr, e.tjsj, e.shr, e.shsj
                FROM dxl e
                JOIN dryjbxx p ON p.dwbm = e.dwbm AND p.grbm = e.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND %s
                UNION ALL
                SELECT p.uid, 'position', z.id, p.dwbm, dw.dwmc, p.grbm, p.xm,
                       TRIM(COALESCE(NULLIF(TRIM(z.xzzw), ''), z.xrzw, '')) AS summary,
                       TRIM(COALESCE(NULLIF(TRIM(z.xrzw), ''), z.xzzw, '')) AS position_name,
                       TRIM(COALESCE(z.srny, '')) AS effective_year_month,
                       (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'dryzwbh' AND att.record_id = z.id AND att.record_key = '') AS attachment_count,
                       TRIM(COALESCE(z.bbz, '')) AS approval_status,
                       'dryzwbh' AS audit_target_type, CAST(z.id AS CHAR) AS audit_target_id,
                       z.tjr, z.tjsj, z.shr, z.shsj
                FROM dryzwbh z
                JOIN dryjbxx p ON p.dwbm = z.dwbm AND p.grbm = z.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND %s
                UNION ALL
                SELECT p.uid, 'assessment', a.id, p.dwbm, dw.dwmc, p.grbm, p.xm,
                       TRIM(CONCAT(COALESCE(a.khnd, ''), CASE WHEN TRIM(COALESCE(a.khjg, '')) <> ''
                            THEN CONCAT(' - ', a.khjg) ELSE '' END)) AS summary,
                       '' AS position_name, TRIM(COALESCE(a.khnd, '')) AS effective_year_month, (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'dndkh' AND att.record_id = a.id AND att.record_key = '') AS attachment_count,
                       TRIM(COALESCE(a.bbz, '')) AS approval_status,
                       'dndkh' AS audit_target_type, CAST(a.id AS CHAR) AS audit_target_id,
                       a.tjr, a.tjsj, a.shr, a.shsj
                FROM dndkh a
                JOIN dryjbxx p ON p.dwbm = a.dwbm AND p.grbm = a.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND %s
                UNION ALL
                SELECT p.uid, 'award', aw.id, p.dwbm, dw.dwmc, p.grbm, p.xm,
                       TRIM(COALESCE(aw.hjmc, '')) AS summary,
                       '' AS position_name, TRIM(COALESCE(aw.hjsj, '')) AS effective_year_month, (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'hjxx' AND att.record_id = aw.id AND att.record_key = '') AS attachment_count,
                       TRIM(COALESCE(aw.bbz, '')) AS approval_status,
                       'hjxx' AS audit_target_type, CAST(aw.id AS CHAR) AS audit_target_id,
                       aw.tjr, aw.tjsj, aw.shr, aw.shsj
                FROM hjxx aw
                JOIN dryjbxx p ON p.dwbm = aw.dwbm AND p.grbm = aw.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND %s
                UNION ALL
                SELECT p.uid, 'rank', r.id, p.dwbm, dw.dwmc, p.grbm, p.xm,
                       TRIM(COALESCE(r.jx, '')) AS summary,
                       '' AS position_name, TRIM(COALESCE(r.sysj, '')) AS effective_year_month, (SELECT COUNT(*) FROM app_subrecord_attachment att WHERE att.table_name = 'jx' AND att.record_id = r.id AND att.record_key = '') AS attachment_count,
                       TRIM(COALESCE(r.bbz, '')) AS approval_status,
                       'jx' AS audit_target_type, CAST(r.id AS CHAR) AS audit_target_id,
                       r.tjr, r.tjsj, r.shr, r.shsj
                FROM jx r
                JOIN dryjbxx p ON p.dwbm = r.dwbm AND p.grbm = r.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND %s
                """.formatted(
                orgFilter, statusFilter.formatted("p"),
                orgFilter, statusFilter.formatted("e"),
                orgFilter, statusFilter.formatted("z"),
                orgFilter, statusFilter.formatted("a"),
                orgFilter, statusFilter.formatted("aw"),
                orgFilter, statusFilter.formatted("r"));
    }

    private MapSqlParameterSource approvalTrackingParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String approvalStatus,
            boolean submittedByMe,
            String submittedByUsername,
            Integer approvedWithinDays,
            String recordType,
            String assessmentYear) {
        MapSqlParameterSource base = parameters(organizationScope, organizationCode, keyword);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", base.getValue("allOrganizations"))
                .addValue("organizationCodes", base.getValue("organizationCodes"))
                .addValue("organizationFilter", base.getValue("organizationFilter"))
                .addValue("organizationCodePrefixLike", base.getValue("organizationCodePrefixLike"))
                .addValue("keyword", base.getValue("keyword"))
                .addValue("keywordLike", base.getValue("keywordLike"))
                .addValue("approvalStatus", approvalStatus)
                .addValue("submittedByMe", submittedByMe)
                .addValue("submittedByUsername", submittedByUsername == null ? "" : submittedByUsername.trim())
                .addValue("approvedSince", resolveApprovedSince(approvedWithinDays))
                .addValue("recordType", emptyToNull(recordType))
                .addValue("assessmentYear", emptyToNull(assessmentYear));
    }

    private static Timestamp resolveApprovedSince(Integer approvedWithinDays) {
        if (approvedWithinDays == null || approvedWithinDays <= 0) {
            return null;
        }
        return Timestamp.valueOf(LocalDateTime.now().minusDays(approvedWithinDays));
    }
}

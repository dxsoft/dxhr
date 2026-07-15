package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.SensitiveData;
import com.dxsoft.rsgzgl.common.SqlText;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class PersonnelRepository {

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
            SqlText.trim(rs.getString("zjbm"))
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

    private static final RowMapper<PersonnelMaintenanceRecord> MAINTENANCE_MAPPER = (rs, rowNum) -> new PersonnelMaintenanceRecord(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("dwbz")),
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
            SqlText.trim(rs.getString("dah")));

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
            rs.getBoolean("app_created")
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
            rs.getBoolean("app_created")
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
            rs.getBoolean("app_created")
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
            null
    );

    private static final RowMapper<ChangedPersonnelRecord> CHANGED_PERSONNEL_MAPPER = (rs, rowNum) -> new ChangedPersonnelRecord(
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
            SqlText.trim(rs.getString("jsnf")),
            SqlText.trim(rs.getString("jsyf")),
            SqlText.trim(rs.getString("jslb")),
            null,
            null,
            null,
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            rs.getBigDecimal("hj2"),
            SqlText.trim(rs.getString("tbnd")),
            SqlText.trim(rs.getString("jbtbz")),
            SqlText.trim(rs.getString("bz"))
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    PersonnelRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    List<PersonnelSummary> findAll(OrganizationScope organizationScope, String organizationFilter, String keyword, PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = parameters(organizationScope, organizationFilter, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.sfzh, p.xb, p.csny,
                       p.ryfl, p.dwsx, p.gwfl, p.xrzw, p.zjbm
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR p.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:keyword IS NULL OR p.xm LIKE :keywordLike OR p.grbm LIKE :keywordLike OR p.sfzh LIKE :keywordLike)
                ORDER BY p.dwbm, p.grbm
                LIMIT :limit OFFSET :offset
                """, params, SUMMARY_MAPPER);
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
                  AND (:organizationFilter IS NULL OR p.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:keyword IS NULL OR p.xm LIKE :keywordLike OR p.grbm LIKE :keywordLike OR p.sfzh LIKE :keywordLike)
                """, parameters(organizationScope, organizationFilter, keyword), Long.class);
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
        return jdbcTemplate.query(comprehensiveQuerySql() + """
                ORDER BY p.dwbm, p.grbm
                LIMIT :limit OFFSET :offset
                """, params, COMPREHENSIVE_QUERY_MAPPER);
    }

    long countComprehensiveQueries(OrganizationScope organizationScope, PersonnelComprehensiveQueryCriteria criteria) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                """ + comprehensiveQueryFromSql() + comprehensiveQueryWhereSql(),
                comprehensiveQueryParameters(organizationScope, criteria),
                Long.class);
        return count == null ? 0 : count;
    }

    private String comprehensiveQuerySql() {
        return """
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.sfzh, p.xb, p.csny,
                       p.ryfl, p.dwsx, p.gwfl, p.cjgzny, p.zzny, p.xlbm, p.zgxl, p.gznx,
                       z.zwbm AS appointment_position_code, z.xzzw AS appointment_position_name, z.srny AS appointment_start,
                       h.jsnf, h.jsyf, h.zwbm2 AS payroll_position_code, h.zwgw2 AS payroll_position_name,
                       h.jbgzjb2 AS grade_level, h.zwgzdc2 AS grade_step, h.hj2 AS total_salary
                """ + comprehensiveQueryFromSql() + comprehensiveQueryWhereSql();
    }

    private String comprehensiveQueryFromSql() {
        return """
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                LEFT JOIN dryzwbh z ON z.dwbm = p.dwbm AND z.grbm = p.grbm AND z.xrzwbz = '1'
                LEFT JOIN hisbase h ON h.dwbm = p.dwbm AND h.grbm = p.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
                """;
    }

    private String comprehensiveQueryWhereSql() {
        return """
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR p.sfzh LIKE :keywordLike
                       OR p.xrzw LIKE :keywordLike
                       OR z.xzzw LIKE :keywordLike
                       OR z.zwbm LIKE :keywordLike
                       OR h.zwgw2 LIKE :keywordLike)
                  AND (:gender IS NULL OR TRIM(p.xb) = :gender)
                  AND (:personnelCategory IS NULL OR p.ryfl LIKE :personnelCategoryLike)
                  AND (:organizationType IS NULL OR TRIM(p.dwsx) = :organizationType)
                  AND (:postCategory IS NULL OR p.gwfl LIKE :postCategoryLike)
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
                  AND (:positionCode IS NULL OR TRIM(z.zwbm) = :positionCode)
                  AND (:positionCodePrefix IS NULL OR LEFT(TRIM(z.zwbm), 2) = :positionCodePrefix)
                  AND (:gradeLevelFrom IS NULL OR CAST(NULLIF(TRIM(h.jbgzjb2), '') AS UNSIGNED) >= CAST(:gradeLevelFrom AS UNSIGNED))
                  AND (:gradeLevelTo IS NULL OR CAST(NULLIF(TRIM(h.jbgzjb2), '') AS UNSIGNED) <= CAST(:gradeLevelTo AS UNSIGNED))
                """;
    }

    private MapSqlParameterSource comprehensiveQueryParameters(
            OrganizationScope organizationScope,
            PersonnelComprehensiveQueryCriteria criteria) {
        String keyword = SqlText.trim(criteria.keyword());
        String personnelCategory = SqlText.trim(criteria.personnelCategory());
        String postCategory = SqlText.trim(criteria.postCategory());
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationCode", emptyToNull(criteria.organizationCode()))
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

    Optional<PersonnelMaintenanceRecord> findMaintenanceByUid(int uid) {
        return jdbcTemplate.query("""
                SELECT p.*, dw.dwmc, dw.dwbz
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
                    :workStartYearMonth, :regularizationYearMonth, '', '', 0, :salaryYears, '', 0, :educationCode, :highestEducation, 0, '', '',
                    '', :currentPositionLevel, :currentRankCode, :currentPosition, :currentPositionStartYearMonth, 0, '', '', '', '', '', '', '',
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

    void deletePersonnel(int uid) {
        jdbcTemplate.update("DELETE FROM dryjbxx WHERE uid = :uid", new MapSqlParameterSource("uid", uid));
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

    Optional<PersonKey> findEducationKeyById(int id) {
        return findSubrecordKeyById("dxl", id);
    }

    Optional<PersonKey> findPositionKeyById(int id) {
        return findSubrecordKeyById("dryzwbh", id);
    }

    Optional<PersonKey> findAssessmentKeyById(int id) {
        return findSubrecordKeyById("dndkh", id);
    }

    List<PositionRecord> findPositions(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT z.id, z.dwbm, z.grbm, z.xrzwbm, z.xrzw, z.zwjb, z.zjbm, z.zwbm, z.xzzw,
                       z.srny, z.kjnx, z.xrzwbz, z.jsbz, marker.record_id IS NOT NULL AS app_created
                FROM dryzwbh z
                LEFT JOIN app_record_marker marker ON marker.table_name = 'dryzwbh' AND marker.record_id = CAST(z.id AS CHAR) AND marker.marker = 'APP_CREATED'
                WHERE z.dwbm = :dwbm AND z.grbm = :grbm
                ORDER BY CASE WHEN z.xrzwbz = '1' THEN 0 ELSE 1 END, z.srny DESC, z.id DESC
                """, keyParameters(key), POSITION_MAPPER);
    }

    List<PersonnelPositionHistoryRecord> findPositionHistories(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = personnelHistoryParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT z.id, z.dwbm, dw.dwmc, z.grbm, p.xm, z.xrzwbm, z.xrzw, z.zwjb, z.zjbm,
                       z.zwbm, z.xzzw, z.srny, z.kjnx, z.xrzwbz, z.jsbz
                FROM dryzwbh z
                LEFT JOIN dryjbxx p ON p.dwbm = z.dwbm AND p.grbm = z.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = z.dwbm
                WHERE (:allOrganizations = TRUE OR z.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR z.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:keyword IS NULL OR z.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike
                       OR z.xrzw LIKE :keywordLike OR z.xzzw LIKE :keywordLike OR z.zwbm = :keyword)
                ORDER BY z.dwbm, z.grbm, z.srny DESC, z.id DESC
                LIMIT :limit OFFSET :offset
                """, params, POSITION_HISTORY_MAPPER);
    }

    long countPositionHistories(OrganizationScope organizationScope, String organizationCode, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryzwbh z
                LEFT JOIN dryjbxx p ON p.dwbm = z.dwbm AND p.grbm = z.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = z.dwbm
                WHERE (:allOrganizations = TRUE OR z.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR z.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:keyword IS NULL OR z.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike
                       OR z.xrzw LIKE :keywordLike OR z.xzzw LIKE :keywordLike OR z.zwbm = :keyword)
                """, personnelHistoryParameters(organizationScope, organizationCode, keyword), Long.class);
        return count == null ? 0 : count;
    }

    List<EducationRecord> findEducation(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT e.id, e.dwbm, e.grbm, e.xlbm, e.xl, e.byyx, e.rxsj, e.bysj, e.xz, e.xllb, e.bz,
                       marker.record_id IS NOT NULL AS app_created
                FROM dxl e
                LEFT JOIN app_record_marker marker ON marker.table_name = 'dxl' AND marker.record_id = CAST(e.id AS CHAR) AND marker.marker = 'APP_CREATED'
                WHERE e.dwbm = :dwbm AND e.grbm = :grbm
                ORDER BY bysj DESC, xlbm
                """, keyParameters(key), EDUCATION_MAPPER);
    }

    List<PersonnelEducationHistoryRecord> findEducationHistories(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = personnelHistoryParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT e.id, e.dwbm, dw.dwmc, e.grbm, p.xm, e.xlbm, e.xl, e.byyx,
                       e.rxsj, e.bysj, e.xz, e.xllb, e.bz
                FROM dxl e
                LEFT JOIN dryjbxx p ON p.dwbm = e.dwbm AND p.grbm = e.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = e.dwbm
                WHERE (:allOrganizations = TRUE OR e.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR e.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:keyword IS NULL OR e.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike
                       OR e.xlbm = :keyword OR e.xl LIKE :keywordLike OR e.byyx LIKE :keywordLike OR e.xllb LIKE :keywordLike)
                ORDER BY e.dwbm, e.grbm, e.bysj DESC, e.xlbm, e.id DESC
                LIMIT :limit OFFSET :offset
                """, params, EDUCATION_HISTORY_MAPPER);
    }

    long countEducationHistories(OrganizationScope organizationScope, String organizationCode, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dxl e
                LEFT JOIN dryjbxx p ON p.dwbm = e.dwbm AND p.grbm = e.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = e.dwbm
                WHERE (:allOrganizations = TRUE OR e.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR e.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:keyword IS NULL OR e.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike
                       OR e.xlbm = :keyword OR e.xl LIKE :keywordLike OR e.byyx LIKE :keywordLike OR e.xllb LIKE :keywordLike)
                """, personnelHistoryParameters(organizationScope, organizationCode, keyword), Long.class);
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
                SELECT b.dwbm, dw.dwmc, b.grbm, b.xm, b.sfzh, b.xb, b.csny, b.ryfl, b.dwsx, b.gwfl,
                       h.jsnf, h.jsyf, h.jslb, h.zwbm2, h.zwgw2, h.hj2, h.tbnd, h.jbtbz, b.bz
                FROM dryjbxxb b
                LEFT JOIN dwbm dw ON dw.dwbm = b.dwbm
                LEFT JOIN hisbaseb h ON h.dwbm = b.dwbm AND h.grbm = b.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
                WHERE (:allOrganizations = TRUE OR b.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR b.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:period IS NULL OR CONCAT(h.jsnf, h.jsyf) = :period)
                  AND (:keyword IS NULL OR b.grbm LIKE :keywordLike OR b.xm LIKE :keywordLike
                       OR b.sfzh LIKE :keywordLike OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY b.dwbm, b.grbm, h.jsnf DESC, h.jsyf DESC, h.jslb
                LIMIT :limit OFFSET :offset
                """, params, CHANGED_PERSONNEL_MAPPER);
    }

    long countChangedPersonnel(OrganizationScope organizationScope, String organizationCode, String period, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryjbxxb b
                LEFT JOIN dwbm dw ON dw.dwbm = b.dwbm
                LEFT JOIN hisbaseb h ON h.dwbm = b.dwbm AND h.grbm = b.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
                WHERE (:allOrganizations = TRUE OR b.dwbm IN (:organizationCodes))
                  AND (:organizationFilter IS NULL OR b.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:period IS NULL OR CONCAT(h.jsnf, h.jsyf) = :period)
                  AND (:keyword IS NULL OR b.grbm LIKE :keywordLike OR b.xm LIKE :keywordLike
                       OR b.sfzh LIKE :keywordLike OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                """, changedPersonnelParameters(organizationScope, organizationCode, period, keyword), Long.class);
        return count == null ? 0 : count;
    }

    List<AssessmentRecord> findAssessments(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT a.id, a.dwbm, a.grbm, a.khnd, a.khjg, marker.record_id IS NOT NULL AS app_created
                FROM dndkh a
                LEFT JOIN app_record_marker marker ON marker.table_name = 'dndkh' AND marker.record_id = CAST(a.id AS CHAR) AND marker.marker = 'APP_CREATED'
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
        result.put("awards", tableRows("hjxx", key, "hjsj DESC, id DESC"));
        result.put("rankRecords", tableRows("jx", key, "sysj DESC, id DESC"));
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
                INSERT INTO dxl (dwbm, grbm, xlbm, xl, byyx, rxsj, bysj, xz, xllb, bz)
                VALUES (:dwbm, :grbm, :educationCode, :educationName, :school, :enrollmentDate, :graduationDate, :studyYears, :educationType, :remark)
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
                INSERT INTO dryzwbh (dwbm, grbm, xrzwbm, xrzw, zwjb, zjbm, zwbm, xzzw, zwlb, srny, kjnx, xrzwbz, jsbz)
                VALUES (:dwbm, :grbm, :currentPositionCode, :currentPosition, :positionLevel, :rankCode, :positionCode, :positionName, '', :startYearMonth, :intervalYears, :activeFlag, :promotionFlag)
                """, positionParameters(key, request));
        int id = lastInsertId();
        markAppCreated("dryzwbh", id);
        return id;
    }

    void updatePosition(int id, PositionMaintenanceRequest request) {
        jdbcTemplate.update("""
                UPDATE dryzwbh
                SET xrzwbm = :currentPositionCode, xrzw = :currentPosition, zwjb = :positionLevel, zjbm = :rankCode,
                    zwbm = :positionCode, xzzw = :positionName, srny = :startYearMonth,
                    kjnx = :intervalYears, xrzwbz = :activeFlag, jsbz = :promotionFlag
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
                INSERT INTO dndkh (dwbm, grbm, khnd, khjg)
                VALUES (:dwbm, :grbm, :year, :result)
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

    List<BatchAssessmentEntryRow> findBatchAssessmentEntries(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String keyword,
            boolean includeDescendants) {
        if (organizationScope.noneScope() || emptyToNull(organizationCode) == null || emptyToNull(year) == null) {
            return List.of();
        }
        MapSqlParameterSource params = batchAssessmentParameters(organizationScope, organizationCode, year, keyword, includeDescendants);
        List<BatchAssessmentEntryRow> rows = jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.ryfl, p.dwsx, p.xrzw,
                       :year AS khnd, a.id AS assessment_id, a.khjg AS assessment_result
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                LEFT JOIN dndkh a ON a.dwbm = p.dwbm AND a.grbm = p.grbm AND a.khnd = :year
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (%s)
                  AND (:keyword IS NULL OR p.xm LIKE :keywordLike OR p.grbm LIKE :keywordLike OR p.sfzh LIKE :keywordLike)
                ORDER BY p.dwbm, p.grbm
                """.formatted(batchOrganizationFilterSql(includeDescendants)), params, BATCH_ASSESSMENT_ENTRY_MAPPER);
        return rows.stream()
                .map(row -> new BatchAssessmentEntryRow(
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
                        defaultAssessmentResultText(row.personnelCategory(), row.organizationType())))
                .toList();
    }

    Optional<PersonnelSummary> findPersonnelSummary(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.sfzh, p.xb, p.csny,
                       p.ryfl, p.dwsx, p.gwfl, p.xrzw, p.zjbm
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
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
                  AND (:organizationFilter IS NULL OR a.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:year IS NULL OR a.khnd = :year)
                  AND (:keyword IS NULL OR a.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike OR a.khjg LIKE :keywordLike)
                ORDER BY a.khnd DESC, a.dwbm, a.grbm
                LIMIT :limit OFFSET :offset
                """, params, ANNUAL_ASSESSMENT_MAPPER);
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
                  AND (:organizationFilter IS NULL OR a.dwbm LIKE :organizationFilterLike OR dw.dwmc LIKE :organizationFilterLike)
                  AND (:year IS NULL OR a.khnd = :year)
                  AND (:keyword IS NULL OR a.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike OR a.khjg LIKE :keywordLike)
                """, assessmentParameters(organizationScope, organizationCode, year, keyword), Long.class);
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
                .addValue("organizationFilterLike", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : "%" + trimmedOrganizationFilter + "%")
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
                .addValue("archiveNumber", valueOrBlank(request.archiveNumber()));
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
                .addValue("remark", valueOrBlank(request.remark()));
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
                .addValue("promotionFlag", valueOrBlank(request.promotionFlag()));
    }

    private MapSqlParameterSource assessmentParameters(PersonKey key, AssessmentMaintenanceRequest request) {
        return new MapSqlParameterSource()
                .addValue("dwbm", key.organizationCode())
                .addValue("grbm", key.personCode())
                .addValue("year", valueOrBlank(request.year()))
                .addValue("result", valueOrBlank(request.result()));
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
                .addValue("organizationFilterLike", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : "%" + trimmedOrganizationFilter + "%")
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
                .addValue("organizationCodeLike", trimmedOrganizationCode + "%")
                .addValue("year", trimmedOrganizationCode == null || trimmedOrganizationCode.isEmpty() ? null : SqlText.trim(year))
                .addValue("keyword", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : "%" + trimmedKeyword + "%")
                .addValue("includeDescendants", includeDescendants);
    }

    private String batchOrganizationFilterSql(boolean includeDescendants) {
        return includeDescendants
                ? "p.dwbm LIKE :organizationCodeLike"
                : "p.dwbm = :organizationCode";
    }

    private String defaultAssessmentResultText(String personnelCategory, String organizationType) {
        String text = (personnelCategory == null ? "" : personnelCategory)
                + " " + (organizationType == null ? "" : organizationType);
        return text.contains("事业") ? "合格" : "称职";
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
        String trimmedKeyword = SqlText.trim(keyword);
        String trimmedOrganizationFilter = SqlText.trim(organizationCode);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationFilter", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : trimmedOrganizationFilter)
                .addValue("organizationFilterLike", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : "%" + trimmedOrganizationFilter + "%")
                .addValue("keyword", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : "%" + trimmedKeyword + "%");
    }

    private MapSqlParameterSource changedPersonnelParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String period,
            String keyword) {
        String trimmedKeyword = SqlText.trim(keyword);
        String trimmedOrganizationFilter = SqlText.trim(organizationCode);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationFilter", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : trimmedOrganizationFilter)
                .addValue("organizationFilterLike", trimmedOrganizationFilter == null || trimmedOrganizationFilter.isEmpty() ? null : "%" + trimmedOrganizationFilter + "%")
                .addValue("period", emptyToNull(period))
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
                .collect(java.util.stream.Collectors.toMap(TableColumn::name, column -> column));
        List<TableColumn> commonColumns = new ArrayList<>();
        for (TableColumn targetColumn : targetColumns) {
            if (excludedColumn != null && targetColumn.name().equalsIgnoreCase(excludedColumn)) {
                continue;
            }
            if (sourceByName.containsKey(targetColumn.name())) {
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
                .map(column -> sourceExpression(sourceAlias, column))
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
                WHERE table_schema = DATABASE() AND table_name = :tableName
                """, new MapSqlParameterSource("tableName", tableName), Integer.class);
        return count != null && count > 0;
    }

    private List<TableColumn> tableColumns(String tableName) {
        return jdbcTemplate.query("""
                SELECT column_name, data_type
                FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = :tableName
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
        return "COALESCE(" + source + ", '')";
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
}

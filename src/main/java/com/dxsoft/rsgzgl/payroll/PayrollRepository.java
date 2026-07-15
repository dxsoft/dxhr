package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.SqlText;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class PayrollRepository {

    private static final RowMapper<PayrollFieldMetadata> FIELD_MAPPER = (rs, rowNum) -> new PayrollFieldMetadata(
            rs.getInt("id"),
            rs.getInt("sequence"),
            rs.getInt("field_cate"),
            SqlText.trim(rs.getString("tblname")),
            SqlText.trim(rs.getString("field_name")),
            SqlText.trim(rs.getString("field_type")),
            rs.getInt("field_len"),
            rs.getInt("field_dec"),
            SqlText.trim(rs.getString("field_cap")),
            SqlText.trim(rs.getString("field_caps")),
            SqlText.trim(rs.getString("field_capj")),
            SqlText.trim(rs.getString("sfsy06")),
            SqlText.trim(rs.getString("sfsy")),
            SqlText.trim(rs.getString("lrfs")),
            SqlText.trim(rs.getString("category")),
            rs.getBoolean("jbt"),
            SqlText.trim(rs.getString("gld")),
            rs.getInt("jxryff"),
            rs.getBoolean("jbtbz"),
            SqlText.trim(rs.getString("qsff")),
            rs.getBigDecimal("gdz"),
            rs.getBoolean("readonly"),
            rs.getBoolean("isgroup"),
            rs.getBoolean("iscount"));

    private static final RowMapper<PositionSalaryStandard> POSITION_STANDARD_MAPPER = (rs, rowNum) -> new PositionSalaryStandard(
            SqlText.trim(rs.getString("tbnd")),
            SqlText.trim(rs.getString("zwbm")),
            rs.getInt("bz"));

    private static final RowMapper<AllowanceStandard> ALLOWANCE_STANDARD_MAPPER = (rs, rowNum) -> new AllowanceStandard(
            rs.getInt("id"),
            SqlText.trim(rs.getString("tbnd")),
            SqlText.trim(rs.getString("item")),
            SqlText.trim(rs.getString("zwbm")),
            SqlText.trim(rs.getString("mc")),
            rs.getInt("worklower"),
            rs.getInt("workupper"),
            rs.getInt("bz"),
            rs.getInt("jxlb"));

    private static final RowMapper<RankAllowanceStandard> RANK_ALLOWANCE_STANDARD_MAPPER = (rs, rowNum) -> new RankAllowanceStandard(
            rs.getInt("id"),
            SqlText.trim(rs.getString("tbnd")),
            SqlText.trim(rs.getString("jxbm")),
            SqlText.trim(rs.getString("jx")),
            rs.getInt("jtbz"),
            SqlText.trim(rs.getString("lb")));

    private static final RowMapper<RetainedAllowanceStandard> RETAINED_ALLOWANCE_STANDARD_MAPPER = (rs, rowNum) -> new RetainedAllowanceStandard(
            SqlText.trim(rs.getString("zwbm")),
            SqlText.trim(rs.getString("mc")),
            rs.getInt("bz"));

    private static final RowMapper<YearAllowanceStandard> YEAR_ALLOWANCE_STANDARD_MAPPER = (rs, rowNum) -> new YearAllowanceStandard(
            SqlText.trim(rs.getString("tbnd")),
            rs.getBigDecimal("a1"),
            rs.getBigDecimal("a2"),
            rs.getBigDecimal("a3"),
            rs.getBigDecimal("a4"));

    private static final RowMapper<InternSalaryStandard> INTERN_SALARY_STANDARD_MAPPER = (rs, rowNum) -> new InternSalaryStandard(
            SqlText.trim(rs.getString("tbnd")),
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("xlmc")),
            SqlText.trim(rs.getString("zzzwbm")),
            SqlText.trim(rs.getString("zzzwmc")),
            SqlText.trim(rs.getString("zzdc")),
            SqlText.trim(rs.getString("zzjb")),
            rs.getInt("gz1"),
            rs.getInt("gz2"));

    private static final RowMapper<WageReformStandard> WAGE_REFORM_STANDARD_MAPPER = (rs, rowNum) -> new WageReformStandard(
            SqlText.trim(rs.getString("zwbm")),
            rs.getInt("rzns"),
            rs.getInt("rznz"),
            rs.getInt("tgns"),
            rs.getInt("tgnz"),
            SqlText.trim(rs.getString("jb")),
            SqlText.trim(rs.getString("dc")));

    private static final RowMapper<WageReformPosition> WAGE_REFORM_POSITION_MAPPER = (rs, rowNum) -> new WageReformPosition(
            normalizeAppointmentPositionCode(SqlText.trim(rs.getString("zwbm"))),
            SqlText.trim(rs.getString("xzzw")),
            SqlText.trim(rs.getString("srny")),
            rs.getInt("kjnx"));

    private static final RowMapper<RankAllowanceChange> RANK_ALLOWANCE_CHANGE_MAPPER = (rs, rowNum) -> new RankAllowanceChange(
            SqlText.trim(rs.getString("jx")),
            SqlText.trim(rs.getString("sysj")),
            SqlText.trim(rs.getString("lb")));

    private static final RowMapper<OtherAllowanceStandard> OTHER_ALLOWANCE_STANDARD_MAPPER = (rs, rowNum) -> new OtherAllowanceStandard(
            SqlText.trim(rs.getString("standard_type")),
            SqlText.trim(rs.getString("tbnd")),
            SqlText.trim(rs.getString("code")),
            SqlText.trim(rs.getString("name")),
            rs.getBigDecimal("amount"),
            rs.getBigDecimal("average_amount"),
            rs.getBigDecimal("multiplier"));

    private static final RowMapper<PayrollHistorySnapshot> HISTORY_MAPPER = (rs, rowNum) -> new PayrollHistorySnapshot(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("jsnf")),
            SqlText.trim(rs.getString("jsyf")),
            SqlText.trim(rs.getString("jslb")),
            SqlText.trim(rs.getString("dwsx")),
            rs.getInt("dfbt"),
            SqlText.trim(rs.getString("jzgb")),
            SqlText.trim(rs.getString("spdw")),
            SqlText.trim(rs.getString("cjgzny")),
            SqlText.trim(rs.getString("srny")),
            rs.getInt("gznx"),
            rs.getInt("zdgznx"),
            SqlText.trim(rs.getString("xckhndjb")),
            SqlText.trim(rs.getString("xckhndzw")),
            SqlText.trim(rs.getString("jhlqsny")),
            rs.getInt("zdjhlnx"),
            rs.getInt("tgbl"),
            SqlText.trim(rs.getString("jxjtbz")),
            SqlText.trim(rs.getString("jx")),
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            SqlText.trim(rs.getString("zwgzdc2")),
            SqlText.trim(rs.getString("fddc")),
            SqlText.trim(rs.getString("jbgzjb2")),
            SqlText.trim(rs.getString("djc2")),
            SqlText.trim(rs.getString("tbnd")),
            SqlText.trim(rs.getString("jbtbz")),
            SqlText.trim(rs.getString("gwjtbz")),
            SqlText.trim(rs.getString("gwjtlb")),
            rs.getInt("zwgzse2"),
            rs.getInt("jbgzse2"),
            rs.getInt("jsdjgz2"),
            rs.getInt("dfbt2"),
            rs.getInt("sdbt"),
            rs.getInt("blfb2"),
            rs.getInt("jhljt"),
            rs.getInt("jsfszwtg2"),
            rs.getInt("jxjt"),
            rs.getInt("fdgz2"),
            rs.getInt("jjjy2"),
            rs.getInt("gwjt2"),
            rs.getInt("tgblbf"),
            rs.getInt("pgbc"),
            rs.getBigDecimal("njbt"),
            rs.getInt("hj2"));

    private static final RowMapper<PayrollHistoryRecord> PAYROLL_HISTORY_MAPPER = (rs, rowNum) -> new PayrollHistoryRecord(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("sid")),
            isCurrentPayroll(rs.getString("sid")),
            rs.getBoolean("app_created"),
            rs.getObject("personnel_uid") == null ? null : rs.getInt("personnel_uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("jsnf")),
            SqlText.trim(rs.getString("jsyf")),
            SqlText.trim(rs.getString("jslb")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("dwsx")),
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            SqlText.trim(rs.getString("zwgzdc2")),
            SqlText.trim(rs.getString("jbgzjb2")),
            SqlText.trim(rs.getString("xckhndjb")),
            SqlText.trim(rs.getString("xckhndzw")),
            SqlText.trim(rs.getString("tbnd")),
            SqlText.trim(rs.getString("jbtbz")),
            rs.getInt("zwgzse2"),
            rs.getInt("jbgzse2"),
            rs.getInt("jsdjgz2"),
            rs.getInt("dfbt2"),
            rs.getInt("blfb2"),
            rs.getInt("jxjt"),
            rs.getInt("fdgz2"),
            rs.getInt("jjjy2"),
            rs.getInt("jhljt"),
            rs.getInt("jsfszwtg2"),
            rs.getBigDecimal("njbt"),
            rs.getInt("pgbc"),
            rs.getInt("hj2"));

    private static final RowMapper<TeachingAllowanceAdjustment> TEACHING_ALLOWANCE_ADJUSTMENT_MAPPER = (rs, rowNum) -> new TeachingAllowanceAdjustment(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("calculation_period")),
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            SqlText.trim(rs.getString("jhlqsny")),
            rs.getInt("zdjhlnx"),
            rs.getInt("teaching_years"),
            rs.getInt("jhljt"),
            rs.getInt("calculated_amount"),
            rs.getInt("difference_amount"),
            rs.getBoolean("eligible"),
            null,
            null);

    private static final RowMapper<RankAllowanceStandardContext> RANK_ALLOWANCE_STANDARD_CONTEXT_MAPPER = (rs, rowNum) -> new RankAllowanceStandardContext(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("calculation_period")),
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            SqlText.trim(rs.getString("jx")),
            SqlText.trim(rs.getString("jxjtbz")),
            SqlText.trim(rs.getString("jcjtbz")),
            SqlText.trim(rs.getString("spjtbz")),
            rs.getInt("jxjt"),
            rs.getInt("hj2"));

    private static final RowMapper<InternSalaryChangePreview> INTERN_SALARY_CHANGE_MAPPER = (rs, rowNum) -> new InternSalaryChangePreview(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("calculation_period")),
            SqlText.trim(rs.getString("cjgzny")),
            SqlText.trim(rs.getString("zzny")),
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("zgxl")),
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            SqlText.trim(rs.getString("tbnd")),
            rs.getInt("stored_intern_salary"),
            null,
            null,
            rs.getInt("hj2"),
            null,
            null,
            rs.getBoolean("eligible"),
            null,
            null);

    private static final RowMapper<WageReform2006Candidate> WAGE_REFORM_2006_MAPPER = (rs, rowNum) -> new WageReform2006Candidate(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("cjgzny")),
            SqlText.trim(rs.getString("zzny")),
            SqlText.trim(rs.getString("position_code")),
            SqlText.trim(rs.getString("position_name")),
            SqlText.trim(rs.getString("position_start")),
            rs.getInt("gznx"),
            SqlText.trim(rs.getString("payroll_history_id")),
            SqlText.trim(rs.getString("current_change_type")),
            SqlText.trim(rs.getString("jsnf")),
            SqlText.trim(rs.getString("jsyf")));

    private static final RowMapper<NewPersonnelSalaryCandidate> NEW_PERSONNEL_SALARY_MAPPER = (rs, rowNum) -> new NewPersonnelSalaryCandidate(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("jrny")),
            SqlText.trim(rs.getString("jrfs")),
            SqlText.trim(rs.getString("position_code")),
            SqlText.trim(rs.getString("position_name")),
            SqlText.trim(rs.getString("position_start")),
            SqlText.trim(rs.getString("cjgzny")),
            SqlText.trim(rs.getString("zzny")),
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("zgxl")),
            rs.getInt("gznx"),
            SqlText.trim(rs.getString("payroll_history_id")),
            SqlText.trim(rs.getString("current_change_type")));

    private static final RowMapper<OtherPayrollChangePreview> OTHER_PAYROLL_CHANGE_MAPPER = (rs, rowNum) -> new OtherPayrollChangePreview(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("calculation_period")),
            SqlText.trim(rs.getString("jslb")),
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            rs.getInt("zwgzse2"),
            rs.getInt("jbgzse2"),
            rs.getInt("jsdjgz2"),
            rs.getInt("dfbt2"),
            rs.getInt("blfb2"),
            rs.getInt("hj2"),
            null,
            null);

    private static final RowMapper<FloatingToFixedPreview> FLOATING_TO_FIXED_MAPPER = (rs, rowNum) -> new FloatingToFixedPreview(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("calculation_period")),
            null,
            SqlText.trim(rs.getString("fdsj")),
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            SqlText.trim(rs.getString("zwgzdc2")),
            null,
            SqlText.trim(rs.getString("fddc")),
            rs.getInt("fdgz2"),
            rs.getInt("jbgzse2"),
            null,
            null,
            rs.getInt("hj2"),
            null,
            null,
            rs.getBoolean("eligible"),
            null,
            null);

    private static final RowMapper<SalaryStandardAdjustmentPreview> SALARY_STANDARD_ADJUSTMENT_MAPPER = (rs, rowNum) -> new SalaryStandardAdjustmentPreview(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("calculation_period")),
            SqlText.trim(rs.getString("jslb")),
            null,
            SqlText.trim(rs.getString("tbnd")),
            SqlText.trim(rs.getString("jbtbz")),
            null,
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            rs.getInt("zwgzse2"),
            rs.getInt("jbgzse2"),
            rs.getInt("hj2"),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    private static final RowMapper<AllowanceRecalculationPreview> ALLOWANCE_RECALCULATION_MAPPER = (rs, rowNum) -> new AllowanceRecalculationPreview(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("calculation_period")),
            SqlText.trim(rs.getString("jslb")),
            SqlText.trim(rs.getString("jbtbz")),
            null,
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            rs.getInt("dfbt2"),
            rs.getInt("sdbt"),
            rs.getInt("blfb2"),
            rs.getInt("jsfszwtg2"),
            null,
            null,
            null,
            null,
            rs.getInt("hj2"),
            null,
            null,
            null,
            null,
            null);

    private static final RowMapper<PositionChangeCandidate> POSITION_CHANGE_CANDIDATE_MAPPER = (rs, rowNum) -> new PositionChangeCandidate(
            normalizeAppointmentPositionCode(SqlText.trim(rs.getString("zwbm"))),
            SqlText.trim(rs.getString("xzzw")),
            SqlText.trim(rs.getString("srny")));

    private static final RowMapper<PositionChangeDisplayPair> POSITION_CHANGE_DISPLAY_PAIR_MAPPER = (rs, rowNum) -> new PositionChangeDisplayPair(
            SqlText.trim(rs.getString("before_position_code")),
            SqlText.trim(rs.getString("before_position_name")),
            SqlText.trim(rs.getString("after_position_code")),
            SqlText.trim(rs.getString("after_position_name")));

    private static final RowMapper<PositionLevelRange> POSITION_LEVEL_RANGE_MAPPER = (rs, rowNum) -> new PositionLevelRange(
            SqlText.trim(rs.getString("zwbm")),
            rs.getInt("min"),
            rs.getInt("max"));

    private static final RowMapper<EducationPromotionSource> EDUCATION_PROMOTION_SOURCE_MAPPER = (rs, rowNum) -> new EducationPromotionSource(
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("xl")),
            SqlText.trim(rs.getString("bysj")));

    private static final RowMapper<EducationRegularizationStandard> EDUCATION_REGULARIZATION_STANDARD_MAPPER = (rs, rowNum) -> new EducationRegularizationStandard(
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("xlmc")),
            SqlText.trim(rs.getString("zzzwbm")),
            SqlText.trim(rs.getString("zzzwmc")),
            SqlText.trim(rs.getString("zzjb")),
            SqlText.trim(rs.getString("zzdc")));

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // Cache of the static civil-servant grade salary standard (bz06_jbgz), keyed by "tbnd|jb".
    // Each entry holds columns dc1..dc20 (index 1..20; index 0 unused, values default to 0 when missing).
    private static final int[] EMPTY_GRADE_SALARY_ROW = new int[21];
    private final Map<String, int[]> gradeSalaryRowCache = new ConcurrentHashMap<>();

    PayrollRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    List<PayrollFieldMetadata> findFields(Boolean enabledIn2006Policy, PageRequest pageRequest) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("enabledIn2006Policy", enabledIn2006Policy == null ? null : enabledIn2006Policy ? "√" : "")
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());

        return jdbcTemplate.query("""
                SELECT id, sequence, field_cate, tblname, field_name, field_type, field_len, field_dec,
                       field_cap, field_caps, field_capj, sfsy06, sfsy, lrfs, category, jbt, gld,
                       jxryff, jbtbz, qsff, gdz, `readonly`, isgroup, iscount
                FROM fldgz
                WHERE (:enabledIn2006Policy IS NULL OR sfsy06 = :enabledIn2006Policy)
                ORDER BY sequence, id
                LIMIT :limit OFFSET :offset
                """, parameters, FIELD_MAPPER);
    }

    long countFields(Boolean enabledIn2006Policy) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("enabledIn2006Policy", enabledIn2006Policy == null ? null : enabledIn2006Policy ? "√" : "");
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM fldgz
                WHERE (:enabledIn2006Policy IS NULL OR sfsy06 = :enabledIn2006Policy)
                """, parameters, Long.class);
        return count == null ? 0 : count;
    }

    List<PositionSalaryStandard> findPositionStandards(String standardYearMonth, String positionCode, PageRequest pageRequest) {
        MapSqlParameterSource parameters = standardParameters(standardYearMonth, positionCode)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT tbnd, zwbm, bz
                FROM bz06_zwgz
                WHERE (:standardYearMonth IS NULL OR tbnd = :standardYearMonth)
                  AND (:positionCode IS NULL OR zwbm = :positionCode)
                ORDER BY tbnd DESC, zwbm
                LIMIT :limit OFFSET :offset
                """, parameters, POSITION_STANDARD_MAPPER);
    }

    long countPositionStandards(String standardYearMonth, String positionCode) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM bz06_zwgz
                WHERE (:standardYearMonth IS NULL OR tbnd = :standardYearMonth)
                  AND (:positionCode IS NULL OR zwbm = :positionCode)
                """, standardParameters(standardYearMonth, positionCode), Long.class);
        return count == null ? 0 : count;
    }

    List<AllowanceStandard> findAllowanceStandards(String standardYearMonth, String item, String positionCode, PageRequest pageRequest) {
        MapSqlParameterSource parameters = standardParameters(standardYearMonth, positionCode)
                .addValue("item", emptyToNull(item))
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT id, tbnd, item, zwbm, mc, worklower, workupper, bz, jxlb
                FROM bz06_jbt
                WHERE (:standardYearMonth IS NULL OR tbnd = :standardYearMonth)
                  AND (:item IS NULL OR item = :item)
                  AND (:positionCode IS NULL OR zwbm = :positionCode)
                ORDER BY tbnd DESC, item, zwbm, worklower, id
                LIMIT :limit OFFSET :offset
                """, parameters, ALLOWANCE_STANDARD_MAPPER);
    }

    long countAllowanceStandards(String standardYearMonth, String item, String positionCode) {
        MapSqlParameterSource parameters = standardParameters(standardYearMonth, positionCode)
                .addValue("item", emptyToNull(item));
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM bz06_jbt
                WHERE (:standardYearMonth IS NULL OR tbnd = :standardYearMonth)
                  AND (:item IS NULL OR item = :item)
                  AND (:positionCode IS NULL OR zwbm = :positionCode)
                """, parameters, Long.class);
        return count == null ? 0 : count;
    }

    List<RankAllowanceStandard> findRankAllowanceStandards(
            String standardYearMonth,
            String rankName,
            String category,
            PageRequest pageRequest) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("rankName", emptyToNull(rankName))
                .addValue("rankNameLike", emptyToNull(rankName) == null ? null : "%" + rankName.trim() + "%")
                .addValue("category", emptyToNull(category))
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT id, tbnd, jxbm, jx, jtbz, lb
                FROM jxjtbz
                WHERE (:standardYearMonth IS NULL OR tbnd = :standardYearMonth)
                  AND (:rankName IS NULL OR jx LIKE :rankNameLike OR jxbm = :rankName)
                  AND (:category IS NULL OR lb = :category)
                ORDER BY tbnd DESC, lb, jxbm, jx
                LIMIT :limit OFFSET :offset
                """, parameters, RANK_ALLOWANCE_STANDARD_MAPPER);
    }

    long countRankAllowanceStandards(String standardYearMonth, String rankName, String category) {
        String trimmedRank = emptyToNull(rankName);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("rankName", trimmedRank)
                .addValue("rankNameLike", trimmedRank == null ? null : "%" + trimmedRank + "%")
                .addValue("category", emptyToNull(category));
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM jxjtbz
                WHERE (:standardYearMonth IS NULL OR tbnd = :standardYearMonth)
                  AND (:rankName IS NULL OR jx LIKE :rankNameLike OR jxbm = :rankName)
                  AND (:category IS NULL OR lb = :category)
                """, parameters, Long.class);
        return count == null ? 0 : count;
    }

    List<RetainedAllowanceStandard> findRetainedAllowanceStandards(String keyword, PageRequest pageRequest) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("keyword", emptyToNull(keyword))
                .addValue("keywordLike", emptyToNull(keyword) == null ? null : "%" + keyword.trim() + "%")
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT zwbm, mc, bz
                FROM bz06_blfb
                WHERE (:keyword IS NULL OR zwbm = :keyword OR mc LIKE :keywordLike)
                ORDER BY zwbm
                LIMIT :limit OFFSET :offset
                """, parameters, RETAINED_ALLOWANCE_STANDARD_MAPPER);
    }

    long countRetainedAllowanceStandards(String keyword) {
        String trimmedKeyword = emptyToNull(keyword);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("keyword", trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null ? null : "%" + trimmedKeyword + "%");
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM bz06_blfb
                WHERE (:keyword IS NULL OR zwbm = :keyword OR mc LIKE :keywordLike)
                """, parameters, Long.class);
        return count == null ? 0 : count;
    }

    List<YearAllowanceStandard> findYearAllowanceStandards(String standardYearMonth, PageRequest pageRequest) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT tbnd, a1, a2, a3, a4
                FROM njbt
                WHERE (:standardYearMonth IS NULL OR tbnd = :standardYearMonth)
                ORDER BY tbnd DESC
                LIMIT :limit OFFSET :offset
                """, parameters, YEAR_ALLOWANCE_STANDARD_MAPPER);
    }

    long countYearAllowanceStandards(String standardYearMonth) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM njbt
                WHERE (:standardYearMonth IS NULL OR tbnd = :standardYearMonth)
                """, new MapSqlParameterSource("standardYearMonth", emptyToNull(standardYearMonth)), Long.class);
        return count == null ? 0 : count;
    }

    List<InternSalaryStandard> findInternSalaryStandards(
            String standardYearMonth,
            String keyword,
            PageRequest pageRequest) {
        String trimmedKeyword = emptyToNull(keyword);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("keyword", trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null ? null : "%" + trimmedKeyword + "%")
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT tbnd, xlbm, xlmc, zzzwbm, zzzwmc, zzdc, zzjb, gz1, gz2
                FROM bz06_zzdz
                WHERE (:standardYearMonth IS NULL OR tbnd = :standardYearMonth)
                  AND (:keyword IS NULL OR xlbm = :keyword OR zzzwbm = :keyword OR xlmc LIKE :keywordLike OR zzzwmc LIKE :keywordLike)
                ORDER BY tbnd DESC, zzzwbm, xlbm
                LIMIT :limit OFFSET :offset
                """, parameters, INTERN_SALARY_STANDARD_MAPPER);
    }

    long countInternSalaryStandards(String standardYearMonth, String keyword) {
        String trimmedKeyword = emptyToNull(keyword);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("keyword", trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null ? null : "%" + trimmedKeyword + "%");
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM bz06_zzdz
                WHERE (:standardYearMonth IS NULL OR tbnd = :standardYearMonth)
                  AND (:keyword IS NULL OR xlbm = :keyword OR zzzwbm = :keyword OR xlmc LIKE :keywordLike OR zzzwmc LIKE :keywordLike)
                """, parameters, Long.class);
        return count == null ? 0 : count;
    }

    List<WageReformStandard> findWageReformStandards(String positionCode, PageRequest pageRequest) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("positionCode", emptyToNull(positionCode))
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT zwbm, rzns, rznz, tgns, tgnz, jb, dc
                FROM bz06_tgb
                WHERE (:positionCode IS NULL OR zwbm = :positionCode)
                ORDER BY zwbm, rzns, rznz, tgns, tgnz
                LIMIT :limit OFFSET :offset
                """, parameters, WAGE_REFORM_STANDARD_MAPPER);
    }

    long countWageReformStandards(String positionCode) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM bz06_tgb
                WHERE (:positionCode IS NULL OR zwbm = :positionCode)
                """, new MapSqlParameterSource("positionCode", emptyToNull(positionCode)), Long.class);
        return count == null ? 0 : count;
    }

    Optional<WageReformStandard> findWageReformStandard(String positionCode, int appointmentYears, int reformYears) {
        return jdbcTemplate.query("""
                SELECT zwbm, rzns, rznz, tgns, tgnz, jb, dc
                FROM bz06_tgb
                WHERE zwbm = :positionCode
                  AND :appointmentYears BETWEEN rzns AND rznz
                  AND :reformYears BETWEEN tgns AND tgnz
                ORDER BY rzns, tgns
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("positionCode", emptyToNull(positionCode))
                .addValue("appointmentYears", appointmentYears)
                .addValue("reformYears", reformYears), WAGE_REFORM_STANDARD_MAPPER).stream().findFirst();
    }

    Optional<WageReformStandard> findNearestWageReformStandard(String positionCode, int appointmentYears, int reformYears) {
        return jdbcTemplate.query("""
                SELECT zwbm, rzns, rznz, tgns, tgnz, jb, dc
                FROM bz06_tgb
                WHERE zwbm = :positionCode
                ORDER BY CASE WHEN :reformYears BETWEEN tgns AND tgnz THEN 0 ELSE 1 END,
                         CASE WHEN :appointmentYears BETWEEN rzns AND rznz THEN 0 ELSE 1 END,
                         ABS((CAST(rzns AS SIGNED) + CAST(rznz AS SIGNED)) / 2.0 - :appointmentYears),
                         ABS((CAST(tgns AS SIGNED) + CAST(tgnz AS SIGNED)) / 2.0 - :reformYears),
                         rzns,
                         tgns
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("positionCode", emptyToNull(positionCode))
                .addValue("appointmentYears", appointmentYears)
                .addValue("reformYears", reformYears), WAGE_REFORM_STANDARD_MAPPER).stream().findFirst();
    }

    Optional<WageReformStandard> findFirstWageReformStandardForPosition(String positionCode) {
        return jdbcTemplate.query("""
                SELECT zwbm, rzns, rznz, tgns, tgnz, jb, dc
                FROM bz06_tgb
                WHERE zwbm = :positionCode
                ORDER BY tgns, rzns
                LIMIT 1
                """, new MapSqlParameterSource("positionCode", emptyToNull(positionCode)), WAGE_REFORM_STANDARD_MAPPER).stream().findFirst();
    }

    Optional<String> findPersonnelEducationCode(String organizationCode, String personCode) {
        return jdbcTemplate.query("""
                SELECT xlbm
                FROM dryjbxx
                WHERE dwbm = :organizationCode AND grbm = :personCode
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), (rs, rowNum) -> SqlText.trim(rs.getString("xlbm"))).stream().findFirst();
    }

    int calculatedWageReformYears(String organizationCode, String personCode) {
        Integer years = queryInteger("""
                SELECT GREATEST(1, 2006 - CAST(LEFT(cjgzny, 4) AS SIGNED) + 1 + bjglxlnx - zdgznx)
                FROM dryjbxx
                WHERE dwbm = :organizationCode AND grbm = :personCode
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode));
        return years == null ? 0 : years;
    }

    List<OtherAllowanceStandard> findOtherAllowanceStandards(
            String standardType,
            String standardYearMonth,
            String code,
            PageRequest pageRequest) {
        OtherAllowanceStandardQuery query = otherAllowanceStandardQuery(standardType);
        MapSqlParameterSource parameters = otherAllowanceParameters(standardYearMonth, code)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT '%s' AS standard_type, %s
                FROM %s
                WHERE %s
                ORDER BY %s
                LIMIT :limit OFFSET :offset
                """.formatted(query.standardType(), query.columns(), query.tableName(), query.whereClause(), query.orderBy()),
                parameters,
                OTHER_ALLOWANCE_STANDARD_MAPPER);
    }

    long countOtherAllowanceStandards(String standardType, String standardYearMonth, String code) {
        OtherAllowanceStandardQuery query = otherAllowanceStandardQuery(standardType);
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM %s
                WHERE %s
                """.formatted(query.tableName(), query.whereClause()), otherAllowanceParameters(standardYearMonth, code), Long.class);
        return count == null ? 0 : count;
    }

    List<BasicStandardRecord> findBasicStandards(
            String standardType,
            String standardYearMonth,
            String code,
            PageRequest pageRequest) {
        BasicStandardQuery query = basicStandardQuery(standardType);
        MapSqlParameterSource parameters = basicStandardParameters(standardYearMonth, code)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.queryForList("""
                SELECT %s
                FROM %s
                WHERE (:standardYearMonth IS NULL OR tbnd = :standardYearMonth)
                  AND (:code IS NULL OR %s)
                ORDER BY %s
                LIMIT :limit OFFSET :offset
                """.formatted(query.columns(), query.tableName(), query.codePredicate(), query.orderBy()), parameters)
                .stream()
                .map(row -> new BasicStandardRecord(standardType, new LinkedHashMap<>(row)))
                .toList();
    }

    long countBasicStandards(String standardType, String standardYearMonth, String code) {
        BasicStandardQuery query = basicStandardQuery(standardType);
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM %s
                WHERE (:standardYearMonth IS NULL OR tbnd = :standardYearMonth)
                  AND (:code IS NULL OR %s)
                """.formatted(query.tableName(), query.codePredicate()), basicStandardParameters(standardYearMonth, code), Long.class);
        return count == null ? 0 : count;
    }

    Optional<PayrollHistorySnapshot> findLatestHistory(int uid) {
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                       h.dwsx, dw.dfbt, h.jzgb, h.spdw, p.cjgzny, h.srny, p.gznx, p.zdgznx,
                       h.xckhndjb, h.xckhndzw, h.jhlqsny, h.zdjhlnx, h.tgbl, h.jxjtbz, h.jx,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.fddc, h.jbgzjb2, h.djc2, h.tbnd, h.jbtbz,
                       h.gwjtbz, h.gwjtlb,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.sdbt, h.blfb2,
                       h.jhljt, h.jsfszwtg2, h.jxjt, h.fdgz2, h.jjjy2, h.gwjt2, h.tgblbf,
                       h.pgbc, h.njbt, h.hj2
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE p.uid = :uid
                ORDER BY CASE WHEN h.sid IS NULL OR TRIM(h.sid) = '' THEN 0 ELSE 1 END,
                         h.jsnf DESC, h.jsyf DESC, h.id DESC
                LIMIT 1
                """, new MapSqlParameterSource("uid", uid), HISTORY_MAPPER).stream().findFirst();
    }

    Map<Integer, PayrollHistorySnapshot> findLatestHistoriesByUids(List<Integer> uids) {
        if (uids == null || uids.isEmpty()) {
            return Map.of();
        }
        List<Integer> distinctUids = uids.stream().distinct().toList();
        Map<Integer, PayrollHistorySnapshot> histories = new LinkedHashMap<>();
        jdbcTemplate.query("""
                WITH ranked AS (
                    SELECT p.uid AS personnel_uid,
                           h.id, h.dwbm, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                           h.dwsx, dw.dfbt, h.jzgb, h.spdw, p.cjgzny, h.srny, p.gznx, p.zdgznx,
                           h.xckhndjb, h.xckhndzw, h.jhlqsny, h.zdjhlnx, h.tgbl, h.jxjtbz, h.jx,
                           h.zwbm2, h.zwgw2, h.zwgzdc2, h.fddc, h.jbgzjb2, h.djc2, h.tbnd, h.jbtbz,
                           h.gwjtbz, h.gwjtlb,
                           h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.sdbt, h.blfb2,
                           h.jhljt, h.jsfszwtg2, h.jxjt, h.fdgz2, h.jjjy2, h.gwjt2, h.tgblbf,
                           h.pgbc, h.njbt, h.hj2,
                           ROW_NUMBER() OVER (
                               PARTITION BY p.uid
                               ORDER BY CASE WHEN h.sid IS NULL OR TRIM(h.sid) = '' THEN 0 ELSE 1 END,
                                        h.jsnf DESC, h.jsyf DESC, h.id DESC
                           ) AS rn
                    FROM dryjbxx p
                    JOIN hisbase h ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                    LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                    WHERE p.uid IN (:uids)
                )
                SELECT personnel_uid, id, dwbm, grbm, xm, jsnf, jsyf, jslb,
                       dwsx, dfbt, jzgb, spdw, cjgzny, srny, gznx, zdgznx,
                       xckhndjb, xckhndzw, jhlqsny, zdjhlnx, tgbl, jxjtbz, jx,
                       zwbm2, zwgw2, zwgzdc2, fddc, jbgzjb2, djc2, tbnd, jbtbz,
                       gwjtbz, gwjtlb,
                       zwgzse2, jbgzse2, jsdjgz2, dfbt2, sdbt, blfb2,
                       jhljt, jsfszwtg2, jxjt, fdgz2, jjjy2, gwjt2, tgblbf,
                       pgbc, njbt, hj2
                FROM ranked
                WHERE rn = 1
                """, new MapSqlParameterSource("uids", distinctUids), (rs, rowNum) -> {
            int uid = rs.getInt("personnel_uid");
            histories.put(uid, HISTORY_MAPPER.mapRow(rs, rowNum));
            return uid;
        });
        return histories;
    }

    Map<Integer, PositionChangeDisplayPair> findProcessedPositionChangeDisplaysByUids(List<Integer> uids) {
        if (uids == null || uids.isEmpty()) {
            return Map.of();
        }
        Map<Integer, PositionChangeDisplayPair> displays = new LinkedHashMap<>();
        jdbcTemplate.query("""
                WITH ranked AS (
                    SELECT p.uid AS personnel_uid,
                           h1.zwbm2 AS before_position_code,
                           h1.zwgw2 AS before_position_name,
                           h.zwbm2 AS after_position_code,
                           h.zwgw2 AS after_position_name,
                           ROW_NUMBER() OVER (
                               PARTITION BY p.uid
                               ORDER BY h.jsnf DESC, h.jsyf DESC, h.id DESC
                           ) AS rn
                    FROM dryjbxx p
                    JOIN hisbase h ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                    LEFT JOIN hisbase h1
                      ON h1.dwbm = h.dwbm
                     AND h1.grbm = h.grbm
                     AND h1.sid = h.id
                    WHERE p.uid IN (:uids)
                      AND (h.sid IS NULL OR TRIM(h.sid) = '')
                      AND TRIM(h.jslb) IN ('职务变化','职级晋升','法检套改','警员套改','警务套改','职级套改')
                )
                SELECT personnel_uid, before_position_code, before_position_name,
                       after_position_code, after_position_name
                FROM ranked
                WHERE rn = 1
                """, new MapSqlParameterSource("uids", uids.stream().distinct().toList()), (rs, rowNum) -> {
            int uid = rs.getInt("personnel_uid");
            displays.put(uid, POSITION_CHANGE_DISPLAY_PAIR_MAPPER.mapRow(rs, rowNum));
            return uid;
        });
        return displays;
    }

    Map<String, PositionChangeDisplayPair> findProcessedPositionChangeDisplaysByHistoryIds(Collection<String> historyIds) {
        if (historyIds == null || historyIds.isEmpty()) {
            return Map.of();
        }
        List<String> ids = historyIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<String, PositionChangeDisplayPair> displays = new LinkedHashMap<>();
        jdbcTemplate.query("""
                SELECT h.id AS history_id,
                       h1.zwbm2 AS before_position_code,
                       h1.zwgw2 AS before_position_name,
                       h.zwbm2 AS after_position_code,
                       h.zwgw2 AS after_position_name
                FROM hisbase h
                LEFT JOIN hisbase h1
                  ON h1.dwbm = h.dwbm
                 AND h1.grbm = h.grbm
                 AND h1.sid = h.id
                WHERE h.id IN (:ids)
                """, new MapSqlParameterSource("ids", ids), (rs, rowNum) -> {
            displays.put(SqlText.trim(rs.getString("history_id")), POSITION_CHANGE_DISPLAY_PAIR_MAPPER.mapRow(rs, rowNum));
            return null;
        });
        return displays;
    }

    Map<Integer, PositionChangeCandidate> findCurrentPositionChangeCandidatesByUids(List<Integer> uids) {
        if (uids == null || uids.isEmpty()) {
            return Map.of();
        }
        Map<Integer, PositionChangeCandidate> candidates = new LinkedHashMap<>();
        jdbcTemplate.query("""
                WITH ranked AS (
                    SELECT p.uid AS personnel_uid,
                           b.zwbm, b.xzzw, b.srny,
                           ROW_NUMBER() OVER (
                               PARTITION BY p.uid
                               ORDER BY b.srny DESC, b.id DESC
                           ) AS rn
                    FROM dryjbxx p
                    JOIN dryzwbh b ON p.dwbm = b.dwbm AND p.grbm = b.grbm
                    INNER JOIN hisbase h
                        ON h.dwbm = b.dwbm
                       AND h.grbm = b.grbm
                       AND (h.sid IS NULL OR TRIM(h.sid) = '')
                    WHERE p.uid IN (:uids)
                      AND b.xrzwbz = '1'
                      AND b.srny >= '2006.07'
                      AND b.srny >= h.srny
                      AND (
                           COALESCE(h.zwbm2, '') <> COALESCE(b.zwbm, '')
                           OR COALESCE(h.zjbm, '') <> COALESCE(b.zjbm, '')
                      )
                      AND (
                           CASE WHEN LEFT(COALESCE(h.zwbm2, ''), 2) IN ('07','08','09','10','11') THEN 1 ELSE 0 END
                           =
                           CASE WHEN LEFT(COALESCE(b.zwbm, ''), 2) IN ('07','08','09','10','11') THEN 1 ELSE 0 END
                      )
                )
                SELECT personnel_uid, zwbm, xzzw, srny
                FROM ranked
                WHERE rn = 1
                """, new MapSqlParameterSource("uids", uids.stream().distinct().toList()), (rs, rowNum) -> {
            int uid = rs.getInt("personnel_uid");
            candidates.put(uid, POSITION_CHANGE_CANDIDATE_MAPPER.mapRow(rs, rowNum));
            return uid;
        });
        return candidates;
    }

    Map<String, PositionLevelRange> findPositionLevelRanges(Collection<String> positionCodes) {
        if (positionCodes == null || positionCodes.isEmpty()) {
            return Map.of();
        }
        List<String> codes = positionCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .distinct()
                .toList();
        if (codes.isEmpty()) {
            return Map.of();
        }
        return jdbcTemplate.query("""
                SELECT zwbm, `min`, `max`
                FROM bz06_zw_jb_xj
                WHERE zwbm IN (:codes)
                """, new MapSqlParameterSource("codes", codes), POSITION_LEVEL_RANGE_MAPPER).stream()
                .collect(Collectors.toMap(PositionLevelRange::positionCode, range -> range, (left, right) -> left, LinkedHashMap::new));
    }

    Map<String, Integer> findPositionSalaries(String standardYearMonth, Collection<String> positionCodes) {
        if (positionCodes == null || positionCodes.isEmpty() || emptyToNull(standardYearMonth) == null) {
            return Map.of();
        }
        List<String> codes = positionCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .distinct()
                .toList();
        if (codes.isEmpty()) {
            return Map.of();
        }
        Map<String, String> mappedCodes = codes.stream()
                .collect(Collectors.toMap(code -> code, this::mapPositionSalaryCode, (left, right) -> left, LinkedHashMap::new));
        List<String> queryCodes = mappedCodes.values().stream().distinct().toList();
        Map<String, Integer> salariesByMappedCode = new HashMap<>();
        jdbcTemplate.query("""
                SELECT zwbm, bz
                FROM bz06_zwgz
                WHERE tbnd = :standardYearMonth AND zwbm IN (:codes)
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("codes", queryCodes), (rs, rowNum) -> {
            salariesByMappedCode.put(SqlText.trim(rs.getString("zwbm")), rs.getInt("bz"));
            return null;
        });
        Map<String, Integer> salaries = new LinkedHashMap<>();
        mappedCodes.forEach((originalCode, mappedCode) -> {
            Integer amount = salariesByMappedCode.get(mappedCode);
            if (amount != null) {
                salaries.put(originalCode, amount);
            }
        });
        return salaries;
    }

    Optional<PayrollHistorySnapshot> findHistoryAtOrBefore(int uid, String period) {
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                       h.dwsx, dw.dfbt, h.jzgb, h.spdw, p.cjgzny, h.srny, p.gznx, p.zdgznx,
                       h.xckhndjb, h.xckhndzw, h.jhlqsny, h.zdjhlnx, h.tgbl, h.jxjtbz, h.jx,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.fddc, h.jbgzjb2, h.djc2, h.tbnd, h.jbtbz,
                       h.gwjtbz, h.gwjtlb,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.sdbt, h.blfb2,
                       h.jhljt, h.jsfszwtg2, h.jxjt, h.fdgz2, h.jjjy2, h.gwjt2, h.tgblbf,
                       h.pgbc, h.njbt, h.hj2
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE p.uid = :uid AND CONCAT(h.jsnf, h.jsyf) <= :period
                ORDER BY h.jsnf DESC, h.jsyf DESC, h.id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("uid", uid)
                .addValue("period", period), HISTORY_MAPPER).stream().findFirst();
    }

    List<PayrollHistorySnapshot> findHistoryChain(String organizationCode, String personCode) {
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                       h.dwsx, dw.dfbt, h.jzgb, h.spdw, p.cjgzny, h.srny, p.gznx, p.zdgznx,
                       h.xckhndjb, h.xckhndzw, h.jhlqsny, h.zdjhlnx, h.tgbl, h.jxjtbz, h.jx,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.fddc, h.jbgzjb2, h.djc2, h.tbnd, h.jbtbz,
                       h.gwjtbz, h.gwjtlb,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.sdbt, h.blfb2,
                       h.jhljt, h.jsfszwtg2, h.jxjt, h.fdgz2, h.jjjy2, h.gwjt2, h.tgblbf,
                       h.pgbc, h.njbt, h.hj2
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE h.dwbm = :organizationCode AND h.grbm = :personCode
                ORDER BY h.jsnf, h.jsyf, h.id
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), HISTORY_MAPPER);
    }

    String findRegularizationYearMonth(String organizationCode, String personCode) {
        return jdbcTemplate.queryForList("""
                SELECT zzny
                FROM dryjbxx
                WHERE dwbm = :organizationCode AND grbm = :personCode
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), String.class).stream().findFirst().map(SqlText::trim).orElse("");
    }

    Optional<PositionChangeCandidate> findAdministrativePositionBeforeReform(String organizationCode, String personCode) {
        return jdbcTemplate.query("""
                SELECT zwbm, xzzw, srny
                FROM dryzwbh
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND LEFT(zwbm, 2) = '01'
                  AND REPLACE(srny, '.', '') < '200607'
                ORDER BY srny DESC, id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), POSITION_CHANGE_CANDIDATE_MAPPER).stream().findFirst();
    }

    Optional<PositionChangeCandidate> findPositionAtPeriod(String organizationCode, String personCode, String period) {
        String normalizedPeriod = period == null ? "" : period.replace(".", "");
        return jdbcTemplate.query("""
                SELECT zwbm, xzzw, srny
                FROM dryzwbh
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND REPLACE(srny, '.', '') = :period
                ORDER BY srny DESC, id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("period", normalizedPeriod), POSITION_CHANGE_CANDIDATE_MAPPER).stream().findFirst();
    }

    Optional<PositionChangeCandidate> findPositionAtOrBefore(String organizationCode, String personCode, String period) {
        String normalizedPeriod = period == null ? "" : period.replace(".", "");
        return jdbcTemplate.query("""
                SELECT zwbm, xzzw, srny
                FROM dryzwbh
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND REPLACE(srny, '.', '') <= :period
                ORDER BY srny DESC, id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("period", normalizedPeriod), POSITION_CHANGE_CANDIDATE_MAPPER).stream().findFirst();
    }

    List<PositionChangeCandidate> findPositionChangesBetween(
            String organizationCode,
            String personCode,
            String afterPeriod,
            String beforeOrAtPeriod,
            Set<String> prefixes) {
        String normalizedAfter = afterPeriod == null ? "" : afterPeriod.replace(".", "");
        String normalizedBeforeOrAt = beforeOrAtPeriod == null ? "" : beforeOrAtPeriod.replace(".", "");
        return jdbcTemplate.query("""
                SELECT zwbm, xzzw, srny
                FROM dryzwbh
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND REPLACE(srny, '.', '') >= :afterPeriod
                  AND REPLACE(srny, '.', '') <= :beforeOrAtPeriod
                  AND LEFT(zwbm, 2) IN (:prefixes)
                ORDER BY srny, id
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("afterPeriod", normalizedAfter)
                .addValue("beforeOrAtPeriod", normalizedBeforeOrAt)
                .addValue("prefixes", prefixes == null || prefixes.isEmpty() ? List.of("__NO_PREFIX__") : prefixes), POSITION_CHANGE_CANDIDATE_MAPPER);
    }

    boolean hasDemotionDisciplinaryRecord(String organizationCode, String personCode, String period) {
        String normalizedPeriod = period == null ? "" : period.replace(".", "");
        Integer count = queryInteger("""
                SELECT COUNT(*)
                FROM hjxx
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND (REPLACE(hjsj, '.', '') = :period OR REPLACE(tqyjjssj, '.', '') = :period)
                  AND (hjmc LIKE '%降职%' OR hjmc LIKE '%撤职%' OR jllx LIKE '%处分%' OR qtqk LIKE '%降职%' OR qtqk LIKE '%撤职%')
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("period", normalizedPeriod));
        return count != null && count > 0;
    }

    Optional<OrganizationPayrollPolicy> findOrganizationPayrollPolicy(String organizationCode) {
        if (emptyToNull(organizationCode) == null) {
            return Optional.empty();
        }
        return jdbcTemplate.query("""
                SELECT zwbhhjsdj, cdchjsdj, zwbh10
                FROM cyxx
                WHERE dwbm = :organizationCode
                ORDER BY ID
                LIMIT 1
                """, new MapSqlParameterSource("organizationCode", organizationCode), (rs, rowNum) -> new OrganizationPayrollPolicy(
                SqlText.trim(rs.getString("zwbhhjsdj")),
                SqlText.trim(rs.getString("cdchjsdj")),
                SqlText.trim(rs.getString("zwbh10")))).stream().findFirst();
    }

    Optional<PositionChangeCandidate> findLatestPositionBefore(
            String organizationCode,
            String personCode,
            String beforePeriod,
            Set<String> prefixes) {
        String normalizedPeriod = beforePeriod == null ? "" : beforePeriod.replace(".", "");
        return jdbcTemplate.query("""
                SELECT zwbm, xzzw, srny
                FROM dryzwbh
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND REPLACE(srny, '.', '') < :period
                  AND LEFT(zwbm, 2) IN (:prefixes)
                ORDER BY srny DESC, id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("period", normalizedPeriod)
                .addValue("prefixes", prefixes == null || prefixes.isEmpty() ? List.of("__NO_PREFIX__") : prefixes), POSITION_CHANGE_CANDIDATE_MAPPER).stream().findFirst();
    }

    /** 套改月（200607）前最近一条见习/试用期任职，排除套改后任职如 200609。 */
    Optional<PositionChangeCandidate> findLatestInternPositionBefore(
            String organizationCode,
            String personCode,
            String beforePeriod) {
        String normalizedPeriod = beforePeriod == null ? "" : beforePeriod.replace(".", "");
        return jdbcTemplate.query("""
                SELECT zwbm, xzzw, srny
                FROM dryzwbh
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND REPLACE(srny, '.', '') < :period
                  AND (
                        zwbm LIKE '%F%'
                     OR xzzw LIKE '%见习%'
                     OR xzzw LIKE '%试用%'
                  )
                ORDER BY srny DESC, id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("period", normalizedPeriod), POSITION_CHANGE_CANDIDATE_MAPPER).stream().findFirst();
    }

    List<WageReformPosition> findWageReformPositionsBefore(
            String organizationCode,
            String personCode,
            String beforePeriod,
            Set<String> prefixes) {
        String normalizedPeriod = beforePeriod == null ? "" : beforePeriod.replace(".", "");
        return jdbcTemplate.query("""
                SELECT zwbm, xzzw, srny, kjnx
                FROM dryzwbh
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND REPLACE(srny, '.', '') < :period
                  AND LEFT(zwbm, 2) IN (:prefixes)
                ORDER BY srny DESC, id DESC
                LIMIT 32
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("period", normalizedPeriod)
                .addValue("prefixes", prefixes == null || prefixes.isEmpty() ? List.of("__NO_PREFIX__") : prefixes), WAGE_REFORM_POSITION_MAPPER);
    }

    Optional<PersonnelDtgxxFields> findPersonnelDtgxxFields(String organizationCode, String personCode) {
        return jdbcTemplate.query("""
                SELECT cjgzny, bjglxlnx, zdgznx, xlbm, zgxl
                FROM dryjbxx
                WHERE dwbm = :organizationCode AND grbm = :personCode
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), (rs, rowNum) -> new PersonnelDtgxxFields(
                SqlText.trim(rs.getString("cjgzny")),
                rs.getInt("bjglxlnx"),
                rs.getInt("zdgznx"),
                SqlText.trim(rs.getString("xlbm")),
                SqlText.trim(rs.getString("zgxl")))).stream().findFirst();
    }

    boolean hasDtgxxRecord(String organizationCode, String personCode) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dtgxx
                WHERE dwbm = :organizationCode AND grbm = :personCode
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), Integer.class);
        return count != null && count > 0;
    }

    boolean hasAppCreatedDtgxxRecord(String organizationCode, String personCode) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dtgxx t
                INNER JOIN app_record_marker marker
                    ON marker.table_name = 'dtgxx'
                   AND marker.record_id = CAST(t.id AS CHAR)
                   AND marker.marker = 'APP_CREATED'
                WHERE t.dwbm = :organizationCode AND t.grbm = :personCode
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), Integer.class);
        return count != null && count > 0;
    }

    int insertWageReform2006Dtgxx(WageReform2006DtgxxSnapshot snapshot) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("organizationCode", snapshot.organizationCode())
                .addValue("personCode", snapshot.personCode())
                .addValue("workStartYearMonth", valueOrBlank(snapshot.workStartYearMonth()))
                .addValue("schoolingYears", snapshot.schoolingYears())
                .addValue("minimumWorkYears", snapshot.minimumWorkYears())
                .addValue("deductionYears", snapshot.deductionYears())
                .addValue("wageReformYears", snapshot.wageReformYears())
                .addValue("currentPositionCode", valueOrBlank(snapshot.currentPositionCode()))
                .addValue("currentPositionName", valueOrBlank(snapshot.currentPositionName()))
                .addValue("currentAppointmentPeriod", valueOrBlank(snapshot.currentAppointmentPeriod()))
                .addValue("currentAppointmentYears", snapshot.currentAppointmentYears())
                .addValue("currentPositionDeductionYears", snapshot.currentPositionDeductionYears())
                .addValue("lowerPositionCode", valueOrBlank(snapshot.lowerPositionCode()))
                .addValue("lowerPositionName", valueOrBlank(snapshot.lowerPositionName()))
                .addValue("lowerAppointmentPeriod", valueOrBlank(snapshot.lowerAppointmentPeriod()))
                .addValue("lowerAppointmentYears", snapshot.lowerAppointmentYears())
                .addValue("lowerPositionDeductionYears", snapshot.lowerPositionDeductionYears())
                .addValue("educationCode", valueOrBlank(snapshot.educationCode()))
                .addValue("educationName", valueOrBlank(snapshot.educationName()))
                .addValue("reformPositionCode", valueOrBlank(snapshot.reformPositionCode()))
                .addValue("reformPositionName", valueOrBlank(snapshot.reformPositionName()))
                .addValue("reformLevel", valueOrBlank(snapshot.reformLevel()))
                .addValue("reformStep", valueOrBlank(snapshot.reformStep()))
                .addValue("fixedStep", snapshot.fixedStep())
                .addValue("pendingStep", snapshot.pendingStep())
                .addValue("fixedLevel", snapshot.fixedLevel())
                .addValue("pendingLevel", snapshot.pendingLevel())
                .addValue("remark", valueOrBlank(snapshot.remark()));
        jdbcTemplate.update("""
                INSERT INTO dtgxx (
                    dwbm, grbm, cjgzny, xlnx, zdgznx, kjnx, tgnx,
                    zwbm, zwmc, rzsj, rznx, zwkjnx,
                    zwbm1, zwmc1, rzsj1, rznx1, zwkjnx1,
                    xlbm, xl, tgzwbm, tgzw, tgjb, tgdc,
                    gddc, dddc, gdjb, ddjb, remark
                ) VALUES (
                    :organizationCode, :personCode, :workStartYearMonth, :schoolingYears, :minimumWorkYears, :deductionYears, :wageReformYears,
                    :currentPositionCode, :currentPositionName, :currentAppointmentPeriod, :currentAppointmentYears, :currentPositionDeductionYears,
                    :lowerPositionCode, :lowerPositionName, :lowerAppointmentPeriod, :lowerAppointmentYears, :lowerPositionDeductionYears,
                    :educationCode, :educationName, :reformPositionCode, :reformPositionName, :reformLevel, :reformStep,
                    :fixedStep, :pendingStep, :fixedLevel, :pendingLevel, :remark
                )
                """, params);
        Integer id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Integer.class);
        int recordId = id == null ? 0 : id;
        if (recordId > 0) {
            markAppCreated("dtgxx", recordId);
        }
        return recordId;
    }

    void deleteAppCreatedDtgxxRecords(String organizationCode, String personCode) {
        List<Integer> ids = jdbcTemplate.query("""
                SELECT t.id
                FROM dtgxx t
                INNER JOIN app_record_marker marker
                    ON marker.table_name = 'dtgxx'
                   AND marker.record_id = CAST(t.id AS CHAR)
                   AND marker.marker = 'APP_CREATED'
                WHERE t.dwbm = :organizationCode AND t.grbm = :personCode
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), (rs, rowNum) -> rs.getInt("id"));
        for (Integer id : ids) {
            jdbcTemplate.update("DELETE FROM dtgxx WHERE id = :id", new MapSqlParameterSource("id", id));
            unmarkAppCreated("dtgxx", id);
        }
    }

    Optional<DtgxxHighGradeFields> findLatestDtgxxHighGradeFields(String organizationCode, String personCode) {
        return jdbcTemplate.query("""
                SELECT gddc, dddc, gdjb, ddjb
                FROM dtgxx
                WHERE dwbm = :organizationCode AND grbm = :personCode
                ORDER BY id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), (rs, rowNum) -> new DtgxxHighGradeFields(
                rs.getInt("gddc"),
                rs.getInt("dddc"),
                rs.getInt("gdjb"),
                rs.getInt("ddjb"))).stream().findFirst();
    }

    Optional<StoredWageReformSnapshot> findStoredWageReformSnapshot(String organizationCode, String personCode) {
        return jdbcTemplate.query("""
                SELECT zwbm1, zwmc1, rzsj1, zwkjnx1, xlbm, xl, tgjb, tgdc, remark
                FROM dtgxx
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                ORDER BY id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), (rs, rowNum) -> {
            String lowerPositionCode = normalizeAppointmentPositionCode(SqlText.trim(rs.getString("zwbm1")));
            String lowerPositionName = SqlText.trim(rs.getString("zwmc1"));
            String lowerStart = SqlText.trim(rs.getString("rzsj1"));
            int lowerYears = rs.getInt("zwkjnx1");
            String educationCode = SqlText.trim(rs.getString("xlbm"));
            String educationName = SqlText.trim(rs.getString("xl"));
            String resultLevel = SqlText.trim(rs.getString("tgjb"));
            String resultStep = SqlText.trim(rs.getString("tgdc"));
            String remark = SqlText.trim(rs.getString("remark"));
            Optional<WageReformPosition> lowerPosition = lowerPositionCode == null || lowerPositionCode.isBlank()
                    ? Optional.empty()
                    : Optional.of(new WageReformPosition(
                            lowerPositionCode, lowerPositionName, lowerStart, lowerYears));
            Optional<EducationPromotionSource> education = educationCode == null || educationCode.isBlank()
                    ? Optional.empty()
                    : Optional.of(new EducationPromotionSource(educationCode, educationName, lowerStart));
            return new StoredWageReformSnapshot(lowerPosition, education, resultLevel, resultStep, remark);
        }).stream().findFirst();
    }

    List<PayrollHistoryRecord> findPayrollHistories(
            OrganizationScope organizationScope,
            String organizationCode,
            String period,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = payrollHistoryParameters(organizationScope, organizationCode, period, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT h.id, h.sid, marker.record_id IS NOT NULL AS app_created,
                       p.uid AS personnel_uid,
                       h.dwbm, dw.dwmc, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                       h.ryfl, h.dwsx, h.zwbm2, h.zwgw2, h.zwgzdc2, h.jbgzjb2,
                       h.xckhndjb, h.xckhndzw,
                       h.tbnd, h.jbtbz, h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2,
                       h.blfb2, h.jxjt, h.fdgz2, h.jjjy2, h.jhljt, h.jsfszwtg2,
                       h.njbt, h.pgbc, h.hj2
                FROM hisbase h
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                LEFT JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN app_record_marker marker ON marker.table_name = 'hisbase' AND marker.record_id = h.id AND marker.marker = 'APP_CREATED'
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (:period IS NULL OR CONCAT(h.jsnf, h.jsyf) = :period)
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike
                       OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY h.dwbm, h.grbm,
                         CASE WHEN h.sid IS NULL OR TRIM(h.sid) = '' THEN 0 ELSE 1 END,
                         h.jsnf DESC, h.jsyf DESC, h.id DESC
                LIMIT :limit OFFSET :offset
                """, parameters, PAYROLL_HISTORY_MAPPER);
    }

    long countPayrollHistories(
            OrganizationScope organizationScope,
            String organizationCode,
            String period,
            String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM hisbase h
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (:period IS NULL OR CONCAT(h.jsnf, h.jsyf) = :period)
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike
                       OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                """, payrollHistoryParameters(organizationScope, organizationCode, period, keyword), Long.class);
        return count == null ? 0 : count;
    }

    List<TeachingAllowanceAdjustment> findTeachingAllowanceAdjustments(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, dw.dwmc, h.grbm, h.xm, CONCAT(h.jsnf, h.jsyf) AS calculation_period,
                       h.zwbm2, h.zwgw2, h.jhlqsny, h.zdjhlnx, h.jhljt,
                       CASE
                           WHEN LEFT(h.zwbm2, 2) >= '07' AND LEFT(h.zwbm2, 2) < '20'
                                AND CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) > 0
                                AND CAST(h.jsnf AS UNSIGNED) - CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) > 0
                           THEN CAST(h.jsnf AS UNSIGNED) - CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) - h.zdjhlnx
                           ELSE 0
                       END AS teaching_years,
                       CASE
                           WHEN LEFT(h.zwbm2, 2) >= '07' AND LEFT(h.zwbm2, 2) < '20'
                                AND CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) > 0
                                AND CAST(h.jsnf AS UNSIGNED) - CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) > 0
                           THEN CASE
                               WHEN CAST(h.jsnf AS UNSIGNED) - CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) - h.zdjhlnx BETWEEN 5 AND 9 THEN 3
                               WHEN CAST(h.jsnf AS UNSIGNED) - CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) - h.zdjhlnx BETWEEN 10 AND 14 THEN 5
                               WHEN CAST(h.jsnf AS UNSIGNED) - CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) - h.zdjhlnx BETWEEN 15 AND 19 THEN 7
                               WHEN CAST(h.jsnf AS UNSIGNED) - CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) - h.zdjhlnx BETWEEN 20 AND 99 THEN 10
                               ELSE 0
                           END
                           ELSE 0
                       END AS calculated_amount,
                       CASE
                           WHEN LEFT(h.zwbm2, 2) >= '07' AND LEFT(h.zwbm2, 2) < '20'
                                AND CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) > 0
                                AND CAST(h.jsnf AS UNSIGNED) - CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) > 0
                           THEN CASE
                               WHEN CAST(h.jsnf AS UNSIGNED) - CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) - h.zdjhlnx BETWEEN 5 AND 9 THEN 3
                               WHEN CAST(h.jsnf AS UNSIGNED) - CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) - h.zdjhlnx BETWEEN 10 AND 14 THEN 5
                               WHEN CAST(h.jsnf AS UNSIGNED) - CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) - h.zdjhlnx BETWEEN 15 AND 19 THEN 7
                               WHEN CAST(h.jsnf AS UNSIGNED) - CAST(LEFT(h.jhlqsny, 4) AS UNSIGNED) - h.zdjhlnx BETWEEN 20 AND 99 THEN 10
                               ELSE 0
                           END
                           ELSE 0
                       END - h.jhljt AS difference_amount,
                       (LEFT(h.zwbm2, 2) >= '07' AND LEFT(h.zwbm2, 2) < '20') AS eligible
                FROM hisbase h
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND (LEFT(h.zwbm2, 2) >= '07' AND LEFT(h.zwbm2, 2) < '20' OR h.jhljt <> 0)
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY h.dwbm, h.grbm
                LIMIT :limit OFFSET :offset
                """, parameters, TEACHING_ALLOWANCE_ADJUSTMENT_MAPPER);
    }

    long countTeachingAllowanceAdjustments(OrganizationScope organizationScope, String organizationCode, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM hisbase h
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND (LEFT(h.zwbm2, 2) >= '07' AND LEFT(h.zwbm2, 2) < '20' OR h.jhljt <> 0)
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                """, payrollChangeParameters(organizationScope, organizationCode, keyword), Long.class);
        return count == null ? 0 : count;
    }

    List<RankAllowanceStandardContext> findRankAllowanceStandardAdjustmentCandidates(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String category,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("category", emptyToNull(category))
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, dw.dwmc, h.grbm, h.xm, CONCAT(h.jsnf, h.jsyf) AS calculation_period,
                       h.zwbm2, h.zwgw2, h.jx, h.jxjtbz, h.jcjtbz, h.spjtbz, h.jxjt, h.hj2
                FROM hisbase h
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND LEFT(h.zwbm2, 2) IN ('01', '02', '03')
                  AND (
                      (:category = 'jx' AND h.jx LIKE '%警%')
                      OR (:category = 'jc' AND (h.jx LIKE '%检察%' OR h.jx LIKE '%检察官%'))
                      OR (:category = 'sp' AND (h.jx LIKE '%法%' OR h.jx LIKE '%审判%' OR h.jx LIKE '%法官%'))
                      OR (:category = 'mt' AND h.jx LIKE '%监察%')
                  )
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY h.dwbm, h.grbm
                LIMIT :limit OFFSET :offset
                """, parameters, RANK_ALLOWANCE_STANDARD_CONTEXT_MAPPER);
    }

    long countRankAllowanceStandardAdjustmentCandidates(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String category) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM hisbase h
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND LEFT(h.zwbm2, 2) IN ('01', '02', '03')
                  AND (
                      (:category = 'jx' AND h.jx LIKE '%警%')
                      OR (:category = 'jc' AND (h.jx LIKE '%检察%' OR h.jx LIKE '%检察官%'))
                      OR (:category = 'sp' AND (h.jx LIKE '%法%' OR h.jx LIKE '%审判%' OR h.jx LIKE '%法官%'))
                      OR (:category = 'mt' AND h.jx LIKE '%监察%')
                  )
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                """, payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("category", emptyToNull(category)), Long.class);
        return count == null ? 0 : count;
    }

    Optional<RankAllowanceStandardContext> findRankAllowanceStandardContext(String payrollHistoryId) {
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, dw.dwmc, h.grbm, h.xm, CONCAT(h.jsnf, h.jsyf) AS calculation_period,
                       h.zwbm2, h.zwgw2, h.jx, h.jxjtbz, h.jcjtbz, h.spjtbz, h.jxjt, h.hj2
                FROM hisbase h
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE h.id = :payrollHistoryId
                LIMIT 1
                """, new MapSqlParameterSource("payrollHistoryId", payrollHistoryId), RANK_ALLOWANCE_STANDARD_CONTEXT_MAPPER)
                .stream()
                .findFirst();
    }

    String createRankAllowanceStandardHistoryFromLatest(int uid, RankAllowanceStandardHistoryMutation mutation) {
        PayrollHistorySnapshot latest = findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        String id = java.util.UUID.randomUUID().toString().toUpperCase();
        jdbcTemplate.update("""
                INSERT INTO hisbase
                SELECT :id, h.dwbm, h.grbm, h.xm, h.ryfl, h.dwsx, h.gwfl, h.jrny, h.jrfs,
                       h.zdgznx, h.gznx, h.jhlqsny, h.zdjhlnx, h.xlbm, h.zgxl, h.bjglxlnx,
                       h.tc, h.xckhndzw, h.xckhndjb, h.bgdwjc, h.zwjb, h.zjbm, h.xrzw, h.srny,
                       h.jx, h.tgbl, h.jtbl, h.fddc, h.fdgd, h.fdsj,
                       :calculationYear, :calculationMonth, :changeType,
                       h.khqk, h.dynkh, h.denkh, h.bbz, :totalAmount,
                       h.zwbm2, h.zwgw2, h.zwgzse2, h.zwgzse2, h.jbgzse2, h.jbgzse2, h.jbgzse2,
                       h.jcgz2, h.glgz2, h.jsdjgz2, h.grjj2, h.blfb2, h.jsfszwtg2,
                       h.jt2, h.fdgz2, h.jjjy2, h.dfbt2, h.gwjt2, h.bh, h.jxgz, h.zzbc,
                       h.zwjt, h.zfbt, h.dsznf, h.nzgwsf, h.jzmcbt, h.sdbt, h.grsds, h.zfgjj,
                       h.ylbxf, h.ylf, h.qtdk, h.bfyqgz, h.kjyqgz, h.sfgz, h.qtbt, :rankAllowance,
                       h.gryhzh, h.tfnf, h.tfyf, h.spdw, h.tbnd, :policeStandardYearMonth, h.jbtbz, h.jhljt,
                       h.pgbc, h.sidbt, h.jzgb, h.nrjxgzbf, h.tgblbf, :prosecutionStandardYearMonth, :judicialStandardYearMonth, h.njbt,
                       h.gwjtbz, h.gwjtlb, h.sfjzgb, ''
                FROM hisbase h
                WHERE h.id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("sourceId", latest.id())
                .addValue("calculationYear", mutation.calculationYear())
                .addValue("calculationMonth", mutation.calculationMonth())
                .addValue("changeType", mutation.changeType())
                .addValue("policeStandardYearMonth", valueOrBlank(mutation.policeStandardYearMonth()))
                .addValue("prosecutionStandardYearMonth", valueOrBlank(mutation.prosecutionStandardYearMonth()))
                .addValue("judicialStandardYearMonth", valueOrBlank(mutation.judicialStandardYearMonth()))
                .addValue("rankAllowance", mutation.rankAllowance() == null ? 0 : mutation.rankAllowance())
                .addValue("totalAmount", mutation.totalAmount() == null ? 0 : mutation.totalAmount()));
        jdbcTemplate.update("""
                UPDATE hisbase
                SET sid = :newId
                WHERE id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("newId", id)
                .addValue("sourceId", latest.id()));
        markAppCreated("hisbase", id);
        return id;
    }

    List<FloatingToFixedPreview> findFloatingToFixedPreviews(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, dw.dwmc, h.grbm, h.xm, CONCAT(h.jsnf, h.jsyf) AS calculation_period,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.fddc, p.fdsj, h.fdgz2, h.jbgzse2, h.hj2,
                       (CAST(NULLIF(TRIM(h.fddc), '') AS SIGNED) > 0
                           AND NULLIF(TRIM(p.fdsj), '') IS NOT NULL) AS eligible
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND (CAST(NULLIF(TRIM(h.fddc), '') AS SIGNED) > 0 OR h.jslb = '浮动固定')
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY h.dwbm, h.grbm
                LIMIT :limit OFFSET :offset
                """, parameters, FLOATING_TO_FIXED_MAPPER);
    }

    long countFloatingToFixedPreviews(OrganizationScope organizationScope, String organizationCode, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND (CAST(NULLIF(TRIM(h.fddc), '') AS SIGNED) > 0 OR h.jslb = '浮动固定')
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                """, payrollChangeParameters(organizationScope, organizationCode, keyword), Long.class);
        return count == null ? 0 : count;
    }

    void updatePersonnelFixedFloatingTotal(String organizationCode, String personCode, int total) {
        jdbcTemplate.update("""
                UPDATE dryjbxx
                SET fdgd = :total
                WHERE dwbm = :organizationCode AND grbm = :personCode
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("total", String.valueOf(total)));
    }

    String findPersonnelFixedFloatingTotal(String organizationCode, String personCode) {
        List<String> rows = jdbcTemplate.query("""
                SELECT fdgd
                FROM dryjbxx
                WHERE dwbm = :organizationCode AND grbm = :personCode
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), (rs, rowNum) -> SqlText.trim(rs.getString("fdgd")));
        return rows.isEmpty() ? "" : rows.getFirst();
    }

    String createFloatingToFixedHistoryFromLatest(int uid, FloatingToFixedHistoryMutation mutation) {
        PayrollHistorySnapshot latest = findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        String id = java.util.UUID.randomUUID().toString().toUpperCase();
        jdbcTemplate.update("""
                INSERT INTO hisbase
                SELECT :id, h.dwbm, h.grbm, h.xm, h.ryfl, h.dwsx, h.gwfl, h.jrny, h.jrfs,
                       h.zdgznx, h.gznx, h.jhlqsny, h.zdjhlnx, h.xlbm, h.zgxl, h.bjglxlnx,
                       h.tc, h.xckhndzw, h.xckhndjb, h.bgdwjc, h.zwjb, h.zjbm, h.xrzw, h.srny,
                       h.jx, h.tgbl, h.jtbl, '', :personnelFixedFloatingTotal, h.fdsj,
                       :calculationYear, :calculationMonth, :changeType,
                       h.khqk, h.dynkh, h.denkh, h.bbz, :totalAmount,
                       h.zwbm2, h.zwgw2, :positionSalaryGrade, :positionSalary, h.jbgzjb2, h.djc2, :gradeSalary,
                       h.jcgz2, h.glgz2, h.jsdjgz2, h.grjj2, h.blfb2, h.jsfszwtg2,
                       h.jt2, 0, h.jjjy2, h.dfbt2, h.gwjt2, h.bh, h.jxgz, h.zzbc,
                       h.zwjt, h.zfbt, h.dsznf, h.nzgwsf, h.jzmcbt, h.sdbt, h.grsds, h.zfgjj,
                       h.ylbxf, h.ylf, h.qtdk, h.bfyqgz, h.kjyqgz, h.sfgz, h.qtbt, h.jxjt,
                       h.gryhzh, h.tfnf, h.tfyf, h.spdw, h.tbnd, h.jxjtbz, h.jbtbz, h.jhljt,
                       h.pgbc, h.sidbt, h.jzgb, h.nrjxgzbf, h.tgblbf, h.jcjtbz, h.spjtbz, h.njbt,
                       h.gwjtbz, h.gwjtlb, h.sfjzgb, ''
                FROM hisbase h
                WHERE h.id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("sourceId", latest.id())
                .addValue("calculationYear", mutation.calculationYear())
                .addValue("calculationMonth", mutation.calculationMonth())
                .addValue("changeType", mutation.changeType())
                .addValue("positionSalaryGrade", valueOrBlank(mutation.positionSalaryGrade()))
                .addValue("positionSalary", mutation.positionSalary() == null ? 0 : mutation.positionSalary())
                .addValue("gradeSalary", mutation.gradeSalary() == null ? 0 : mutation.gradeSalary())
                .addValue("totalAmount", mutation.totalAmount() == null ? 0 : mutation.totalAmount())
                .addValue("personnelFixedFloatingTotal", String.valueOf(mutation.personnelFixedFloatingTotal() == null ? 0 : mutation.personnelFixedFloatingTotal())));
        jdbcTemplate.update("""
                UPDATE hisbase
                SET sid = :newId
                WHERE id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("newId", id)
                .addValue("sourceId", latest.id()));
        updatePersonnelFixedFloatingTotal(latest.organizationCode(), latest.personCode(), mutation.personnelFixedFloatingTotal());
        markAppCreated("hisbase", id);
        return id;
    }

    List<InternSalaryChangePreview> findInternSalaryChanges(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, dw.dwmc, h.grbm, h.xm, CONCAT(h.jsnf, h.jsyf) AS calculation_period,
                       p.cjgzny, p.zzny, p.xlbm, p.zgxl, h.zwbm2, h.zwgw2, h.tbnd,
                       CASE
                           WHEN h.jxgz > 0 THEN h.jxgz
                           ELSE h.zwgzse2 + h.jbgzse2
                       END AS stored_intern_salary,
                       h.hj2,
                       (INSTR(h.zwbm2, 'F') > 0 OR h.zwgw2 LIKE '%见习%' OR h.zwgw2 LIKE '%试用%') AS eligible
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND (INSTR(h.zwbm2, 'F') > 0 OR h.zwgw2 LIKE '%见习%' OR h.zwgw2 LIKE '%试用%' OR h.jslb = '见习工资')
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY h.dwbm, h.grbm
                LIMIT :limit OFFSET :offset
                """, parameters, INTERN_SALARY_CHANGE_MAPPER);
    }

    long countInternSalaryChanges(OrganizationScope organizationScope, String organizationCode, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND (INSTR(h.zwbm2, 'F') > 0 OR h.zwgw2 LIKE '%见习%' OR h.zwgw2 LIKE '%试用%' OR h.jslb = '见习工资')
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                """, payrollChangeParameters(organizationScope, organizationCode, keyword), Long.class);
        return count == null ? 0 : count;
    }

    String createInternSalaryHistoryFromLatest(int uid, InternSalaryHistoryMutation mutation) {
        PayrollHistorySnapshot latest = findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        String id = java.util.UUID.randomUUID().toString().toUpperCase();
        int internSalary = mutation.internSalary() == null ? 0 : mutation.internSalary();
        jdbcTemplate.update("""
                INSERT INTO hisbase
                SELECT :id, h.dwbm, h.grbm, h.xm, h.ryfl, h.dwsx, h.gwfl, h.jrny, h.jrfs,
                       h.zdgznx, h.gznx, h.jhlqsny, h.zdjhlnx, h.xlbm, h.zgxl, h.bjglxlnx,
                       h.tc, h.xckhndzw, h.xckhndjb, h.bgdwjc, h.zwjb, h.zjbm, h.xrzw, h.srny,
                       h.jx, h.tgbl, h.jtbl, h.fddc, h.fdgd, h.fdsj,
                       :calculationYear, :calculationMonth, :changeType,
                       h.khqk, h.dynkh, h.denkh, h.bbz, :totalAmount,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, :internSalary, h.jbgzjb2, h.djc2, 0,
                       h.jcgz2, h.glgz2, h.jsdjgz2, h.grjj2, h.blfb2, :performanceAllowance,
                       h.jt2, h.fdgz2, h.jjjy2, h.dfbt2, h.gwjt2, h.bh, :internSalary, h.zzbc,
                       h.zwjt, h.zfbt, h.dsznf, h.nzgwsf, h.jzmcbt, h.sdbt, h.grsds, h.zfgjj,
                       h.ylbxf, h.ylf, h.qtdk, h.bfyqgz, h.kjyqgz, h.sfgz, h.qtbt, h.jxjt,
                       h.gryhzh, h.tfnf, h.tfyf, h.spdw, h.tbnd, h.jxjtbz, h.jbtbz, h.jhljt,
                       h.pgbc, h.sidbt, h.jzgb, h.nrjxgzbf, h.tgblbf, h.jcjtbz, h.spjtbz, h.njbt,
                       h.gwjtbz, h.gwjtlb, h.sfjzgb, ''
                FROM hisbase h
                WHERE h.id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("sourceId", latest.id())
                .addValue("calculationYear", mutation.calculationYear())
                .addValue("calculationMonth", mutation.calculationMonth())
                .addValue("changeType", mutation.changeType())
                .addValue("internSalary", internSalary)
                .addValue("performanceAllowance", mutation.performanceAllowance() == null ? 0 : mutation.performanceAllowance())
                .addValue("totalAmount", mutation.totalAmount() == null ? 0 : mutation.totalAmount()));
        jdbcTemplate.update("""
                UPDATE hisbase
                SET sid = :newId
                WHERE id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("newId", id)
                .addValue("sourceId", latest.id()));
        markAppCreated("hisbase", id);
        return id;
    }

    String createSalaryStandardAdjustmentHistoryFromLatest(int uid, SalaryStandardAdjustmentHistoryMutation mutation) {
        PayrollHistorySnapshot latest = findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        String id = java.util.UUID.randomUUID().toString().toUpperCase();
        jdbcTemplate.update("""
                INSERT INTO hisbase
                SELECT :id, h.dwbm, h.grbm, h.xm, h.ryfl, h.dwsx, h.gwfl, h.jrny, h.jrfs,
                       h.zdgznx, h.gznx, h.jhlqsny, h.zdjhlnx, h.xlbm, h.zgxl, h.bjglxlnx,
                       h.tc, h.xckhndzw, h.xckhndjb, h.bgdwjc, h.zwjb, h.zjbm, h.xrzw, h.srny,
                       h.jx, h.tgbl, h.jtbl, h.fddc, h.fdgd, h.fdsj,
                       :calculationYear, :calculationMonth, :changeType,
                       h.khqk, h.dynkh, h.denkh, h.bbz, :totalAmount,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, :positionSalary, h.jbgzjb2, h.djc2, :gradeSalary,
                       h.jcgz2, h.glgz2, :technicalGradeSalary, h.grjj2, :retainedAllowance, :salaryIncrease,
                       h.jt2, :floatingSalary, h.jjjy2, :performanceAllowance, h.gwjt2, h.bh, h.jxgz, h.zzbc,
                       h.zwjt, h.zfbt, h.dsznf, h.nzgwsf, h.jzmcbt, :subsidyAllowance, h.grsds, h.zfgjj,
                       h.ylbxf, h.ylf, h.qtdk, h.bfyqgz, h.kjyqgz, h.sfgz, h.qtbt, h.jxjt,
                       h.gryhzh, h.tfnf, h.tfyf, h.spdw, :salaryStandardYearMonth, h.jxjtbz, :allowanceStandardYearMonth, h.jhljt,
                       h.pgbc, h.sidbt, h.jzgb, h.nrjxgzbf, h.tgblbf, h.jcjtbz, h.spjtbz, h.njbt,
                       h.gwjtbz, h.gwjtlb, h.sfjzgb, ''
                FROM hisbase h
                WHERE h.id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("sourceId", latest.id())
                .addValue("calculationYear", mutation.calculationYear())
                .addValue("calculationMonth", mutation.calculationMonth())
                .addValue("changeType", mutation.changeType())
                .addValue("positionSalary", mutation.positionSalary() == null ? 0 : mutation.positionSalary())
                .addValue("gradeSalary", mutation.gradeSalary() == null ? 0 : mutation.gradeSalary())
                .addValue("technicalGradeSalary", mutation.technicalGradeSalary() == null ? 0 : mutation.technicalGradeSalary())
                .addValue("performanceAllowance", mutation.performanceAllowance() == null ? 0 : mutation.performanceAllowance())
                .addValue("subsidyAllowance", mutation.subsidyAllowance() == null ? 0 : mutation.subsidyAllowance())
                .addValue("retainedAllowance", mutation.retainedAllowance() == null ? 0 : mutation.retainedAllowance())
                .addValue("floatingSalary", mutation.floatingSalary() == null ? 0 : mutation.floatingSalary())
                .addValue("salaryIncrease", mutation.salaryIncrease() == null ? 0 : mutation.salaryIncrease())
                .addValue("totalAmount", mutation.totalAmount() == null ? 0 : mutation.totalAmount())
                .addValue("salaryStandardYearMonth", valueOrBlank(mutation.salaryStandardYearMonth()))
                .addValue("allowanceStandardYearMonth", valueOrBlank(mutation.allowanceStandardYearMonth())));
        jdbcTemplate.update("""
                UPDATE hisbase
                SET sid = :newId
                WHERE id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("newId", id)
                .addValue("sourceId", latest.id()));
        markAppCreated("hisbase", id);
        return id;
    }

    List<SalaryStandardAdjustmentPreview> findSalaryStandardAdjustmentPreviews(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String targetStandardYearMonth,
            StandardAdjustmentScope scope,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("targetStandard", targetStandardYearMonth)
                .addValue("targetPeriod", targetStandardYearMonth)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, dw.dwmc, h.grbm, h.xm, CONCAT(h.jsnf, LPAD(TRIM(h.jsyf), 2, '0')) AS calculation_period,
                       h.jslb, h.tbnd, h.jbtbz, h.zwbm2, h.zwgw2, h.zwgzse2, h.jbgzse2, h.hj2
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND (
                      %s
                      OR (
                          h.jslb IN (:rollbackChangeTypes)
                          AND CONCAT(h.jsnf, LPAD(TRIM(h.jsyf), 2, '0')) = :targetPeriod
                      )
                  )
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY h.dwbm, h.grbm
                LIMIT :limit OFFSET :offset
                """.formatted(salaryStandardAdjustmentCandidateFilter(scope)),
                parameters.addValue("rollbackChangeTypes", scope.rollbackChangeTypes()),
                SALARY_STANDARD_ADJUSTMENT_MAPPER);
    }

    long countSalaryStandardAdjustmentPreviews(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            String targetStandardYearMonth,
            StandardAdjustmentScope scope) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("targetStandard", targetStandardYearMonth)
                .addValue("targetPeriod", targetStandardYearMonth)
                .addValue("rollbackChangeTypes", scope.rollbackChangeTypes());
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND (
                      %s
                      OR (
                          h.jslb IN (:rollbackChangeTypes)
                          AND CONCAT(h.jsnf, LPAD(TRIM(h.jsyf), 2, '0')) = :targetPeriod
                      )
                  )
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                """.formatted(salaryStandardAdjustmentCandidateFilter(scope)),
                parameters, Long.class);
        return count == null ? 0 : count;
    }

    private static String salaryStandardAdjustmentCandidateFilter(StandardAdjustmentScope scope) {
        return switch (scope) {
            case BASIC -> """
                    COALESCE(NULLIF(TRIM(h.tbnd), ''), '000000') < :targetStandard
                    """;
            case CIVIL_ALLOWANCE -> """
                    COALESCE(NULLIF(TRIM(h.jbtbz), ''), '000000') < :targetStandard
                    AND LEFT(COALESCE(h.zwbm2, ''), 2) IN ('01','02','03','04','05','06','21','22','23','24','25','26','27','28','29')
                    """;
            case PERFORMANCE -> """
                    COALESCE(NULLIF(TRIM(h.jbtbz), ''), '000000') < :targetStandard
                    AND LEFT(COALESCE(h.zwbm2, ''), 2) NOT IN ('01','02','03','04','05','06','21','22','23','24','25','26','27','28','29')
                    """;
            case ALL -> """
                    COALESCE(NULLIF(TRIM(h.tbnd), ''), '000000') < :targetStandard
                    OR COALESCE(NULLIF(TRIM(h.jbtbz), ''), '000000') < :targetStandard
                    """;
        };
    }

    private static final RowMapper<PerformanceRatioAdjustmentPreview> PERFORMANCE_RATIO_ADJUSTMENT_MAPPER = (rs, rowNum) -> new PerformanceRatioAdjustmentPreview(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("calculation_period")),
            SqlText.trim(rs.getString("jslb")),
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            SqlText.trim(rs.getString("stored_jtbl")),
            SqlText.trim(rs.getString("target_jtbl")),
            rs.getInt("dfbt2"),
            null,
            rs.getInt("hj2"),
            null,
            null,
            null,
            null,
            null);

    List<PerformanceRatioAdjustmentPreview> findPerformanceRatioAdjustmentPreviews(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, dw.dwmc, h.grbm, h.xm,
                       CONCAT(h.jsnf, LPAD(TRIM(h.jsyf), 2, '0')) AS calculation_period,
                       h.jslb, h.zwbm2, h.zwgw2, h.dfbt2, h.hj2,
                       COALESCE(NULLIF(TRIM(h.jtbl), ''), '') AS stored_jtbl,
                       COALESCE(NULLIF(TRIM(p.jtbl), ''), '') AS target_jtbl
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND NULLIF(TRIM(p.jtbl), '') IS NOT NULL
                  AND LEFT(COALESCE(h.zwbm2, ''), 2) NOT IN ('01','02','03','04','05','06','21','22','23','24','25','26','27','28','29')
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY h.dwbm, h.grbm
                LIMIT :limit OFFSET :offset
                """, parameters, PERFORMANCE_RATIO_ADJUSTMENT_MAPPER);
    }

    long countPerformanceRatioAdjustmentPreviews(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword);
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND NULLIF(TRIM(p.jtbl), '') IS NOT NULL
                  AND LEFT(COALESCE(h.zwbm2, ''), 2) NOT IN ('01','02','03','04','05','06','21','22','23','24','25','26','27','28','29')
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                """, parameters, Long.class);
        return count == null ? 0 : count;
    }

    String findPersonnelPerformanceRatio(String organizationCode, String personCode) {
        List<String> values = jdbcTemplate.queryForList("""
                SELECT jtbl
                FROM dryjbxx
                WHERE dwbm = :organizationCode AND grbm = :personCode
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", emptyToNull(organizationCode))
                .addValue("personCode", emptyToNull(personCode)), String.class);
        if (values.isEmpty()) {
            return null;
        }
        return SqlText.trim(values.getFirst());
    }

    String findHistoryPerformanceRatio(String payrollHistoryId) {
        List<String> values = jdbcTemplate.queryForList("""
                SELECT jtbl
                FROM hisbase
                WHERE id = :payrollHistoryId
                LIMIT 1
                """, new MapSqlParameterSource("payrollHistoryId", emptyToNull(payrollHistoryId)), String.class);
        if (values.isEmpty()) {
            return null;
        }
        return SqlText.trim(values.getFirst());
    }

    String createPerformanceRatioAdjustmentHistoryFromLatest(int uid, PerformanceRatioAdjustmentHistoryMutation mutation) {
        PayrollHistorySnapshot latest = findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        String id = java.util.UUID.randomUUID().toString().toUpperCase();
        jdbcTemplate.update("""
                INSERT INTO hisbase
                SELECT :id, h.dwbm, h.grbm, h.xm, h.ryfl, h.dwsx, h.gwfl, h.jrny, h.jrfs,
                       h.zdgznx, h.gznx, h.jhlqsny, h.zdjhlnx, h.xlbm, h.zgxl, h.bjglxlnx,
                       h.tc, h.xckhndzw, h.xckhndjb, h.bgdwjc, h.zwjb, h.zjbm, h.xrzw, h.srny,
                       h.jx, h.tgbl, :performanceRatio, h.fddc, h.fdgd, h.fdsj,
                       :calculationYear, :calculationMonth, :changeType,
                       h.khqk, h.dynkh, h.denkh, h.bbz, :totalAmount,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.zwgzse2, h.jbgzjb2, h.djc2, h.jbgzse2,
                       h.jcgz2, h.glgz2, h.jsdjgz2, h.grjj2, h.blfb2, h.jsfszwtg2,
                       h.jt2, h.fdgz2, h.jjjy2, :performanceAllowance, h.gwjt2, h.bh, h.jxgz, h.zzbc,
                       h.zwjt, h.zfbt, h.dsznf, h.nzgwsf, h.jzmcbt, h.sdbt, h.grsds, h.zfgjj,
                       h.ylbxf, h.ylf, h.qtdk, h.bfyqgz, h.kjyqgz, h.sfgz, h.qtbt, h.jxjt,
                       h.gryhzh, h.tfnf, h.tfyf, h.spdw, h.tbnd, h.jxjtbz, h.jbtbz, h.jhljt,
                       h.pgbc, h.sidbt, h.jzgb, h.nrjxgzbf, h.tgblbf, h.jcjtbz, h.spjtbz, h.njbt,
                       h.gwjtbz, h.gwjtlb, h.sfjzgb, ''
                FROM hisbase h
                WHERE h.id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("sourceId", latest.id())
                .addValue("calculationYear", mutation.calculationYear())
                .addValue("calculationMonth", mutation.calculationMonth())
                .addValue("changeType", mutation.changeType())
                .addValue("performanceRatio", valueOrBlank(mutation.performanceRatio()))
                .addValue("performanceAllowance", mutation.performanceAllowance() == null ? 0 : mutation.performanceAllowance())
                .addValue("totalAmount", mutation.totalAmount() == null ? 0 : mutation.totalAmount()));
        jdbcTemplate.update("""
                UPDATE hisbase
                SET sid = :newId
                WHERE id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("newId", id)
                .addValue("sourceId", latest.id()));
        markAppCreated("hisbase", id);
        return id;
    }

    String createAllowanceRecalculationHistoryFromLatest(int uid, AllowanceRecalculationHistoryMutation mutation) {
        PayrollHistorySnapshot latest = findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        String id = java.util.UUID.randomUUID().toString().toUpperCase();
        jdbcTemplate.update("""
                INSERT INTO hisbase
                SELECT :id, h.dwbm, h.grbm, h.xm, h.ryfl, h.dwsx, h.gwfl, h.jrny, h.jrfs,
                       h.zdgznx, h.gznx, h.jhlqsny, h.zdjhlnx, h.xlbm, h.zgxl, h.bjglxlnx,
                       h.tc, h.xckhndzw, h.xckhndjb, h.bgdwjc, h.zwjb, h.zjbm, h.xrzw, h.srny,
                       h.jx, h.tgbl, h.jtbl, h.fddc, h.fdgd, h.fdsj,
                       :calculationYear, :calculationMonth, :changeType,
                       h.khqk, h.dynkh, h.denkh, h.bbz, :totalAmount,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.zwgzse2, h.jbgzjb2, h.djc2, h.jbgzse2,
                       h.jcgz2, h.glgz2, h.jsdjgz2, h.grjj2, :retainedAllowance, :salaryIncrease,
                       h.jt2, h.fdgz2, h.jjjy2, :performanceAllowance, h.gwjt2, h.bh, h.jxgz, h.zzbc,
                       h.zwjt, h.zfbt, h.dsznf, h.nzgwsf, h.jzmcbt, :subsidyAllowance, h.grsds, h.zfgjj,
                       h.ylbxf, h.ylf, h.qtdk, h.bfyqgz, h.kjyqgz, h.sfgz, h.qtbt, h.jxjt,
                       h.gryhzh, h.tfnf, h.tfyf, h.spdw, h.tbnd, h.jxjtbz, :allowanceStandardYearMonth, h.jhljt,
                       h.pgbc, h.sidbt, h.jzgb, h.nrjxgzbf, h.tgblbf, h.jcjtbz, h.spjtbz, :yearAllowance,
                       h.gwjtbz, h.gwjtlb, h.sfjzgb, ''
                FROM hisbase h
                WHERE h.id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("sourceId", latest.id())
                .addValue("calculationYear", mutation.calculationYear())
                .addValue("calculationMonth", mutation.calculationMonth())
                .addValue("changeType", mutation.changeType())
                .addValue("performanceAllowance", mutation.performanceAllowance() == null ? 0 : mutation.performanceAllowance())
                .addValue("subsidyAllowance", mutation.subsidyAllowance() == null ? 0 : mutation.subsidyAllowance())
                .addValue("retainedAllowance", mutation.retainedAllowance() == null ? 0 : mutation.retainedAllowance())
                .addValue("salaryIncrease", mutation.salaryIncrease() == null ? 0 : mutation.salaryIncrease())
                .addValue("yearAllowance", mutation.yearAllowance() == null ? 0 : mutation.yearAllowance())
                .addValue("totalAmount", mutation.totalAmount() == null ? 0 : mutation.totalAmount())
                .addValue("allowanceStandardYearMonth", valueOrBlank(mutation.allowanceStandardYearMonth())));
        jdbcTemplate.update("""
                UPDATE hisbase
                SET sid = :newId
                WHERE id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("newId", id)
                .addValue("sourceId", latest.id()));
        markAppCreated("hisbase", id);
        return id;
    }

    List<AllowanceRecalculationPreview> findAllowanceRecalculationPreviews(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, dw.dwmc, h.grbm, h.xm, CONCAT(h.jsnf, LPAD(TRIM(h.jsyf), 2, '0')) AS calculation_period,
                       h.jslb, h.jbtbz, h.zwbm2, h.zwgw2, h.dfbt2, h.sdbt, h.blfb2, h.jsfszwtg2, h.hj2
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY h.dwbm, h.grbm
                LIMIT :limit OFFSET :offset
                """, parameters, ALLOWANCE_RECALCULATION_MAPPER);
    }

    long countAllowanceRecalculationPreviews(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                """, payrollChangeParameters(organizationScope, organizationCode, keyword), Long.class);
        return count == null ? 0 : count;
    }

    int internSalaryMode() {
        return queryInteger("""
                SELECT jxgz
                FROM cyxx
                ORDER BY ID
                LIMIT 1
                """, new MapSqlParameterSource());
    }

    int internSalaryRaiseEnabled() {
        Integer flag = jdbcTemplate.queryForObject("""
                SELECT zzrs
                FROM cyxx
                ORDER BY ID
                LIMIT 1
                """, new MapSqlParameterSource(), Integer.class);
        return flag == null ? 0 : flag;
    }

    RegularizationHighGradePolicy findRegularizationHighGradePolicy() {
        return jdbcTemplate.query("""
                SELECT jqdm, zzrs
                FROM cyxx
                ORDER BY ID
                LIMIT 1
                """, new MapSqlParameterSource(), (rs, rowNum) -> {
            int policeRankStartLevel = rs.getBigDecimal("jqdm") == null ? 0 : rs.getBigDecimal("jqdm").intValue();
            int highGradeIncrement = Math.max(0, policeRankStartLevel - 1);
            int activeStaffFlag = rs.getObject("zzrs", Integer.class) == null ? 0 : rs.getObject("zzrs", Integer.class);
            boolean policeHighGradeEnabled = (activeStaffFlag & 1) != 0;
            return new RegularizationHighGradePolicy(highGradeIncrement, policeHighGradeEnabled);
        }).stream().findFirst().orElse(RegularizationHighGradePolicy.empty());
    }

    List<Integer> findRegularizationHighGradeCandidateUids(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT p.uid
                FROM dryjbxx p
                INNER JOIN hisbase h
                    ON h.dwbm = p.dwbm
                   AND h.grbm = p.grbm
                   AND (h.sid IS NULL OR TRIM(h.sid) = '')
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND COALESCE(h.zwbm2, '') NOT LIKE '%F%'
                  AND REPLACE(COALESCE(NULLIF(TRIM(p.zzny), ''), '000000'), '.', '') >= '190001'
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR h.zwgw2 LIKE :keywordLike)
                ORDER BY p.dwbm, p.grbm
                """, payrollChangeParameters(organizationScope, organizationCode, keyword), Integer.class);
    }

    List<WageReform2006Candidate> findWageReform2006Candidates(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        return jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm,
                       p.cjgzny, p.zzny, p.gznx,
                       z.zwbm AS position_code, z.xzzw AS position_name, z.srny AS position_start,
                       h.id AS payroll_history_id, h.jslb AS current_change_type, h.jsnf, h.jsyf
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                LEFT JOIN dryzwbh z ON z.dwbm = p.dwbm AND z.grbm = p.grbm AND z.xrzwbz = '1'
                LEFT JOIN hisbase h ON h.dwbm = p.dwbm AND h.grbm = p.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND REPLACE(COALESCE(NULLIF(TRIM(p.cjgzny), ''), '999999'), '.', '') < '200607'
                  AND (
                        h.id IS NULL
                        OR (TRIM(h.jslb) = '套改' AND h.jsnf = '2006' AND h.jsyf = '07')
                  )
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR z.xzzw LIKE :keywordLike
                       OR z.zwbm LIKE :keywordLike)
                ORDER BY p.dwbm, p.grbm
                """, payrollChangeParameters(organizationScope, organizationCode, keyword), WAGE_REFORM_2006_MAPPER);
    }

    Optional<WageReform2006Candidate> findWageReform2006Candidate(int uid) {
        List<WageReform2006Candidate> rows = jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm,
                       p.cjgzny, p.zzny, p.gznx,
                       z.zwbm AS position_code, z.xzzw AS position_name, z.srny AS position_start,
                       h.id AS payroll_history_id, h.jslb AS current_change_type, h.jsnf, h.jsyf
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                LEFT JOIN dryzwbh z ON z.dwbm = p.dwbm AND z.grbm = p.grbm AND z.xrzwbz = '1'
                LEFT JOIN hisbase h ON h.dwbm = p.dwbm AND h.grbm = p.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
                WHERE p.uid = :uid
                  AND REPLACE(COALESCE(NULLIF(TRIM(p.cjgzny), ''), '999999'), '.', '') < '200607'
                  AND (
                        h.id IS NULL
                        OR (TRIM(h.jslb) = '套改' AND h.jsnf = '2006' AND h.jsyf = '07')
                  )
                """, new MapSqlParameterSource("uid", uid), WAGE_REFORM_2006_MAPPER);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    List<NewPersonnelSalaryCandidate> findNewPersonnelSalaryCandidates(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.jrny, p.jrfs,
                       z.zwbm AS position_code, z.xzzw AS position_name, z.srny AS position_start,
                       p.cjgzny, p.zzny, p.xlbm, p.zgxl, p.gznx,
                       h.id AS payroll_history_id, h.jslb AS current_change_type
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                INNER JOIN dryzwbh z ON z.dwbm = p.dwbm AND z.grbm = p.grbm AND z.xrzwbz = '1'
                LEFT JOIN hisbase h ON h.dwbm = p.dwbm AND h.grbm = p.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND REPLACE(COALESCE(NULLIF(TRIM(z.srny), ''), '000000'), '.', '') >= '200607'
                  AND (
                        h.id IS NULL
                        OR (h.jslb = '调入定资' AND NOT EXISTS (
                            SELECT 1 FROM hisbase hp WHERE hp.sid = h.id
                        ))
                  )
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR z.xzzw LIKE :keywordLike
                       OR z.zwbm LIKE :keywordLike)
                ORDER BY p.dwbm, p.grbm
                LIMIT :limit OFFSET :offset
                """, parameters, NEW_PERSONNEL_SALARY_MAPPER);
    }

    long countNewPersonnelSalaryCandidates(OrganizationScope organizationScope, String organizationCode, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryjbxx p
                INNER JOIN dryzwbh z ON z.dwbm = p.dwbm AND z.grbm = p.grbm AND z.xrzwbz = '1'
                LEFT JOIN hisbase h ON h.dwbm = p.dwbm AND h.grbm = p.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND REPLACE(COALESCE(NULLIF(TRIM(z.srny), ''), '000000'), '.', '') >= '200607'
                  AND (
                        h.id IS NULL
                        OR (h.jslb = '调入定资' AND NOT EXISTS (
                            SELECT 1 FROM hisbase hp WHERE hp.sid = h.id
                        ))
                  )
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR z.xzzw LIKE :keywordLike
                       OR z.zwbm LIKE :keywordLike)
                """, payrollChangeParameters(organizationScope, organizationCode, keyword), Long.class);
        return count == null ? 0 : count;
    }

    Optional<NewPersonnelSalaryCandidate> findNewPersonnelSalaryCandidate(int uid) {
        List<NewPersonnelSalaryCandidate> rows = jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.jrny, p.jrfs,
                       z.zwbm AS position_code, z.xzzw AS position_name, z.srny AS position_start,
                       p.cjgzny, p.zzny, p.xlbm, p.zgxl, p.gznx,
                       h.id AS payroll_history_id, h.jslb AS current_change_type
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                INNER JOIN dryzwbh z ON z.dwbm = p.dwbm AND z.grbm = p.grbm AND z.xrzwbz = '1'
                LEFT JOIN hisbase h ON h.dwbm = p.dwbm AND h.grbm = p.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
                WHERE p.uid = :uid
                  AND REPLACE(COALESCE(NULLIF(TRIM(z.srny), ''), '000000'), '.', '') >= '200607'
                  AND (
                        h.id IS NULL
                        OR (h.jslb = '调入定资' AND NOT EXISTS (
                            SELECT 1 FROM hisbase hp WHERE hp.sid = h.id
                        ))
                  )
                LIMIT 1
                """, new MapSqlParameterSource("uid", uid), NEW_PERSONNEL_SALARY_MAPPER);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    String insertInitialPayrollHistory(int uid, InitialPayrollHistoryMutation mutation) {
        String id = java.util.UUID.randomUUID().toString().toUpperCase();
        jdbcTemplate.update("""
                INSERT INTO hisbase
                SELECT :id, p.dwbm, p.grbm, p.xm,
                       COALESCE(NULLIF(TRIM(p.ryfl), ''), ''), COALESCE(NULLIF(TRIM(p.dwsx), ''), ''),
                       COALESCE(NULLIF(TRIM(p.gwfl), ''), ''),
                       COALESCE(NULLIF(TRIM(p.jrny), ''), COALESCE(NULLIF(TRIM(z.srny), ''), '')),
                       COALESCE(NULLIF(TRIM(p.jrfs), ''), ''),
                       COALESCE(p.zdgznx, 0), COALESCE(p.gznx, 0),
                       COALESCE(NULLIF(TRIM(p.jhlqsny), ''), ''), COALESCE(p.zdjhlnx, 0),
                       COALESCE(NULLIF(TRIM(p.xlbm), ''), ''), COALESCE(NULLIF(TRIM(p.zgxl), ''), ''),
                       COALESCE(p.bjglxlnx, 0), COALESCE(NULLIF(TRIM(p.tc), ''), ''),
                       COALESCE(NULLIF(TRIM(p.xckhndzw), ''), ''), COALESCE(NULLIF(TRIM(p.xckhndjb), ''), ''),
                       '', COALESCE(NULLIF(TRIM(p.zwjb), ''), ''), COALESCE(NULLIF(TRIM(p.zjbm), ''), ''),
                       COALESCE(NULLIF(TRIM(p.xrzw), ''), ''), COALESCE(NULLIF(TRIM(z.srny), ''), COALESCE(NULLIF(TRIM(p.srny), ''), '')),
                       COALESCE(NULLIF(TRIM(p.jx), ''), ''), COALESCE(p.tgbl, 0), COALESCE(NULLIF(TRIM(p.jtbl), ''), ''),
                       COALESCE(NULLIF(TRIM(p.fddc), ''), ''), COALESCE(NULLIF(TRIM(p.fdgd), ''), ''),
                       COALESCE(NULLIF(TRIM(p.fdsj), ''), ''),
                       :calculationYear, :calculationMonth, :changeType,
                       COALESCE(NULLIF(TRIM(p.khqk), ''), ''), '', '', '', :totalAmount,
                       :positionCode, :positionName, :positionSalaryGrade, :positionSalary,
                       :gradeSalaryLevel, :gradeSalaryStep, :gradeSalary,
                       0, 0, :technicalGradeSalary, 0, :retainedAllowance, :salaryIncrease,
                       0, 0, 0, :performanceAllowance, :subsidyAllowance, '', :internSalary, 0,
                       0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                       COALESCE(NULLIF(TRIM(p.gryhzh), ''), ''), '', '',
                       COALESCE(NULLIF(TRIM(p.spdw), ''), ''), :standardYearMonth, '', :allowanceStandardYearMonth,
                       :teachingAllowance, 0, 0, '', 0, 0, '', '', 0, '', '', '', ''
                FROM dryjbxx p
                INNER JOIN dryzwbh z ON z.dwbm = p.dwbm AND z.grbm = p.grbm AND z.xrzwbz = '1'
                WHERE p.uid = :uid
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("uid", uid)
                .addValue("calculationYear", mutation.calculationYear())
                .addValue("calculationMonth", mutation.calculationMonth())
                .addValue("changeType", mutation.changeType())
                .addValue("positionCode", valueOrBlank(mutation.positionCode()))
                .addValue("positionName", valueOrBlank(mutation.positionName()))
                .addValue("positionSalaryGrade", valueOrBlank(mutation.positionSalaryGrade()))
                .addValue("positionSalary", mutation.positionSalary() == null ? 0 : mutation.positionSalary())
                .addValue("gradeSalaryLevel", valueOrBlank(mutation.gradeSalaryLevel()))
                .addValue("gradeSalaryStep", valueOrBlank(mutation.gradeSalaryStep()))
                .addValue("gradeSalary", mutation.gradeSalary() == null ? 0 : mutation.gradeSalary())
                .addValue("technicalGradeSalary", mutation.technicalGradeSalary() == null ? 0 : mutation.technicalGradeSalary())
                .addValue("internSalary", mutation.internSalary() == null ? 0 : mutation.internSalary())
                .addValue("performanceAllowance", mutation.performanceAllowance() == null ? 0 : mutation.performanceAllowance())
                .addValue("subsidyAllowance", mutation.subsidyAllowance() == null ? 0 : mutation.subsidyAllowance())
                .addValue("retainedAllowance", mutation.retainedAllowance() == null ? 0 : mutation.retainedAllowance())
                .addValue("teachingAllowance", mutation.teachingAllowance() == null ? 0 : mutation.teachingAllowance())
                .addValue("salaryIncrease", mutation.salaryIncrease() == null ? 0 : mutation.salaryIncrease())
                .addValue("totalAmount", mutation.totalAmount() == null ? 0 : mutation.totalAmount())
                .addValue("standardYearMonth", valueOrBlank(mutation.standardYearMonth()))
                .addValue("allowanceStandardYearMonth", valueOrBlank(mutation.allowanceStandardYearMonth())));
        jdbcTemplate.update("""
                UPDATE hisbase
                SET xckhndjb = :stepAssessmentStartYear,
                    xckhndzw = :levelAssessmentStartYear
                WHERE id = :id
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("levelAssessmentStartYear", valueOrBlank(mutation.levelAssessmentStartYear()))
                .addValue("stepAssessmentStartYear", valueOrBlank(mutation.stepAssessmentStartYear())));
        markAppCreated("hisbase", id);
        return id;
    }

    List<OtherPayrollChangePreview> findOtherPayrollChangePreviews(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, dw.dwmc, h.grbm, h.xm, CONCAT(h.jsnf, h.jsyf) AS calculation_period,
                       h.jslb, h.zwbm2, h.zwgw2, h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.blfb2, h.hj2
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike
                       OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY h.dwbm, h.grbm
                LIMIT :limit OFFSET :offset
                """, parameters, OTHER_PAYROLL_CHANGE_MAPPER);
    }

    long countOtherPayrollChangePreviews(OrganizationScope organizationScope, String organizationCode, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike
                       OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                """, payrollChangeParameters(organizationScope, organizationCode, keyword), Long.class);
        return count == null ? 0 : count;
    }

    boolean isAppCreatedPayrollHistory(String payrollHistoryId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM app_record_marker
                WHERE table_name = 'hisbase'
                  AND record_id = :payrollHistoryId
                  AND marker = 'APP_CREATED'
                """, new MapSqlParameterSource("payrollHistoryId", payrollHistoryId), Integer.class);
        return count != null && count > 0;
    }

    List<Integer> findPersonnelUidsWithPayrollHistory(OrganizationScope organizationScope, PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.queryForList("""
                SELECT p.uid
                FROM dryjbxx p
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND EXISTS (
                      SELECT 1
                      FROM hisbase h
                      WHERE h.dwbm = p.dwbm AND h.grbm = p.grbm
                  )
                ORDER BY p.dwbm, p.grbm
                LIMIT :limit OFFSET :offset
                """, parameters, Integer.class);
    }

    List<Integer> findAllPersonnelUidsWithPayrollHistory(OrganizationScope organizationScope) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT p.uid
                FROM dryjbxx p
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND EXISTS (
                      SELECT 1
                      FROM hisbase h
                      WHERE h.dwbm = p.dwbm AND h.grbm = p.grbm
                  )
                ORDER BY p.dwbm, p.grbm
                """, new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes()), Integer.class);
    }

    List<Integer> findPersonnelUidsWithCurrentPayroll(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.queryForList("""
                SELECT p.uid
                FROM dryjbxx p
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND EXISTS (
                      SELECT 1
                      FROM hisbase h
                      WHERE h.dwbm = p.dwbm
                        AND h.grbm = p.grbm
                        AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  )
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR EXISTS (
                           SELECT 1
                           FROM hisbase hk
                           WHERE hk.dwbm = p.dwbm
                             AND hk.grbm = p.grbm
                             AND hk.zwgw2 LIKE :keywordLike
                       ))
                ORDER BY p.dwbm, p.grbm
                LIMIT :limit OFFSET :offset
                """, parameters, Integer.class);
    }

    List<Integer> findProbationPersonnelUidsWithCurrentPayroll(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.queryForList("""
                SELECT p.uid
                FROM dryjbxx p
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND EXISTS (
                      SELECT 1
                      FROM hisbase h
                      WHERE h.dwbm = p.dwbm
                        AND h.grbm = p.grbm
                        AND (h.sid IS NULL OR TRIM(h.sid) = '')
                        AND h.zwbm2 LIKE '%F%'
                  )
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR EXISTS (
                           SELECT 1
                           FROM hisbase hk
                           WHERE hk.dwbm = p.dwbm
                             AND hk.grbm = p.grbm
                             AND hk.zwgw2 LIKE :keywordLike
                       ))
                ORDER BY p.dwbm, p.grbm
                LIMIT :limit OFFSET :offset
                """, parameters, Integer.class);
    }

    long countProbationPersonnelWithCurrentPayroll(OrganizationScope organizationScope, String organizationCode, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryjbxx p
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND EXISTS (
                      SELECT 1
                      FROM hisbase h
                      WHERE h.dwbm = p.dwbm
                        AND h.grbm = p.grbm
                        AND (h.sid IS NULL OR TRIM(h.sid) = '')
                        AND h.zwbm2 LIKE '%F%'
                  )
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR EXISTS (
                           SELECT 1
                           FROM hisbase hk
                           WHERE hk.dwbm = p.dwbm
                             AND hk.grbm = p.grbm
                             AND hk.zwgw2 LIKE :keywordLike
                       ))
                """, payrollChangeParameters(organizationScope, organizationCode, keyword), Long.class);
        return count == null ? 0 : count;
    }

    List<Integer> findPersonnelUidsWithCurrentPayroll(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT p.uid
                FROM dryjbxx p
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND EXISTS (
                      SELECT 1
                      FROM hisbase h
                      WHERE h.dwbm = p.dwbm
                        AND h.grbm = p.grbm
                        AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  )
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR EXISTS (
                           SELECT 1
                           FROM hisbase hk
                           WHERE hk.dwbm = p.dwbm
                             AND hk.grbm = p.grbm
                             AND hk.zwgw2 LIKE :keywordLike
                       ))
                ORDER BY p.dwbm, p.grbm
                """, payrollChangeParameters(organizationScope, organizationCode, keyword), Integer.class);
    }

    long countPersonnelWithCurrentPayroll(OrganizationScope organizationScope, String organizationCode, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryjbxx p
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND EXISTS (
                      SELECT 1
                      FROM hisbase h
                      WHERE h.dwbm = p.dwbm
                        AND h.grbm = p.grbm
                        AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  )
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR EXISTS (
                           SELECT 1
                           FROM hisbase hk
                           WHERE hk.dwbm = p.dwbm
                             AND hk.grbm = p.grbm
                             AND hk.zwgw2 LIKE :keywordLike
                       ))
                """, payrollChangeParameters(organizationScope, organizationCode, keyword), Long.class);
        return count == null ? 0 : count;
    }

    int countQualifiedAssessmentYears(String organizationCode, String personCode, int startYear, int endYear) {
        if (emptyToNull(organizationCode) == null || emptyToNull(personCode) == null || startYear <= 0 || endYear < startYear) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(DISTINCT khnd)
                FROM dndkh
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND khnd BETWEEN :startYear AND :endYear
                  AND khjg IN ('优秀', '称职', '合格')
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("startYear", String.valueOf(startYear))
                .addValue("endYear", String.valueOf(endYear)), Long.class);
        return count == null ? 0 : count.intValue();
    }

    Set<Integer> assessmentYears(String organizationCode, String personCode, int startYear, int endYear) {
        if (emptyToNull(organizationCode) == null || emptyToNull(personCode) == null) {
            return Set.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT khnd
                FROM dndkh
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND khnd BETWEEN :startYear AND :endYear
                """,
                new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("startYear", String.valueOf(startYear))
                .addValue("endYear", String.valueOf(endYear)),
                String.class)
                .stream()
                .map(this::intValue)
                .filter(year -> year > 0)
                .collect(Collectors.toSet());
    }

    Map<Integer, List<PersonnelAssessmentYear>> findAssessmentYearsByUids(List<Integer> uids, int startYear, int endYear) {
        if (uids == null || uids.isEmpty() || endYear < startYear) {
            return Map.of();
        }
        List<Integer> distinctUids = uids.stream().distinct().toList();
        Map<Integer, List<PersonnelAssessmentYear>> assessments = new LinkedHashMap<>();
        jdbcTemplate.query("""
                SELECT p.uid, d.khnd, d.khjg
                FROM dryjbxx p
                INNER JOIN dndkh d
                    ON d.dwbm = p.dwbm
                   AND d.grbm = p.grbm
                WHERE p.uid IN (:uids)
                  AND d.khnd BETWEEN :startYear AND :endYear
                ORDER BY p.uid, d.khnd
                """, new MapSqlParameterSource()
                .addValue("uids", distinctUids)
                .addValue("startYear", String.valueOf(startYear))
                .addValue("endYear", String.valueOf(endYear)), (rs, rowNum) -> {
            int uid = rs.getInt("uid");
            int year = intValue(rs.getString("khnd"));
            if (year <= 0) {
                return null;
            }
            assessments.computeIfAbsent(uid, ignored -> new java.util.ArrayList<>())
                    .add(new PersonnelAssessmentYear(year, SqlText.trim(rs.getString("khjg"))));
            return null;
        });
        return assessments;
    }

    long countPersonnelWithPayrollHistory(OrganizationScope organizationScope) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryjbxx p
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND EXISTS (
                      SELECT 1
                      FROM hisbase h
                      WHERE h.dwbm = p.dwbm AND h.grbm = p.grbm
                  )
                """, new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes()), Long.class);
        return count == null ? 0 : count;
    }

    Map<String, Object> findLatestHistoryValues(int uid) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT h.*
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE p.uid = :uid
                ORDER BY CASE WHEN h.sid IS NULL OR TRIM(h.sid) = '' THEN 0 ELSE 1 END,
                         h.jsnf DESC, h.jsyf DESC, h.id DESC
                LIMIT 1
                """, new MapSqlParameterSource("uid", uid));
        if (rows.isEmpty()) {
            throw new NotFoundException("Payroll history not found for personnel record: " + uid);
        }
        return new LinkedHashMap<>(rows.getFirst());
    }

    Map<String, Object> findHistoryValuesById(String id) {
        return findHistoryValuesById(id, null, null, null, null)
                .orElseThrow(() -> new NotFoundException("Payroll history not found: " + id));
    }

    Map<String, Map<String, Object>> findHistoryValuesByIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        List<String> normalizedIds = ids.stream().map(String::valueOf).map(String::trim).filter(id -> !id.isBlank()).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Map.of();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT h.*,
                       dw.dwmc AS approval_dwmc,
                       dw.dwsx AS approval_dwsx,
                       dw.jxbl AS approval_jxbl,
                       p.sfzh AS approval_sfzh,
                       p.xb AS approval_xb,
                       p.csny AS approval_csny,
                       p.zgxl AS approval_zgxl,
                       p.cjgzny AS approval_cjgzny,
                       p.gznx AS approval_gznx,
                       p.dah AS approval_dah,
                       p.srny AS approval_rzny
                FROM hisbase h
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                LEFT JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE CAST(h.id AS CHAR) IN (:ids)
                """, new MapSqlParameterSource("ids", normalizedIds));
        Map<String, Map<String, Object>> valuesById = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String id = SqlText.trim(String.valueOf(row.get("id")));
            if (id != null && !id.isBlank()) {
                valuesById.putIfAbsent(id, new LinkedHashMap<>(row));
            }
        }
        return valuesById;
    }

    Optional<Map<String, Object>> findHistoryValuesById(
            String id,
            String organizationCode,
            String personCode,
            String calculationYear,
            String calculationMonth) {
        if (id != null && !id.isBlank()) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                    SELECT h.*,
                           dw.dwmc AS approval_dwmc,
                           dw.dwsx AS approval_dwsx,
                           dw.jxbl AS approval_jxbl,
                           p.sfzh AS approval_sfzh,
                           p.xb AS approval_xb,
                           p.csny AS approval_csny,
                           p.zgxl AS approval_zgxl,
                           p.cjgzny AS approval_cjgzny,
                           p.gznx AS approval_gznx,
                           p.dah AS approval_dah,
                           p.srny AS approval_rzny
                    FROM hisbase h
                    LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                    LEFT JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                    WHERE CAST(h.id AS CHAR) = CAST(:id AS CHAR)
                    LIMIT 1
                    """, new MapSqlParameterSource("id", id.trim()));
            if (!rows.isEmpty()) {
                return Optional.of(new LinkedHashMap<>(rows.getFirst()));
            }
        }
        if (organizationCode == null || personCode == null || calculationYear == null || calculationMonth == null) {
            return Optional.empty();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT h.*
                FROM hisbase h
                WHERE h.dwbm = :organizationCode
                  AND h.grbm = :personCode
                  AND h.jsnf = :calculationYear
                  AND h.jsyf = :calculationMonth
                ORDER BY CASE WHEN h.sid IS NULL OR TRIM(h.sid) = '' THEN 0 ELSE 1 END, h.id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("calculationYear", calculationYear)
                .addValue("calculationMonth", calculationMonth));
        return rows.isEmpty() ? Optional.empty() : Optional.of(new LinkedHashMap<>(rows.getFirst()));
    }

    Optional<PayrollPersonnelRef> findPayrollPersonnelRef(int uid) {
        return jdbcTemplate.query("""
                SELECT uid, dwbm, grbm, xm
                FROM dryjbxx
                WHERE uid = :uid
                LIMIT 1
                """, new MapSqlParameterSource("uid", uid), (rs, rowNum) -> new PayrollPersonnelRef(
                rs.getInt("uid"),
                SqlText.trim(rs.getString("dwbm")),
                SqlText.trim(rs.getString("grbm")),
                SqlText.trim(rs.getString("xm")))).stream().findFirst();
    }

    Optional<Map<String, Object>> findPredecessorHistoryValues(String id) {
        return jdbcTemplate.queryForList("""
                SELECT previous.*
                FROM hisbase current
                JOIN hisbase previous ON previous.dwbm = current.dwbm
                                      AND previous.grbm = current.grbm
                                      AND CAST(previous.id AS CHAR) <> CAST(current.id AS CHAR)
                WHERE CAST(current.id AS CHAR) = CAST(:id AS CHAR)
                  AND (
                      CAST(TRIM(previous.sid) AS CHAR) = CAST(TRIM(current.id) AS CHAR)
                      OR (
                          CONCAT(previous.jsnf, previous.jsyf) < CONCAT(current.jsnf, current.jsyf)
                          AND (previous.sid IS NULL OR TRIM(previous.sid) = '')
                      )
                  )
                ORDER BY CASE WHEN CAST(TRIM(previous.sid) AS CHAR) = CAST(TRIM(current.id) AS CHAR) THEN 0 ELSE 1 END,
                         previous.jsnf DESC, previous.jsyf DESC, previous.id DESC
                LIMIT 1
                """, new MapSqlParameterSource("id", id))
                .stream()
                .findFirst()
                .map(LinkedHashMap::new);
    }

    Map<String, Map<String, Object>> findPredecessorHistoryValuesByIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        List<String> normalizedIds = ids.stream().map(String::valueOf).map(String::trim).filter(id -> !id.isBlank()).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Map.of();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT ranked.*
                FROM (
                    SELECT CAST(current.id AS CHAR) AS current_history_id,
                           previous.*,
                           ROW_NUMBER() OVER (
                               PARTITION BY current.id
                               ORDER BY CASE
                                            WHEN CAST(TRIM(previous.sid) AS CHAR) = CAST(TRIM(current.id) AS CHAR) THEN 0
                                            ELSE 1
                                        END,
                                        previous.jsnf DESC,
                                        previous.jsyf DESC,
                                        previous.id DESC
                           ) AS predecessor_rank
                    FROM hisbase current
                    JOIN hisbase previous ON previous.dwbm = current.dwbm
                                          AND previous.grbm = current.grbm
                                          AND CAST(previous.id AS CHAR) <> CAST(current.id AS CHAR)
                    WHERE CAST(current.id AS CHAR) IN (:ids)
                      AND (
                          CAST(TRIM(previous.sid) AS CHAR) = CAST(TRIM(current.id) AS CHAR)
                          OR (
                              CONCAT(previous.jsnf, previous.jsyf) < CONCAT(current.jsnf, current.jsyf)
                              AND (previous.sid IS NULL OR TRIM(previous.sid) = '')
                          )
                      )
                ) ranked
                WHERE ranked.predecessor_rank = 1
                """, new MapSqlParameterSource("ids", normalizedIds));
        Map<String, Map<String, Object>> predecessors = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String currentId = SqlText.trim(String.valueOf(row.get("current_history_id")));
            if (currentId == null || currentId.isBlank()) {
                continue;
            }
            Map<String, Object> values = new LinkedHashMap<>(row);
            values.remove("current_history_id");
            values.remove("predecessor_rank");
            predecessors.putIfAbsent(currentId, values);
        }
        return predecessors;
    }

    Optional<String> findHistoryOrganizationCode(String id) {
        return jdbcTemplate.queryForList("""
                SELECT dwbm
                FROM hisbase
                WHERE id = :id
                """, new MapSqlParameterSource("id", id), String.class).stream().findFirst().map(SqlText::trim);
    }

    Optional<Integer> findPersonnelUidByCurrentHistoryId(String id) {
        return jdbcTemplate.queryForList("""
                SELECT p.uid
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE h.id = :id
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                """, new MapSqlParameterSource("id", id), Integer.class).stream().findFirst();
    }

    Optional<PayrollHistorySnapshot> findCurrentHistoryById(String id) {
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                       h.dwsx, dw.dfbt, h.jzgb, h.spdw, p.cjgzny, h.srny, p.gznx, p.zdgznx,
                       h.xckhndjb, h.xckhndzw, h.jhlqsny, h.zdjhlnx, h.tgbl, h.jxjtbz, h.jx,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.fddc, h.jbgzjb2, h.djc2, h.tbnd, h.jbtbz,
                       h.gwjtbz, h.gwjtlb,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.sdbt, h.blfb2,
                       h.jhljt, h.jsfszwtg2, h.jxjt, h.fdgz2, h.jjjy2, h.gwjt2, h.tgblbf,
                       h.pgbc, h.njbt, h.hj2
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE h.id = :id
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                LIMIT 1
                """, new MapSqlParameterSource("id", id), HISTORY_MAPPER).stream().findFirst();
    }

    Optional<String> findPredecessorHistoryId(String id) {
        return jdbcTemplate.queryForList("""
                SELECT id
                FROM hisbase
                WHERE sid IS NOT NULL
                  AND TRIM(sid) <> ''
                  AND (
                      sid = :id
                      OR TRIM(sid) = TRIM(:id)
                      OR REPLACE(TRIM(sid), '-', '') = REPLACE(TRIM(:id), '-', '')
                  )
                LIMIT 1
                """, new MapSqlParameterSource("id", id), String.class).stream().findFirst().map(SqlText::trim);
    }

    Optional<PositionChangeDisplayPair> findProcessedPositionChangeDisplay(
            String organizationCode,
            String personCode) {
        return jdbcTemplate.query("""
                SELECT h1.zwbm2 AS before_position_code,
                       h1.zwgw2 AS before_position_name,
                       h.zwbm2 AS after_position_code,
                       h.zwgw2 AS after_position_name
                FROM hisbase h
                LEFT JOIN hisbase h1
                  ON h1.dwbm = h.dwbm
                 AND h1.grbm = h.grbm
                 AND h1.sid = h.id
                WHERE h.dwbm = :organizationCode
                  AND h.grbm = :personCode
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                  AND TRIM(h.jslb) IN ('职务变化','职级晋升','法检套改','警员套改','警务套改','职级套改')
                ORDER BY h.jsnf DESC, h.jsyf DESC, h.id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), POSITION_CHANGE_DISPLAY_PAIR_MAPPER).stream().findFirst();
    }

    Optional<PositionChangeDisplayPair> findProcessedPositionChangeDisplayById(String id) {
        return jdbcTemplate.query("""
                SELECT h1.zwbm2 AS before_position_code,
                       h1.zwgw2 AS before_position_name,
                       h.zwbm2 AS after_position_code,
                       h.zwgw2 AS after_position_name
                FROM hisbase h
                LEFT JOIN hisbase h1
                  ON h1.dwbm = h.dwbm
                 AND h1.grbm = h.grbm
                 AND h1.sid = h.id
                WHERE h.id = :id
                LIMIT 1
                """, new MapSqlParameterSource("id", id), POSITION_CHANGE_DISPLAY_PAIR_MAPPER).stream().findFirst();
    }

    Optional<PayrollHistorySnapshot> findPayrollHistoryById(String id) {
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                       h.dwsx, dw.dfbt, h.jzgb, h.spdw, p.cjgzny, h.srny, p.gznx, p.zdgznx,
                       h.xckhndjb, h.xckhndzw, h.jhlqsny, h.zdjhlnx, h.tgbl, h.jxjtbz, h.jx,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.fddc, h.jbgzjb2, h.djc2, h.tbnd, h.jbtbz,
                       h.gwjtbz, h.gwjtlb,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.sdbt, h.blfb2,
                       h.jhljt, h.jsfszwtg2, h.jxjt, h.fdgz2, h.jjjy2, h.gwjt2, h.tgblbf,
                       h.pgbc, h.njbt, h.hj2
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE CAST(TRIM(h.id) AS CHAR) = CAST(TRIM(:id) AS CHAR)
                LIMIT 1
                """, new MapSqlParameterSource("id", id), HISTORY_MAPPER).stream().findFirst();
    }

    Optional<PayrollHistorySnapshot> findPositionChangePredecessor(String currentHistoryId) {
        Optional<PayrollHistorySnapshot> linkedBySid = findPredecessorHistoryId(currentHistoryId)
                .flatMap(this::findPayrollHistoryById);
        if (linkedBySid.isPresent()) {
            return linkedBySid;
        }
        Optional<PayrollHistorySnapshot> linkedPredecessor = jdbcTemplate.query("""
                SELECT h.id, h.dwbm, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                       h.dwsx, dw.dfbt, h.jzgb, h.spdw, p.cjgzny, h.srny, p.gznx, p.zdgznx,
                       h.xckhndjb, h.xckhndzw, h.jhlqsny, h.zdjhlnx, h.tgbl, h.jxjtbz, h.jx,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.fddc, h.jbgzjb2, h.djc2, h.tbnd, h.jbtbz,
                       h.gwjtbz, h.gwjtlb,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.sdbt, h.blfb2,
                       h.jhljt, h.jsfszwtg2, h.jxjt, h.fdgz2, h.jjjy2, h.gwjt2, h.tgblbf,
                       h.pgbc, h.njbt, h.hj2
                FROM hisbase current
                JOIN hisbase h
                  ON h.dwbm = current.dwbm
                 AND h.grbm = current.grbm
                 AND CAST(TRIM(h.sid) AS CHAR) = CAST(TRIM(current.id) AS CHAR)
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE CAST(TRIM(current.id) AS CHAR) = CAST(TRIM(:id) AS CHAR)
                LIMIT 1
                """, new MapSqlParameterSource("id", currentHistoryId), HISTORY_MAPPER).stream().findFirst();
        if (linkedPredecessor.isPresent()) {
            return linkedPredecessor;
        }
        Optional<PayrollHistorySnapshot> reverseLinkedPredecessor = jdbcTemplate.query("""
                SELECT h.id, h.dwbm, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                       h.dwsx, dw.dfbt, h.jzgb, h.spdw, p.cjgzny, h.srny, p.gznx, p.zdgznx,
                       h.xckhndjb, h.xckhndzw, h.jhlqsny, h.zdjhlnx, h.tgbl, h.jxjtbz, h.jx,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.fddc, h.jbgzjb2, h.djc2, h.tbnd, h.jbtbz,
                       h.gwjtbz, h.gwjtlb,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.sdbt, h.blfb2,
                       h.jhljt, h.jsfszwtg2, h.jxjt, h.fdgz2, h.jjjy2, h.gwjt2, h.tgblbf,
                       h.pgbc, h.njbt, h.hj2
                FROM hisbase current
                JOIN hisbase h
                  ON h.dwbm = current.dwbm
                 AND h.grbm = current.grbm
                 AND CAST(TRIM(current.sid) AS CHAR) = CAST(TRIM(h.id) AS CHAR)
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE CAST(TRIM(current.id) AS CHAR) = CAST(TRIM(:id) AS CHAR)
                LIMIT 1
                """, new MapSqlParameterSource("id", currentHistoryId), HISTORY_MAPPER).stream().findFirst();
        return reverseLinkedPredecessor;
    }

    Optional<PositionChangeCandidate> findPreviousDistinctAppointment(
            String organizationCode,
            String personCode,
            String excludePositionCode,
            String beforeOrAtPeriod) {
        String normalizedPeriod = beforeOrAtPeriod == null ? "" : beforeOrAtPeriod.replace(".", "");
        return jdbcTemplate.query("""
                SELECT zwbm, xzzw, srny
                FROM dryzwbh
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND COALESCE(zwbm, '') <> COALESCE(:excludePositionCode, '')
                  AND REPLACE(srny, '.', '') <= :period
                ORDER BY srny DESC, id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("excludePositionCode", emptyToNull(excludePositionCode))
                .addValue("period", normalizedPeriod), POSITION_CHANGE_CANDIDATE_MAPPER).stream().findFirst();
    }

    String createPayrollHistoryFromLatest(int uid, PayrollHistoryMaintenanceRequest request) {
        PayrollHistorySnapshot latest = findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        String id = java.util.UUID.randomUUID().toString().toUpperCase();
        jdbcTemplate.update("""
                INSERT INTO hisbase
                SELECT :id, h.dwbm, h.grbm, h.xm, h.ryfl, h.dwsx, h.gwfl, h.jrny, h.jrfs,
                       h.zdgznx, h.gznx, h.jhlqsny, h.zdjhlnx, h.xlbm, h.zgxl, h.bjglxlnx,
                       h.tc, h.xckhndzw, h.xckhndjb, h.bgdwjc, h.zwjb, h.zjbm, h.xrzw, h.srny,
                       h.jx, h.tgbl, h.jtbl, h.fddc, h.fdgd, h.fdsj,
                       :calculationYear, :calculationMonth, :changeType,
                       h.khqk, h.dynkh, h.denkh, h.bbz, :totalAmount,
                       :positionCode, :positionName, h.zwgzdc2, :positionSalary, h.jbgzjb2, h.djc2, :gradeSalary,
                       h.jcgz2, h.glgz2, :technicalGradeSalary, h.grjj2, :retainedAllowance, h.jsfszwtg2,
                       h.jt2, h.fdgz2, h.jjjy2, :performanceAllowance, h.gwjt2, h.bh, h.jxgz, h.zzbc,
                       h.zwjt, h.zfbt, h.dsznf, h.nzgwsf, h.jzmcbt, h.sdbt, h.grsds, h.zfgjj,
                       h.ylbxf, h.ylf, h.qtdk, h.bfyqgz, h.kjyqgz, h.sfgz, h.qtbt, h.jxjt,
                       h.gryhzh, h.tfnf, h.tfyf, h.spdw, h.tbnd, h.jxjtbz, h.jbtbz, h.jhljt,
                       h.pgbc, h.sidbt, h.jzgb, h.nrjxgzbf, h.tgblbf, h.jcjtbz, h.spjtbz, h.njbt,
                       h.gwjtbz, h.gwjtlb, h.sfjzgb, ''
                FROM hisbase h
                WHERE h.id = :sourceId
                """, payrollHistoryRequestParameters(request)
                .addValue("id", id)
                .addValue("sourceId", latest.id()));
        jdbcTemplate.update("""
                UPDATE hisbase
                SET sid = :newId
                WHERE id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("newId", id)
                .addValue("sourceId", latest.id()));
        markAppCreated("hisbase", id);
        return id;
    }

    String createPromotionHistoryFromLatest(int uid, PromotionHistoryMutation mutation) {
        PayrollHistorySnapshot latest = findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        String id = java.util.UUID.randomUUID().toString().toUpperCase();
        jdbcTemplate.update("""
                INSERT INTO hisbase
                SELECT :id, h.dwbm, h.grbm, h.xm, h.ryfl, h.dwsx, h.gwfl, h.jrny, h.jrfs,
                       h.zdgznx, h.gznx, h.jhlqsny, h.zdjhlnx, h.xlbm, h.zgxl, h.bjglxlnx,
                       h.tc, :nextStepAssessmentStartYear, :nextLevelAssessmentStartYear, h.bgdwjc, h.zwjb, h.zjbm, h.xrzw, h.srny,
                       h.jx, h.tgbl, h.jtbl, h.fddc, h.fdgd, h.fdsj,
                       :calculationYear, :calculationMonth, :changeType,
                       h.khqk, h.dynkh, h.denkh, h.bbz, :totalAmount,
                       h.zwbm2, h.zwgw2, :positionSalaryGrade, h.zwgzse2, :gradeSalaryLevel, :gradeSalaryStep, :gradeSalary,
                       h.jcgz2, h.glgz2, h.jsdjgz2, h.grjj2, h.blfb2, h.jsfszwtg2,
                       h.jt2, h.fdgz2, h.jjjy2, h.dfbt2, h.gwjt2, h.bh, h.jxgz, h.zzbc,
                       h.zwjt, h.zfbt, h.dsznf, h.nzgwsf, h.jzmcbt, h.sdbt, h.grsds, h.zfgjj,
                       h.ylbxf, h.ylf, h.qtdk, h.bfyqgz, h.kjyqgz, h.sfgz, h.qtbt, h.jxjt,
                       h.gryhzh, h.tfnf, h.tfyf, h.spdw, h.tbnd, h.jxjtbz, h.jbtbz, h.jhljt,
                       h.pgbc, h.sidbt, h.jzgb, h.nrjxgzbf, h.tgblbf, h.jcjtbz, h.spjtbz, h.njbt,
                       h.gwjtbz, h.gwjtlb, h.sfjzgb, ''
                FROM hisbase h
                WHERE h.id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("sourceId", latest.id())
                .addValue("calculationYear", mutation.calculationYear())
                .addValue("calculationMonth", mutation.calculationMonth())
                .addValue("changeType", mutation.changeType())
                .addValue("nextStepAssessmentStartYear", valueOrBlank(mutation.nextStepAssessmentStartYear()))
                .addValue("nextLevelAssessmentStartYear", valueOrBlank(mutation.nextLevelAssessmentStartYear()))
                .addValue("positionSalaryGrade", valueOrBlank(mutation.positionSalaryGrade()))
                .addValue("gradeSalaryLevel", valueOrBlank(mutation.gradeSalaryLevel()))
                .addValue("gradeSalaryStep", valueOrBlank(mutation.gradeSalaryStep()))
                .addValue("gradeSalary", mutation.gradeSalary() == null ? 0 : mutation.gradeSalary())
                .addValue("totalAmount", mutation.totalAmount() == null ? 0 : mutation.totalAmount()));
        jdbcTemplate.update("""
                UPDATE hisbase
                SET sid = :newId
                WHERE id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("newId", id)
                .addValue("sourceId", latest.id()));
        markAppCreated("hisbase", id);
        return id;
    }

    String createPositionChangeHistoryFromLatest(int uid, PositionChangeHistoryMutation mutation) {
        PayrollHistorySnapshot latest = findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        String id = java.util.UUID.randomUUID().toString().toUpperCase();
        jdbcTemplate.update("""
                INSERT INTO hisbase
                SELECT :id, h.dwbm, h.grbm, h.xm, h.ryfl, h.dwsx, h.gwfl, h.jrny, h.jrfs,
                       h.zdgznx, h.gznx, h.jhlqsny, h.zdjhlnx, h.xlbm, h.zgxl, h.bjglxlnx,
                       h.tc, :nextStepAssessmentStartYear, :nextLevelAssessmentStartYear, h.bgdwjc, h.zwjb, h.zjbm, h.xrzw, h.srny,
                       h.jx, h.tgbl, h.jtbl, h.fddc, h.fdgd, h.fdsj,
                       :calculationYear, :calculationMonth, :changeType,
                       h.khqk, h.dynkh, h.denkh, h.bbz, :totalAmount,
                       :positionCode, :positionName, :positionSalaryGrade, :positionSalary, :gradeSalaryLevel, :gradeSalaryStep, :gradeSalary,
                       h.jcgz2, h.glgz2, COALESCE(:technicalGradeSalary, h.jsdjgz2), h.grjj2, COALESCE(:retainedAllowance, h.blfb2), COALESCE(:salaryIncrease, h.jsfszwtg2),
                       h.jt2, h.fdgz2, h.jjjy2, COALESCE(:performanceAllowance, h.dfbt2), h.gwjt2, h.bh, h.jxgz, h.zzbc,
                       h.zwjt, h.zfbt, h.dsznf, h.nzgwsf, h.jzmcbt, COALESCE(:subsidyAllowance, h.sdbt), h.grsds, h.zfgjj,
                       h.ylbxf, h.ylf, h.qtdk, h.bfyqgz, h.kjyqgz, h.sfgz, h.qtbt, h.jxjt,
                       h.gryhzh, h.tfnf, h.tfyf, h.spdw, h.tbnd, h.jxjtbz, h.jbtbz, h.jhljt,
                       :pgbc, h.sidbt, h.jzgb, h.nrjxgzbf, h.tgblbf, h.jcjtbz, h.spjtbz, h.njbt,
                       h.gwjtbz, h.gwjtlb, h.sfjzgb, ''
                FROM hisbase h
                WHERE h.id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("sourceId", latest.id())
                .addValue("calculationYear", mutation.calculationYear())
                .addValue("calculationMonth", mutation.calculationMonth())
                .addValue("changeType", valueOrBlank(mutation.changeType()))
                .addValue("nextStepAssessmentStartYear", valueOrBlank(mutation.nextStepAssessmentStartYear()))
                .addValue("nextLevelAssessmentStartYear", valueOrBlank(mutation.nextLevelAssessmentStartYear()))
                .addValue("positionCode", valueOrBlank(mutation.positionCode()))
                .addValue("positionName", valueOrBlank(mutation.positionName()))
                .addValue("positionSalary", mutation.positionSalary() == null ? 0 : mutation.positionSalary())
                .addValue("positionSalaryGrade", valueOrBlank(mutation.positionSalaryGrade()))
                .addValue("gradeSalaryLevel", valueOrBlank(mutation.gradeSalaryLevel()))
                .addValue("gradeSalaryStep", valueOrBlank(mutation.gradeSalaryStep()))
                .addValue("gradeSalary", mutation.gradeSalary() == null ? 0 : mutation.gradeSalary())
                .addValue("technicalGradeSalary", mutation.technicalGradeSalary())
                .addValue("performanceAllowance", mutation.performanceAllowance())
                .addValue("subsidyAllowance", mutation.subsidyAllowance())
                .addValue("retainedAllowance", mutation.retainedAllowance())
                .addValue("salaryIncrease", mutation.salaryIncrease())
                .addValue("pgbc", mutation.pgbc() == null ? 0 : mutation.pgbc())
                .addValue("totalAmount", mutation.totalAmount() == null ? 0 : mutation.totalAmount()));
        jdbcTemplate.update("""
                UPDATE hisbase
                SET sid = :newId
                WHERE id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("newId", id)
                .addValue("sourceId", latest.id()));
        markAppCreated("hisbase", id);
        return id;
    }

    void syncPositionChangeMetadata(
            String historyId,
            String positionStartYearMonth,
            String positionCode,
            String positionName) {
        jdbcTemplate.update("""
                UPDATE hisbase
                SET srny = :positionStartYearMonth,
                    zjbm = :positionCode,
                    xrzw = :positionName
                WHERE id = :historyId
                """, new MapSqlParameterSource()
                .addValue("historyId", historyId)
                .addValue("positionStartYearMonth", valueOrBlank(positionStartYearMonth))
                .addValue("positionCode", valueOrBlank(positionCode))
                .addValue("positionName", valueOrBlank(positionName)));
    }

    String createTeachingAllowanceHistoryFromLatest(int uid, TeachingAllowanceHistoryMutation mutation) {
        PayrollHistorySnapshot latest = findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        String id = java.util.UUID.randomUUID().toString().toUpperCase();
        jdbcTemplate.update("""
                INSERT INTO hisbase
                SELECT :id, h.dwbm, h.grbm, h.xm, h.ryfl, h.dwsx, h.gwfl, h.jrny, h.jrfs,
                       h.zdgznx, h.gznx, h.jhlqsny, h.zdjhlnx, h.xlbm, h.zgxl, h.bjglxlnx,
                       h.tc, h.xckhndzw, h.xckhndjb, h.bgdwjc, h.zwjb, h.zjbm, h.xrzw, h.srny,
                       h.jx, h.tgbl, h.jtbl, h.fddc, h.fdgd, h.fdsj,
                       :calculationYear, :calculationMonth, :changeType,
                       h.khqk, h.dynkh, h.denkh, h.bbz, :totalAmount,
                       h.zwbm2, h.zwgw2, h.zwgzse2, h.zwgzse2, h.jbgzse2, h.jbgzse2, h.jbgzse2,
                       h.jcgz2, h.glgz2, h.jsdjgz2, h.grjj2, h.blfb2, h.jsfszwtg2,
                       h.jt2, h.fdgz2, h.jjjy2, h.dfbt2, h.gwjt2, h.bh, h.jxgz, h.zzbc,
                       h.zwjt, h.zfbt, h.dsznf, h.nzgwsf, h.jzmcbt, h.sdbt, h.grsds, h.zfgjj,
                       h.ylbxf, h.ylf, h.qtdk, h.bfyqgz, h.kjyqgz, h.sfgz, h.qtbt, h.jxjt,
                       h.gryhzh, h.tfnf, h.tfyf, h.spdw, h.tbnd, h.jxjtbz, h.jbtbz, :teachingAllowance,
                       h.pgbc, h.sidbt, h.jzgb, h.nrjxgzbf, h.tgblbf, h.jcjtbz, h.spjtbz, h.njbt,
                       h.gwjtbz, h.gwjtlb, h.sfjzgb, ''
                FROM hisbase h
                WHERE h.id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("sourceId", latest.id())
                .addValue("calculationYear", mutation.calculationYear())
                .addValue("calculationMonth", mutation.calculationMonth())
                .addValue("changeType", mutation.changeType())
                .addValue("teachingAllowance", mutation.teachingAllowance() == null ? 0 : mutation.teachingAllowance())
                .addValue("totalAmount", mutation.totalAmount() == null ? 0 : mutation.totalAmount()));
        jdbcTemplate.update("""
                UPDATE hisbase
                SET sid = :newId
                WHERE id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("newId", id)
                .addValue("sourceId", latest.id()));
        markAppCreated("hisbase", id);
        return id;
    }

    int insertAllowanceStandard(AllowanceStandardRequest request) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("standardYearMonth", request.standardYearMonth())
                .addValue("item", request.item())
                .addValue("positionCode", request.positionCode())
                .addValue("name", request.name())
                .addValue("workYearsLower", request.workYearsLower() == null ? 0 : request.workYearsLower())
                .addValue("workYearsUpper", request.workYearsUpper() == null ? 0 : request.workYearsUpper())
                .addValue("amount", request.amount() == null ? 0 : request.amount())
                .addValue("performanceCategory", request.performanceCategory() == null ? 0 : request.performanceCategory());
        jdbcTemplate.update("""
                INSERT INTO bz06_jbt (tbnd, item, zwbm, mc, worklower, workupper, bz, jxlb)
                VALUES (:standardYearMonth, :item, :positionCode, :name, :workYearsLower, :workYearsUpper, :amount, :performanceCategory)
                """, parameters);
        Integer id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Integer.class);
        return id == null ? 0 : id;
    }

    void updateAllowanceStandard(int id, AllowanceStandardRequest request) {
        jdbcTemplate.update("""
                UPDATE bz06_jbt
                SET tbnd = :standardYearMonth,
                    item = :item,
                    zwbm = :positionCode,
                    mc = :name,
                    worklower = :workYearsLower,
                    workupper = :workYearsUpper,
                    bz = :amount,
                    jxlb = :performanceCategory
                WHERE id = :id
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("standardYearMonth", request.standardYearMonth())
                .addValue("item", request.item())
                .addValue("positionCode", request.positionCode())
                .addValue("name", request.name())
                .addValue("workYearsLower", request.workYearsLower() == null ? 0 : request.workYearsLower())
                .addValue("workYearsUpper", request.workYearsUpper() == null ? 0 : request.workYearsUpper())
                .addValue("amount", request.amount() == null ? 0 : request.amount())
                .addValue("performanceCategory", request.performanceCategory() == null ? 0 : request.performanceCategory()));
    }

    void deleteAllowanceStandard(int id) {
        jdbcTemplate.update("DELETE FROM bz06_jbt WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    AllowanceStandard findAllowanceStandardById(int id) {
        List<AllowanceStandard> rows = jdbcTemplate.query("""
                SELECT id, tbnd, item, zwbm, mc, worklower, workupper, bz, jxlb
                FROM bz06_jbt WHERE id = :id LIMIT 1
                """, new MapSqlParameterSource("id", id), ALLOWANCE_STANDARD_MAPPER);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    int insertRankAllowanceStandard(RankAllowanceStandardRequest request) {
        jdbcTemplate.update("""
                INSERT INTO jxjtbz (tbnd, jxbm, jx, jtbz, lb)
                VALUES (:standardYearMonth, :rankCode, :rankName, :amount, :category)
                """, rankAllowanceStandardParameters(request));
        Integer id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Integer.class);
        return id == null ? 0 : id;
    }

    void updateRankAllowanceStandard(int id, RankAllowanceStandardRequest request) {
        jdbcTemplate.update("""
                UPDATE jxjtbz
                SET tbnd = :standardYearMonth,
                    jxbm = :rankCode,
                    jx = :rankName,
                    jtbz = :amount,
                    lb = :category
                WHERE id = :id
                """, rankAllowanceStandardParameters(request).addValue("id", id));
    }

    void deleteRankAllowanceStandard(int id) {
        jdbcTemplate.update("DELETE FROM jxjtbz WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    RankAllowanceStandard findRankAllowanceStandardById(int id) {
        List<RankAllowanceStandard> rows = jdbcTemplate.query("""
                SELECT id, tbnd, jxbm, jx, jtbz, lb
                FROM jxjtbz WHERE id = :id LIMIT 1
                """, new MapSqlParameterSource("id", id), RANK_ALLOWANCE_STANDARD_MAPPER);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void insertRetainedAllowanceStandard(RetainedAllowanceStandardRequest request) {
        jdbcTemplate.update("""
                INSERT INTO bz06_blfb (zwbm, mc, bz)
                VALUES (:positionCode, :name, :amount)
                """, retainedAllowanceStandardParameters(request));
    }

    void updateRetainedAllowanceStandard(String positionCode, RetainedAllowanceStandardRequest request) {
        jdbcTemplate.update("""
                UPDATE bz06_blfb
                SET mc = :name,
                    bz = :amount
                WHERE zwbm = :positionCode
                """, new MapSqlParameterSource()
                .addValue("positionCode", positionCode)
                .addValue("name", request.name())
                .addValue("amount", request.amount() == null ? 0 : request.amount()));
    }

    void deleteRetainedAllowanceStandard(String positionCode) {
        jdbcTemplate.update("DELETE FROM bz06_blfb WHERE zwbm = :positionCode",
                new MapSqlParameterSource("positionCode", positionCode));
    }

    RetainedAllowanceStandard findRetainedAllowanceStandardByPositionCode(String positionCode) {
        List<RetainedAllowanceStandard> rows = jdbcTemplate.query("""
                SELECT zwbm, mc, bz
                FROM bz06_blfb WHERE zwbm = :positionCode LIMIT 1
                """, new MapSqlParameterSource("positionCode", positionCode), RETAINED_ALLOWANCE_STANDARD_MAPPER);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void insertYearAllowanceStandard(YearAllowanceStandardRequest request) {
        jdbcTemplate.update("""
                INSERT INTO njbt (tbnd, a1, a2, a3, a4)
                VALUES (:standardYearMonth, :categoryOneAmount, :categoryTwoAmount, :categoryThreeAmount, :categoryFourAmount)
                """, yearAllowanceStandardParameters(request));
    }

    void updateYearAllowanceStandard(String standardYearMonth, YearAllowanceStandardRequest request) {
        jdbcTemplate.update("""
                UPDATE njbt
                SET a1 = :categoryOneAmount,
                    a2 = :categoryTwoAmount,
                    a3 = :categoryThreeAmount,
                    a4 = :categoryFourAmount
                WHERE tbnd = :standardYearMonth
                """, yearAllowanceStandardParameters(request).addValue("standardYearMonth", standardYearMonth));
    }

    void deleteYearAllowanceStandard(String standardYearMonth) {
        jdbcTemplate.update("DELETE FROM njbt WHERE tbnd = :standardYearMonth",
                new MapSqlParameterSource("standardYearMonth", standardYearMonth));
    }

    YearAllowanceStandard findYearAllowanceStandardByYearMonth(String standardYearMonth) {
        List<YearAllowanceStandard> rows = jdbcTemplate.query("""
                SELECT tbnd, a1, a2, a3, a4
                FROM njbt WHERE tbnd = :standardYearMonth LIMIT 1
                """, new MapSqlParameterSource("standardYearMonth", standardYearMonth), YEAR_ALLOWANCE_STANDARD_MAPPER);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void insertPositionSalaryStandard(PositionSalaryStandardRequest request) {
        jdbcTemplate.update("""
                INSERT INTO bz06_zwgz (tbnd, zwbm, bz)
                VALUES (:standardYearMonth, :positionCode, :amount)
                """, positionSalaryStandardParameters(request));
    }

    void updatePositionSalaryStandard(String standardYearMonth, String positionCode, PositionSalaryStandardRequest request) {
        jdbcTemplate.update("""
                UPDATE bz06_zwgz
                SET bz = :amount
                WHERE tbnd = :standardYearMonth AND zwbm = :positionCode
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("positionCode", positionCode)
                .addValue("amount", request.amount() == null ? 0 : request.amount()));
    }

    void deletePositionSalaryStandard(String standardYearMonth, String positionCode) {
        jdbcTemplate.update("""
                DELETE FROM bz06_zwgz
                WHERE tbnd = :standardYearMonth AND zwbm = :positionCode
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("positionCode", positionCode));
    }

    PositionSalaryStandard findPositionSalaryStandard(String standardYearMonth, String positionCode) {
        List<PositionSalaryStandard> rows = jdbcTemplate.query("""
                SELECT tbnd, zwbm, bz
                FROM bz06_zwgz
                WHERE tbnd = :standardYearMonth AND zwbm = :positionCode
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("positionCode", positionCode), (rs, rowNum) -> new PositionSalaryStandard(
                rs.getString("tbnd"),
                rs.getString("zwbm"),
                rs.getInt("bz")));
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void insertGradeSalaryStandard(GradeSalaryStandardRequest request) {
        jdbcTemplate.update("""
                INSERT INTO bz06_jbgz (tbnd, jb, dc1, dc2, dc3, dc4, dc5, dc6, dc7, dc8, dc9, dc10,
                    dc11, dc12, dc13, dc14, dc15, dc16, dc17, dc18, dc19, dc20)
                VALUES (:standardYearMonth, :gradeLevel,
                    :dc1, :dc2, :dc3, :dc4, :dc5, :dc6, :dc7, :dc8, :dc9, :dc10,
                    :dc11, :dc12, :dc13, :dc14, :dc15, :dc16, :dc17, :dc18, :dc19, :dc20)
                """, gradeSalaryStandardParameters(request.standardYearMonth(), request.gradeLevel(), request.gradeSteps()));
    }

    void updateGradeSalaryStandard(String standardYearMonth, String gradeLevel, GradeSalaryStandardRequest request) {
        jdbcTemplate.update("""
                UPDATE bz06_jbgz
                SET dc1 = :dc1, dc2 = :dc2, dc3 = :dc3, dc4 = :dc4, dc5 = :dc5,
                    dc6 = :dc6, dc7 = :dc7, dc8 = :dc8, dc9 = :dc9, dc10 = :dc10,
                    dc11 = :dc11, dc12 = :dc12, dc13 = :dc13, dc14 = :dc14, dc15 = :dc15,
                    dc16 = :dc16, dc17 = :dc17, dc18 = :dc18, dc19 = :dc19, dc20 = :dc20
                WHERE tbnd = :standardYearMonth AND jb = :gradeLevel
                """, gradeSalaryStandardParameters(standardYearMonth, gradeLevel, request.gradeSteps()));
    }

    void deleteGradeSalaryStandard(String standardYearMonth, String gradeLevel) {
        jdbcTemplate.update("""
                DELETE FROM bz06_jbgz
                WHERE tbnd = :standardYearMonth AND jb = :gradeLevel
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("gradeLevel", gradeLevel));
    }

    GradeSalaryStandard findGradeSalaryStandard(String standardYearMonth, String gradeLevel) {
        List<GradeSalaryStandard> rows = jdbcTemplate.query("""
                SELECT tbnd, jb, dc1, dc2, dc3, dc4, dc5, dc6, dc7, dc8, dc9, dc10,
                       dc11, dc12, dc13, dc14, dc15, dc16, dc17, dc18, dc19, dc20
                FROM bz06_jbgz
                WHERE tbnd = :standardYearMonth AND jb = :gradeLevel
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("gradeLevel", gradeLevel), (rs, rowNum) -> new GradeSalaryStandard(
                rs.getString("tbnd"),
                rs.getString("jb"),
                readGradeSteps(rs)));
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void insertPositionGradeSalaryStandard(PositionGradeSalaryStandardRequest request) {
        MapSqlParameterSource parameters = positionGradeSalaryStandardParameters(
                request.standardYearMonth(),
                request.positionCode(),
                request.technicalGradeSalary(),
                request.gradeSteps());
        jdbcTemplate.update("""
                INSERT INTO bz06_zwgz_gr (tbnd, zwbm, dc1, dc2, dc3, dc4, dc5, dc6, dc7, dc8, dc9, dc10,
                    dc11, dc12, dc13, dc14, dc15, dc16, dc17, dc18, dc19, dc20, jsdjgz)
                VALUES (:standardYearMonth, :positionCode,
                    :dc1, :dc2, :dc3, :dc4, :dc5, :dc6, :dc7, :dc8, :dc9, :dc10,
                    :dc11, :dc12, :dc13, :dc14, :dc15, :dc16, :dc17, :dc18, :dc19, :dc20, :technicalGradeSalary)
                """, parameters);
    }

    void updatePositionGradeSalaryStandard(
            String standardYearMonth,
            String positionCode,
            PositionGradeSalaryStandardRequest request) {
        jdbcTemplate.update("""
                UPDATE bz06_zwgz_gr
                SET dc1 = :dc1, dc2 = :dc2, dc3 = :dc3, dc4 = :dc4, dc5 = :dc5,
                    dc6 = :dc6, dc7 = :dc7, dc8 = :dc8, dc9 = :dc9, dc10 = :dc10,
                    dc11 = :dc11, dc12 = :dc12, dc13 = :dc13, dc14 = :dc14, dc15 = :dc15,
                    dc16 = :dc16, dc17 = :dc17, dc18 = :dc18, dc19 = :dc19, dc20 = :dc20,
                    jsdjgz = :technicalGradeSalary
                WHERE tbnd = :standardYearMonth AND zwbm = :positionCode
                """, positionGradeSalaryStandardParameters(
                standardYearMonth,
                positionCode,
                request.technicalGradeSalary(),
                request.gradeSteps()));
    }

    void deletePositionGradeSalaryStandard(String standardYearMonth, String positionCode) {
        jdbcTemplate.update("""
                DELETE FROM bz06_zwgz_gr
                WHERE tbnd = :standardYearMonth AND zwbm = :positionCode
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("positionCode", positionCode));
    }

    PositionGradeSalaryStandard findPositionGradeSalaryStandard(String standardYearMonth, String positionCode) {
        List<PositionGradeSalaryStandard> rows = jdbcTemplate.query("""
                SELECT tbnd, zwbm, jsdjgz, dc1, dc2, dc3, dc4, dc5, dc6, dc7, dc8, dc9, dc10,
                       dc11, dc12, dc13, dc14, dc15, dc16, dc17, dc18, dc19, dc20
                FROM bz06_zwgz_gr
                WHERE tbnd = :standardYearMonth AND zwbm = :positionCode
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("positionCode", positionCode), (rs, rowNum) -> new PositionGradeSalaryStandard(
                rs.getString("tbnd"),
                rs.getString("zwbm"),
                rs.getInt("jsdjgz"),
                readGradeSteps(rs)));
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void insertSalaryLevelStandard(SalaryLevelStandardRequest request) {
        jdbcTemplate.update("""
                INSERT INTO bz06_xjgz (tbnd, gwflbm, xj, bz, jc, jce)
                VALUES (:standardYearMonth, :jobCategoryCode, :salaryLevel, :amount, :baseAmount, :baseAmountExtra)
                """, salaryLevelStandardParameters(request));
    }

    void updateSalaryLevelStandard(
            String standardYearMonth,
            String jobCategoryCode,
            String salaryLevel,
            SalaryLevelStandardRequest request) {
        jdbcTemplate.update("""
                UPDATE bz06_xjgz
                SET bz = :amount,
                    jc = :baseAmount,
                    jce = :baseAmountExtra
                WHERE tbnd = :standardYearMonth AND gwflbm = :jobCategoryCode AND xj = :salaryLevel
                """, salaryLevelStandardParameters(new SalaryLevelStandardRequest(
                standardYearMonth,
                jobCategoryCode,
                salaryLevel,
                request.amount(),
                request.baseAmount(),
                request.baseAmountExtra())));
    }

    void deleteSalaryLevelStandard(String standardYearMonth, String jobCategoryCode, String salaryLevel) {
        jdbcTemplate.update("""
                DELETE FROM bz06_xjgz
                WHERE tbnd = :standardYearMonth AND gwflbm = :jobCategoryCode AND xj = :salaryLevel
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("jobCategoryCode", jobCategoryCode)
                .addValue("salaryLevel", salaryLevel));
    }

    SalaryLevelStandard findSalaryLevelStandard(String standardYearMonth, String jobCategoryCode, String salaryLevel) {
        List<SalaryLevelStandard> rows = jdbcTemplate.query("""
                SELECT tbnd, gwflbm, xj, bz, jc, jce
                FROM bz06_xjgz
                WHERE tbnd = :standardYearMonth AND gwflbm = :jobCategoryCode AND xj = :salaryLevel
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("jobCategoryCode", jobCategoryCode)
                .addValue("salaryLevel", salaryLevel), (rs, rowNum) -> new SalaryLevelStandard(
                rs.getString("tbnd"),
                rs.getString("gwflbm"),
                rs.getString("xj"),
                rs.getInt("bz"),
                rs.getInt("jc"),
                rs.getInt("jce")));
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void invalidateGradeSalaryRowCache(String standardYearMonth, String gradeLevel) {
        if (emptyToNull(standardYearMonth) == null || emptyToNull(gradeLevel) == null) {
            gradeSalaryRowCache.clear();
            return;
        }
        gradeSalaryRowCache.remove(standardYearMonth + "|" + intValue(gradeLevel));
    }

    void insertInternSalaryStandard(InternSalaryStandardRequest request) {
        jdbcTemplate.update("""
                INSERT INTO bz06_zzdz (tbnd, xlbm, xlmc, zzzwbm, zzzwmc, zzdc, zzjb, gz1, gz2)
                VALUES (:standardYearMonth, :educationCode, :educationName, :regularPositionCode,
                    :regularPositionName, :regularGradeStep, :regularLevel, :firstYearAmount, :secondYearAmount)
                """, internSalaryStandardParameters(request));
    }

    void updateInternSalaryStandard(
            String standardYearMonth,
            String educationCode,
            String regularPositionCode,
            InternSalaryStandardRequest request) {
        jdbcTemplate.update("""
                UPDATE bz06_zzdz
                SET xlmc = :educationName,
                    zzzwmc = :regularPositionName,
                    zzdc = :regularGradeStep,
                    zzjb = :regularLevel,
                    gz1 = :firstYearAmount,
                    gz2 = :secondYearAmount
                WHERE tbnd = :standardYearMonth AND xlbm = :educationCode AND zzzwbm = :regularPositionCode
                """, internSalaryStandardParameters(request)
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("educationCode", educationCode)
                .addValue("regularPositionCode", regularPositionCode));
    }

    void deleteInternSalaryStandard(String standardYearMonth, String educationCode, String regularPositionCode) {
        jdbcTemplate.update("""
                DELETE FROM bz06_zzdz
                WHERE tbnd = :standardYearMonth AND xlbm = :educationCode AND zzzwbm = :regularPositionCode
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("educationCode", educationCode)
                .addValue("regularPositionCode", regularPositionCode));
    }

    InternSalaryStandard findInternSalaryStandardByKey(
            String standardYearMonth,
            String educationCode,
            String regularPositionCode) {
        List<InternSalaryStandard> rows = jdbcTemplate.query("""
                SELECT tbnd, xlbm, xlmc, zzzwbm, zzzwmc, zzdc, zzjb, gz1, gz2
                FROM bz06_zzdz
                WHERE tbnd = :standardYearMonth AND xlbm = :educationCode AND zzzwbm = :regularPositionCode
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("educationCode", educationCode)
                .addValue("regularPositionCode", regularPositionCode), INTERN_SALARY_STANDARD_MAPPER);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void insertWageReformStandard(WageReformStandardRequest request) {
        jdbcTemplate.update("""
                INSERT INTO bz06_tgb (zwbm, rzns, rznz, tgns, tgnz, jb, dc)
                VALUES (:positionCode, :appointmentYearsLower, :appointmentYearsUpper,
                    :reformYearsLower, :reformYearsUpper, :convertedLevel, :convertedStep)
                """, wageReformStandardParameters(request));
    }

    void updateWageReformStandard(
            String positionCode,
            int appointmentYearsLower,
            int appointmentYearsUpper,
            int reformYearsLower,
            int reformYearsUpper,
            WageReformStandardRequest request) {
        jdbcTemplate.update("""
                UPDATE bz06_tgb
                SET jb = :convertedLevel,
                    dc = :convertedStep
                WHERE zwbm = :positionCode
                  AND rzns = :appointmentYearsLower
                  AND rznz = :appointmentYearsUpper
                  AND tgns = :reformYearsLower
                  AND tgnz = :reformYearsUpper
                """, wageReformStandardParameters(request)
                .addValue("positionCode", positionCode)
                .addValue("appointmentYearsLower", appointmentYearsLower)
                .addValue("appointmentYearsUpper", appointmentYearsUpper)
                .addValue("reformYearsLower", reformYearsLower)
                .addValue("reformYearsUpper", reformYearsUpper));
    }

    void deleteWageReformStandard(
            String positionCode,
            int appointmentYearsLower,
            int appointmentYearsUpper,
            int reformYearsLower,
            int reformYearsUpper) {
        jdbcTemplate.update("""
                DELETE FROM bz06_tgb
                WHERE zwbm = :positionCode
                  AND rzns = :appointmentYearsLower
                  AND rznz = :appointmentYearsUpper
                  AND tgns = :reformYearsLower
                  AND tgnz = :reformYearsUpper
                """, new MapSqlParameterSource()
                .addValue("positionCode", positionCode)
                .addValue("appointmentYearsLower", appointmentYearsLower)
                .addValue("appointmentYearsUpper", appointmentYearsUpper)
                .addValue("reformYearsLower", reformYearsLower)
                .addValue("reformYearsUpper", reformYearsUpper));
    }

    WageReformStandard findWageReformStandardByKey(
            String positionCode,
            int appointmentYearsLower,
            int appointmentYearsUpper,
            int reformYearsLower,
            int reformYearsUpper) {
        List<WageReformStandard> rows = jdbcTemplate.query("""
                SELECT zwbm, rzns, rznz, tgns, tgnz, jb, dc
                FROM bz06_tgb
                WHERE zwbm = :positionCode
                  AND rzns = :appointmentYearsLower
                  AND rznz = :appointmentYearsUpper
                  AND tgns = :reformYearsLower
                  AND tgnz = :reformYearsUpper
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("positionCode", positionCode)
                .addValue("appointmentYearsLower", appointmentYearsLower)
                .addValue("appointmentYearsUpper", appointmentYearsUpper)
                .addValue("reformYearsLower", reformYearsLower)
                .addValue("reformYearsUpper", reformYearsUpper), WAGE_REFORM_STANDARD_MAPPER);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void insertOtherAllowanceStandard(OtherAllowanceStandardRequest request) {
        switch (normalizeOtherAllowanceType(request.standardType())) {
            case "property" -> jdbcTemplate.update("""
                    INSERT INTO bz_wybt (tbnd, zwbm, bz)
                    VALUES (:standardYearMonth, :code, :amount)
                    """, otherAllowanceWriteParameters(request));
            case "communication" -> jdbcTemplate.update("""
                    INSERT INTO bz_txbt (tbnd, zwbm, bz)
                    VALUES (:standardYearMonth, :code, :amount)
                    """, otherAllowanceWriteParameters(request));
            case "civilized" -> jdbcTemplate.update("""
                    INSERT INTO bz_wmj (jb, bz, mul)
                    VALUES (:code, :amount, :multiplier)
                    """, otherAllowanceWriteParameters(request));
            case "assessment" -> jdbcTemplate.update("""
                    INSERT INTO bz_pskhj (tbnd, khjg, bz, pjsp)
                    VALUES (:standardYearMonth, :code, :amount, :averageAmount)
                    """, otherAllowanceWriteParameters(request));
            default -> throw new IllegalArgumentException("Unsupported other allowance standard type: " + request.standardType());
        }
    }

    void updateOtherAllowanceStandard(String standardType, OtherAllowanceStandardRequest request) {
        switch (normalizeOtherAllowanceType(standardType)) {
            case "property" -> jdbcTemplate.update("""
                    UPDATE bz_wybt SET bz = :amount
                    WHERE tbnd = :standardYearMonth AND zwbm = :code
                    """, otherAllowanceWriteParameters(request));
            case "communication" -> jdbcTemplate.update("""
                    UPDATE bz_txbt SET bz = :amount
                    WHERE tbnd = :standardYearMonth AND zwbm = :code
                    """, otherAllowanceWriteParameters(request));
            case "civilized" -> jdbcTemplate.update("""
                    UPDATE bz_wmj SET bz = :amount, mul = :multiplier
                    WHERE jb = :code
                    """, otherAllowanceWriteParameters(request));
            case "assessment" -> jdbcTemplate.update("""
                    UPDATE bz_pskhj SET bz = :amount, pjsp = :averageAmount
                    WHERE tbnd = :standardYearMonth AND khjg = :code
                    """, otherAllowanceWriteParameters(request));
            default -> throw new IllegalArgumentException("Unsupported other allowance standard type: " + standardType);
        }
    }

    void deleteOtherAllowanceStandard(String standardType, String standardYearMonth, String code) {
        switch (normalizeOtherAllowanceType(standardType)) {
            case "property" -> jdbcTemplate.update("""
                    DELETE FROM bz_wybt WHERE tbnd = :standardYearMonth AND zwbm = :code
                    """, new MapSqlParameterSource()
                    .addValue("standardYearMonth", standardYearMonth)
                    .addValue("code", code));
            case "communication" -> jdbcTemplate.update("""
                    DELETE FROM bz_txbt WHERE tbnd = :standardYearMonth AND zwbm = :code
                    """, new MapSqlParameterSource()
                    .addValue("standardYearMonth", standardYearMonth)
                    .addValue("code", code));
            case "civilized" -> jdbcTemplate.update("""
                    DELETE FROM bz_wmj WHERE jb = :code
                    """, new MapSqlParameterSource("code", code));
            case "assessment" -> jdbcTemplate.update("""
                    DELETE FROM bz_pskhj WHERE tbnd = :standardYearMonth AND khjg = :code
                    """, new MapSqlParameterSource()
                    .addValue("standardYearMonth", standardYearMonth)
                    .addValue("code", code));
            default -> throw new IllegalArgumentException("Unsupported other allowance standard type: " + standardType);
        }
    }

    OtherAllowanceStandard findOtherAllowanceStandardByKey(String standardType, String standardYearMonth, String code) {
        OtherAllowanceStandardQuery query = otherAllowanceStandardQuery(standardType);
        String whereClause = switch (normalizeOtherAllowanceType(standardType)) {
            case "civilized" -> "jb = :code";
            default -> "tbnd = :standardYearMonth AND "
                    + ("property".equals(normalizeOtherAllowanceType(standardType)) || "communication".equals(normalizeOtherAllowanceType(standardType))
                    ? "zwbm = :code" : "khjg = :code");
        };
        List<OtherAllowanceStandard> rows = jdbcTemplate.query("""
                SELECT '%s' AS standard_type, %s
                FROM %s
                WHERE %s
                LIMIT 1
                """.formatted(query.standardType(), query.columns(), query.tableName(), whereClause),
                new MapSqlParameterSource()
                        .addValue("standardYearMonth", standardYearMonth)
                        .addValue("code", code),
                OTHER_ALLOWANCE_STANDARD_MAPPER);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void rollbackCurrentHistory(String currentId, String previousId) {
        jdbcTemplate.update("DELETE FROM hisbase WHERE id = :currentId", new MapSqlParameterSource("currentId", currentId));
        jdbcTemplate.update("""
                UPDATE hisbase
                SET sid = ''
                WHERE id = :previousId
                """, new MapSqlParameterSource("previousId", previousId));
        unmarkAppCreated("hisbase", currentId);
    }

    void updatePayrollHistory(String id, PayrollHistoryMaintenanceRequest request) {
        jdbcTemplate.update("""
                UPDATE hisbase
                SET jsnf = :calculationYear,
                    jsyf = :calculationMonth,
                    jslb = :changeType,
                    zwbm2 = :positionCode,
                    zwgw2 = :positionName,
                    zwgzse2 = :positionSalary,
                    jbgzse2 = :gradeSalary,
                    jsdjgz2 = :technicalGradeSalary,
                    dfbt2 = :performanceAllowance,
                    blfb2 = :retainedAllowance,
                    hj2 = :totalAmount
                WHERE id = :id
                """, payrollHistoryRequestParameters(request).addValue("id", id));
    }

    void deletePayrollHistory(String id) {
        jdbcTemplate.update("DELETE FROM hisbase WHERE id = :id", new MapSqlParameterSource("id", id));
        unmarkAppCreated("hisbase", id);
    }

    List<PayrollFieldMetadata> findCalculationFields() {
        return jdbcTemplate.query("""
                SELECT id, sequence, field_cate, tblname, field_name, field_type, field_len, field_dec,
                       field_cap, field_caps, field_capj, sfsy06, sfsy, lrfs, category, jbt, gld,
                       jxryff, jbtbz, qsff, gdz, `readonly`, isgroup, iscount
                FROM fldgz
                WHERE sfsy06 = '√' AND field_type = 'N'
                ORDER BY sequence, id
                """, FIELD_MAPPER);
    }

    List<PositionSalaryStandard> findMatchedPositionStandards(PayrollHistorySnapshot history) {
        return jdbcTemplate.query("""
                SELECT tbnd, zwbm, bz
                FROM bz06_zwgz
                WHERE tbnd = :standardYearMonth AND zwbm = :positionCode
                ORDER BY zwbm
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", history.salaryStandardYearMonth())
                .addValue("positionCode", history.positionCode()), POSITION_STANDARD_MAPPER);
    }

    List<AllowanceStandard> findMatchedAllowanceStandards(PayrollHistorySnapshot history) {
        return jdbcTemplate.query("""
                SELECT id, tbnd, item, zwbm, mc, worklower, workupper, bz, jxlb
                FROM bz06_jbt
                WHERE tbnd = :standardYearMonth
                  AND (zwbm = :positionCode OR zwbm = '' OR zwbm IS NULL)
                ORDER BY item, zwbm, worklower, id
                LIMIT 100
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", history.allowanceStandardYearMonth())
                .addValue("positionCode", history.positionCode()), ALLOWANCE_STANDARD_MAPPER);
    }

    int positionSalary(String positionCode, String standardYearMonth) {
        return queryInteger("""
                SELECT bz
                FROM bz06_zwgz
                WHERE tbnd = :standardYearMonth AND zwbm = :positionCode
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("positionCode", mapPositionSalaryCode(positionCode)));
    }

    Optional<PositionChangeCandidate> findCurrentPositionChangeCandidate(String organizationCode, String personCode) {
        return jdbcTemplate.query("""
                SELECT b.zwbm, b.xzzw, b.srny
                FROM dryzwbh b
                INNER JOIN hisbase h
                    ON h.dwbm = b.dwbm
                   AND h.grbm = b.grbm
                   AND (h.sid IS NULL OR TRIM(h.sid) = '')
                WHERE b.dwbm = :organizationCode
                  AND b.grbm = :personCode
                  AND b.xrzwbz = '1'
                  AND b.srny >= '2006.07'
                  AND b.srny >= h.srny
                  AND (
                       COALESCE(h.zwbm2, '') <> COALESCE(b.zwbm, '')
                       OR COALESCE(h.zjbm, '') <> COALESCE(b.zjbm, '')
                  )
                  AND (
                       CASE WHEN LEFT(COALESCE(h.zwbm2, ''), 2) IN ('07','08','09','10','11') THEN 1 ELSE 0 END
                       =
                       CASE WHEN LEFT(COALESCE(b.zwbm, ''), 2) IN ('07','08','09','10','11') THEN 1 ELSE 0 END
                  )
                ORDER BY b.srny DESC, b.id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), POSITION_CHANGE_CANDIDATE_MAPPER).stream().findFirst();
    }

    List<Integer> findPositionChangePromotionPersonnelUids(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        return findPositionChangePromotionPage(organizationScope, organizationCode, keyword, pageRequest)
                .rows()
                .stream()
                .map(PositionChangePromotionCandidateRow::uid)
                .toList();
    }

    PositionChangePromotionPage findPositionChangePromotionPage(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return new PositionChangePromotionPage(List.of(), 0);
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        List<PositionChangePromotionCandidateRow> rows = new java.util.ArrayList<>();
        final long[] total = {0};
        jdbcTemplate.query(positionChangePromotionPersonnelSql() + """
                SELECT p.uid,
                       r.px,
                       r.zwbm2 AS before_position_code,
                       r.zwgw2 AS before_position_name,
                       r.zwbm AS after_position_code,
                       r.zwgw AS after_position_name,
                       r.jslb AS payroll_change_type,
                       COUNT(*) OVER() AS total_count
                FROM ranked r
                INNER JOIN dryjbxx p ON p.dwbm = r.dwbm AND p.grbm = r.grbm
                WHERE r.rn = 1
                  AND NOT (
                      LEFT(COALESCE(r.zwbm2, ''), 2) IN ('07','08','09','10')
                      AND COALESCE(r.zjbm, '') <> COALESCE(r.zjbma, '')
                      AND COALESCE(r.zwbm2, '') = COALESCE(r.zwbm, '')
                  )
                  AND (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR r.zwgw2 LIKE :keywordLike
                       OR r.zwgw LIKE :keywordLike)
                ORDER BY p.dwbm, p.grbm
                LIMIT :limit OFFSET :offset
                """, parameters, (rs, rowNum) -> {
            if (total[0] == 0) {
                total[0] = rs.getLong("total_count");
            }
            rows.add(new PositionChangePromotionCandidateRow(
                    rs.getInt("uid"),
                    rs.getInt("px"),
                    SqlText.trim(rs.getString("before_position_code")),
                    SqlText.trim(rs.getString("before_position_name")),
                    SqlText.trim(rs.getString("after_position_code")),
                    SqlText.trim(rs.getString("after_position_name")),
                    SqlText.trim(rs.getString("payroll_change_type"))));
            return null;
        });
        return new PositionChangePromotionPage(rows, total[0]);
    }

    List<LevelPromotionCandidateRow> findLevelPromotionCandidateRows(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            int promotionYear) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = payrollChangeParameters(organizationScope, organizationCode, keyword)
                .addValue("promotionYear", String.valueOf(promotionYear));
        List<LevelPromotionCandidateRow> rows = new java.util.ArrayList<>();
        jdbcTemplate.query(levelPromotionCandidateSql() + """
                SELECT p.uid, r.px
                FROM ranked r
                INNER JOIN dryjbxx p ON p.dwbm = r.dwbm AND p.grbm = r.grbm
                WHERE r.rn = 1
                  AND (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR r.zwgw2 LIKE :keywordLike)
                ORDER BY p.dwbm, p.grbm
                """, parameters, (rs, rowNum) -> {
            rows.add(new LevelPromotionCandidateRow(rs.getInt("uid"), rs.getInt("px")));
            return null;
        });
        return rows;
    }

    long countPositionChangePromotionPersonnel(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject(positionChangePromotionPersonnelSql() + """
                SELECT COUNT(*)
                FROM ranked r
                INNER JOIN dryjbxx p ON p.dwbm = r.dwbm AND p.grbm = r.grbm
                WHERE r.rn = 1
                  AND NOT (
                      LEFT(COALESCE(r.zwbm2, ''), 2) IN ('07','08','09','10')
                      AND COALESCE(r.zjbm, '') <> COALESCE(r.zjbma, '')
                      AND COALESCE(r.zwbm2, '') = COALESCE(r.zwbm, '')
                  )
                  AND (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR p.dwbm = :organizationCode)
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR r.zwgw2 LIKE :keywordLike
                       OR r.zwgw LIKE :keywordLike)
                """, payrollChangeParameters(organizationScope, organizationCode, keyword), Long.class);
        return count == null ? 0 : count;
    }

    private static String positionChangePromotionPersonnelSql() {
        return """
                WITH all_data AS (
                    SELECT
                        1 AS px,
                        b.srny AS pxny,
                        a.dwbm,
                        a.grbm,
                        h.zwgw2,
                        h.zwbm2,
                        b.xzzw AS zwgw,
                        b.zwbm,
                        h.zjbm,
                        b.zjbm AS zjbma,
                        h.jslb,
                        h.jsnf,
                        h.jsyf
                    FROM dryjbxx a
                    INNER JOIN hisbase h
                        ON a.dwbm = h.dwbm
                       AND a.grbm = h.grbm
                       AND (h.sid IS NULL OR TRIM(h.sid) = '')
                    INNER JOIN dryzwbh b
                        ON a.dwbm = b.dwbm
                       AND a.grbm = b.grbm
                       AND b.xrzwbz = '1'
                       AND b.srny >= '2006.07'
                       AND b.srny >= h.srny
                       AND (
                            COALESCE(h.zwbm2, '') <> COALESCE(b.zwbm, '')
                            OR COALESCE(h.zjbm, '') <> COALESCE(b.zjbm, '')
                       )
                       AND (
                            CASE WHEN LEFT(COALESCE(h.zwbm2, ''), 2) IN ('07','08','09','10','11') THEN 1 ELSE 0 END
                            =
                            CASE WHEN LEFT(COALESCE(b.zwbm, ''), 2) IN ('07','08','09','10','11') THEN 1 ELSE 0 END
                       )
                       AND h.jslb NOT IN ('职务变化','职级晋升','法检套改','警员套改','警务套改','职级套改')
                    WHERE (:allOrganizations = TRUE OR a.dwbm IN (:organizationCodes))
                      AND (:organizationCode IS NULL OR a.dwbm = :organizationCode)
                    UNION ALL
                    SELECT
                        2 AS px,
                        CONCAT(h.jsnf, LPAD(h.jsyf, 2, '0')) AS pxny,
                        a.dwbm,
                        a.grbm,
                        h1.zwgw2,
                        h1.zwbm2,
                        h.zwgw2 AS zwgw,
                        h.zwbm2 AS zwbm,
                        h.zjbm,
                        h.zjbm AS zjbma,
                        h.jslb,
                        h.jsnf,
                        h.jsyf
                    FROM dryjbxx a
                    INNER JOIN hisbase h
                        ON a.dwbm = h.dwbm
                       AND a.grbm = h.grbm
                       AND (h.sid IS NULL OR TRIM(h.sid) = '')
                       AND h.jslb IN ('职务变化','职级晋升','法检套改','警员套改','警务套改','职级套改')
                    LEFT JOIN hisbase h1
                        ON a.dwbm = h1.dwbm
                       AND h1.grbm = h.grbm
                       AND h1.sid = h.id
                    WHERE (:allOrganizations = TRUE OR a.dwbm IN (:organizationCodes))
                      AND (:organizationCode IS NULL OR a.dwbm = :organizationCode)
                ),
                ranked AS (
                    SELECT
                        *,
                        ROW_NUMBER() OVER (
                            PARTITION BY dwbm, grbm
                            ORDER BY px ASC, pxny DESC, jsnf DESC, jsyf DESC
                        ) AS rn
                    FROM all_data
                )
                """;
    }

    private static String levelPromotionCandidateSql() {
        return """
                WITH all_data AS (
                    SELECT
                        1 AS px,
                        a.dwbm,
                        a.grbm,
                        h.zwgw2,
                        h.jslb,
                        h.jsnf
                    FROM dryjbxx a
                    INNER JOIN hisbase h
                        ON a.dwbm = h.dwbm
                       AND a.grbm = h.grbm
                       AND (h.sid IS NULL OR TRIM(h.sid) = '')
                    WHERE LEFT(COALESCE(h.zwbm2, ''), 2) IN ('01','02','04','21','22','23','24','25','26','27','28')
                      AND CAST(COALESCE(NULLIF(TRIM(h.jbgzjb2), ''), '0') AS UNSIGNED) > 1
                      AND (
                           h.jslb <> '正常级别'
                           OR CAST(COALESCE(NULLIF(TRIM(h.jsnf), ''), '0') AS UNSIGNED) <> CAST(:promotionYear AS UNSIGNED)
                      )
                      AND (:allOrganizations = TRUE OR a.dwbm IN (:organizationCodes))
                      AND (:organizationCode IS NULL OR a.dwbm = :organizationCode)
                    UNION ALL
                    SELECT
                        2 AS px,
                        a.dwbm,
                        a.grbm,
                        h.zwgw2,
                        h.jslb,
                        h.jsnf
                    FROM dryjbxx a
                    INNER JOIN hisbase h
                        ON a.dwbm = h.dwbm
                       AND a.grbm = h.grbm
                       AND (h.sid IS NULL OR TRIM(h.sid) = '')
                       AND h.jslb = '正常级别'
                       AND CAST(COALESCE(NULLIF(TRIM(h.jsnf), ''), '0') AS UNSIGNED) = CAST(:promotionYear AS UNSIGNED)
                    WHERE (:allOrganizations = TRUE OR a.dwbm IN (:organizationCodes))
                      AND (:organizationCode IS NULL OR a.dwbm = :organizationCode)
                ),
                ranked AS (
                    SELECT
                        *,
                        ROW_NUMBER() OVER (
                            PARTITION BY dwbm, grbm
                            ORDER BY px ASC, jsnf DESC
                        ) AS rn
                    FROM all_data
                )
                """;
    }

    void updatePositionPromotionFlag(
            String organizationCode,
            String personCode,
            String startYearMonth,
            String positionCode,
            String promotionFlag) {
        jdbcTemplate.update("""
                UPDATE dryzwbh
                SET jsbz = :promotionFlag
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND zwbm = :positionCode
                  AND srny = :startYearMonth
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("startYearMonth", emptyToNull(startYearMonth))
                .addValue("positionCode", emptyToNull(positionCode))
                .addValue("promotionFlag", valueOrBlank(promotionFlag)));
    }

    Optional<PositionLevelRange> findPositionLevelRange(String positionCode) {
        return jdbcTemplate.query("""
                SELECT zwbm, `min`, `max`
                FROM bz06_zw_jb_xj
                WHERE zwbm = :positionCode
                LIMIT 1
                """, new MapSqlParameterSource("positionCode", emptyToNull(positionCode)), POSITION_LEVEL_RANGE_MAPPER).stream().findFirst();
    }

    Optional<EducationPromotionSource> findLatestEducationForPromotion(
            String organizationCode,
            String personCode,
            String currentPeriod) {
        return jdbcTemplate.query("""
                SELECT xlbm, xl, bysj
                FROM dxl
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND xllb <> '后取'
                  AND bysj IS NOT NULL
                  AND TRIM(REPLACE(bysj, '.', '')) <> ''
                  AND REPLACE(bysj, '.', '') <= :currentPeriod
                ORDER BY xlbm ASC, bysj DESC, id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("currentPeriod", currentPeriod), EDUCATION_PROMOTION_SOURCE_MAPPER).stream().findFirst();
    }

    List<EducationPromotionSource> findEducationRecordsBetween(
            String organizationCode,
            String personCode,
            String startPeriod,
            String endPeriod) {
        return jdbcTemplate.query("""
                SELECT xlbm, xl, bysj
                FROM dxl
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND xllb <> '后取'
                  AND bysj IS NOT NULL
                  AND TRIM(REPLACE(bysj, '.', '')) <> ''
                  AND REPLACE(bysj, '.', '') > :startPeriod
                  AND REPLACE(bysj, '.', '') <= :endPeriod
                ORDER BY REPLACE(bysj, '.', ''), xlbm ASC, id ASC
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("startPeriod", startPeriod)
                .addValue("endPeriod", endPeriod), EDUCATION_PROMOTION_SOURCE_MAPPER);
    }

    Optional<EducationRegularizationStandard> findEducationRegularizationStandard(
            String positionCode,
            String educationCode) {
        return jdbcTemplate.query("""
                SELECT xlbm, xlmc, zzzwbm, zzzwmc, zzjb, zzdc
                FROM bz06_zzdz
                WHERE LEFT(zzzwbm, 2) = LEFT(:positionCode, 2)
                  AND xlbm = :educationCode
                ORDER BY zzzwbm
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("positionCode", normalizeEducationStandardPositionCode(positionCode))
                .addValue("educationCode", emptyToNull(educationCode)), EDUCATION_REGULARIZATION_STANDARD_MAPPER).stream().findFirst();
    }

    /** 对齐旧系统 jxgz06：见习岗位按 bz06_zzdz 取试用期工资（套改时仍试用直接走 gz1）。 */
    Optional<InternSalaryStandard> findInternSalaryStandard(
            String positionCode,
            String educationCode,
            String educationName,
            String standardYearMonth) {
        String lookupPrefix = internSalaryLookupPrefix(positionCode);
        if (lookupPrefix == null || emptyToNull(standardYearMonth) == null) {
            return Optional.empty();
        }
        return jdbcTemplate.query("""
                SELECT tbnd, xlbm, xlmc, zzzwbm, zzzwmc, zzdc, zzjb, gz1, gz2
                FROM bz06_zzdz
                WHERE tbnd = :standardYearMonth
                  AND LEFT(zzzwbm, 2) = :lookupPrefix
                  AND (
                        (:educationCode IS NOT NULL AND xlbm = :educationCode)
                     OR (:educationName IS NOT NULL AND xlmc = :educationName)
                  )
                ORDER BY zzzwbm
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("lookupPrefix", lookupPrefix)
                .addValue("educationCode", emptyToNull(educationCode))
                .addValue("educationName", emptyToNull(educationName)), INTERN_SALARY_STANDARD_MAPPER).stream().findFirst();
    }

    int positionGradeSalary(String positionCode, String positionSalaryGrade, String invertedStep, String standardYearMonth) {
        int grade = intValue(positionSalaryGrade);
        if (emptyToNull(positionCode) == null || emptyToNull(standardYearMonth) == null || grade <= 0 || grade > 20) {
            return 0;
        }
        Integer amount = queryInteger("""
                SELECT dc%s
                FROM bz06_zwgz_gr
                WHERE tbnd = :standardYearMonth AND zwbm = :positionCode
                LIMIT 1
                """.formatted(grade), new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("positionCode", emptyToNull(positionCode)));
        if (intValue(invertedStep) <= 0 || grade <= 1) {
            return amount;
        }
        Integer previous = queryInteger("""
                SELECT dc%s
                FROM bz06_zwgz_gr
                WHERE tbnd = :standardYearMonth AND zwbm = :positionCode
                LIMIT 1
                """.formatted(grade - 1), new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("positionCode", emptyToNull(positionCode)));
        return amount + amount - previous;
    }

    int highestGradeStepForLevel(String gradeLevel) {
        return highestGradeStep(gradeLevel);
    }

    int civilServantGradeSalary(
            String gradeLevel,
            String gradeStep,
            String stepDifferenceCount,
            String standardYearMonth) {
        int step = intValue(gradeStep);
        int extra = intValue(stepDifferenceCount);
        if (step <= 0 || emptyToNull(gradeLevel) == null) {
            return 0;
        }
        int highestStep = highestGradeStep(gradeLevel);
        int effectiveStep = step >= highestStep ? highestStep + Math.max(0, extra) : step;
        return gradeSalary(gradeLevel, String.valueOf(effectiveStep), standardYearMonth);
    }

    int gradeSalary(String gradeLevel, String gradeStep, String standardYearMonth) {
        int step = intValue(gradeStep);
        if (step <= 0 || emptyToNull(gradeLevel) == null) {
            return 0;
        }
        int[] row = gradeSalaryRow(gradeLevel, standardYearMonth);
        int direct = gradeSalaryAtStep(row, Math.min(step, 20));

        int highestStep = highestGradeStep(gradeLevel);
        if (step <= highestStep || highestStep <= 1 || highestStep > 20) {
            return direct;
        }

        int highest = gradeSalaryAtStep(row, highestStep);
        int previous = gradeSalaryAtStep(row, highestStep - 1);
        return highest + (highest - previous) * (step - highestStep);
    }

    int policeOfficerGradeSalary(String gradeLevel, String gradeStep, String standardYearMonth) {
        int step = intValue(gradeStep);
        if (step <= 0 || emptyToNull(gradeLevel) == null) {
            return 0;
        }
        String column = "dc" + Math.min(step, 14);
        return queryInteger("""
                SELECT %s
                FROM bz06_djgz
                WHERE tbnd = :standardYearMonth AND CAST(jb AS UNSIGNED) = :gradeLevel
                LIMIT 1
                """.formatted(column), new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("gradeLevel", intValue(gradeLevel)));
    }

    String judicialConversionStep(String currentLevel, String currentStep, String targetPositionCode) {
        int level = intValue(currentLevel);
        int step = intValue(currentStep);
        if (level < 8 || level > 26 || step <= 0 || targetPositionCode == null || targetPositionCode.length() < 4) {
            return "";
        }
        String targetRank = targetPositionCode.substring(3, 4).toLowerCase();
        if (!targetRank.matches("[5-9a-d]")) {
            return "";
        }
        return queryString("""
                SELECT d03%s
                FROM bz06_fjtgb
                WHERE TRIM(jb%d) = :currentStep
                LIMIT 1
                """.formatted(targetRank, level), new MapSqlParameterSource("currentStep", String.valueOf(step)));
    }

    int salaryLevelSalary(String salaryLevel, String inversionStep, String standardYearMonth, String positionCode) {
        String normalizedLevel = leftPadTwo(salaryLevel);
        if (normalizedLevel == null || emptyToNull(positionCode) == null) {
            return 0;
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("salaryLevel", normalizedLevel)
                .addValue("jobCategory", positionCode.substring(0, Math.min(2, positionCode.length())));
        Integer base = queryInteger("""
                SELECT bz
                FROM bz06_xjgz
                WHERE tbnd = :standardYearMonth AND xj = :salaryLevel AND gwflbm = :jobCategory
                LIMIT 1
                """, parameters);
        if (base == null) {
            return 0;
        }

        int inversion = intValue(inversionStep);
        if (inversion <= 0) {
            return base;
        }

        List<Integer> topAmounts = jdbcTemplate.queryForList("""
                SELECT DISTINCT bz
                FROM bz06_xjgz
                WHERE tbnd = :standardYearMonth AND gwflbm = :jobCategory
                ORDER BY bz DESC
                LIMIT 2
                """, parameters, Integer.class);
        if (topAmounts.size() < 2) {
            return base;
        }
        return inversion * (topAmounts.get(0) - topAmounts.get(1)) + base;
    }

    int technicalGradeSalary(String positionCode, String standardYearMonth) {
        return queryInteger("""
                SELECT jsdjgz
                FROM bz06_zwgz_gr
                WHERE tbnd = :standardYearMonth AND zwbm = :positionCode
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("positionCode", emptyToNull(positionCode)));
    }

    int rankAllowance(String positionCode, String rankAllowanceStandardYearMonth, String rankName) {
        if (!isRankAllowanceEligible(positionCode) || emptyToNull(rankAllowanceStandardYearMonth) == null || emptyToNull(rankName) == null) {
            return 0;
        }
        return rankAllowanceByRank(
                rankAllowanceStandardYearMonth,
                rankName,
                resolveRankAllowanceStandardLb(rankName, null));
    }

    int rankAllowanceByRank(String rankAllowanceStandardYearMonth, String rankName, String standardLb) {
        if (emptyToNull(rankAllowanceStandardYearMonth) == null || emptyToNull(rankName) == null) {
            return 0;
        }
        String normalizedLb = emptyToNull(standardLb);
        return queryInteger("""
                SELECT jtbz
                FROM jxjtbz
                WHERE tbnd = :standardYearMonth
                  AND jx = :rankName
                  AND (
                      :standardLb IS NULL
                      OR lb = :standardLb
                      OR (:standardLb = 'jx' AND lb = '')
                  )
                ORDER BY CASE WHEN :standardLb IS NOT NULL AND lb = :standardLb THEN 0
                              WHEN :standardLb = 'jx' AND lb = '' THEN 1
                              ELSE 2 END
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(rankAllowanceStandardYearMonth))
                .addValue("rankName", emptyToNull(rankName))
                .addValue("standardLb", normalizedLb));
    }

    int compositeRankAllowance(
            String policeStandardYearMonth,
            String prosecutionStandardYearMonth,
            String judicialStandardYearMonth,
            String rankName,
            String positionCode) {
        if (!isCivilServantRankAllowanceEligible(positionCode)) {
            return 0;
        }
        String normalizedRank = rankName == null ? "" : rankName.trim();
        int police = isPoliceRankName(normalizedRank) && emptyToNull(policeStandardYearMonth) != null
                ? rankAllowanceByRank(policeStandardYearMonth, normalizedRank, "jx")
                : 0;
        int prosecution = isProsecutionRankName(normalizedRank) && emptyToNull(prosecutionStandardYearMonth) != null
                ? rankAllowanceByRank(prosecutionStandardYearMonth, normalizedRank, "jc")
                : 0;
        int judicial = isJudicialRankName(normalizedRank) && emptyToNull(judicialStandardYearMonth) != null
                ? rankAllowanceByRank(judicialStandardYearMonth, normalizedRank, "sp")
                : 0;
        int supervision = isSupervisionRankName(normalizedRank) && emptyToNull(policeStandardYearMonth) != null
                ? rankAllowanceByRank(policeStandardYearMonth, normalizedRank, "mt")
                : 0;
        return police + prosecution + judicial + supervision;
    }

    static boolean isPoliceRankName(String rankName) {
        return rankName != null && rankName.contains("警");
    }

    static boolean isProsecutionRankName(String rankName) {
        return rankName != null
                && (rankName.contains("检察") || rankName.contains("检察官"))
                && !rankName.contains("监察");
    }

    static boolean isJudicialRankName(String rankName) {
        return rankName != null
                && (rankName.contains("法") || rankName.contains("审判") || rankName.contains("法官"));
    }

    static boolean isSupervisionRankName(String rankName) {
        return rankName != null && rankName.contains("监察");
    }

    String latestRankAllowanceStandardForLb(String period, String standardLb) {
        String normalizedPeriod = period == null ? "" : period.replace(".", "");
        String normalizedLb = emptyToNull(standardLb);
        if (normalizedLb == null) {
            return null;
        }
        return queryString("""
                SELECT tbnd
                FROM jxjtbz
                WHERE tbnd <= :period
                  AND lb = :standardLb
                ORDER BY tbnd DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("period", normalizedPeriod)
                .addValue("standardLb", normalizedLb));
    }

    /**
     * {@code jxjtbz.lb}: jx=警衔津贴, jc=检察津贴, sp=审判津贴, mt=监察津贴。
     */
    String resolveRankAllowanceStandardLb(String rankName, String recordCategory) {
        String normalizedRecord = emptyToNull(recordCategory);
        if (normalizedRecord != null) {
            return switch (normalizedRecord) {
                case "jx", "jc", "sp", "mt" -> normalizedRecord;
                case "警" -> "jx";
                case "法" -> "sp";
                case "检" -> "jc";
                case "监" -> "mt";
                default -> normalizedRecord;
            };
        }
        String normalizedName = rankName == null ? "" : rankName.trim();
        if (normalizedName.contains("检察") || normalizedName.contains("检察官")) {
            return "jc";
        }
        if (normalizedName.contains("法官") || normalizedName.contains("审判")) {
            return "sp";
        }
        if (normalizedName.contains("监察")) {
            return "mt";
        }
        if (normalizedName.contains("警")) {
            return "jx";
        }
        return "jx";
    }

    String rankAllowanceStandardLbForDisplayCategory(String displayCategory) {
        String normalized = emptyToNull(displayCategory);
        if (normalized == null) {
            return null;
        }
        return switch (normalized) {
            case "警" -> "jx";
            case "检" -> "jc";
            case "法" -> "sp";
            case "监" -> "mt";
            default -> normalized;
        };
    }

    Optional<RankAllowanceChange> findLatestRankAllowanceChangeForCategory(
            String organizationCode,
            String personCode,
            String category) {
        return jdbcTemplate.query("""
                SELECT jx, sysj, lb
                FROM jx
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND (
                      (:category = 'jx' AND (lb = 'jx' OR ((lb IS NULL OR TRIM(lb) = '') AND jx LIKE '%警%')))
                      OR (:category = 'jc' AND (lb = 'jc' OR ((lb IS NULL OR TRIM(lb) = '') AND (jx LIKE '%检察%' OR jx LIKE '%检察官%') AND jx NOT LIKE '%监察%')))
                      OR (:category = 'sp' AND (lb = 'sp' OR ((lb IS NULL OR TRIM(lb) = '') AND (jx LIKE '%法%' OR jx LIKE '%审判%' OR jx LIKE '%法官%'))))
                      OR (:category = 'mt' AND (lb = 'mt' OR ((lb IS NULL OR TRIM(lb) = '') AND jx LIKE '%监察%')))
                  )
                ORDER BY REPLACE(sysj, '.', '') DESC, xrjxbz DESC, id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("category", category), RANK_ALLOWANCE_CHANGE_MAPPER)
                .stream()
                .findFirst();
    }

    String createRankAllowanceChangeHistoryFromLatest(int uid, RankAllowanceChangeHistoryMutation mutation) {
        PayrollHistorySnapshot latest = findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        String id = java.util.UUID.randomUUID().toString().toUpperCase();
        jdbcTemplate.update("""
                INSERT INTO hisbase
                SELECT :id, h.dwbm, h.grbm, h.xm, h.ryfl, h.dwsx, h.gwfl, h.jrny, h.jrfs,
                       h.zdgznx, h.gznx, h.jhlqsny, h.zdjhlnx, h.xlbm, h.zgxl, h.bjglxlnx,
                       h.tc, h.xckhndzw, h.xckhndjb, h.bgdwjc, h.zwjb, h.zjbm, h.xrzw, h.srny,
                       :rankName, h.tgbl, h.jtbl, h.fddc, h.fdgd, h.fdsj,
                       :calculationYear, :calculationMonth, :changeType,
                       h.khqk, h.dynkh, h.denkh, h.bbz, :totalAmount,
                       h.zwbm2, h.zwgw2, h.zwgzse2, h.zwgzse2, h.jbgzse2, h.jbgzse2, h.jbgzse2,
                       h.jcgz2, h.glgz2, h.jsdjgz2, h.grjj2, h.blfb2, h.jsfszwtg2,
                       h.jt2, h.fdgz2, h.jjjy2, h.dfbt2, h.gwjt2, h.bh, h.jxgz, h.zzbc,
                       h.zwjt, h.zfbt, h.dsznf, h.nzgwsf, h.jzmcbt, h.sdbt, h.grsds, h.zfgjj,
                       h.ylbxf, h.ylf, h.qtdk, h.bfyqgz, h.kjyqgz, h.sfgz, h.qtbt, :rankAllowance,
                       h.gryhzh, h.tfnf, h.tfyf, h.spdw, h.tbnd, :policeStandardYearMonth, h.jbtbz, h.jhljt,
                       h.pgbc, h.sidbt, h.jzgb, h.nrjxgzbf, h.tgblbf, :prosecutionStandardYearMonth, :judicialStandardYearMonth, h.njbt,
                       h.gwjtbz, h.gwjtlb, h.sfjzgb, ''
                FROM hisbase h
                WHERE h.id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("sourceId", latest.id())
                .addValue("rankName", valueOrBlank(mutation.rankName()))
                .addValue("calculationYear", mutation.calculationYear())
                .addValue("calculationMonth", mutation.calculationMonth())
                .addValue("changeType", mutation.changeType())
                .addValue("policeStandardYearMonth", valueOrBlank(mutation.policeStandardYearMonth()))
                .addValue("prosecutionStandardYearMonth", valueOrBlank(mutation.prosecutionStandardYearMonth()))
                .addValue("judicialStandardYearMonth", valueOrBlank(mutation.judicialStandardYearMonth()))
                .addValue("rankAllowance", mutation.rankAllowance() == null ? 0 : mutation.rankAllowance())
                .addValue("totalAmount", mutation.totalAmount() == null ? 0 : mutation.totalAmount()));
        jdbcTemplate.update("""
                UPDATE hisbase
                SET sid = :newId
                WHERE id = :sourceId
                """, new MapSqlParameterSource()
                .addValue("newId", id)
                .addValue("sourceId", latest.id()));
        markAppCreated("hisbase", id);
        return id;
    }

    Optional<RankAllowanceChange> findRankAllowanceAtOrBefore(String organizationCode, String personCode, String period) {
        String normalizedPeriod = period == null ? "" : period.replace(".", "");
        return jdbcTemplate.query("""
                SELECT jx, sysj, lb
                FROM jx
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND REPLACE(sysj, '.', '') <= :period
                ORDER BY REPLACE(sysj, '.', '') DESC, xrjxbz DESC, id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("period", normalizedPeriod), RANK_ALLOWANCE_CHANGE_MAPPER).stream().findFirst();
    }

    List<RankAllowanceChange> findRankAllowanceChangesBetween(String organizationCode, String personCode, String startPeriod, String targetPeriod) {
        String normalizedStart = startPeriod == null ? "" : startPeriod.replace(".", "");
        String normalizedTarget = targetPeriod == null ? "" : targetPeriod.replace(".", "");
        return jdbcTemplate.query("""
                SELECT jx, sysj, lb
                FROM jx
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND REPLACE(sysj, '.', '') > :startPeriod
                  AND REPLACE(sysj, '.', '') <= :targetPeriod
                ORDER BY REPLACE(sysj, '.', ''), id
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("startPeriod", normalizedStart)
                .addValue("targetPeriod", normalizedTarget), RANK_ALLOWANCE_CHANGE_MAPPER);
    }

    String latestRankAllowanceStandardAtOrBefore(String period) {
        String normalizedPeriod = period == null ? "" : period.replace(".", "");
        return queryString("""
                SELECT tbnd
                FROM jxjtbz
                WHERE tbnd <= :period
                ORDER BY tbnd DESC
                LIMIT 1
                """, new MapSqlParameterSource("period", normalizedPeriod));
    }

    List<String> findRankAllowanceStandardPeriodsBetween(String startPeriod, String targetPeriod) {
        return findRankAllowanceStandardPeriodsBetween(startPeriod, targetPeriod, null);
    }

    List<String> findRankAllowanceStandardPeriodsBetween(String startPeriod, String targetPeriod, String categoryKeyword) {
        String normalizedStart = startPeriod == null ? "" : startPeriod.replace(".", "");
        String normalizedTarget = targetPeriod == null ? "" : targetPeriod.replace(".", "");
        String normalizedCategory = emptyToNull(categoryKeyword);
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT tbnd
                FROM jxjtbz
                WHERE tbnd > :startPeriod
                  AND tbnd <= :targetPeriod
                  AND (:categoryKeyword IS NULL OR jx LIKE :categoryLike)
                ORDER BY tbnd
                """, new MapSqlParameterSource()
                .addValue("startPeriod", normalizedStart)
                .addValue("targetPeriod", normalizedTarget)
                .addValue("categoryKeyword", normalizedCategory)
                .addValue("categoryLike", normalizedCategory == null ? null : "%" + normalizedCategory + "%"), String.class);
    }

    boolean hasRankAllowanceStandardForCategory(String standardYearMonth, String displayCategory) {
        String standardLb = rankAllowanceStandardLbForDisplayCategory(displayCategory);
        if (emptyToNull(standardYearMonth) == null || emptyToNull(standardLb) == null) {
            return false;
        }
        Integer count = queryInteger("""
                SELECT COUNT(*)
                FROM jxjtbz
                WHERE tbnd = :standardYearMonth
                  AND lb = :standardLb
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("standardLb", standardLb));
        return count != null && count > 0;
    }

    String latestBasicSalaryStandardAtOrBefore(String period) {
        String normalizedPeriod = period == null ? "" : period.replace(".", "");
        return queryString("""
                SELECT tbnd
                FROM (
                    SELECT tbnd FROM bz06_jbgz WHERE tbnd <= :period
                    UNION
                    SELECT tbnd FROM bz06_djgz WHERE tbnd <= :period
                    UNION
                    SELECT tbnd FROM bz06_xjgz WHERE tbnd <= :period
                    UNION
                    SELECT tbnd FROM bz06_zwgz WHERE tbnd <= :period
                ) standards
                ORDER BY tbnd DESC
                LIMIT 1
                """, new MapSqlParameterSource("period", normalizedPeriod));
    }

    String latestPositionSalaryStandardAtOrBefore(String period) {
        String normalizedPeriod = period == null ? "" : period.replace(".", "");
        return queryString("""
                SELECT tbnd
                FROM bz06_zwgz
                WHERE tbnd <= :period
                ORDER BY tbnd DESC
                LIMIT 1
                """, new MapSqlParameterSource("period", normalizedPeriod));
    }

    List<String> findBasicSalaryStandardPeriodsBetween(String startPeriod, String targetPeriod) {
        String normalizedStart = startPeriod == null ? "" : startPeriod.replace(".", "");
        String normalizedTarget = targetPeriod == null ? "" : targetPeriod.replace(".", "");
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT tbnd
                FROM (
                    SELECT tbnd FROM bz06_jbgz
                    UNION
                    SELECT tbnd FROM bz06_djgz
                    UNION
                    SELECT tbnd FROM bz06_xjgz
                    UNION
                    SELECT tbnd FROM bz06_zwgz
                ) standards
                WHERE tbnd > :startPeriod
                  AND tbnd <= :targetPeriod
                ORDER BY tbnd
                """, new MapSqlParameterSource()
                .addValue("startPeriod", normalizedStart)
                .addValue("targetPeriod", normalizedTarget), String.class);
    }

    List<String> findAllowanceStandardPeriodsBetween(String organizationCode, String startPeriod, String targetPeriod) {
        String normalizedStart = startPeriod == null ? "" : startPeriod.replace(".", "");
        String normalizedTarget = targetPeriod == null ? "" : targetPeriod.replace(".", "");
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT tbnd
                FROM bz06_jbt
                WHERE tbnd > :startPeriod
                  AND tbnd <= :targetPeriod
                  AND UPPER(item) IN ('DFBT2', 'SDBT')
                ORDER BY tbnd
                """, new MapSqlParameterSource()
                .addValue("startPeriod", normalizedStart)
                .addValue("targetPeriod", normalizedTarget), String.class);
    }

    boolean hasAllowanceStandard(String standardYearMonth, String organizationCode, String positionCode) {
        if (emptyToNull(standardYearMonth) == null) {
            return false;
        }
        int category = allowanceStandardPeriodCategory(organizationCode);
        Integer count = queryInteger("""
                SELECT COUNT(*)
                FROM bz06_jbt
                WHERE tbnd = :standardYearMonth
                  AND UPPER(item) IN ('DFBT2', 'SDBT')
                  AND jxlb = :category
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("category", category));
        return count != null && count > 0;
    }

    boolean hasAllowanceStandardForPosition(String standardYearMonth, String organizationCode, String positionCode) {
        if (emptyToNull(standardYearMonth) == null || emptyToNull(positionCode) == null) {
            return false;
        }
        String lookupPositionCode = performancePositionCode(positionCode, standardYearMonth);
        int category = allowanceAmountLookupCategory(organizationCode, positionCode, standardYearMonth);
        Integer count = queryInteger("""
                SELECT COUNT(*)
                FROM bz06_jbt
                WHERE tbnd = :standardYearMonth
                  AND UPPER(item) = 'DFBT2'
                  AND zwbm = :positionCode
                  AND jxlb = :category
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("positionCode", lookupPositionCode)
                .addValue("category", category));
        if (count != null && count > 0) {
            return true;
        }
        if (!requiresAllowanceCategoryFilter(positionCode)) {
            count = queryInteger("""
                    SELECT COUNT(*)
                    FROM bz06_jbt
                    WHERE tbnd = :standardYearMonth
                      AND UPPER(item) = 'DFBT2'
                      AND zwbm = :positionCode
                    """, new MapSqlParameterSource()
                    .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                    .addValue("positionCode", lookupPositionCode));
            return count != null && count > 0;
        }
        return false;
    }

    String latestAllowanceStandardAtOrBefore(String period, String organizationCode, String positionCode) {
        String normalizedPeriod = period == null ? "" : period.replace(".", "");
        int category = allowanceStandardPeriodCategory(organizationCode);
        return queryString("""
                SELECT tbnd
                FROM bz06_jbt
                WHERE tbnd <= :period
                  AND UPPER(item) = 'DFBT2'
                  AND jxlb = :category
                ORDER BY tbnd DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("period", normalizedPeriod)
                .addValue("category", category));
    }

    String latestAllowanceStandardWithPositionRowAtOrBefore(
            String period,
            String organizationCode,
            String positionCode) {
        String normalizedPeriod = period == null ? "" : period.replace(".", "");
        String lookupPositionCode = performancePositionCode(positionCode, normalizedPeriod);
        if (emptyToNull(lookupPositionCode) == null) {
            return "";
        }
        int category = allowanceAmountLookupCategory(organizationCode, positionCode, normalizedPeriod);
        String withCategory = queryString("""
                SELECT tbnd
                FROM bz06_jbt
                WHERE tbnd <= :period
                  AND UPPER(item) = 'DFBT2'
                  AND zwbm = :positionCode
                  AND jxlb = :category
                ORDER BY tbnd DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("period", normalizedPeriod)
                .addValue("positionCode", lookupPositionCode)
                .addValue("category", category));
        if (emptyToNull(withCategory) != null) {
            return withCategory;
        }
        if (!requiresAllowanceCategoryFilter(positionCode)) {
            return queryString("""
                    SELECT tbnd
                    FROM bz06_jbt
                    WHERE tbnd <= :period
                      AND UPPER(item) = 'DFBT2'
                      AND zwbm = :positionCode
                    ORDER BY tbnd DESC
                    LIMIT 1
                    """, new MapSqlParameterSource()
                    .addValue("period", normalizedPeriod)
                    .addValue("positionCode", lookupPositionCode));
        }
        return "";
    }

    /** 对齐 jbtbz.prg：按单位 jxlb 取 DFBT2 最近标准年月（参公 jxlb=2 时查 jxlb=5）。 */
    int allowanceStandardPeriodCategory(String organizationCode) {
        return mapOrganizationAllowanceCategory(organizationPerformanceCategory(organizationCode));
    }

    /** 对齐 dfbt2.prg / dfbtbz.prg：金额查询用单位 jxlb，事业岗位 07–19 按 jxlb 过滤。 */
    int allowanceAmountLookupCategory(String organizationCode, String positionCode, String standardYearMonth) {
        if (requiresAllowanceCategoryFilter(positionCode)) {
            if (emptyToNull(standardYearMonth) != null && standardYearMonth.compareTo("201410") >= 0) {
                return 5;
            }
            return mapOrganizationAllowanceCategory(organizationPerformanceCategory(organizationCode));
        }
        return mapOrganizationAllowanceCategory(organizationPerformanceCategory(organizationCode));
    }

    private boolean requiresAllowanceCategoryFilter(String positionCode) {
        String normalized = emptyToNull(positionCode);
        if (normalized == null || normalized.length() < 2) {
            return false;
        }
        String prefix = normalized.substring(0, 2);
        return prefix.compareTo("06") > 0 && prefix.compareTo("20") < 0;
    }

    int allowanceLookupCategory(String organizationCode, String positionCode, String standardYearMonth) {
        return allowanceAmountLookupCategory(organizationCode, positionCode, standardYearMonth);
    }

    private int mapOrganizationAllowanceCategory(int organizationCategory) {
        if (organizationCategory == 2) {
            return 5;
        }
        return organizationCategory > 0 ? organizationCategory : 1;
    }

    boolean hasBasicSalaryStandardForSource(String standardYearMonth, String baseSalarySource) {
        if (emptyToNull(standardYearMonth) == null || emptyToNull(baseSalarySource) == null) {
            return false;
        }
        String tableName = switch (baseSalarySource) {
            case "GRADE" -> "bz06_jbgz";
            case "POLICE_GRADE" -> "bz06_djgz";
            case "SALARY_LEVEL" -> "bz06_xjgz";
            default -> null;
        };
        if (tableName == null) {
            return false;
        }
        Integer count = queryInteger("""
                SELECT COUNT(*)
                FROM %s
                WHERE tbnd = :standardYearMonth
                """.formatted(tableName), new MapSqlParameterSource("standardYearMonth", emptyToNull(standardYearMonth)));
        if (count != null && count > 0) {
            return true;
        }
        Integer positionCount = queryInteger("""
                SELECT COUNT(*)
                FROM bz06_zwgz
                WHERE tbnd = :standardYearMonth
                """, new MapSqlParameterSource("standardYearMonth", emptyToNull(standardYearMonth)));
        return positionCount != null && positionCount > 0;
    }

    int floatingSalary(String standardYearMonth, String positionCode, String salaryLevel, String floatingStep) {
        if (emptyToNull(standardYearMonth) == null || emptyToNull(positionCode) == null
                || emptyToNull(salaryLevel) == null || intValue(floatingStep) == 0) {
            return 0;
        }
        int baseLevel = intValue(salaryLevel);
        int targetLevel = baseLevel + intValue(floatingStep);
        if (baseLevel <= 0 || targetLevel <= 0) {
            return 0;
        }
        String jobCategory = positionCode.substring(0, Math.min(2, positionCode.length()));
        Integer base = queryInteger("""
                SELECT bz
                FROM bz06_xjgz
                WHERE tbnd = :standardYearMonth AND gwflbm = :jobCategory AND xj = :salaryLevel
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("jobCategory", jobCategory)
                .addValue("salaryLevel", leftPadTwo(salaryLevel)));
        Integer target = queryInteger("""
                SELECT bz
                FROM bz06_xjgz
                WHERE tbnd = :standardYearMonth AND gwflbm = :jobCategory AND xj = :salaryLevel
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("jobCategory", jobCategory)
                .addValue("salaryLevel", leftPadTwo(String.valueOf(targetLevel))));
        return Math.max(target - base, 0);
    }

    int bonusBalance(PayrollHistorySnapshot history) {
        String positionCode = bonusBalancePositionCode(history);
        if (emptyToNull(positionCode) == null) {
            return 0;
        }
        int workYears = bonusBalanceWorkYears(history);
        int tier = bonusBalanceTier(positionCode, workYears);
        return queryInteger("""
                SELECT a%s
                FROM bz06_jjjy
                WHERE zwbm = :positionCode
                LIMIT 1
                """.formatted(tier), new MapSqlParameterSource("positionCode", normalizeBonusBalancePositionCode(positionCode)));
    }

    int bonusBalanceMode() {
        return queryInteger("""
                SELECT jjjy
                FROM cyxx
                ORDER BY ID
                LIMIT 1
                """, new MapSqlParameterSource());
    }

    int postAllowance(String standardYearMonth, String category) {
        if (emptyToNull(standardYearMonth) == null || emptyToNull(category) == null) {
            return 0;
        }
        return queryInteger("""
                SELECT bz
                FROM bz_gwjt
                WHERE tbnd = :standardYearMonth AND lb = :category
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("category", emptyToNull(category)));
    }

    String performancePositionCode(String positionCode, String standardYearMonth) {
        String normalized = emptyToNull(positionCode);
        if (normalized == null) {
            return null;
        }
        if (normalized.startsWith("03") && normalized.length() >= 4) {
            String rank = normalized.substring(3, 4)
                    .replace("B", "A")
                    .replace("C", "B")
                    .replace("D", "B");
            normalized = "01" + rank + "0";
        }
        if (normalized.startsWith("04") && normalized.length() >= 5) {
            normalized = "01" + normalized.substring(3, 5);
        }
        if (normalized.contains("F") && emptyToNull(standardYearMonth) != null
                && standardYearMonth.length() >= 4 && standardYearMonth.substring(0, 4).compareTo("2009") <= 0) {
            normalized = normalized.substring(0, Math.min(3, normalized.length())) + "F";
        }
        return normalized;
    }

    String subsidyPositionCode(String positionCode) {
        return switch (emptyToNull(positionCode) == null ? "" : positionCode.trim()) {
            case "0427" -> "2707";
            case "0428" -> "2708";
            case "0429" -> "2709";
            case "042A" -> "2710";
            case "042B" -> "2711";
            case "0417" -> "2607";
            case "0418" -> "2608";
            case "0419" -> "2609";
            case "041A" -> "2610";
            case "041B" -> "2611";
            case "0437" -> "2807";
            case "0438" -> "2808";
            case "0439" -> "2809";
            case "043A" -> "2810";
            case "043B" -> "2811";
            default -> emptyToNull(positionCode);
        };
    }

    int organizationPerformanceCategory(String organizationCode) {
        return queryInteger("""
                SELECT jxlb
                FROM dwbm
                WHERE dwbm = :organizationCode
                LIMIT 1
                """, new MapSqlParameterSource("organizationCode", emptyToNull(organizationCode)));
    }

    String organizationPerformanceRatio(String organizationCode) {
        List<String> values = jdbcTemplate.queryForList("""
                SELECT jxbl
                FROM dwbm
                WHERE dwbm = :organizationCode
                LIMIT 1
                """, new MapSqlParameterSource("organizationCode", emptyToNull(organizationCode)), String.class);
        if (values.isEmpty()) {
            return null;
        }
        return SqlText.trim(values.getFirst());
    }

    int organizationYearAllowanceCategory(String organizationCode) {
        Integer value = queryInteger("""
                SELECT njbt
                FROM dwbm
                WHERE dwbm = :organizationCode AND gzczbz = '事业管理'
                LIMIT 1
                """, new MapSqlParameterSource("organizationCode", emptyToNull(organizationCode)));
        if (value == null || value <= 0) {
            return 0;
        }
        return Math.max(value - 1, 0);
    }

    BigDecimal performanceAllowance(String organizationCode, String positionCode, String standardYearMonth) {
        return performanceAllowance(organizationCode, positionCode, standardYearMonth, null);
    }

    BigDecimal performanceAllowance(
            String organizationCode,
            String positionCode,
            String standardYearMonth,
            String performanceRatioOverride) {
        String normalizedPositionCode = performancePositionCode(positionCode, standardYearMonth);
        int category = allowanceAmountLookupCategory(organizationCode, positionCode, standardYearMonth);
        Integer amount = queryPerformanceAllowanceAmount(
                normalizedPositionCode, standardYearMonth, category, requiresAllowanceCategoryFilter(positionCode));
        if (isProbationPosition(normalizedPositionCode) || isCivilServantPosition(normalizedPositionCode)) {
            return BigDecimal.valueOf(nullToZero(amount));
        }

        String ratio = emptyToNull(performanceRatioOverride);
        if (ratio == null) {
            ratio = organizationPerformanceRatio(organizationCode);
        }
        return applyPerformanceRatio(nullToZero(amount), ratio);
    }

    BigDecimal applyPerformanceRatio(int baseAmount, String ratioText) {
        if (baseAmount <= 0 || emptyToNull(ratioText) == null) {
            return BigDecimal.ZERO;
        }
        String normalizedRatio = ratioText.replace("：", ":").replace("/", ":");
        int separatorIndex = normalizedRatio.indexOf(':');
        if (separatorIndex <= 0 || separatorIndex >= normalizedRatio.length() - 1) {
            return BigDecimal.ZERO;
        }
        BigDecimal left = new BigDecimal(normalizedRatio.substring(0, separatorIndex).trim());
        BigDecimal right = new BigDecimal(normalizedRatio.substring(separatorIndex + 1).trim());
        if (left.add(right).compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(baseAmount)
                .multiply(BigDecimal.TEN)
                .multiply(left)
                .divide(left.add(right), 8, java.math.RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(7), 8, java.math.RoundingMode.HALF_UP);
    }

    int subsidyAllowance(String organizationCode, String positionCode, String standardYearMonth) {
        String normalizedPositionCode = subsidyPositionCode(positionCode);
        if (isProbationPosition(normalizedPositionCode)) {
            return 0;
        }
        int category = allowanceAmountLookupCategory(organizationCode, positionCode, standardYearMonth);
        Integer amount = queryInteger("""
                SELECT bz
                FROM bz06_jbt
                WHERE UPPER(item) = 'SDBT' AND zwbm = :positionCode AND tbnd = :standardYearMonth AND jxlb = :category
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("positionCode", normalizedPositionCode)
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("category", category));
        if (nullToZero(amount) > 0 || requiresAllowanceCategoryFilter(positionCode)) {
            return nullToZero(amount);
        }
        return queryInteger("""
                SELECT bz
                FROM bz06_jbt
                WHERE UPPER(item) = 'SDBT' AND zwbm = :positionCode AND tbnd = :standardYearMonth
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("positionCode", normalizedPositionCode)
                .addValue("standardYearMonth", emptyToNull(standardYearMonth)));
    }

    private Integer queryPerformanceAllowanceAmount(
            String positionCode,
            String standardYearMonth,
            int category,
            boolean requiresCategoryFilter) {
        Integer amount = queryInteger("""
                SELECT bz
                FROM bz06_jbt
                WHERE UPPER(item) = 'DFBT2' AND zwbm = :positionCode AND tbnd = :standardYearMonth AND jxlb = :category
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("positionCode", positionCode)
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("category", category));
        if (nullToZero(amount) > 0 || requiresCategoryFilter) {
            return amount;
        }
        return queryInteger("""
                SELECT bz
                FROM bz06_jbt
                WHERE UPPER(item) = 'DFBT2' AND zwbm = :positionCode AND tbnd = :standardYearMonth
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("positionCode", positionCode)
                .addValue("standardYearMonth", emptyToNull(standardYearMonth)));
    }

    private static int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    int retainedAllowance(String positionCode) {
        String normalizedPositionCode = emptyToNull(positionCode);
        if (isProbationPosition(normalizedPositionCode)) {
            normalizedPositionCode = normalizedPositionCode.substring(0, Math.min(3, normalizedPositionCode.length())) + "F";
        }
        return queryInteger("""
                SELECT bz
                FROM bz06_blfb
                WHERE zwbm = :positionCode
                LIMIT 1
                """, new MapSqlParameterSource("positionCode", normalizedPositionCode));
    }

    BigDecimal yearAllowance(String organizationCode, String standardYearMonth) {
        int category = organizationYearAllowanceCategory(organizationCode);
        if (category <= 0 || category > 4) {
            return BigDecimal.ZERO;
        }
        String column = "a" + category;
        List<BigDecimal> values = jdbcTemplate.queryForList("""
                SELECT %s
                FROM njbt
                WHERE tbnd = :standardYearMonth
                LIMIT 1
                """.formatted(column), new MapSqlParameterSource("standardYearMonth", emptyToNull(standardYearMonth)), BigDecimal.class);
        if (values.isEmpty() || values.getFirst() == null) {
            return BigDecimal.ZERO;
        }
        return values.getFirst();
    }

    BigDecimal decimalValue(Map<String, Object> row, String fieldName) {
        Object value = row.get(fieldName);
        if (value == null) {
            value = row.get(fieldName.toLowerCase());
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value instanceof String text && !text.isBlank()) {
            return new BigDecimal(text.trim());
        }
        return BigDecimal.ZERO;
    }

    String mapPositionSalaryCode(String positionCode) {
        return mapPositionSalaryCodeStatic(positionCode);
    }

    private static String mapPositionSalaryCodeStatic(String positionCode) {
        return switch (staticEmptyToNull(positionCode) == null ? "" : positionCode.trim()) {
            case "0416", "0426" -> "0161";
            case "0417", "0427", "0437" -> "0171";
            case "0418", "0428", "0438" -> "0181";
            case "0419", "0429", "0439" -> "0191";
            case "041A", "042A", "043A" -> "01A1";
            case "041B", "042B", "043B" -> "01B0";
            case "043C" -> "01C0";
            default -> staticEmptyToNull(positionCode);
        };
    }

    private static String staticEmptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 任职表职务编码与工资职务编码对齐（对应旧系统 chgZwbm.prg），避免 0116/0117 等旧码
     * 在推算时无法匹配 bz06 级别范围而退回错误职务。
     */
    static String normalizeAppointmentPositionCode(String positionCode) {
        String normalized = staticEmptyToNull(positionCode);
        if (normalized == null) {
            return null;
        }
        String trimmed = normalized.trim();
        if (trimmed.length() >= 4 && List.of("01", "02", "03").contains(trimmed.substring(0, 2))) {
            trimmed = switch (trimmed) {
                case "0117", "0217", "0317" -> "01B0";
                case "0115", "0215", "0315" -> "01A0";
                case "0116", "0216", "0316" -> "01A1";
                case "0113", "0213", "0313" -> "0190";
                case "0114", "0214", "0314" -> "0191";
                case "0111", "0211", "0311" -> "0180";
                case "0112", "0212", "0312" -> "0181";
                case "0118", "0218", "0318" -> "01C0";
                case "0101", "0201", "0301" -> "0110";
                case "0102", "0202", "0302" -> "0120";
                case "0103", "0203", "0303" -> "0130";
                case "0104", "0204", "0304" -> "0140";
                case "0105", "0205", "0305" -> "0150";
                case "0106", "0206", "0306" -> "0151";
                case "0107", "0207", "0307" -> "0160";
                case "0108", "0208", "0308" -> "0161";
                case "0109", "0209", "0309" -> "0170";
                case "0110", "0210", "0310" -> "0171";
                case "0199", "0299", "0399" -> "01FF";
                default -> trimmed;
            };
        }
        String mapped = mapPositionSalaryCodeStatic(trimmed);
        return mapped == null ? trimmed : mapped;
    }

    int intValue(String value) {
        String trimmed = emptyToNull(value);
        if (trimmed == null) {
            return 0;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private MapSqlParameterSource standardParameters(String standardYearMonth, String positionCode) {
        return new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("positionCode", emptyToNull(positionCode));
    }

    private MapSqlParameterSource otherAllowanceParameters(String standardYearMonth, String code) {
        return new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("code", emptyToNull(code));
    }

    private MapSqlParameterSource internSalaryStandardParameters(InternSalaryStandardRequest request) {
        return new MapSqlParameterSource()
                .addValue("standardYearMonth", request.standardYearMonth())
                .addValue("educationCode", request.educationCode())
                .addValue("educationName", request.educationName())
                .addValue("regularPositionCode", request.regularPositionCode())
                .addValue("regularPositionName", request.regularPositionName())
                .addValue("regularGradeStep", request.regularGradeStep())
                .addValue("regularLevel", request.regularLevel())
                .addValue("firstYearAmount", request.firstYearAmount() == null ? 0 : request.firstYearAmount())
                .addValue("secondYearAmount", request.secondYearAmount() == null ? 0 : request.secondYearAmount());
    }

    private MapSqlParameterSource wageReformStandardParameters(WageReformStandardRequest request) {
        return new MapSqlParameterSource()
                .addValue("positionCode", request.positionCode())
                .addValue("appointmentYearsLower", request.appointmentYearsLower() == null ? 0 : request.appointmentYearsLower())
                .addValue("appointmentYearsUpper", request.appointmentYearsUpper() == null ? 0 : request.appointmentYearsUpper())
                .addValue("reformYearsLower", request.reformYearsLower() == null ? 0 : request.reformYearsLower())
                .addValue("reformYearsUpper", request.reformYearsUpper() == null ? 0 : request.reformYearsUpper())
                .addValue("convertedLevel", request.convertedLevel())
                .addValue("convertedStep", request.convertedStep());
    }

    private MapSqlParameterSource otherAllowanceWriteParameters(OtherAllowanceStandardRequest request) {
        return new MapSqlParameterSource()
                .addValue("standardYearMonth", request.standardYearMonth())
                .addValue("code", request.code())
                .addValue("amount", request.amount() == null ? 0 : request.amount())
                .addValue("averageAmount", request.averageAmount() == null ? 0 : request.averageAmount())
                .addValue("multiplier", request.multiplier() == null ? 0 : request.multiplier());
    }

    private String normalizeOtherAllowanceType(String standardType) {
        String normalized = emptyToNull(standardType);
        if (normalized == null) {
            throw new IllegalArgumentException("标准类型不能为空。");
        }
        return normalized.trim().toLowerCase();
    }

    private MapSqlParameterSource payrollHistoryParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String period,
            String keyword) {
        String trimmedKeyword = emptyToNull(keyword);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationCode", emptyToNull(organizationCode))
                .addValue("period", emptyToNull(period))
                .addValue("keyword", trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null ? null : "%" + trimmedKeyword + "%");
    }

    private MapSqlParameterSource payrollChangeParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword) {
        String trimmedKeyword = emptyToNull(keyword);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationCode", emptyToNull(organizationCode))
                .addValue("keyword", trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null ? null : "%" + trimmedKeyword + "%");
    }

    private MapSqlParameterSource payrollHistoryRequestParameters(PayrollHistoryMaintenanceRequest request) {
        return new MapSqlParameterSource()
                .addValue("calculationYear", valueOrBlank(request.calculationYear()))
                .addValue("calculationMonth", valueOrBlank(request.calculationMonth()))
                .addValue("changeType", valueOrBlank(request.changeType()))
                .addValue("positionCode", valueOrBlank(request.positionCode()))
                .addValue("positionName", valueOrBlank(request.positionName()))
                .addValue("positionSalary", request.positionSalary() == null ? 0 : request.positionSalary())
                .addValue("gradeSalary", request.gradeSalary() == null ? 0 : request.gradeSalary())
                .addValue("technicalGradeSalary", request.technicalGradeSalary() == null ? 0 : request.technicalGradeSalary())
                .addValue("performanceAllowance", request.performanceAllowance() == null ? 0 : request.performanceAllowance())
                .addValue("retainedAllowance", request.retainedAllowance() == null ? 0 : request.retainedAllowance())
                .addValue("totalAmount", request.totalAmount() == null ? 0 : request.totalAmount());
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
        String trimmed = emptyToNull(value);
        return trimmed == null ? "" : trimmed;
    }

    private static boolean isCurrentPayroll(String successorId) {
        String trimmedSuccessorId = SqlText.trim(successorId);
        return trimmedSuccessorId == null || trimmedSuccessorId.isEmpty();
    }

    private MapSqlParameterSource basicStandardParameters(String standardYearMonth, String code) {
        return new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("code", emptyToNull(code));
    }

    private MapSqlParameterSource rankAllowanceStandardParameters(RankAllowanceStandardRequest request) {
        return new MapSqlParameterSource()
                .addValue("standardYearMonth", request.standardYearMonth())
                .addValue("rankCode", request.rankCode())
                .addValue("rankName", request.rankName())
                .addValue("amount", request.amount() == null ? 0 : request.amount())
                .addValue("category", request.category());
    }

    private MapSqlParameterSource retainedAllowanceStandardParameters(RetainedAllowanceStandardRequest request) {
        return new MapSqlParameterSource()
                .addValue("positionCode", request.positionCode())
                .addValue("name", request.name())
                .addValue("amount", request.amount() == null ? 0 : request.amount());
    }

    private MapSqlParameterSource yearAllowanceStandardParameters(YearAllowanceStandardRequest request) {
        return new MapSqlParameterSource()
                .addValue("standardYearMonth", request.standardYearMonth())
                .addValue("categoryOneAmount", request.categoryOneAmount() == null ? 0 : request.categoryOneAmount())
                .addValue("categoryTwoAmount", request.categoryTwoAmount() == null ? 0 : request.categoryTwoAmount())
                .addValue("categoryThreeAmount", request.categoryThreeAmount() == null ? 0 : request.categoryThreeAmount())
                .addValue("categoryFourAmount", request.categoryFourAmount() == null ? 0 : request.categoryFourAmount());
    }

    private MapSqlParameterSource positionSalaryStandardParameters(PositionSalaryStandardRequest request) {
        return new MapSqlParameterSource()
                .addValue("standardYearMonth", request.standardYearMonth())
                .addValue("positionCode", request.positionCode())
                .addValue("amount", request.amount() == null ? 0 : request.amount());
    }

    private MapSqlParameterSource gradeSalaryStandardParameters(
            String standardYearMonth,
            String gradeLevel,
            List<Integer> gradeSteps) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("gradeLevel", gradeLevel);
        appendGradeStepParameters(parameters, gradeSteps);
        return parameters;
    }

    private MapSqlParameterSource positionGradeSalaryStandardParameters(
            String standardYearMonth,
            String positionCode,
            Integer technicalGradeSalary,
            List<Integer> gradeSteps) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("positionCode", positionCode)
                .addValue("technicalGradeSalary", technicalGradeSalary == null ? 0 : technicalGradeSalary);
        appendGradeStepParameters(parameters, gradeSteps);
        return parameters;
    }

    private MapSqlParameterSource salaryLevelStandardParameters(SalaryLevelStandardRequest request) {
        return new MapSqlParameterSource()
                .addValue("standardYearMonth", request.standardYearMonth())
                .addValue("jobCategoryCode", request.jobCategoryCode())
                .addValue("salaryLevel", request.salaryLevel())
                .addValue("amount", request.amount() == null ? 0 : request.amount())
                .addValue("baseAmount", request.baseAmount() == null ? 0 : request.baseAmount())
                .addValue("baseAmountExtra", request.baseAmountExtra() == null ? 0 : request.baseAmountExtra());
    }

    private void appendGradeStepParameters(MapSqlParameterSource parameters, List<Integer> gradeSteps) {
        for (int step = 1; step <= 20; step++) {
            parameters.addValue("dc" + step, gradeStepAmount(gradeSteps, step));
        }
    }

    private int gradeStepAmount(List<Integer> gradeSteps, int step) {
        if (gradeSteps == null || gradeSteps.size() < step) {
            return 0;
        }
        Integer amount = gradeSteps.get(step - 1);
        return amount == null ? 0 : amount;
    }

    private List<Integer> readGradeSteps(java.sql.ResultSet rs) throws java.sql.SQLException {
        List<Integer> steps = new java.util.ArrayList<>(20);
        for (int step = 1; step <= 20; step++) {
            steps.add(rs.getInt("dc" + step));
        }
        return steps;
    }

    private BasicStandardQuery basicStandardQuery(String standardType) {
        return switch (emptyToNull(standardType) == null ? "" : standardType.trim().toLowerCase()) {
            case "position" -> new BasicStandardQuery(
                    "bz06_zwgz",
                    "tbnd, zwbm, bz",
                    "zwbm = :code",
                    "tbnd DESC, zwbm");
            case "position-grade" -> new BasicStandardQuery(
                    "bz06_zwgz_gr",
                    "tbnd, zwbm, dc1, dc2, dc3, dc4, dc5, dc6, dc7, dc8, dc9, dc10, dc11, dc12, dc13, dc14, dc15, dc16, dc17, dc18, dc19, dc20, jsdjgz",
                    "zwbm = :code",
                    "tbnd DESC, zwbm");
            case "grade" -> new BasicStandardQuery(
                    "bz06_jbgz",
                    "tbnd, jb, dc1, dc2, dc3, dc4, dc5, dc6, dc7, dc8, dc9, dc10, dc11, dc12, dc13, dc14, dc15, dc16, dc17, dc18, dc19, dc20",
                    "jb = :code",
                    "tbnd DESC, CAST(jb AS UNSIGNED)");
            case "salary-level" -> new BasicStandardQuery(
                    "bz06_xjgz",
                    "tbnd, gwflbm, xj, bz, jc, jce",
                    "gwflbm = :code OR xj = :code",
                    "tbnd DESC, gwflbm, xj");
            default -> throw new IllegalArgumentException("Unsupported basic standard type: " + standardType);
        };
    }

    private record BasicStandardQuery(String tableName, String columns, String codePredicate, String orderBy) {
    }

    private OtherAllowanceStandardQuery otherAllowanceStandardQuery(String standardType) {
        return switch (emptyToNull(standardType) == null ? "" : standardType.trim().toLowerCase()) {
            case "property" -> new OtherAllowanceStandardQuery(
                    "property",
                    "bz_wybt",
                    "tbnd, zwbm AS code, NULL AS name, bz AS amount, NULL AS average_amount, NULL AS multiplier",
                    "(:standardYearMonth IS NULL OR tbnd = :standardYearMonth) AND (:code IS NULL OR zwbm = :code)",
                    "tbnd DESC, zwbm");
            case "communication" -> new OtherAllowanceStandardQuery(
                    "communication",
                    "bz_txbt",
                    "tbnd, zwbm AS code, NULL AS name, bz AS amount, NULL AS average_amount, NULL AS multiplier",
                    "(:standardYearMonth IS NULL OR tbnd = :standardYearMonth) AND (:code IS NULL OR zwbm = :code)",
                    "tbnd DESC, zwbm");
            case "civilized" -> new OtherAllowanceStandardQuery(
                    "civilized",
                    "bz_wmj",
                    "NULL AS tbnd, jb AS code, NULL AS name, bz AS amount, NULL AS average_amount, mul AS multiplier",
                    "(:code IS NULL OR jb = :code)",
                    "jb");
            case "assessment" -> new OtherAllowanceStandardQuery(
                    "assessment",
                    "bz_pskhj",
                    "tbnd, khjg AS code, khjg AS name, bz AS amount, pjsp AS average_amount, NULL AS multiplier",
                    "(:standardYearMonth IS NULL OR tbnd = :standardYearMonth) AND (:code IS NULL OR khjg = :code)",
                    "tbnd DESC, khjg");
            default -> throw new IllegalArgumentException("Unsupported other allowance standard type: " + standardType);
        };
    }

    private record OtherAllowanceStandardQuery(String standardType, String tableName, String columns, String whereClause, String orderBy) {
    }

    private String emptyToNull(String value) {
        String trimmed = SqlText.trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }

    private int gradeSalaryAtStep(int[] gradeSalaryRow, int step) {
        if (step <= 0 || step > 20) {
            return 0;
        }
        return gradeSalaryRow[step];
    }

    private int[] gradeSalaryRow(String gradeLevel, String standardYearMonth) {
        String standard = emptyToNull(standardYearMonth);
        int level = intValue(gradeLevel);
        if (standard == null || level <= 0) {
            return EMPTY_GRADE_SALARY_ROW;
        }
        return gradeSalaryRowCache.computeIfAbsent(standard + "|" + level, key -> loadGradeSalaryRow(standard, level));
    }

    private int[] loadGradeSalaryRow(String standardYearMonth, int level) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT dc1, dc2, dc3, dc4, dc5, dc6, dc7, dc8, dc9, dc10,
                       dc11, dc12, dc13, dc14, dc15, dc16, dc17, dc18, dc19, dc20
                FROM bz06_jbgz
                WHERE tbnd = :standardYearMonth AND CAST(jb AS UNSIGNED) = :gradeLevel
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth)
                .addValue("gradeLevel", level));
        int[] values = new int[21];
        if (!rows.isEmpty()) {
            Map<String, Object> row = rows.getFirst();
            for (int step = 1; step <= 20; step++) {
                Object value = row.get("dc" + step);
                values[step] = value instanceof Number number ? number.intValue() : 0;
            }
        }
        return values;
    }

    private Integer queryInteger(String sql, MapSqlParameterSource parameters) {
        List<Integer> values = jdbcTemplate.queryForList(sql, parameters, Integer.class);
        if (values.isEmpty() || values.getFirst() == null) {
            return 0;
        }
        return values.getFirst();
    }

    private String queryString(String sql, MapSqlParameterSource parameters) {
        List<String> values = jdbcTemplate.queryForList(sql, parameters, String.class);
        if (values.isEmpty() || values.getFirst() == null) {
            return "";
        }
        return SqlText.trim(values.getFirst());
    }

    private int highestGradeStep(String gradeLevel) {
        return switch (String.valueOf(intValue(gradeLevel))) {
            case "1" -> 6;
            case "2" -> 7;
            case "3", "23", "24" -> 8;
            case "4", "22" -> 9;
            case "5", "21" -> 10;
            case "6", "7", "8", "9", "10", "20" -> 11;
            case "11", "19" -> 12;
            case "12", "17", "18" -> 13;
            case "13", "14", "15", "16" -> 14;
            case "25" -> 7;
            case "26", "27" -> 6;
            default -> 99;
        };
    }

    private String leftPadTwo(String value) {
        String trimmed = emptyToNull(value);
        if (trimmed == null) {
            return null;
        }
        return trimmed.length() >= 2 ? trimmed : "0" + trimmed;
    }

    private boolean isProbationPosition(String positionCode) {
        return emptyToNull(positionCode) != null && positionCode.contains("F");
    }

    private boolean isCivilServantPosition(String positionCode) {
        if (emptyToNull(positionCode) == null || positionCode.length() < 2) {
            return false;
        }
        return List.of("01", "02", "03", "04", "05", "06", "21", "22", "23", "24", "25", "26", "27", "28", "29")
                .contains(positionCode.substring(0, 2));
    }

    private boolean isRankAllowanceEligible(String positionCode) {
        if (emptyToNull(positionCode) == null || positionCode.length() < 2) {
            return false;
        }
        return List.of("01", "02", "03", "21", "22", "23", "24", "25", "26", "27", "28")
                .contains(positionCode.substring(0, 2));
    }

    boolean isCivilServantRankAllowanceEligible(String positionCode) {
        if (emptyToNull(positionCode) == null || positionCode.length() < 2) {
            return false;
        }
        return List.of("01", "02", "03").contains(positionCode.substring(0, 2));
    }

    private String bonusBalancePositionCode(PayrollHistorySnapshot history) {
        if (bonusBalanceMode() == 1) {
            List<String> values = jdbcTemplate.queryForList("""
                    SELECT zwbm
                    FROM dryzwbh
                    WHERE dwbm = :organizationCode
                      AND grbm = :personCode
                      AND LEFT(zwbm, 2) = :organizationType
                      AND srny <= '1993.09'
                    ORDER BY srny DESC, id DESC
                    LIMIT 1
                    """, new MapSqlParameterSource()
                    .addValue("organizationCode", history.organizationCode())
                    .addValue("personCode", history.personCode())
                    .addValue("organizationType", history.organizationType()), String.class);
            if (!values.isEmpty()) {
                return SqlText.trim(values.getFirst());
            }
            return null;
        }
        return history.positionCode();
    }

    private int bonusBalanceWorkYears(PayrollHistorySnapshot history) {
        if (bonusBalanceMode() == 3) {
            return history.salaryYears();
        }
        int workYears = 1993 - yearOf(history.workStartYearMonth()) + 1;
        if (workYears > history.interruptedSalaryYears() + 1) {
            workYears -= history.interruptedSalaryYears();
        }
        if (workYears < 1 || emptyToNull(history.workStartYearMonth()) == null || history.workStartYearMonth().compareTo("1993.10.01") > 0) {
            return 1;
        }
        return workYears;
    }

    private int bonusBalanceTier(String positionCode, int workYears) {
        String normalized = normalizeBonusBalancePositionCode(positionCode);
        if (normalized != null && normalized.length() >= 2
                && List.of("05", "06", "08", "09").contains(normalized.substring(0, 2))) {
            return Math.min((workYears + 5) / 10 + 1, 5);
        }
        return Math.min(workYears / 10 + 1, 4);
    }

    private String normalizeBonusBalancePositionCode(String positionCode) {
        String normalized = emptyToNull(positionCode);
        if (normalized != null && normalized.compareTo("1000") > 0 && normalized.length() >= 2) {
            return "10" + normalized.substring(normalized.length() - 2);
        }
        return normalized;
    }

    private String internSalaryLookupPrefix(String positionCode) {
        String normalized = emptyToNull(positionCode);
        if (normalized == null || !normalized.contains("F")) {
            return null;
        }
        if (normalized.length() < 2) {
            return null;
        }
        String prefix = normalized.substring(0, 2);
        if (List.of("01", "02", "21", "22", "23", "24", "25", "26", "27", "28").contains(prefix)) {
            return "01";
        }
        if (normalized.compareTo("10") > 0) {
            return "10";
        }
        return prefix;
    }

    private String normalizeEducationStandardPositionCode(String positionCode) {
        String normalized = emptyToNull(positionCode);
        if (normalized == null || normalized.length() < 2) {
            return normalized;
        }
        String prefix = normalized.substring(0, 2);
        if (normalized.compareTo("10") > 0 && !List.of("21", "22", "23", "24", "25", "26", "27", "28").contains(prefix)
                && normalized.length() >= 4) {
            normalized = "10" + normalized.substring(2);
            prefix = normalized.substring(0, 2);
        }
        if (("03".equals(prefix) || "04".equals(prefix)) && normalized.length() >= 4) {
            normalized = "01" + normalized.substring(2);
        }
        return normalized;
    }

    private int yearOf(String yearOrYearMonth) {
        if (yearOrYearMonth == null || yearOrYearMonth.length() < 4) {
            return 0;
        }
        return intValue(yearOrYearMonth.substring(0, 4));
    }

    Optional<PensionBaseRecord> findPensionBaseRecord(String organizationCode, String personCode, String year) {
        return jdbcTemplate.query("""
                SELECT id, dwbm, grbm, xm, sfzh, nd, zwbm2, zwgw2, jbgzjb2, zwgzdc2,
                       zwgzse2, jbgzse2, jsdjgz2, jsfszwtg2, fdgz2, dfbt2, blfb2, jjjy2,
                       jxjt, jhljt, tgblbf, js, bz
                FROM jfjs
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND nd = :year
                ORDER BY id DESC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("year", year), (rs, rowNum) -> new PensionBaseRecord(
                rs.getInt("id"),
                SqlText.trim(rs.getString("dwbm")),
                SqlText.trim(rs.getString("grbm")),
                SqlText.trim(rs.getString("xm")),
                SqlText.trim(rs.getString("sfzh")),
                SqlText.trim(rs.getString("nd")),
                SqlText.trim(rs.getString("zwbm2")),
                SqlText.trim(rs.getString("zwgw2")),
                SqlText.trim(rs.getString("jbgzjb2")),
                SqlText.trim(rs.getString("zwgzdc2")),
                rs.getObject("zwgzse2", Integer.class),
                rs.getObject("jbgzse2", Integer.class),
                rs.getObject("jsdjgz2", Integer.class),
                rs.getObject("jsfszwtg2", Integer.class),
                rs.getObject("fdgz2", Integer.class),
                rs.getObject("dfbt2", Integer.class),
                rs.getObject("blfb2", Integer.class),
                rs.getObject("jjjy2", Integer.class),
                rs.getObject("jxjt", Integer.class),
                rs.getObject("jhljt", Integer.class),
                rs.getObject("tgblbf", Integer.class),
                rs.getObject("js", Integer.class),
                SqlText.trim(rs.getString("bz")))).stream().findFirst();
    }

    Optional<String> findPersonIdCard(String organizationCode, String personCode) {
        return jdbcTemplate.queryForList("""
                SELECT sfzh
                FROM dryjbxx
                WHERE dwbm = :organizationCode AND grbm = :personCode
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), String.class).stream()
                .map(SqlText::trim)
                .filter(value -> !value.isBlank())
                .findFirst();
    }

    int nextPensionBaseId() {
        Integer maxId = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) FROM jfjs", new MapSqlParameterSource(), Integer.class);
        return maxId == null ? 1 : maxId + 1;
    }

    void savePensionBaseRecord(PensionBaseRecord record) {
        Optional<PensionBaseRecord> existing = findPensionBaseRecord(
                record.organizationCode(), record.personCode(), record.year());
        if (existing.isPresent()) {
            jdbcTemplate.update("""
                    UPDATE jfjs
                    SET xm = :name,
                        sfzh = :idCard,
                        zwbm2 = :positionCode,
                        zwgw2 = :positionName,
                        jbgzjb2 = :gradeLevel,
                        zwgzdc2 = :gradeStep,
                        zwgzse2 = :positionSalary,
                        jbgzse2 = :gradeSalary,
                        jsdjgz2 = :technicalGradeSalary,
                        jsfszwtg2 = :salaryIncrease,
                        fdgz2 = :floatingSalary,
                        dfbt2 = :performanceAllowance,
                        blfb2 = :retainedAllowance,
                        jjjy2 = :bonusBalance,
                        jxjt = :rankAllowanceBonus,
                        jhljt = :teachingAllowance,
                        tgblbf = :postAllowanceBonus,
                        js = :averageSalary,
                        bz = :remark
                    WHERE id = :id
                    """, pensionBaseParameters(record).addValue("id", existing.get().id()));
            return;
        }
        jdbcTemplate.update("""
                INSERT INTO jfjs (
                    id, dwbm, grbm, xm, sfzh, nd, zwbm2, zwgw2, jbgzjb2, zwgzdc2,
                    zwgzse2, jbgzse2, jsdjgz2, jsfszwtg2, fdgz2, dfbt2, blfb2, jjjy2,
                    jxjt, jhljt, tgblbf, js, bz
                ) VALUES (
                    :id, :organizationCode, :personCode, :name, :idCard, :year, :positionCode, :positionName,
                    :gradeLevel, :gradeStep, :positionSalary, :gradeSalary, :technicalGradeSalary,
                    :salaryIncrease, :floatingSalary, :performanceAllowance, :retainedAllowance,
                    :bonusBalance, :rankAllowanceBonus, :teachingAllowance, :postAllowanceBonus,
                    :averageSalary, :remark
                )
                """, pensionBaseParameters(record).addValue("id", record.id()));
    }

    private MapSqlParameterSource pensionBaseParameters(PensionBaseRecord record) {
        return new MapSqlParameterSource()
                .addValue("organizationCode", record.organizationCode())
                .addValue("personCode", record.personCode())
                .addValue("name", valueOrBlank(record.name()))
                .addValue("idCard", valueOrBlank(record.idCard()))
                .addValue("year", record.year())
                .addValue("positionCode", valueOrBlank(record.positionCode()))
                .addValue("positionName", valueOrBlank(record.positionName()))
                .addValue("gradeLevel", valueOrBlank(record.gradeLevel()))
                .addValue("gradeStep", valueOrBlank(record.gradeStep()))
                .addValue("positionSalary", record.positionSalary() == null ? 0 : record.positionSalary())
                .addValue("gradeSalary", record.gradeSalary() == null ? 0 : record.gradeSalary())
                .addValue("technicalGradeSalary", record.technicalGradeSalary() == null ? 0 : record.technicalGradeSalary())
                .addValue("salaryIncrease", record.salaryIncrease() == null ? 0 : record.salaryIncrease())
                .addValue("floatingSalary", record.floatingSalary() == null ? 0 : record.floatingSalary())
                .addValue("performanceAllowance", record.performanceAllowance() == null ? 0 : record.performanceAllowance())
                .addValue("retainedAllowance", record.retainedAllowance() == null ? 0 : record.retainedAllowance())
                .addValue("bonusBalance", record.bonusBalance() == null ? 0 : record.bonusBalance())
                .addValue("rankAllowanceBonus", record.rankAllowanceBonus() == null ? 0 : record.rankAllowanceBonus())
                .addValue("teachingAllowance", record.teachingAllowance() == null ? 0 : record.teachingAllowance())
                .addValue("postAllowanceBonus", record.postAllowanceBonus() == null ? 0 : record.postAllowanceBonus())
                .addValue("averageSalary", record.averageSalary() == null ? 0 : record.averageSalary())
                .addValue("remark", valueOrBlank(record.remark()));
    }

    void deletePensionBaseRecord(String organizationCode, String personCode, String year, String remark) {
        jdbcTemplate.update("""
                DELETE FROM jfjs
                WHERE dwbm = :organizationCode
                  AND grbm = :personCode
                  AND nd = :year
                  AND bz = :remark
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode)
                .addValue("year", year)
                .addValue("remark", remark));
    }
}

package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.SqlText;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            rs.getInt("gznx"),
            rs.getInt("zdgznx"),
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

    private final NamedParameterJdbcTemplate jdbcTemplate;

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
                       h.dwsx, dw.dfbt, h.jzgb, h.spdw, p.cjgzny, p.gznx, p.zdgznx, h.jhlqsny, h.zdjhlnx, h.tgbl, h.jxjtbz, h.jx,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.fddc, h.jbgzjb2, h.djc2, h.tbnd, h.jbtbz,
                       h.gwjtbz, h.gwjtlb,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2, h.sdbt, h.blfb2,
                       h.jhljt, h.jsfszwtg2, h.jxjt, h.fdgz2, h.jjjy2, h.gwjt2, h.tgblbf,
                       h.pgbc, h.njbt, h.hj2
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE p.uid = :uid
                ORDER BY h.jsnf DESC, h.jsyf DESC, h.id DESC
                LIMIT 1
                """, new MapSqlParameterSource("uid", uid), HISTORY_MAPPER).stream().findFirst();
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
                SELECT h.id, h.dwbm, dw.dwmc, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                       h.ryfl, h.dwsx, h.zwbm2, h.zwgw2, h.zwgzdc2, h.jbgzjb2,
                       h.tbnd, h.jbtbz, h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.dfbt2,
                       h.blfb2, h.jxjt, h.fdgz2, h.jjjy2, h.jhljt, h.jsfszwtg2,
                       h.njbt, h.pgbc, h.hj2
                FROM hisbase h
                LEFT JOIN dwbm dw ON dw.dwbm = h.dwbm
                WHERE (:allOrganizations = TRUE OR h.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR h.dwbm = :organizationCode)
                  AND (:period IS NULL OR CONCAT(h.jsnf, h.jsyf) = :period)
                  AND (:keyword IS NULL OR h.grbm LIKE :keywordLike OR h.xm LIKE :keywordLike
                       OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY h.dwbm, h.grbm, h.jsnf DESC, h.jsyf DESC, h.id DESC
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
                ORDER BY h.jsnf DESC, h.jsyf DESC, h.id DESC
                LIMIT 1
                """, new MapSqlParameterSource("uid", uid));
        if (rows.isEmpty()) {
            throw new NotFoundException("Payroll history not found for personnel record: " + uid);
        }
        return new LinkedHashMap<>(rows.getFirst());
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

    int gradeSalary(String gradeLevel, String gradeStep, String standardYearMonth) {
        int step = intValue(gradeStep);
        if (step <= 0 || emptyToNull(gradeLevel) == null) {
            return 0;
        }
        String column = "dc" + Math.min(step, 20);
        Integer direct = queryInteger("""
                SELECT %s
                FROM bz06_jbgz
                WHERE tbnd = :standardYearMonth AND CAST(jb AS UNSIGNED) = :gradeLevel
                LIMIT 1
                """.formatted(column), new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("gradeLevel", intValue(gradeLevel)));

        if (direct == null) {
            return 0;
        }

        int highestStep = highestGradeStep(gradeLevel);
        if (step <= highestStep || highestStep <= 1 || highestStep > 20) {
            return direct;
        }

        Integer highest = gradeSalaryAtStep(gradeLevel, highestStep, standardYearMonth);
        Integer previous = gradeSalaryAtStep(gradeLevel, highestStep - 1, standardYearMonth);
        if (highest == null || previous == null) {
            return direct;
        }
        return highest + (highest - previous) * (step - highestStep);
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
        return queryInteger("""
                SELECT jtbz
                FROM jxjtbz
                WHERE tbnd = :standardYearMonth AND jx = :rankName AND (lb = '' OR lb = 'jx')
                ORDER BY CASE WHEN lb = '' THEN 0 ELSE 1 END
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(rankAllowanceStandardYearMonth))
                .addValue("rankName", emptyToNull(rankName)));
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
        return Math.max(value - 1, 0);
    }

    BigDecimal performanceAllowance(String organizationCode, String positionCode, String standardYearMonth) {
        String normalizedPositionCode = performancePositionCode(positionCode, standardYearMonth);
        int category = performanceCategoryFor(organizationCode, normalizedPositionCode, standardYearMonth);
        Integer amount = queryInteger("""
                SELECT bz
                FROM bz06_jbt
                WHERE UPPER(item) = 'DFBT2' AND zwbm = :positionCode AND tbnd = :standardYearMonth AND jxlb = :category
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("positionCode", normalizedPositionCode)
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("category", category));
        if (isProbationPosition(normalizedPositionCode) || isCivilServantPosition(normalizedPositionCode)) {
            return BigDecimal.valueOf(amount);
        }

        String ratio = organizationPerformanceRatio(organizationCode);
        if (ratio == null || !ratio.contains(":")) {
            return BigDecimal.ZERO;
        }
        String normalizedRatio = ratio.replace("：", ":");
        BigDecimal left = new BigDecimal(normalizedRatio.substring(0, normalizedRatio.indexOf(':')).trim());
        BigDecimal right = new BigDecimal(normalizedRatio.substring(normalizedRatio.indexOf(':') + 1).trim());
        if (left.add(right).compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(amount)
                .multiply(BigDecimal.TEN)
                .multiply(left)
                .divide(left.add(right), 8, java.math.RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(7), 8, java.math.RoundingMode.HALF_UP);
    }

    int subsidyAllowance(String positionCode, String standardYearMonth) {
        String normalizedPositionCode = subsidyPositionCode(positionCode);
        if (isProbationPosition(normalizedPositionCode)) {
            return 0;
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
        return switch (emptyToNull(positionCode) == null ? "" : positionCode.trim()) {
            case "0416", "0426" -> "0161";
            case "0417", "0427", "0437" -> "0171";
            case "0418", "0428", "0438" -> "0181";
            case "0419", "0429", "0439" -> "0191";
            case "041A", "042A", "043A" -> "01A1";
            case "041B", "042B", "043B" -> "01B0";
            case "043C" -> "01C0";
            default -> emptyToNull(positionCode);
        };
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

    private MapSqlParameterSource basicStandardParameters(String standardYearMonth, String code) {
        return new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("code", emptyToNull(code));
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

    private String emptyToNull(String value) {
        String trimmed = SqlText.trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }

    private Integer gradeSalaryAtStep(String gradeLevel, int step, String standardYearMonth) {
        if (step <= 0 || step > 20) {
            return null;
        }
        return queryInteger("""
                SELECT dc%s
                FROM bz06_jbgz
                WHERE tbnd = :standardYearMonth AND CAST(jb AS UNSIGNED) = :gradeLevel
                LIMIT 1
                """.formatted(step), new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("gradeLevel", intValue(gradeLevel)));
    }

    private Integer queryInteger(String sql, MapSqlParameterSource parameters) {
        List<Integer> values = jdbcTemplate.queryForList(sql, parameters, Integer.class);
        if (values.isEmpty() || values.getFirst() == null) {
            return 0;
        }
        return values.getFirst();
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

    private int performanceCategoryFor(String organizationCode, String positionCode, String standardYearMonth) {
        if (emptyToNull(positionCode) == null) {
            return 1;
        }
        if (emptyToNull(standardYearMonth) != null
                && standardYearMonth.compareTo("201410") >= 0
                && List.of("07", "08", "09", "10", "11").contains(positionCode.substring(0, Math.min(2, positionCode.length())))) {
            return 5;
        }
        if (List.of("07", "08", "09", "10", "11").contains(positionCode.substring(0, Math.min(2, positionCode.length())))) {
            return organizationPerformanceCategory(organizationCode);
        }
        return 1;
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

    private int yearOf(String yearOrYearMonth) {
        if (yearOrYearMonth == null || yearOrYearMonth.length() < 4) {
            return 0;
        }
        return intValue(yearOrYearMonth.substring(0, 4));
    }
}

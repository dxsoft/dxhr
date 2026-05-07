package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.SqlText;
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

    private static final RowMapper<PayrollHistorySnapshot> HISTORY_MAPPER = (rs, rowNum) -> new PayrollHistorySnapshot(
            SqlText.trim(rs.getString("id")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("jsnf")),
            SqlText.trim(rs.getString("jsyf")),
            SqlText.trim(rs.getString("jslb")),
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            SqlText.trim(rs.getString("zwgzdc2")),
            SqlText.trim(rs.getString("jbgzjb2")),
            SqlText.trim(rs.getString("djc2")),
            SqlText.trim(rs.getString("tbnd")),
            SqlText.trim(rs.getString("jbtbz")),
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

    Optional<PayrollHistorySnapshot> findLatestHistory(int uid) {
        return jdbcTemplate.query("""
                SELECT h.id, h.dwbm, h.grbm, h.xm, h.jsnf, h.jsyf, h.jslb,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.jbgzjb2, h.djc2, h.tbnd, h.jbtbz, h.hj2
                FROM hisbase h
                JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                WHERE p.uid = :uid
                ORDER BY h.jsnf DESC, h.jsyf DESC, h.id DESC
                LIMIT 1
                """, new MapSqlParameterSource("uid", uid), HISTORY_MAPPER).stream().findFirst();
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

    private MapSqlParameterSource standardParameters(String standardYearMonth, String positionCode) {
        return new MapSqlParameterSource()
                .addValue("standardYearMonth", emptyToNull(standardYearMonth))
                .addValue("positionCode", emptyToNull(positionCode));
    }

    private String emptyToNull(String value) {
        String trimmed = SqlText.trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }
}

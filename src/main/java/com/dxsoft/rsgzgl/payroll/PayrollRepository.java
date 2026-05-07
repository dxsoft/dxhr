package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.SqlText;
import java.util.List;
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

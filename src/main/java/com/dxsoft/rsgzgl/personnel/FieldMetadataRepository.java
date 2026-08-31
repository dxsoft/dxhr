package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.SqlText;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class FieldMetadataRepository {

    private static final RowMapper<FieldMetadataRecord> MAPPER = (rs, rowNum) -> new FieldMetadataRecord(
            SqlText.trim(rs.getString("field_name")),
            SqlText.trim(rs.getString("field_cap")),
            SqlText.trim(rs.getString("category")),
            rs.getInt("isgz") != 0,
            rs.getInt("readonly") != 0,
            rs.getInt("canbyhand") != 0,
            rs.getInt("sequence"));

    private final NamedParameterJdbcTemplate jdbcTemplate;

    FieldMetadataRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    List<FieldMetadataRecord> findRyjbxxFields(String unitProperty) {
        String specificCategory = PersonnelBasicFieldRegistry.resolveFldjbxxCategory(unitProperty);
        try {
            List<FieldMetadataRecord> rows = jdbcTemplate.query("""
                    SELECT field_name, field_cap, category, isgz, readonly, canbyhand, sequence
                    FROM fldjbxx
                    WHERE TRIM(tblname) = 'ryjbxx'
                      AND TRIM(sfsy) = '√'
                      AND category IN ('00', :specificCategory)
                      AND UPPER(TRIM(field_name)) NOT IN ('JSLB', 'JSNF', 'JSYF')
                    ORDER BY sequence, field_name
                    """, new MapSqlParameterSource("specificCategory", specificCategory), MAPPER);
            if (!rows.isEmpty()) {
                return rows;
            }
        } catch (Exception ignored) {
            // H2 / missing table — fall back to static bindings.
        }
        return PersonnelBasicFieldRegistry.defaultMetadata(unitProperty);
    }
}

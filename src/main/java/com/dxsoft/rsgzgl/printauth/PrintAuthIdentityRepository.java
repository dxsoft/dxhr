package com.dxsoft.rsgzgl.printauth;

import com.dxsoft.rsgzgl.common.SqlText;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class PrintAuthIdentityRepository {

    private final NamedParameterJdbcTemplate jdbc;

    PrintAuthIdentityRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 打印身份：code/name 取要打印单位 {@code dwbm}；membership 取 {@code cyxx.szds}（区县）。
     */
    PrintAuthIdentity findByOrganizationCode(String organizationCode) {
        String code = organizationCode == null ? "" : organizationCode.trim();
        if (code.isEmpty()) {
            return new PrintAuthIdentity(findMembership(), "", "");
        }
        List<PrintAuthIdentity> rows = jdbc.query("""
                SELECT dwbm, dwmc
                FROM dwbm
                WHERE TRIM(dwbm) = :code
                LIMIT 1
                """, new MapSqlParameterSource("code", code), (rs, rowNum) -> new PrintAuthIdentity(
                findMembership(),
                SqlText.trim(rs.getString("dwbm")),
                SqlText.trim(rs.getString("dwmc"))));
        if (!rows.isEmpty()) {
            return rows.getFirst();
        }
        return new PrintAuthIdentity(findMembership(), code, "");
    }

    private String findMembership() {
        List<String> rows = jdbc.query("""
                SELECT szds
                FROM cyxx
                ORDER BY ID
                LIMIT 1
                """, new MapSqlParameterSource(), (rs, rowNum) -> SqlText.trim(rs.getString("szds")));
        return rows.isEmpty() ? "" : empty(rows.getFirst());
    }

    private static String empty(String value) {
        return value == null ? "" : value.trim();
    }
}

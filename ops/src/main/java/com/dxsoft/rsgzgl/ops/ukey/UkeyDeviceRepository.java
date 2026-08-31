package com.dxsoft.rsgzgl.ops.ukey;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class UkeyDeviceRepository {

    private final JdbcTemplate jdbcTemplate;

    UkeyDeviceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    List<UkeyDeviceView> findAll(String keyword) {
        String trimmed = keyword == null || keyword.isBlank() ? null : keyword.trim();
        if (trimmed == null) {
            return jdbcTemplate.query("""
                    SELECT id, chip_id, sm2_user_id, pubkey_x, pubkey_y, enc_algo_key, auth_modes,
                           username, org_code, note, status, provisioned_at
                    FROM ukey_device
                    ORDER BY provisioned_at DESC, id DESC
                    """, this::mapRow);
        }
        String like = "%" + trimmed + "%";
        return jdbcTemplate.query("""
                SELECT id, chip_id, sm2_user_id, pubkey_x, pubkey_y, enc_algo_key, auth_modes,
                       username, org_code, note, status, provisioned_at
                FROM ukey_device
                WHERE chip_id LIKE ? OR sm2_user_id LIKE ? OR username LIKE ? OR org_code LIKE ?
                   OR note LIKE ? OR enc_algo_key LIKE ?
                ORDER BY provisioned_at DESC, id DESC
                """, this::mapRow, like, like, like, like, like, like);
    }

    Long findIdByChipId(String chipId) {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT id FROM ukey_device WHERE chip_id = ?",
                Long.class,
                chipId);
        return ids.isEmpty() ? null : ids.getFirst();
    }

    UkeyDeviceView upsert(UkeyDeviceRegisterRequest request) {
        String chipId = request.chipId().trim();
        Long existing = findIdByChipId(chipId);
        if (existing == null) {
            jdbcTemplate.update("""
                    INSERT INTO ukey_device
                    (chip_id, sm2_user_id, pubkey_x, pubkey_y, enc_algo_key, auth_modes,
                     username, org_code, note, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')
                    """,
                    chipId,
                    blankToNull(request.sm2UserId()),
                    upperOrNull(request.pubkeyX()),
                    upperOrNull(request.pubkeyY()),
                    upperOrNull(request.encAlgoKey()),
                    blankToNull(request.authModes()),
                    blankToNull(request.username()),
                    blankToNull(request.orgCode()),
                    blankToNull(request.note()));
        } else {
            jdbcTemplate.update("""
                    UPDATE ukey_device
                    SET sm2_user_id = ?, pubkey_x = ?, pubkey_y = ?, enc_algo_key = ?, auth_modes = ?,
                        username = ?, org_code = ?, note = ?, status = 'ACTIVE',
                        provisioned_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    blankToNull(request.sm2UserId()),
                    upperOrNull(request.pubkeyX()),
                    upperOrNull(request.pubkeyY()),
                    upperOrNull(request.encAlgoKey()),
                    blankToNull(request.authModes()),
                    blankToNull(request.username()),
                    blankToNull(request.orgCode()),
                    blankToNull(request.note()),
                    existing);
        }
        return findAll(chipId).stream()
                .filter(d -> chipId.equalsIgnoreCase(d.chipId()))
                .findFirst()
                .orElseThrow();
    }

    private UkeyDeviceView mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Timestamp ts = rs.getTimestamp("provisioned_at");
        return new UkeyDeviceView(
                rs.getLong("id"),
                rs.getString("chip_id"),
                rs.getString("sm2_user_id"),
                rs.getString("pubkey_x"),
                rs.getString("pubkey_y"),
                rs.getString("enc_algo_key"),
                rs.getString("auth_modes"),
                rs.getString("username"),
                rs.getString("org_code"),
                rs.getString("note"),
                rs.getString("status"),
                ts == null ? null : ts.toLocalDateTime());
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String upperOrNull(String value) {
        String t = blankToNull(value);
        return t == null ? null : t.toUpperCase();
    }
}

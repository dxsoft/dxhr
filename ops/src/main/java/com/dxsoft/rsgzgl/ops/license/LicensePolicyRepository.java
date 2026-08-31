package com.dxsoft.rsgzgl.ops.license;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class LicensePolicyRepository {

    private static final int SINGLETON_ID = 1;

    private final JdbcTemplate jdbcTemplate;

    LicensePolicyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    void save(LicenseLocalPolicy policy) {
        if (policy == null) {
            jdbcTemplate.update("DELETE FROM license_local_policy WHERE id = ?", SINGLETON_ID);
            return;
        }
        String json = LicenseCrypto.localPolicyToJson(policy);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM license_local_policy WHERE id = ?",
                Integer.class,
                SINGLETON_ID);
        if (count != null && count > 0) {
            jdbcTemplate.update(
                    "UPDATE license_local_policy SET policy_json = ?, imported_at = CURRENT_TIMESTAMP WHERE id = ?",
                    json,
                    SINGLETON_ID);
        } else {
            jdbcTemplate.update(
                    "INSERT INTO license_local_policy (id, policy_json, imported_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                    SINGLETON_ID,
                    json);
        }
    }

    LicenseLocalPolicy find() {
        List<String> rows = jdbcTemplate.query(
                "SELECT policy_json FROM license_local_policy WHERE id = ?",
                (rs, rowNum) -> rs.getString("policy_json"),
                SINGLETON_ID);
        if (rows.isEmpty() || rows.getFirst() == null || rows.getFirst().isBlank()) {
            return null;
        }
        return LicenseCrypto.parseLocalPolicyJson(rows.getFirst());
    }

    boolean exists() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM license_local_policy WHERE id = ?",
                Integer.class,
                SINGLETON_ID);
        return count != null && count > 0;
    }
}

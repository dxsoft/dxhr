package com.dxsoft.rsgzgl.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrganizationScopeResolver {

    private final JdbcTemplate jdbcTemplate;

    public OrganizationScopeResolver(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Expands root organization codes to include all descendant units whose {@code dwbm}
     * starts with the root prefix (same rule as data exchange filters).
     */
    public Set<String> expandWithDescendants(Collection<String> rootCodes) {
        if (rootCodes == null || rootCodes.isEmpty()) {
            return Set.of();
        }
        List<String> roots = rootCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (roots.isEmpty()) {
            return Set.of();
        }
        List<String> allCodes = jdbcTemplate.queryForList("SELECT dwbm FROM dwbm ORDER BY dwbm", String.class);
        Set<String> resolved = new LinkedHashSet<>();
        for (String code : allCodes) {
            if (code == null || code.isBlank()) {
                continue;
            }
            String trimmed = code.trim();
            if (roots.stream().anyMatch(root -> trimmed.equals(root) || trimmed.startsWith(root))) {
                resolved.add(trimmed);
            }
        }
        return resolved;
    }

    public boolean organizationExists(String organizationCode) {
        if (organizationCode == null || organizationCode.isBlank()) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dwbm WHERE dwbm = ?",
                Integer.class,
                organizationCode.trim());
        return count != null && count > 0;
    }
}

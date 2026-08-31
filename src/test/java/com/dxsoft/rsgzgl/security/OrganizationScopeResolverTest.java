package com.dxsoft.rsgzgl.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class OrganizationScopeResolverTest {

    private OrganizationScopeResolver resolver;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:org_scope_" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                CREATE TABLE dwbm (
                    dwbm VARCHAR(20) PRIMARY KEY
                )
                """);
        jdbcTemplate.update("INSERT INTO dwbm (dwbm) VALUES ('001'), ('00105'), ('00107'), ('002')");
        resolver = new OrganizationScopeResolver(jdbcTemplate);
    }

    @Test
    void expandWithDescendantsIncludesRootAndPrefixMatches() {
        Set<String> expanded = resolver.expandWithDescendants(List.of("001"));
        assertEquals(Set.of("001", "00105", "00107"), expanded);
        assertFalse(expanded.contains("002"));
    }

    @Test
    void organizationExistsChecksDwbmTable() {
        assertTrue(resolver.organizationExists("001"));
        assertFalse(resolver.organizationExists("999"));
    }
}

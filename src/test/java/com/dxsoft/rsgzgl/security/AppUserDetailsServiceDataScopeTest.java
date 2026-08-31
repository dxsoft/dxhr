package com.dxsoft.rsgzgl.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class AppUserDetailsServiceDataScopeTest {

    private JdbcTemplate jdbcTemplate;
    private OrganizationScopeResolver organizationScopeResolver;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:user_data_scope_" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        jdbcTemplate = new JdbcTemplate(dataSource);
        organizationScopeResolver = new OrganizationScopeResolver(jdbcTemplate);
        jdbcTemplate.execute("""
                CREATE TABLE dwbm (dwbm VARCHAR(20) PRIMARY KEY);
                CREATE TABLE app_user (
                    id BIGINT PRIMARY KEY,
                    home_organization_code VARCHAR(20) NULL,
                    all_organizations TINYINT NOT NULL DEFAULT 0
                );
                INSERT INTO dwbm (dwbm) VALUES ('001'), ('00105'), ('002');
                INSERT INTO app_user (id, home_organization_code, all_organizations) VALUES (1, '001', 0);
                INSERT INTO app_user (id, home_organization_code, all_organizations) VALUES (2, NULL, 1);
                """);
    }

    @Test
    void userHomeOrganizationExpandsDescendantsOnly() {
        Set<String> expanded = organizationScopeResolver.expandWithDescendants(
                List.of(jdbcTemplate.queryForObject(
                        "SELECT home_organization_code FROM app_user WHERE id = 1",
                        String.class)));
        assertTrue(expanded.contains("001"));
        assertTrue(expanded.contains("00105"));
        assertFalse(expanded.contains("002"));
    }

    @Test
    void allOrganizationsUserSkipsExpansion() {
        Integer allOrganizations = jdbcTemplate.queryForObject(
                "SELECT all_organizations FROM app_user WHERE id = 2",
                Integer.class);
        assertTrue(allOrganizations != null && allOrganizations == 1);
    }
}

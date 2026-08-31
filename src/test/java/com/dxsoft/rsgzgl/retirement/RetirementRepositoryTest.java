package com.dxsoft.rsgzgl.retirement;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

class RetirementRepositoryTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private RetirementRepository repository;

    @BeforeEach
    void setUp() {
        EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("classpath:retirement-repository-test.sql")
                .build();
        jdbcTemplate = new NamedParameterJdbcTemplate(database);
        repository = new RetirementRepository(jdbcTemplate);
    }

    @Test
    void allocateRetireePersonCode_startsAtOneWhenUnitHasNoRetirees() {
        assertThat(repository.allocateRetireePersonCode("016")).isEqualTo("00001");
    }

    @Test
    void allocateRetireePersonCode_incrementsFromMaxCode() {
        jdbcTemplate.getJdbcTemplate().update(
                "INSERT INTO ryjbxxb (dwbm, grbm) VALUES ('016', '00017')");
        assertThat(repository.allocateRetireePersonCode("016")).isEqualTo("00018");
    }
}

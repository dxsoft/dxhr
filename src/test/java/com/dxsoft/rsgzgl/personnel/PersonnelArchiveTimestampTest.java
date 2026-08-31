package com.dxsoft.rsgzgl.personnel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

class PersonnelArchiveTimestampTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private PersonnelRepository repository;

    @BeforeEach
    void setUp() {
        EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("personnel_archive;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false")
                .addScript("classpath:personnel-archive-timestamp-test.sql")
                .build();
        jdbcTemplate = new NamedParameterJdbcTemplate(database);
        repository = new PersonnelRepository(jdbcTemplate);
    }

    @Test
    void archivePersonnelChange_keepsNullApprovalTimestamps() {
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dwbm (dwbm, dwmc, dwbz, dwsx, gzczbz, jkjs)
                VALUES ('016', '浉河区检察院', '', '', '', FALSE)
                """);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dryjbxx (
                    dwbm, grbm, xm, sfzh, xb, csny, ryfl, dwsx, gwfl, cjgzny, zzny, gznx,
                    xlbm, zgxl, zwjb, zjbm, xrzw, srny, mz, zzmm, dah, jrny, jrfs, bbz, tc
                ) VALUES (
                    '016', '00017', '刘军', '413001000000000003', '男', '196301', '公务员', '机关', '综合管理',
                    '198501', '198601', 40, '01', '本科', '', '', '科员', '199001', '汉', '党员', '', '198501', '招录', '', ''
                )
                """);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO dxl (dwbm, grbm, xl, shsj, tjsj)
                VALUES ('016', '00017', '本科', NULL, NULL)
                """);
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO hisbase (id, dwbm, grbm, sid, jsnf, jsyf, hj2)
                VALUES ('1', '016', '00017', '', '2024', '08', 0)
                """);

        repository.archivePersonnelChange(1, new PersonnelChangeRequest("退休", "", "测试退休"));

        assertThat(jdbcTemplate.getJdbcTemplate().queryForObject(
                "SELECT shsj FROM dryjbxxb WHERE dwbm = '016' AND grbm = '00017'", String.class))
                .isNull();
        assertThat(jdbcTemplate.getJdbcTemplate().queryForObject(
                "SELECT shsj FROM dxlb WHERE dwbm = '016' AND grbm = '00017'", String.class))
                .isNull();
        assertThat(jdbcTemplate.getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM dryjbxx WHERE uid = 1", Integer.class))
                .isZero();
    }
}

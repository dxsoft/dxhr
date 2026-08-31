package com.dxsoft.rsgzgl.dataexchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.dxsoft.rsgzgl.security.AccessControlService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

class DataExchangeRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private DataExchangeRepository repository;

    @BeforeEach
    void setUp() {
        EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("classpath:dataexchange-repository-test.sql")
                .build();
        jdbcTemplate = new JdbcTemplate(database);
        repository = new DataExchangeRepository(jdbcTemplate, mock(AccessControlService.class));
    }

    @Test
    void exportRelatedTablesIncludesDryjbxxFirst() {
        jdbcTemplate.update("""
                INSERT INTO dryjbxx (dwbm, grbm, xm, tc)
                VALUES ('02108', '00001', '测试员', '已定工资')
                """);

        PersonnelExportRecord person = sample("02108", "00001", "测试员");
        List<DataExchangeService.ExchangeTable> tables = repository.exportRelatedTables(List.of(person));

        assertThat(tables).isNotEmpty();
        assertThat(tables.getFirst().tableName()).isEqualTo("dryjbxx");
        assertThat(tables.getFirst().rows()).hasSize(1);
        assertThat(String.valueOf(tables.getFirst().rows().getFirst().get("tc"))).isEqualTo("已定工资");
    }

    @Test
    void replaceReceivedPersonnelPreservesTcFromPackageRow() {
        jdbcTemplate.update("""
                INSERT INTO dryjbxx (dwbm, grbm, xm, tc)
                VALUES ('02108', '00001', '旧数据', '')
                """);

        Map<String, Object> baseRow = new LinkedHashMap<>();
        baseRow.put("dwbm", "02108");
        baseRow.put("grbm", "00001");
        baseRow.put("xm", "测试员");
        baseRow.put("tc", "已定工资");

        PersonnelExportRecord person = sample("02108", "00001", "测试员");
        List<DataExchangeService.ExchangeTable> relatedTables = List.of(
                new DataExchangeService.ExchangeTable("dryjbxx", List.of(baseRow)));

        int count = repository.replaceReceivedPersonnel(List.of(person), relatedTables);

        assertThat(count).isEqualTo(1);
        String tc = jdbcTemplate.queryForObject(
                "SELECT tc FROM dryjbxx WHERE dwbm = ? AND grbm = ?",
                String.class,
                "02108",
                "00001");
        assertThat(tc).isEqualTo("已定工资");
    }

    @Test
    void replaceReceivedPersonnelWithoutDryjbxxTableFallsBackToLegacyInsert() {
        jdbcTemplate.update("""
                INSERT INTO dryjbxx (dwbm, grbm, xm, tc)
                VALUES ('02108', '00002', '旧数据', '已定工资')
                """);

        PersonnelExportRecord person = sample("02108", "00002", "测试员");
        int count = repository.replaceReceivedPersonnel(List.of(person), List.of());

        assertThat(count).isEqualTo(1);
        String tc = jdbcTemplate.queryForObject(
                "SELECT tc FROM dryjbxx WHERE dwbm = ? AND grbm = ?",
                String.class,
                "02108",
                "00002");
        assertThat(tc).isBlank();
    }

    @Test
    void applyApprovalReceiveUpdatePreservesUidAndUpdatesBjglxlnx() {
        jdbcTemplate.update("""
                INSERT INTO dryjbxx (dwbm, grbm, xm, bjglxlnx, zdgznx, gznx)
                VALUES ('02108', '00040', '马世明', 0, 0, 39)
                """);

        Map<String, Object> baseRow = new LinkedHashMap<>();
        baseRow.put("dwbm", "02108");
        baseRow.put("grbm", "00040");
        baseRow.put("xm", "马世明");
        baseRow.put("bjglxlnx", 2);
        baseRow.put("zdgznx", 0);
        baseRow.put("gznx", 41);

        PersonnelExportRecord person = sample("02108", "00040", "马世明");
        List<DataExchangeService.ExchangeTable> relatedTables = List.of(
                new DataExchangeService.ExchangeTable("dryjbxx", List.of(baseRow)));

        int count = repository.applyApprovalReceive(List.of(person), relatedTables, "UPDATE");

        assertThat(count).isEqualTo(1);
        Integer uid = jdbcTemplate.queryForObject(
                "SELECT uid FROM dryjbxx WHERE dwbm = ? AND grbm = ?",
                Integer.class,
                "02108", "00040");
        assertThat(uid).isEqualTo(1);
        Integer bjglxlnx = jdbcTemplate.queryForObject(
                "SELECT bjglxlnx FROM dryjbxx WHERE dwbm = ? AND grbm = ?",
                Integer.class,
                "02108", "00040");
        Integer gznx = jdbcTemplate.queryForObject(
                "SELECT gznx FROM dryjbxx WHERE dwbm = ? AND grbm = ?",
                Integer.class,
                "02108", "00040");
        assertThat(bjglxlnx).isEqualTo(2);
        assertThat(gznx).isEqualTo(41);
    }

    @Test
    void applyApprovalReceiveReplaceRestoresJxFromPackage() {
        jdbcTemplate.update("""
                INSERT INTO dryjbxx (dwbm, grbm, xm, bjglxlnx)
                VALUES ('02108', '00001', '测试员', 0)
                """);
        jdbcTemplate.update("""
                INSERT INTO jx (dwbm, grbm, jx)
                VALUES ('02108', '00001', '本地绩效')
                """);

        Map<String, Object> baseRow = Map.of(
                "dwbm", "02108",
                "grbm", "00001",
                "xm", "测试员",
                "bjglxlnx", 2);
        Map<String, Object> jxRow = Map.of(
                "dwbm", "02108",
                "grbm", "00001",
                "jx", "审批包绩效");

        PersonnelExportRecord person = sample("02108", "00001", "测试员");
        List<DataExchangeService.ExchangeTable> relatedTables = List.of(
                new DataExchangeService.ExchangeTable("dryjbxx", List.of(baseRow)),
                new DataExchangeService.ExchangeTable("jx", List.of(jxRow)));

        int count = repository.applyApprovalReceive(List.of(person), relatedTables, "REPLACE");

        assertThat(count).isEqualTo(1);
        String jx = jdbcTemplate.queryForObject(
                "SELECT jx FROM jx WHERE dwbm = ? AND grbm = ?",
                String.class,
                "02108",
                "00001");
        assertThat(jx).isEqualTo("审批包绩效");
        Integer bjglxlnx = jdbcTemplate.queryForObject(
                "SELECT bjglxlnx FROM dryjbxx WHERE dwbm = ? AND grbm = ?",
                Integer.class,
                "02108",
                "00001");
        assertThat(bjglxlnx).isEqualTo(2);
    }

    private static PersonnelExportRecord sample(String org, String person, String name) {
        return new PersonnelExportRecord(
                org, "单位", person, name, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null);
    }
}

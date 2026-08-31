package com.dxsoft.rsgzgl.dataexchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dxsoft.rsgzgl.backup.BackupPackageInspector;
import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LegacyPersonnelPackageConverterTest {

    private static final Charset GBK = Charset.forName("GBK");

    @TempDir
    Path tempDir;

    private LegacyPersonnelPackageConverter converter;

    @BeforeEach
    void setUp() {
        converter = new LegacyPersonnelPackageConverter(new BackupPackageInspector());
    }

    @Test
    void toPersonnelExportRecordUsesCurrentHisbaseAndLatestPosition() {
        Map<String, String> orgNames = Map.of("001", "测试单位");
        Map<String, List<Map<String, Object>>> dryzwbh = Map.of(
                "001|00001", List.of(
                        Map.of("dwbm", "001", "grbm", "00001", "srny", "202001"),
                        Map.of("dwbm", "001", "grbm", "00001", "srny", "202301")));
        Map<String, List<Map<String, Object>>> hisbase = Map.of(
                "001|00001", List.of(
                        Map.of("dwbm", "001", "grbm", "00001", "sid", "1", "jsnf", "2024", "jsyf", "01", "id", "9"),
                        Map.of(
                                "dwbm",
                                "001",
                                "grbm",
                                "00001",
                                "sid",
                                "",
                                "jsnf",
                                "2025",
                                "jsyf",
                                "06",
                                "id",
                                "10",
                                "zwbm2",
                                "岗位A",
                                "zwgw2",
                                "职务A",
                                "jbgzjb2",
                                "12",
                                "zwgzdc2",
                                "3")));

        PersonnelExportRecord record = LegacyPersonnelPackageConverter.toPersonnelExportRecord(
                Map.of(
                        "dwbm", "001",
                        "grbm", "00001",
                        "xm", "张三",
                        "sfzh", "110101199001011234",
                        "gznx", 10),
                orgNames,
                dryzwbh,
                hisbase);

        assertThat(record.organizationName()).isEqualTo("测试单位");
        assertThat(record.name()).isEqualTo("张三");
        assertThat(record.positionStart()).isEqualTo("202301");
        assertThat(record.currentJob()).isEqualTo("岗位A");
        assertThat(record.currentGrade()).isEqualTo("职务A");
        assertThat(record.currentLevel()).isEqualTo("12");
        assertThat(record.currentTechGrade()).isEqualTo("3");
        assertThat(record.salaryYears()).isEqualTo(10);
    }

    @Test
    void convertBuildsPackageFromMinimalLegacyZip() throws Exception {
        Path extractDir = tempDir.resolve("legacy");
        Files.createDirectories(extractDir);
        Files.writeString(extractDir.resolve("xxbak.id"), "legacy-marker");
        writeDryjbxxDbf(extractDir.resolve("dryjbxx2.dbf"), "001", "00001", "张三");
        writeHisbaseDbf(extractDir.resolve("hisbase2.dbf"), "001", "00001", "", "2025", "06");

        DataExchangeService.PersonnelExchangePackage payload = converter.convert(extractDir);

        assertThat(payload.packageType()).isEqualTo("PERSONNEL");
        assertThat(payload.personnel()).hasSize(1);
        assertThat(payload.personnel().getFirst().organizationCode()).isEqualTo("001");
        assertThat(payload.personnel().getFirst().personCode()).isEqualTo("00001");
        assertThat(payload.personnel().getFirst().name()).isEqualTo("张三");
        assertThat(payload.relatedTables()).extracting(DataExchangeService.ExchangeTable::tableName)
                .contains("dryjbxx", "hisbase", "dryzwbh");
        assertThat(payload.relatedTables().stream()
                        .filter(table -> "dryjbxx".equals(table.tableName()))
                        .findFirst()
                        .orElseThrow()
                        .rows())
                .hasSize(1);
    }

    @Test
    void convertRejectsMissingDryjbxx() throws Exception {
        Path extractDir = tempDir.resolve("empty-legacy");
        Files.createDirectories(extractDir);
        Files.writeString(extractDir.resolve("xxbak.id"), "legacy-marker");

        assertThatThrownBy(() -> converter.convert(extractDir))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dryjbxx2.dbf");
    }

    @Test
    void convertFromZipArchive() throws Exception {
        Path archive = tempDir.resolve("sample.zl");
        Path extractDir = tempDir.resolve("from-zip");
        Files.createDirectories(extractDir);
        Files.writeString(extractDir.resolve("xxbak2026.id"), "legacy-marker");
        writeDryjbxxDbf(extractDir.resolve("dryjbxx2.dbf"), "02108", "00002", "李四");
        zipDirectory(extractDir, archive);

        Path unzipDir = tempDir.resolve("unzipped");
        new BackupPackageInspector().unzip(archive, unzipDir);
        DataExchangeService.PersonnelExchangePackage payload = converter.convert(unzipDir);

        assertThat(payload.personnel()).hasSize(1);
        assertThat(payload.personnel().getFirst().name()).isEqualTo("李四");
    }

    private static void writeDryjbxxDbf(Path path, String dwbm, String grbm, String xm) throws IOException {
        DBFField[] fields = new DBFField[] {
            field("dwbm", DBFDataType.CHARACTER, 10),
            field("grbm", DBFDataType.CHARACTER, 10),
            field("xm", DBFDataType.CHARACTER, 20),
            field("sfzh", DBFDataType.CHARACTER, 18),
        };
        try (OutputStream out = Files.newOutputStream(path);
                DBFWriter writer = new DBFWriter(out, GBK)) {
            writer.setFields(fields);
            writer.addRecord(new Object[] {dwbm, grbm, xm, "110101199001011234"});
        }
    }

    private static void writeHisbaseDbf(
            Path path, String dwbm, String grbm, String sid, String jsnf, String jsyf) throws IOException {
        DBFField[] fields = new DBFField[] {
            field("dwbm", DBFDataType.CHARACTER, 10),
            field("grbm", DBFDataType.CHARACTER, 10),
            field("sid", DBFDataType.CHARACTER, 10),
            field("jsnf", DBFDataType.CHARACTER, 4),
            field("jsyf", DBFDataType.CHARACTER, 2),
            field("zwbm2", DBFDataType.CHARACTER, 20),
        };
        try (OutputStream out = Files.newOutputStream(path);
                DBFWriter writer = new DBFWriter(out, GBK)) {
            writer.setFields(fields);
            writer.addRecord(new Object[] {dwbm, grbm, sid, jsnf, jsyf, "岗位B"});
        }
    }

    private static DBFField field(String name, DBFDataType type, int length) {
        DBFField field = new DBFField();
        field.setName(name);
        field.setType(type);
        field.setLength(length);
        return field;
    }

    private static void zipDirectory(Path sourceDir, Path archive) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archive));
                var stream = Files.walk(sourceDir)) {
            for (Path path : stream.filter(Files::isRegularFile).toList()) {
                String entryName = sourceDir.relativize(path).toString().replace('\\', '/');
                zip.putNextEntry(new ZipEntry(entryName));
                Files.copy(path, zip);
                zip.closeEntry();
            }
        }
    }
}

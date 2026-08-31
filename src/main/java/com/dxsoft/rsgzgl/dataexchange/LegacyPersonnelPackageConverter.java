package com.dxsoft.rsgzgl.dataexchange;

import com.dxsoft.rsgzgl.backup.BackupFormat;
import com.dxsoft.rsgzgl.backup.BackupInspectResult;
import com.dxsoft.rsgzgl.backup.BackupPackageInspector;
import com.dxsoft.rsgzgl.backup.DbfTableReader;
import com.dxsoft.rsgzgl.dataexchange.DataExchangeService.ExchangeTable;
import com.dxsoft.rsgzgl.dataexchange.DataExchangeService.PersonnelExchangePackage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
class LegacyPersonnelPackageConverter {

    private static final String PERSONNEL_BASE_TABLE = "dryjbxx";

    private static final List<String> RELATED_TABLES = List.of(
            "hisbase",
            "dryzwbh",
            "dxl",
            "dndkh",
            "jx",
            "dtgxx",
            "tgqgz2006",
            "hjxx");

    private final BackupPackageInspector backupPackageInspector;

    LegacyPersonnelPackageConverter(BackupPackageInspector backupPackageInspector) {
        this.backupPackageInspector = backupPackageInspector;
    }

    PersonnelExchangePackage convert(Path extractDir) throws IOException {
        return convert(extractDir, null);
    }

    PersonnelExchangePackage convert(Path extractDir, List<DataExchangeController.PersonKey> selectedPersonnel)
            throws IOException {
        BackupInspectResult inspect = backupPackageInspector.inspectExtracted(extractDir);
        if (inspect.format() != BackupFormat.LEGACY) {
            throw new IllegalArgumentException(
                    inspect.message() == null || inspect.message().isBlank()
                            ? "不是旧系统 .zl 备份包（需含 xxbak*.id 标识文件）。"
                            : inspect.message());
        }

        Map<String, Path> dbfIndex = indexDbfFiles(extractDir);
        Path dryjbxxDbf = dbfIndex.get("dryjbxx2.dbf");
        if (dryjbxxDbf == null) {
            throw new IllegalArgumentException("备份包中无人员基本信息（缺少 dryjbxx2.dbf）。");
        }

        List<Map<String, Object>> dryjbxxRows = DbfTableReader.readAllRows(dryjbxxDbf);
        Set<String> selectedKeys = selectedPersonKeys(selectedPersonnel);
        if (!selectedKeys.isEmpty()) {
            dryjbxxRows = dryjbxxRows.stream()
                    .filter(row -> selectedKeys.contains(personKey(row)))
                    .toList();
        }

        Map<String, String> organizationNames = loadOrganizationNames(dbfIndex);
        Map<String, List<Map<String, Object>>> dryzwbhByPerson = indexRowsByPerson(
                readTableRows(dbfIndex, "dryzwbh"), selectedKeys);
        Map<String, List<Map<String, Object>>> hisbaseByPerson = indexRowsByPerson(
                readTableRows(dbfIndex, "hisbase"), selectedKeys);

        List<PersonnelExportRecord> personnel = dryjbxxRows.stream()
                .map(row -> toPersonnelExportRecord(row, organizationNames, dryzwbhByPerson, hisbaseByPerson))
                .toList();

        List<ExchangeTable> relatedTables = new ArrayList<>();
        relatedTables.add(new ExchangeTable(
                PERSONNEL_BASE_TABLE,
                filterRowsForPeople(dryjbxxRows, selectedKeys)));
        for (String table : RELATED_TABLES) {
            relatedTables.add(new ExchangeTable(
                    table,
                    filterRowsForPeople(readTableRows(dbfIndex, table), selectedKeys)));
        }

        List<String> organizationCodes = personnel.stream()
                .map(PersonnelExportRecord::organizationCode)
                .filter(code -> code != null && !code.isBlank())
                .distinct()
                .sorted()
                .toList();

        return new PersonnelExchangePackage(
                "PERSONNEL",
                LocalDateTime.now().toString(),
                organizationCodes,
                false,
                personnel,
                relatedTables);
    }

    static PersonnelExportRecord toPersonnelExportRecord(
            Map<String, Object> row,
            Map<String, String> organizationNames,
            Map<String, List<Map<String, Object>>> dryzwbhByPerson,
            Map<String, List<Map<String, Object>>> hisbaseByPerson) {
        String dwbm = textValue(row, "dwbm");
        String grbm = textValue(row, "grbm");
        String key = personKey(dwbm, grbm);

        Map<String, Object> currentHisbase = findCurrentHisbase(hisbaseByPerson.get(key));
        String positionStart = findLatestPositionStart(dryzwbhByPerson.get(key));

        return new PersonnelExportRecord(
                dwbm,
                organizationNames.getOrDefault(dwbm, ""),
                grbm,
                textValue(row, "xm"),
                textValue(row, "sfzh"),
                textValue(row, "xb"),
                textValue(row, "csny"),
                textValue(row, "ryfl"),
                textValue(row, "dwsx"),
                textValue(row, "gwfl"),
                textValue(row, "cjgzny"),
                textValue(row, "zzny"),
                intValue(row, "gznx"),
                textValue(row, "xlbm"),
                textValue(row, "zgxl"),
                textValue(row, "zwjb"),
                textValue(row, "zjbm"),
                textValue(row, "xrzw"),
                positionStart,
                textValue(row, "mz"),
                textValue(row, "zzmm"),
                textValue(row, "dah"),
                textValue(currentHisbase, "zwbm2"),
                textValue(currentHisbase, "zwgw2"),
                textValue(currentHisbase, "jbgzjb2"),
                textValue(currentHisbase, "zwgzdc2"));
    }

    private static Map<String, String> loadOrganizationNames(Map<String, Path> dbfIndex) throws IOException {
        Path dwbmDbf = dbfIndex.get("dwbm2.dbf");
        if (dwbmDbf == null) {
            return Map.of();
        }
        Map<String, String> names = new HashMap<>();
        for (Map<String, Object> row : DbfTableReader.readAllRows(dwbmDbf)) {
            String dwbm = textValue(row, "dwbm");
            String dwmc = textValue(row, "dwmc");
            if (!dwbm.isEmpty() && !dwmc.isEmpty()) {
                names.putIfAbsent(dwbm, dwmc);
            }
        }
        return names;
    }

    private static List<Map<String, Object>> readTableRows(Map<String, Path> dbfIndex, String tableName)
            throws IOException {
        Path dbf = dbfIndex.get((tableName + "2.dbf").toLowerCase(Locale.ROOT));
        if (dbf == null) {
            return List.of();
        }
        return DbfTableReader.readAllRows(dbf);
    }

    private static List<Map<String, Object>> filterRowsForPeople(
            List<Map<String, Object>> rows,
            Set<String> selectedKeys) {
        if (selectedKeys.isEmpty()) {
            return rows;
        }
        return rows.stream()
                .filter(row -> selectedKeys.contains(personKey(row)))
                .toList();
    }

    private static Map<String, List<Map<String, Object>>> indexRowsByPerson(
            List<Map<String, Object>> rows,
            Set<String> selectedKeys) {
        Map<String, List<Map<String, Object>>> index = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String key = personKey(row);
            if (!selectedKeys.isEmpty() && !selectedKeys.contains(key)) {
                continue;
            }
            index.computeIfAbsent(key, ignored -> new ArrayList<>()).add(row);
        }
        return index;
    }

    private static Set<String> selectedPersonKeys(List<DataExchangeController.PersonKey> selectedPersonnel) {
        if (selectedPersonnel == null || selectedPersonnel.isEmpty()) {
            return Set.of();
        }
        Set<String> keys = new LinkedHashSet<>();
        for (DataExchangeController.PersonKey key : selectedPersonnel) {
            if (key == null) {
                continue;
            }
            String org = key.organizationCode() == null ? "" : key.organizationCode().trim();
            String person = key.personCode() == null ? "" : key.personCode().trim();
            if (!org.isEmpty() && !person.isEmpty()) {
                keys.add(personKey(org, person));
            }
        }
        return keys;
    }

    private static Map<String, Object> findCurrentHisbase(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        return rows.stream()
                .filter(LegacyPersonnelPackageConverter::isCurrentHisbaseRow)
                .max(Comparator
                        .comparing((Map<String, Object> row) -> textValue(row, "jsnf"))
                        .thenComparing(row -> textValue(row, "jsyf"))
                        .thenComparing(row -> textValue(row, "id")))
                .orElseGet(() -> rows.stream()
                        .max(Comparator
                                .comparing((Map<String, Object> row) -> textValue(row, "jsnf"))
                                .thenComparing(row -> textValue(row, "jsyf"))
                                .thenComparing(row -> textValue(row, "id")))
                        .orElse(Map.of()));
    }

    private static boolean isCurrentHisbaseRow(Map<String, Object> row) {
        String sid = textValue(row, "sid");
        return sid.isEmpty();
    }

    private static String findLatestPositionStart(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return "";
        }
        return rows.stream()
                .map(row -> textValue(row, "srny"))
                .filter(value -> !value.isEmpty())
                .max(String::compareTo)
                .orElse("");
    }

    private static String personKey(Map<String, Object> row) {
        return personKey(textValue(row, "dwbm"), textValue(row, "grbm"));
    }

    private static String personKey(String organizationCode, String personCode) {
        return String.valueOf(organizationCode).trim() + "|" + String.valueOf(personCode).trim();
    }

    private static String textValue(Map<String, Object> row, String key) {
        if (row == null || row.isEmpty()) {
            return "";
        }
        Object value = row.get(key);
        if (value == null) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)) {
                    value = entry.getValue();
                    break;
                }
            }
        }
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static Integer intValue(Map<String, Object> row, String key) {
        String text = textValue(row, key);
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text.replaceAll("\\.0+$", "").split("\\.")[0]);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Map<String, Path> indexDbfFiles(Path extractDir) throws IOException {
        Map<String, Path> index = new HashMap<>();
        try (var stream = Files.walk(extractDir)) {
            stream.filter(Files::isRegularFile).forEach(path -> index.putIfAbsent(
                    path.getFileName().toString().toLowerCase(Locale.ROOT), path));
        }
        return index;
    }
}

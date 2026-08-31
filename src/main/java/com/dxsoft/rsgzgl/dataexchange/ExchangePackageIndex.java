package com.dxsoft.rsgzgl.dataexchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory index for exchange package tables keyed by dwbm|grbm.
 */
final class ExchangePackageIndex {

    private final Map<String, Map<String, List<Map<String, Object>>>> byPersonAndTable;
    private final Map<String, Map<String, Object>> payrollSummaryByPerson;
    private final Map<String, Integer> payrollCountByPerson;

    private ExchangePackageIndex(
            Map<String, Map<String, List<Map<String, Object>>>> byPersonAndTable,
            Map<String, Map<String, Object>> payrollSummaryByPerson,
            Map<String, Integer> payrollCountByPerson) {
        this.byPersonAndTable = byPersonAndTable;
        this.payrollSummaryByPerson = payrollSummaryByPerson;
        this.payrollCountByPerson = payrollCountByPerson;
    }

    static ExchangePackageIndex from(PayrollSubmissionPackage payload) {
        ExchangePackageIndex related = fromRelatedTables(payload.relatedTables());
        Map<String, Map<String, Object>> payrollSummary = new HashMap<>();
        Map<String, Integer> payrollCount = new HashMap<>();
        if (payload.payrollTables() != null) {
            for (DataExchangeService.ExchangeTable table : payload.payrollTables()) {
                if (!"hisbase".equalsIgnoreCase(table.tableName()) || table.rows() == null) {
                    continue;
                }
                for (Map<String, Object> row : table.rows()) {
                    String key = personKey(textValue(row, "dwbm"), textValue(row, "grbm"));
                    if (key.isEmpty()) {
                        continue;
                    }
                    payrollCount.merge(key, 1, Integer::sum);
                    if (!payrollSummary.containsKey(key) && isCurrentPayrollRow(row)) {
                        payrollSummary.put(key, summarizePayrollRow(row));
                    }
                }
            }
        }
        return new ExchangePackageIndex(related.byPersonAndTable, payrollSummary, payrollCount);
    }

    static ExchangePackageIndex fromRelatedTables(List<DataExchangeService.ExchangeTable> relatedTables) {
        Map<String, Map<String, List<Map<String, Object>>>> index = new HashMap<>();
        if (relatedTables != null) {
            for (DataExchangeService.ExchangeTable table : relatedTables) {
                if (table.rows() == null || table.tableName() == null) {
                    continue;
                }
                String tableName = table.tableName().toLowerCase(Locale.ROOT);
                for (Map<String, Object> row : table.rows()) {
                    String key = personKey(textValue(row, "dwbm"), textValue(row, "grbm"));
                    if (key.isEmpty()) {
                        continue;
                    }
                    index.computeIfAbsent(key, ignored -> new HashMap<>())
                            .computeIfAbsent(tableName, ignored -> new ArrayList<>())
                            .add(row);
                }
            }
        }
        return new ExchangePackageIndex(index, Map.of(), Map.of());
    }

    List<Map<String, Object>> relatedRows(String organizationCode, String personCode, String tableName) {
        Map<String, List<Map<String, Object>>> tables = byPersonAndTable.get(personKey(organizationCode, personCode));
        if (tables == null || tableName == null) {
            return List.of();
        }
        List<Map<String, Object>> rows = tables.get(tableName.toLowerCase(Locale.ROOT));
        return rows == null ? List.of() : rows;
    }

    Optional<Map<String, Object>> dryjbxxRow(String organizationCode, String personCode) {
        List<Map<String, Object>> rows = relatedRows(organizationCode, personCode, "dryjbxx");
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    Map<String, Object> payrollSummary(String organizationCode, String personCode) {
        Map<String, Object> summary = payrollSummaryByPerson.get(personKey(organizationCode, personCode));
        return summary == null ? Map.of() : summary;
    }

    int payrollCount(String organizationCode, String personCode) {
        return payrollCountByPerson.getOrDefault(personKey(organizationCode, personCode), 0);
    }

    static String personKey(String organizationCode, String personCode) {
        if (organizationCode == null || personCode == null) {
            return "";
        }
        return organizationCode.trim() + "|" + personCode.trim();
    }

    private static boolean isCurrentPayrollRow(Map<String, Object> row) {
        String sid = textValue(row, "sid");
        return sid.isBlank();
    }

    private static Map<String, Object> summarizePayrollRow(Map<String, Object> row) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("jslb", textValue(row, "jslb"));
        summary.put("period", textValue(row, "jsnf") + textValue(row, "jsyf"));
        summary.put("hj2", row.getOrDefault("hj2", row.get("HJ2")));
        summary.put("bbz", textValue(row, "bbz"));
        return summary;
    }

    private static String textValue(Map<String, Object> row, String key) {
        if (row == null || key == null) {
            return "";
        }
        Object value = row.get(key);
        if (value == null) {
            value = row.get(key.toUpperCase(Locale.ROOT));
        }
        if (value == null) {
            value = row.get(key.toLowerCase(Locale.ROOT));
        }
        return value == null ? "" : String.valueOf(value).trim();
    }
}

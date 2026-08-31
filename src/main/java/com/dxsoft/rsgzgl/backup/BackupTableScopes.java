package com.dxsoft.rsgzgl.backup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Named table groups for selective backup export / restore.
 * PERSONNEL includes organization tables (dwbm / cyxx) per product requirement.
 */
public final class BackupTableScopes {

    public static final String ALL = "ALL";
    public static final String PERSONNEL = "PERSONNEL";
    public static final String STANDARDS = "STANDARDS";
    public static final String DICTIONARY = "DICTIONARY";
    public static final String SYSTEM = "SYSTEM";
    public static final String OTHER = "OTHER";

    public record ScopeDescriptor(String id, String label, String description) {
    }

    public record ScopeMatch(String id, String label, String description, List<String> tables) {
        public int matchedTables() {
            return tables.size();
        }
    }

    private static final List<ScopeDescriptor> DESCRIPTORS = List.of(
            new ScopeDescriptor(ALL, "全部表", "导出或恢复备份包中的全部业务表"),
            new ScopeDescriptor(PERSONNEL, "人员与单位", "在册/变动人员、子表、工资历史，以及单位 dwbm / cyxx"),
            new ScopeDescriptor(STANDARDS, "工资标准", "职务工资、级别工资、津补贴等标准表（bz06_* / bz_* 等）"),
            new ScopeDescriptor(DICTIONARY, "字典与字段", "代码字典与字段元数据（dmb / fld* 等）"),
            new ScopeDescriptor(SYSTEM, "系统与权限", "登录用户、角色、权限与授权相关表"),
            new ScopeDescriptor(OTHER, "其他表", "未归入以上分组的表"));

    /** Exact tables for 人员与单位 (user choice 1C). */
    private static final Set<String> PERSONNEL_TABLES = Set.of(
            "dryjbxx", "dryjbxxb",
            "hisbase", "hisbaseb",
            "dxl", "dxlb",
            "dryzwbh", "dryzwbhb",
            "dndkh", "dndkhb",
            "dtgxx", "dtgxxb",
            "tgqgz2006", "tgqgz2006b",
            "jx", "jxb",
            "jfjs", "jfjsb",
            "jytgyb", "jytgybb",
            "jytgzzbf", "jytgzzbfb",
            "hjxx", "hjxxb",
            "djxgz", "djxgzb",
            "ryjbxxb",
            "app_record_marker",
            "app_subrecord_attachment",
            "app_personnel_transfer",
            "dwbm", "cyxx");

    private static final Set<String> DICTIONARY_TABLES = Set.of(
            "dmb", "fldjbxx", "fldgz", "fldprop", "zwbm");

    private static final Set<String> SYSTEM_TABLES = Set.of(
            "app_user", "app_role", "app_permission", "app_role_permission",
            "app_user_role", "app_role_org_scope", "app_license",
            "app_operation_log", "app_login_audit");

    private static final Set<String> STANDARDS_EXACT = Set.of(
            "jxjtbz", "njbt", "jbtbz", "jbtbz06", "zzjbtbz06", "jbtgbz");

    private BackupTableScopes() {
    }

    public static List<ScopeDescriptor> descriptors() {
        return DESCRIPTORS;
    }

    public static String labelOf(String scopeId) {
        return DESCRIPTORS.stream()
                .filter(d -> d.id().equalsIgnoreCase(scopeId))
                .map(ScopeDescriptor::label)
                .findFirst()
                .orElse(scopeId);
    }

    /**
     * Resolve selected scope ids to concrete table names present in {@code availableTables}.
     * Empty / null selection means ALL.
     */
    public static List<String> resolveTables(Collection<String> scopeIds, Collection<String> availableTables) {
        List<String> available = availableTables.stream()
                .filter(name -> name != null && !name.isBlank())
                .toList();
        Set<String> scopes = normalizeScopeIds(scopeIds);
        if (scopes.contains(ALL) || scopes.isEmpty()) {
            return available.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
        }
        Set<String> selected = new LinkedHashSet<>();
        for (String table : available) {
            if (matchesAny(table, scopes)) {
                selected.add(table);
            }
        }
        return selected.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    public static List<ScopeMatch> matchScopes(Collection<String> availableTables) {
        List<String> available = availableTables.stream()
                .filter(name -> name != null && !name.isBlank())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        List<ScopeMatch> matches = new ArrayList<>();
        matches.add(new ScopeMatch(ALL, labelOf(ALL), descriptionOf(ALL), available));
        for (String id : List.of(PERSONNEL, STANDARDS, DICTIONARY, SYSTEM, OTHER)) {
            List<String> tables = available.stream()
                    .filter(table -> classify(table).equals(id))
                    .toList();
            if (!tables.isEmpty()) {
                matches.add(new ScopeMatch(id, labelOf(id), descriptionOf(id), tables));
            }
        }
        return matches;
    }

    public static Set<String> normalizeScopeIds(Collection<String> scopeIds) {
        Set<String> scopes = new LinkedHashSet<>();
        if (scopeIds == null) {
            return scopes;
        }
        for (String raw : scopeIds) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            for (String part : raw.split("[,;\\s]+")) {
                if (!part.isBlank()) {
                    scopes.add(part.trim().toUpperCase(Locale.ROOT));
                }
            }
        }
        return scopes;
    }

    static String classify(String tableName) {
        String name = tableName.toLowerCase(Locale.ROOT);
        if (PERSONNEL_TABLES.contains(name)) {
            return PERSONNEL;
        }
        if (DICTIONARY_TABLES.contains(name)) {
            return DICTIONARY;
        }
        if (SYSTEM_TABLES.contains(name) || name.startsWith("app_")) {
            return SYSTEM;
        }
        if (isStandardTable(name)) {
            return STANDARDS;
        }
        return OTHER;
    }

    private static boolean matchesAny(String tableName, Set<String> scopes) {
        String group = classify(tableName);
        return scopes.contains(group);
    }

    private static boolean isStandardTable(String lowerName) {
        if (STANDARDS_EXACT.contains(lowerName)) {
            return true;
        }
        return lowerName.startsWith("bz06_")
                || lowerName.startsWith("bz_")
                || lowerName.endsWith("bz06");
    }

    private static String descriptionOf(String scopeId) {
        return DESCRIPTORS.stream()
                .filter(d -> d.id().equalsIgnoreCase(scopeId))
                .map(ScopeDescriptor::description)
                .findFirst()
                .orElse("");
    }
}

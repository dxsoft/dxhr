package com.dxsoft.rsgzgl.setup;

import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemInitializationService {

    private static final String CONFIRM_PHRASE = "系统初始化";
    private static final List<String> PURGE_TABLES = List.of(
            "app_record_marker",
            "hisbase",
            "hisbaseb",
            "dtgxx",
            "tgqgz2006",
            "jfjs",
            "hjxx",
            "jx",
            "dxl",
            "dryzwbh",
            "dndkh",
            "dryjbxxb",
            "dryjbxx");

    /** 在人员数据清空后，用于「空库 + 导入授权包重建单位」 */
    private static final List<String> ORGANIZATION_LICENSE_TABLES = List.of(
            "app_license",
            "app_role_org_scope",
            "cyxx",
            "dwbm");

    private final NamedParameterJdbcTemplate jdbc;
    private final AccessControlService accessControlService;
    private final OperationLogService operationLogService;

    SystemInitializationService(
            NamedParameterJdbcTemplate jdbc,
            AccessControlService accessControlService,
            OperationLogService operationLogService) {
        this.jdbc = jdbc;
        this.accessControlService = accessControlService;
        this.operationLogService = operationLogService;
    }

    public SystemInitializationPreview preview(boolean clearOrganizationsAndLicense) {
        requireSystemSetupPermission();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String table : PURGE_TABLES) {
            if (tableExists(table)) {
                counts.put(table, countTable(table));
            }
        }
        long organizationCount = tableExists("dwbm") ? countTable("dwbm") : 0L;
        long subjectCount = tableExists("cyxx") ? countTable("cyxx") : 0L;
        long licenseCount = tableExists("app_license") ? countTable("app_license") : 0L;
        if (clearOrganizationsAndLicense) {
            for (String table : ORGANIZATION_LICENSE_TABLES) {
                if (tableExists(table)) {
                    counts.put(table, countTable(table));
                }
            }
        }
        long personnel = counts.getOrDefault("dryjbxx", 0L);
        String warning = clearOrganizationsAndLicense
                ? "将删除全部人员业务数据，并清空单位树（dwbm）、主体信息（cyxx）、授权记录（app_license）及角色单位范围。"
                + "字典与工资标准保留。完成后请到「单位授权」导入授权包重建单位。操作不可撤销，请先备份。"
                : "系统初始化将删除所有人员及相关业务数据（单位信息、字典、工资标准表除外），此操作不可撤销。";
        return new SystemInitializationPreview(
                counts,
                personnel,
                organizationCount,
                subjectCount,
                licenseCount,
                clearOrganizationsAndLicense,
                warning);
    }

    @Transactional
    public SystemInitializationResult execute(String confirmPhrase, boolean clearOrganizationsAndLicense) {
        requireSystemSetupPermission();
        if (!CONFIRM_PHRASE.equals(emptyToNull(confirmPhrase))) {
            throw new IllegalArgumentException("确认短语不正确，请输入「" + CONFIRM_PHRASE + "」。");
        }
        SystemInitializationPreview preview = preview(clearOrganizationsAndLicense);
        Map<String, Integer> deletedCounts = new LinkedHashMap<>();
        int total = 0;
        for (String table : PURGE_TABLES) {
            if (!tableExists(table)) {
                continue;
            }
            int deleted = jdbc.update("DELETE FROM " + table, new MapSqlParameterSource());
            deletedCounts.put(table, deleted);
            total += deleted;
        }
        if (clearOrganizationsAndLicense) {
            for (String table : ORGANIZATION_LICENSE_TABLES) {
                if (!tableExists(table)) {
                    continue;
                }
                int deleted = jdbc.update("DELETE FROM " + table, new MapSqlParameterSource());
                deletedCounts.put(table, deleted);
                total += deleted;
            }
        }
        String detail = clearOrganizationsAndLicense
                ? "系统初始化完成（含清空单位与授权），共清理 " + total + " 条记录；清理前在册人员 "
                + preview.totalPersonnelRecords() + " 人、单位 " + preview.organizationCount() + " 个。"
                : "系统初始化完成，共清理 " + total + " 条记录；清理前在册人员 "
                + preview.totalPersonnelRecords() + " 人。";
        operationLogService.record(
                "SYSTEM_INITIALIZATION",
                "system",
                CONFIRM_PHRASE,
                detail);
        String message = clearOrganizationsAndLicense
                ? "系统初始化完毕：已清空人员业务数据、单位与授权。请导入单位授权包重建单位信息。"
                : "系统初始化完毕，已清理 " + total + " 条人员相关业务记录。";
        return new SystemInitializationResult(deletedCounts, total, clearOrganizationsAndLicense, message);
    }

    private void requireSystemSetupPermission() {
        if (!accessControlService.hasPermission("SYSTEM_CONFIG")) {
            throw new IllegalStateException("当前用户没有系统初始化权限。");
        }
    }

    private boolean tableExists(String table) {
        try {
            jdbc.queryForObject("SELECT COUNT(*) FROM " + table, new MapSqlParameterSource(), Long.class);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private long countTable(String table) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table, new MapSqlParameterSource(), Long.class);
        return count == null ? 0 : count;
    }

    private static String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

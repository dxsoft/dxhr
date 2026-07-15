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

    public SystemInitializationPreview preview() {
        requireSystemSetupPermission();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String table : PURGE_TABLES) {
            if (tableExists(table)) {
                counts.put(table, countTable(table));
            }
        }
        long personnel = counts.getOrDefault("dryjbxx", 0L);
        return new SystemInitializationPreview(
                counts,
                personnel,
                "系统初始化将删除所有人员及相关业务数据（单位信息、字典、工资标准表除外），此操作不可撤销。");
    }

    @Transactional
    public SystemInitializationResult execute(String confirmPhrase) {
        requireSystemSetupPermission();
        if (!CONFIRM_PHRASE.equals(emptyToNull(confirmPhrase))) {
            throw new IllegalArgumentException("确认短语不正确，请输入「" + CONFIRM_PHRASE + "」。");
        }
        SystemInitializationPreview preview = preview();
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
        operationLogService.record(
                "SYSTEM_INITIALIZATION",
                "system",
                CONFIRM_PHRASE,
                "系统初始化完成，共清理 " + total + " 条记录；清理前在册人员 "
                        + preview.totalPersonnelRecords() + " 人。");
        return new SystemInitializationResult(
                deletedCounts,
                total,
                "系统初始化完毕，已清理 " + total + " 条人员相关业务记录。");
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

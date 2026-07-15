package com.dxsoft.rsgzgl.maintenance;

import com.dxsoft.rsgzgl.security.AccessControlService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataMaintenanceService {

    private final NamedParameterJdbcTemplate jdbc;
    private final AccessControlService accessControlService;
    private final OperationLogService operationLogService;

    DataMaintenanceService(
            NamedParameterJdbcTemplate jdbc,
            AccessControlService accessControlService,
            OperationLogService operationLogService) {
        this.jdbc = jdbc;
        this.accessControlService = accessControlService;
        this.operationLogService = operationLogService;
    }

    public DataMaintenanceDiagnostics diagnostics() {
        requireDataMaintenancePermission();
        Map<String, Long> tableCounts = new LinkedHashMap<>();
        for (String table : List.of("dryjbxx", "hisbase", "dtgxx", "dryzwbh", "dmb", "dwbm", "cyxx")) {
            tableCounts.put(table, countTable(table));
        }
        return new DataMaintenanceDiagnostics(
                countTable("dryjbxx"),
                countTable("hisbase"),
                countTable("app_security_audit_log"),
                countTable("app_record_marker"),
                countOrphanAppMarkers(),
                tableCounts);
    }

    @Transactional
    public int purgeAuditLogs(int keepDays) {
        requireDataMaintenancePermission();
        if (keepDays < 7) {
            throw new IllegalArgumentException("操作日志至少保留 7 天。");
        }
        int deleted = jdbc.update("""
                DELETE FROM app_security_audit_log
                WHERE created_at < TIMESTAMPADD(DAY, -:keepDays, CURRENT_TIMESTAMP)
                """, new MapSqlParameterSource("keepDays", keepDays));
        operationLogService.record(
                "PURGE_AUDIT_LOG",
                "app_security_audit_log",
                String.valueOf(keepDays),
                "清理 " + keepDays + " 天前的操作日志，删除 " + deleted + " 条。");
        return deleted;
    }

    @Transactional
    public int purgeOrphanAppRecordMarkers() {
        requireDataMaintenancePermission();
        int deleted = jdbc.update("""
                DELETE marker
                FROM app_record_marker marker
                LEFT JOIN hisbase h ON marker.table_name = 'hisbase' AND marker.record_id = h.id
                WHERE marker.table_name = 'hisbase' AND h.id IS NULL
                """, new MapSqlParameterSource());
        deleted += jdbc.update("""
                DELETE marker
                FROM app_record_marker marker
                LEFT JOIN dtgxx t ON marker.table_name = 'dtgxx' AND marker.record_id = CAST(t.id AS CHAR)
                WHERE marker.table_name = 'dtgxx' AND t.id IS NULL
                """, new MapSqlParameterSource());
        operationLogService.record(
                "PURGE_ORPHAN_MARKERS",
                "app_record_marker",
                String.valueOf(deleted),
                "清理孤立 app_record_marker 记录 " + deleted + " 条。");
        return deleted;
    }

    private void requireDataMaintenancePermission() {
        if (!accessControlService.hasPermission("DATA_MAINTENANCE")) {
            throw new IllegalStateException("当前用户没有数据维护权限。");
        }
    }

    private long countTable(String table) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table, new MapSqlParameterSource(), Long.class);
        return count == null ? 0 : count;
    }

    private long countOrphanAppMarkers() {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM app_record_marker marker
                LEFT JOIN hisbase h ON marker.table_name = 'hisbase' AND marker.record_id = h.id
                WHERE marker.table_name = 'hisbase' AND h.id IS NULL
                """, new MapSqlParameterSource(), Long.class);
        return count == null ? 0 : count;
    }
}

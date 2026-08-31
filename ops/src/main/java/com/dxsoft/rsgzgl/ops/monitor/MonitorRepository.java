package com.dxsoft.rsgzgl.ops.monitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
class MonitorRepository {

    private final JdbcTemplate jdbcTemplate;

    MonitorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    int countTargets() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM monitor_target", Integer.class);
        return count == null ? 0 : count;
    }

    List<MonitorTargetView> listTargets() {
        return jdbcTemplate.query(
                "SELECT * FROM monitor_target ORDER BY id",
                this::mapTarget);
    }

    List<MonitorTargetView> listEnabledTargets() {
        return jdbcTemplate.query(
                "SELECT * FROM monitor_target WHERE enabled = TRUE ORDER BY id",
                this::mapTarget);
    }

    MonitorTargetView insertTarget(String name, String url, int timeoutMs, boolean enabled) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO monitor_target (name, kind, url, timeout_ms, enabled) VALUES (?, 'HTTP', ?, ?, ?)",
                    new String[] {"id"});
            ps.setString(1, name);
            ps.setString(2, url);
            ps.setInt(3, timeoutMs);
            ps.setBoolean(4, enabled);
            return ps;
        }, keys);
        Number id = keys.getKey();
        return findTarget(id == null ? -1L : id.longValue());
    }

    void deleteTarget(long id) {
        int n = jdbcTemplate.update("DELETE FROM monitor_target WHERE id = ?", id);
        if (n == 0) {
            throw new IllegalArgumentException("探测目标不存在");
        }
    }

    MonitorTargetView findTarget(long id) {
        List<MonitorTargetView> rows = jdbcTemplate.query(
                "SELECT * FROM monitor_target WHERE id = ?",
                this::mapTarget,
                id);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void insertSnapshot(String overall, HostSnapshot host, String detailJson) {
        jdbcTemplate.update("""
                INSERT INTO monitor_snapshot (
                    overall, cpu_percent, memory_used_bytes, memory_total_bytes,
                    disk_used_bytes, disk_total_bytes, load_average, hostname, detail
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                overall,
                host.cpuPercent(),
                host.memoryUsedBytes(),
                host.memoryTotalBytes(),
                host.diskUsedBytes(),
                host.diskTotalBytes(),
                host.loadAverage(),
                host.hostname(),
                detailJson);
    }

    List<SnapshotRow> listSnapshots(LocalDateTime since, int limit) {
        return jdbcTemplate.query("""
                SELECT * FROM monitor_snapshot
                WHERE collected_at >= ?
                ORDER BY collected_at DESC
                LIMIT ?
                """,
                this::mapSnapshot,
                since,
                limit);
    }

    SnapshotRow latestSnapshot() {
        List<SnapshotRow> rows = jdbcTemplate.query(
                "SELECT * FROM monitor_snapshot ORDER BY collected_at DESC LIMIT 1",
                this::mapSnapshot);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void pruneSnapshots(LocalDateTime before) {
        jdbcTemplate.update("DELETE FROM monitor_snapshot WHERE collected_at < ?", before);
    }

    void insertAlert(String level, String title, String message, String fingerprint) {
        jdbcTemplate.update(
                "INSERT INTO monitor_alert (level, title, message, fingerprint) VALUES (?, ?, ?, ?)",
                level,
                title,
                message,
                fingerprint);
    }

    String lastAlertFingerprint() {
        List<String> rows = jdbcTemplate.query(
                "SELECT fingerprint FROM monitor_alert ORDER BY id DESC LIMIT 1",
                (rs, rowNum) -> rs.getString("fingerprint"));
        return rows.isEmpty() ? null : rows.getFirst();
    }

    List<MonitorAlertView> listAlerts(int limit) {
        return jdbcTemplate.query("""
                SELECT id, created_at, level, title, message
                FROM monitor_alert
                ORDER BY id DESC
                LIMIT ?
                """,
                (rs, rowNum) -> new MonitorAlertView(
                        rs.getLong("id"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getString("level"),
                        rs.getString("title"),
                        rs.getString("message")),
                limit);
    }

    private MonitorTargetView mapTarget(ResultSet rs, int rowNum) throws SQLException {
        return new MonitorTargetView(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("kind"),
                rs.getString("url"),
                rs.getInt("timeout_ms"),
                rs.getBoolean("enabled"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    private SnapshotRow mapSnapshot(ResultSet rs, int rowNum) throws SQLException {
        return new SnapshotRow(
                rs.getLong("id"),
                rs.getTimestamp("collected_at").toLocalDateTime(),
                rs.getString("overall"),
                rs.getDouble("cpu_percent"),
                rs.getLong("memory_used_bytes"),
                rs.getLong("memory_total_bytes"),
                rs.getLong("disk_used_bytes"),
                rs.getLong("disk_total_bytes"),
                rs.getDouble("load_average"),
                rs.getString("hostname"),
                rs.getString("detail"));
    }

    record SnapshotRow(
            Long id,
            LocalDateTime collectedAt,
            String overall,
            double cpuPercent,
            long memoryUsedBytes,
            long memoryTotalBytes,
            long diskUsedBytes,
            long diskTotalBytes,
            double loadAverage,
            String hostname,
            String detail
    ) {
    }
}

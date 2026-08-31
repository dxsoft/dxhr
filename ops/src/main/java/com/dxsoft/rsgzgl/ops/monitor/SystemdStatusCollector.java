package com.dxsoft.rsgzgl.ops.monitor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class SystemdStatusCollector {

    private final List<String> units;

    SystemdStatusCollector(
            @Value("${rsgzgl.ops.monitor.systemd-units:rsgzgl,rsgzgl@demo,rsgzgl@pq,rsgzgl@xyzzb}") String units) {
        this.units = split(units);
    }

    List<ServiceStatus> collect() {
        if (units.isEmpty() || !hasSystemctl()) {
            return List.of();
        }
        List<ServiceStatus> rows = new ArrayList<>();
        for (String unit : units) {
            rows.add(read(unit));
        }
        return List.copyOf(rows);
    }

    private ServiceStatus read(String unit) {
        String text = run("systemctl", "show", unit, "--no-pager",
                "--property=Id,ActiveState,SubState,NRestarts,Result,LoadState");
        if (text.isBlank()) {
            return new ServiceStatus(unit, "unknown", "", 0, "WARN", "无法读取 systemd 状态");
        }
        String active = property(text, "ActiveState");
        String sub = property(text, "SubState");
        String load = property(text, "LoadState");
        long restarts = parseLong(property(text, "NRestarts"));
        if ("not-found".equalsIgnoreCase(load)) {
            return new ServiceStatus(unit, active, sub, restarts, "CRIT", "单元不存在");
        }
        if ("failed".equalsIgnoreCase(active) || "inactive".equalsIgnoreCase(active) || "deactivating".equalsIgnoreCase(active)) {
            return new ServiceStatus(unit, active, sub, restarts, "CRIT", active + " / " + sub);
        }
        if ("activating".equalsIgnoreCase(active) || "reloading".equalsIgnoreCase(active)) {
            return new ServiceStatus(unit, active, sub, restarts, "WARN", active + " / " + sub);
        }
        if (restarts >= 5) {
            return new ServiceStatus(unit, active, sub, restarts, "WARN", "已重启 " + restarts + " 次");
        }
        return new ServiceStatus(unit, active, sub, restarts, "OK", sub.isBlank() ? active : (active + " / " + sub));
    }

    private static boolean hasSystemctl() {
        String text = run("systemctl", "--version");
        return text.toLowerCase(Locale.ROOT).contains("systemd");
    }

    private static String property(String text, String key) {
        String prefix = key + "=";
        for (String line : text.split("\\R")) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return "";
    }

    private static long parseLong(String raw) {
        try {
            return Long.parseLong(raw.trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    static List<String> split(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<String> items = new ArrayList<>();
        for (String part : raw.split("[,\\s]+")) {
            if (!part.isBlank()) {
                items.add(part.trim());
            }
        }
        return List.copyOf(items);
    }

    private static String run(String... command) {
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(3, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "";
            }
            return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return "";
        }
    }
}

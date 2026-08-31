package com.dxsoft.rsgzgl.ops.monitor;

import com.sun.management.OperatingSystemMXBean;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
class HostMetricsCollector {

    private static final Set<String> SKIP_FS = Set.of(
            "tmpfs", "devtmpfs", "proc", "sysfs", "squashfs", "cgroup", "cgroup2",
            "nsfs", "bpf", "tracefs", "debugfs", "securityfs", "ramfs");

    HostSnapshot collect() {
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpu = os.getCpuLoad();
        if (cpu < 0) {
            cpu = os.getProcessCpuLoad();
        }
        long memTotal = os.getTotalMemorySize();
        long memFree = os.getFreeMemorySize();
        long[] swap = readSwap();
        Map<String, long[]> inodeByMount = readInodes();
        List<DiskUsage> disks = new ArrayList<>();
        DiskUsage primary = null;
        for (FileStore store : FileSystems.getDefault().getFileStores()) {
            try {
                String type = store.type() == null ? "" : store.type().toLowerCase(Locale.ROOT);
                if (SKIP_FS.contains(type) || store.isReadOnly()) {
                    continue;
                }
                long total = store.getTotalSpace();
                if (total <= 0) {
                    continue;
                }
                long used = Math.max(0, total - store.getUsableSpace());
                long[] inodes = inodesFor(store, inodeByMount);
                DiskUsage disk = new DiskUsage(store.toString(), store.type(), used, total, inodes[0], inodes[1]);
                disks.add(disk);
                if (primary == null || total > primary.totalBytes()) {
                    primary = disk;
                }
            } catch (IOException ignored) {
            }
        }
        if (primary == null) {
            primary = new DiskUsage("unknown", "", 0, 0, 0, 0);
        }
        String hostname = "unknown";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
        }
        return new HostSnapshot(
                hostname,
                System.getProperty("os.name", "unknown") + " " + System.getProperty("os.arch", ""),
                cpu < 0 ? 0 : cpu * 100.0,
                Math.max(0, memTotal - memFree),
                memTotal,
                swap[0],
                swap[1],
                primary.usedBytes(),
                primary.totalBytes(),
                primary.name(),
                os.getSystemLoadAverage(),
                ManagementFactory.getRuntimeMXBean().getUptime(),
                Runtime.getRuntime().availableProcessors(),
                List.copyOf(disks));
    }

    private static long[] readSwap() {
        Path meminfo = Path.of("/proc/meminfo");
        if (!Files.isRegularFile(meminfo)) {
            return new long[] {0, 0};
        }
        try {
            long totalKb = -1;
            long freeKb = -1;
            for (String line : Files.readAllLines(meminfo, StandardCharsets.UTF_8)) {
                if (line.startsWith("SwapTotal:")) {
                    totalKb = parseKb(line);
                } else if (line.startsWith("SwapFree:")) {
                    freeKb = parseKb(line);
                }
            }
            if (totalKb < 0 || freeKb < 0) {
                return new long[] {0, 0};
            }
            return new long[] {Math.max(0, totalKb - freeKb) * 1024, totalKb * 1024};
        } catch (IOException ignored) {
            return new long[] {0, 0};
        }
    }

    private static long parseKb(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length < 2) {
            return -1;
        }
        try {
            return Long.parseLong(parts[1]);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static Map<String, long[]> readInodes() {
        Map<String, long[]> byMount = new LinkedHashMap<>();
        String text = run("df", "-iP");
        if (text.isBlank()) {
            return byMount;
        }
        String[] lines = text.split("\\R");
        for (int i = 1; i < lines.length; i++) {
            String[] cols = lines[i].trim().split("\\s+");
            if (cols.length < 6) {
                continue;
            }
            try {
                long total = Long.parseLong(cols[1]);
                long used = Long.parseLong(cols[2]);
                String mount = cols[cols.length - 1];
                byMount.put(mount, new long[] {used, total});
            } catch (NumberFormatException ignored) {
            }
        }
        return byMount;
    }

    private static long[] inodesFor(FileStore store, Map<String, long[]> inodeByMount) {
        String text = store.toString();
        for (Map.Entry<String, long[]> entry : inodeByMount.entrySet()) {
            if (text.equals(entry.getKey()) || text.startsWith(entry.getKey() + " ") || text.contains("(" + entry.getKey() + ")")) {
                return entry.getValue();
            }
        }
        if ((text.startsWith("/ ") || text.equals("/") || text.startsWith("/ ("))
                && inodeByMount.containsKey("/")) {
            return inodeByMount.get("/");
        }
        return new long[] {0, 0};
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

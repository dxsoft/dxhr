package com.dxsoft.rsgzgl.ops.monitor;

import java.time.LocalDateTime;
import java.util.List;

public record MonitorSnapshotView(
        Long id,
        LocalDateTime collectedAt,
        String overall,
        double cpuPercent,
        long memoryUsedBytes,
        long memoryTotalBytes,
        long swapUsedBytes,
        long swapTotalBytes,
        long diskUsedBytes,
        long diskTotalBytes,
        double loadAverage,
        String hostname,
        List<ProbeResult> probes,
        List<DiskUsage> disks,
        List<ServiceStatus> services,
        List<CertificateStatus> certificates,
        List<InstanceRuntime> runtimes,
        String os,
        int processors,
        long jvmUptimeMs,
        String diskName
) {
}

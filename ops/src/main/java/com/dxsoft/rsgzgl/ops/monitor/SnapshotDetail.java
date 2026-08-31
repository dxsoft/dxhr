package com.dxsoft.rsgzgl.ops.monitor;

import java.util.List;

record SnapshotDetail(
        String os,
        int processors,
        long jvmUptimeMs,
        String diskName,
        long swapUsedBytes,
        long swapTotalBytes,
        List<DiskUsage> disks,
        List<ProbeResult> probes,
        List<ServiceStatus> services,
        List<CertificateStatus> certificates,
        List<InstanceRuntime> runtimes
) {
}

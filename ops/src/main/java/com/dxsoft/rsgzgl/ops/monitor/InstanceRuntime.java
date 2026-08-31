package com.dxsoft.rsgzgl.ops.monitor;

public record InstanceRuntime(
        Long targetId,
        String name,
        String url,
        String status,
        String message,
        String dbStatus,
        long heapUsedBytes,
        long heapMaxBytes,
        long gcCount,
        long gcTimeMs,
        int threads,
        int hikariActive,
        int hikariIdle,
        int hikariPending,
        int hikariMax,
        int tomcatBusy,
        int tomcatCurrent,
        int tomcatMax,
        long jvmUptimeMs
) {

    double heapPercent() {
        return heapMaxBytes <= 0 ? 0 : heapUsedBytes * 100.0 / heapMaxBytes;
    }
}

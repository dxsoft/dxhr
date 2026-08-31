package com.dxsoft.rsgzgl.ops.monitor;

record RuntimePayload(
        String status,
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
}

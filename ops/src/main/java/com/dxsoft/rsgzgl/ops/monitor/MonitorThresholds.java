package com.dxsoft.rsgzgl.ops.monitor;

public record MonitorThresholds(
        double cpuWarn,
        double cpuCrit,
        double memoryWarn,
        double memoryCrit,
        double diskWarn,
        double diskCrit,
        double swapWarn,
        double swapCrit,
        double inodeWarn,
        double inodeCrit,
        int certWarnDays,
        int certCritDays,
        double heapWarn,
        double heapCrit
) {
}

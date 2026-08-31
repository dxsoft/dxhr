package com.dxsoft.rsgzgl.ops.monitor;

import java.util.List;

record HostSnapshot(
        String hostname,
        String os,
        double cpuPercent,
        long memoryUsedBytes,
        long memoryTotalBytes,
        long swapUsedBytes,
        long swapTotalBytes,
        long diskUsedBytes,
        long diskTotalBytes,
        String diskName,
        double loadAverage,
        long jvmUptimeMs,
        int processors,
        List<DiskUsage> disks
) {

    double memoryPercent() {
        return memoryTotalBytes <= 0 ? 0 : memoryUsedBytes * 100.0 / memoryTotalBytes;
    }

    double diskPercent() {
        return diskTotalBytes <= 0 ? 0 : diskUsedBytes * 100.0 / diskTotalBytes;
    }

    double swapPercent() {
        return swapTotalBytes <= 0 ? 0 : swapUsedBytes * 100.0 / swapTotalBytes;
    }

    double worstInodePercent() {
        double worst = 0;
        if (disks == null) {
            return 0;
        }
        for (DiskUsage disk : disks) {
            worst = Math.max(worst, disk.inodePercent());
        }
        return worst;
    }
}

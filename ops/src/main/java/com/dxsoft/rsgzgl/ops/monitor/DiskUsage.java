package com.dxsoft.rsgzgl.ops.monitor;

public record DiskUsage(
        String name,
        String type,
        long usedBytes,
        long totalBytes,
        long usedInodes,
        long totalInodes
) {

    double usedPercent() {
        return totalBytes <= 0 ? 0 : usedBytes * 100.0 / totalBytes;
    }

    double inodePercent() {
        return totalInodes <= 0 ? 0 : usedInodes * 100.0 / totalInodes;
    }
}

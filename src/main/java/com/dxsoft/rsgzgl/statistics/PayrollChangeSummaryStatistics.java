package com.dxsoft.rsgzgl.statistics;

public record PayrollChangeSummaryStatistics(
        String changeType,
        String period,
        long changeCount,
        long personnelCount
) {
}

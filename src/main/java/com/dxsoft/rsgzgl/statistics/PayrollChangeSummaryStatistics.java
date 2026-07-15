package com.dxsoft.rsgzgl.statistics;

public record PayrollChangeSummaryStatistics(
        String period,
        long changeCount,
        long personnelCount
) {
}

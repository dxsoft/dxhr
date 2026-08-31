package com.dxsoft.rsgzgl.statistics;

public record PersonnelSummaryStatistics(
        long organizationCount,
        long activePersonnelCount,
        long changedPersonnelCount,
        long probationPersonnelCount
) {
}

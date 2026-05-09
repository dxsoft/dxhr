package com.dxsoft.rsgzgl.personnel;

public record PersonnelStructureSummaryRecord(
        String organizationCode,
        String organizationName,
        String personnelCategory,
        String organizationType,
        String postCategory,
        Long personnelCount
) {
}

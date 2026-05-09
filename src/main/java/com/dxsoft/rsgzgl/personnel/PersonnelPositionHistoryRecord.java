package com.dxsoft.rsgzgl.personnel;

public record PersonnelPositionHistoryRecord(
        Integer id,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String currentPositionCode,
        String currentPosition,
        String positionLevel,
        String rankCode,
        String positionCode,
        String positionName,
        String positionType,
        String startYearMonth,
        Integer intervalYears,
        String activeFlag,
        String calculationStandard
) {
}

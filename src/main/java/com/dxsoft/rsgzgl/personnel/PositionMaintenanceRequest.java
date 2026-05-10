package com.dxsoft.rsgzgl.personnel;

public record PositionMaintenanceRequest(
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

package com.dxsoft.rsgzgl.personnel;

public record PositionMaintenanceRequest(
        String currentPositionCode,
        String currentPosition,
        String positionLevel,
        String rankCode,
        String positionCode,
        String positionName,
        String startYearMonth,
        Integer intervalYears,
        String activeFlag,
        String promotionFlag,
        String positionChangeReason,
        Integer linkedAwardId
) {
}

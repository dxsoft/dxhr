package com.dxsoft.rsgzgl.payroll;

public record PositionChangeCandidate(
        String positionCode,
        String positionName,
        String startYearMonth,
        String positionChangeReason,
        Integer linkedAwardId
) {
    public PositionChangeCandidate(String positionCode, String positionName, String startYearMonth) {
        this(positionCode, positionName, startYearMonth, null, null);
    }
}

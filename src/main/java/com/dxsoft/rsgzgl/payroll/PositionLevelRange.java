package com.dxsoft.rsgzgl.payroll;

public record PositionLevelRange(
        String positionCode,
        Integer minimumLevel,
        Integer maximumLevel
) {
}

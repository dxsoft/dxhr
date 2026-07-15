package com.dxsoft.rsgzgl.payroll;

public record PositionChangeDisplayPair(
        String beforePositionCode,
        String beforePositionName,
        String afterPositionCode,
        String afterPositionName
) {
}

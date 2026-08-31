package com.dxsoft.rsgzgl.retirement;

public record RetirementPositionLevelRange(
        String positionCode,
        boolean applicable,
        Integer minimumLevel,
        Integer maximumLevel) {
}

package com.dxsoft.rsgzgl.retirement;

public record RetirementRatioStandard(
        String category,
        String yearBand1,
        Integer rate1,
        String yearBand2,
        Integer rate2,
        String yearBand3,
        Integer rate3,
        String yearBand4,
        Integer rate4,
        String yearBand5,
        Integer rate5) {
}

package com.dxsoft.rsgzgl.payroll;

public record PositionSalaryStandard(
        String standardYearMonth,
        String positionCode,
        Integer amount
) {
}

package com.dxsoft.rsgzgl.payroll;

public record PositionSalaryStandardRequest(
        String standardYearMonth,
        String positionCode,
        Integer amount
) {
}

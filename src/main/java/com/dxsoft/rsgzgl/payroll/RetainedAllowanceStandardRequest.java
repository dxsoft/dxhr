package com.dxsoft.rsgzgl.payroll;

public record RetainedAllowanceStandardRequest(
        String positionCode,
        String name,
        Integer amount
) {
}

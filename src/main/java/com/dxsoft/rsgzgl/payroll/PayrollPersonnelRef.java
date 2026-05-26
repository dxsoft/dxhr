package com.dxsoft.rsgzgl.payroll;

public record PayrollPersonnelRef(
        int uid,
        String organizationCode,
        String personCode,
        String name
) {
}

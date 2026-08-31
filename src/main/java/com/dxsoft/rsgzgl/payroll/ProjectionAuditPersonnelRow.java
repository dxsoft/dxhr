package com.dxsoft.rsgzgl.payroll;

public record ProjectionAuditPersonnelRow(
        Integer uid,
        String organizationCode,
        String personCode,
        String name,
        String latestPeriod,
        Integer storedTotal
) {
}

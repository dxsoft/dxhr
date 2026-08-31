package com.dxsoft.rsgzgl.payroll;

public record AllowanceStandardPositionRow(
        String positionCode,
        String name,
        Integer performanceCategory,
        Integer dfbt2Id,
        Integer dfbt2Amount,
        Integer sdbtId,
        Integer sdbtAmount
) {
}

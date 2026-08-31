package com.dxsoft.rsgzgl.payroll;

record TransferProjectionAuditStep(
        String changePeriod,
        String changeType,
        String positionCode,
        String positionName,
        String gradeLevel,
        String gradeStep,
        String levelStartYear,
        String stepStartYear,
        String description
) {
}

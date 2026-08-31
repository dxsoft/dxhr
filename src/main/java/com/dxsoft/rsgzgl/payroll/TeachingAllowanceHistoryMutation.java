package com.dxsoft.rsgzgl.payroll;

record TeachingAllowanceHistoryMutation(
        String calculationYear,
        String calculationMonth,
        String changeType,
        Integer teachingAllowance,
        Integer totalAmount
) {
}

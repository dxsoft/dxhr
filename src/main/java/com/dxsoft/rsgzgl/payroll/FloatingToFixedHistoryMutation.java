package com.dxsoft.rsgzgl.payroll;

record FloatingToFixedHistoryMutation(
        String calculationYear,
        String calculationMonth,
        String changeType,
        String positionSalaryGrade,
        Integer positionSalary,
        Integer gradeSalary,
        Integer totalAmount,
        Integer personnelFixedFloatingTotal) {
}

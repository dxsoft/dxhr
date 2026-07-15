package com.dxsoft.rsgzgl.payroll;

public record PositionChangeApplyRequest(
        String calculationYear,
        String calculationMonth,
        String changeType,
        String positionCode,
        String positionName,
        String gradeSalaryLevel,
        String positionSalaryGrade,
        Integer positionSalary,
        Integer gradeSalary,
        Integer totalAmount,
        Boolean manualApply) {
}

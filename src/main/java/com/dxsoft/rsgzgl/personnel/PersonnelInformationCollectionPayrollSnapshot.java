package com.dxsoft.rsgzgl.personnel;

public record PersonnelInformationCollectionPayrollSnapshot(
        String calculationYear,
        String calculationMonth,
        String changeType,
        String positionCode,
        String positionName,
        String gradeLevel,
        String stepOrSalaryLevel,
        Integer positionSalary,
        Integer gradeSalary,
        Integer technicalGradeSalary,
        Integer totalAmount) {
}

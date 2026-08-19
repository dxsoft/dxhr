package com.dxsoft.rsgzgl.payroll;

public record OtherPayrollChangeCalcRequest(
        String calculationYear,
        String calculationMonth,
        String changeType,
        String positionCode,
        String positionName,
        String gradeSalaryLevel,
        String positionSalaryGrade,
        String salaryStandardYearMonth,
        String allowanceStandardYearMonth) {
}

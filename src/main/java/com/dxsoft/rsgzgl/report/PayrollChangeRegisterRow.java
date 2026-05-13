package com.dxsoft.rsgzgl.report;

import java.math.BigDecimal;

public record PayrollChangeRegisterRow(
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
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
        Integer performanceAllowance,
        Integer retainedAllowance,
        Integer rankAllowance,
        BigDecimal yearAllowance,
        Integer pgbc,
        Integer totalAmount
) {
}

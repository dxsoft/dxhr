package com.dxsoft.rsgzgl.report;

import java.math.BigDecimal;

public record PayrollChangeRegisterRow(
        String payrollHistoryId,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String calculationYear,
        String calculationMonth,
        String changeType,
        String beforePositionCode,
        String beforePositionName,
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

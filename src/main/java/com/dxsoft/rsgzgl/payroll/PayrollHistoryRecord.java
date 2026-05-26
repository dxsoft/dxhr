package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record PayrollHistoryRecord(
        String id,
        String successorId,
        boolean currentPayroll,
        Boolean appCreated,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String calculationYear,
        String calculationMonth,
        String changeType,
        String personCategory,
        String organizationType,
        String positionCode,
        String positionName,
        String positionSalaryGrade,
        String gradeSalaryLevel,
        String levelAssessmentStartYear,
        String stepAssessmentStartYear,
        String salaryStandardYearMonth,
        String allowanceStandardYearMonth,
        Integer positionSalary,
        Integer gradeSalary,
        Integer technicalGradeSalary,
        Integer performanceAllowance,
        Integer retainedAllowance,
        Integer rankAllowance,
        Integer floatingSalary,
        Integer bonusBalance,
        Integer teachingAllowance,
        Integer salaryIncrease,
        BigDecimal yearAllowance,
        Integer payGradeRetention,
        Integer totalAmount
) {
}

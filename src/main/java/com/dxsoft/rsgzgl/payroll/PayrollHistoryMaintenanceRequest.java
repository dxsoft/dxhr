package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record PayrollHistoryMaintenanceRequest(
        String calculationYear,
        String calculationMonth,
        String changeType,
        String positionCode,
        String positionName,
        String gradeSalaryLevel,
        String positionSalaryGrade,
        String salaryStandardYearMonth,
        String allowanceStandardYearMonth,
        String teachingIncreaseRatio,
        String rankName,
        String rankAllowanceStandardYearMonth,
        String floatingStep,
        Integer positionSalary,
        Integer gradeSalary,
        Integer technicalGradeSalary,
        Integer internSalary,
        Integer salaryIncrease,
        Integer teachingAllowance,
        Integer floatingSalary,
        Integer subsidyAllowance,
        Integer performanceAllowance,
        Integer payGradeRetention,
        Integer rankAllowance,
        Integer retainedReformAllowance,
        Integer overtimeAllowance,
        Integer retainedSpecialPostAllowance,
        Integer hygieneAllowance,
        Integer retainedAllowance,
        Integer bonusBalance,
        Integer specialPostAllowance,
        Integer otherAllowance,
        BigDecimal yearAllowance,
        Integer totalAmount
) {
}

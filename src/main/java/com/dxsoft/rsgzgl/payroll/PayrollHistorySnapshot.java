package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record PayrollHistorySnapshot(
        String id,
        String organizationCode,
        String personCode,
        String name,
        String calculationYear,
        String calculationMonth,
        String calculationType,
        String organizationType,
        String workStartYearMonth,
        Integer salaryYears,
        Integer interruptedSalaryYears,
        String teachingStartYearMonth,
        Integer teachingInterruptedYears,
        Integer raisePercentage,
        String rankAllowanceStandardYearMonth,
        String rankName,
        String positionCode,
        String positionName,
        String positionSalaryGrade,
        String floatingStep,
        String gradeSalaryLevel,
        String gradeSalaryStep,
        String salaryStandardYearMonth,
        String allowanceStandardYearMonth,
        Integer storedPositionSalary,
        Integer storedGradeSalary,
        Integer storedTechnicalGradeSalary,
        Integer storedPerformanceAllowance,
        Integer storedSubsidyAllowance,
        Integer storedRetainedAllowance,
        Integer storedTeachingAllowance,
        Integer storedSalaryIncrease,
        Integer storedRankAllowance,
        Integer storedFloatingSalary,
        Integer storedBonusBalance,
        BigDecimal storedYearAllowance,
        Integer storedTotal
) {
}

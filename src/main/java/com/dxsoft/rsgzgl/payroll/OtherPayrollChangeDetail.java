package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record OtherPayrollChangeDetail(
        String payrollHistoryId,
        Integer uid,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String currentPeriod,
        String currentChangeType,
        String currentPositionCode,
        String currentPositionName,
        String currentLevel,
        String currentStep,
        String currentSalaryStandardYearMonth,
        String currentAllowanceStandardYearMonth,
        Integer currentPositionSalary,
        Integer currentGradeSalary,
        Integer currentTechnicalGradeSalary,
        Integer currentPerformanceAllowance,
        Integer currentSubsidyAllowance,
        Integer currentRetainedAllowance,
        Integer currentTeachingAllowance,
        Integer currentTotal,
        String currentRankName,
        String performanceAllowanceCaption,
        String subsidyAllowanceCaption,
        Boolean showSubsidyAllowance,
        Boolean institutionPayroll,
        Boolean applyEligible,
        Boolean rollbackEligible,
        List<String> changeTypeOptions) {
}

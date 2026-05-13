package com.dxsoft.rsgzgl.payroll;

public record NormalPromotionPreview(
        Integer uid,
        String payrollHistoryId,
        String organizationCode,
        String personCode,
        String name,
        String calculationPeriod,
        String changeType,
        String positionCode,
        String positionName,
        String salaryStandardYearMonth,
        String currentGradeOrLevel,
        String promotedGradeOrLevel,
        String gradeSalaryLevel,
        String levelAssessmentStartYear,
        String stepAssessmentStartYear,
        Integer qualifiedYears,
        Integer requiredYears,
        Boolean eligible,
        Integer currentBaseSalary,
        Integer promotedBaseSalary,
        Integer increaseAmount,
        String baseSalarySource
) {
}

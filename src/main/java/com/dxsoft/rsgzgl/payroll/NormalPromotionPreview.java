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
        String gradeSalaryStep,
        Integer currentBaseSalary,
        Integer promotedBaseSalary,
        Integer increaseAmount,
        String baseSalarySource
) {
}

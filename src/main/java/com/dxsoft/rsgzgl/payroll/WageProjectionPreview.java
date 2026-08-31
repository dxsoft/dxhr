package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record WageProjectionPreview(
        Integer uid,
        String organizationCode,
        String personCode,
        String name,
        String targetPeriod,
        String basePeriod,
        String regularizationYearMonth,
        String positionCode,
        String positionName,
        String level,
        String stepOrSalaryLevel,
        String levelStepDisplay,
        String levelAssessmentStartYear,
        String stepAssessmentStartYear,
        String baseSalarySource,
        String salaryStandardYearMonth,
        Integer positionSalary,
        Integer gradeSalary,
        String rankName,
        String rankAllowanceStandardYearMonth,
        Integer rankAllowance,
        List<String> explanationLines,
        List<WageProjectionStepDetail> stepDetails
) {
}

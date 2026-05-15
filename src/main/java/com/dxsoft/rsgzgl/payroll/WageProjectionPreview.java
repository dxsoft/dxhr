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
        String levelAssessmentStartYear,
        String stepAssessmentStartYear,
        String baseSalarySource,
        List<String> explanationLines
) {
}

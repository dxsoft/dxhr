package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record GradeSalaryStandard(
        String standardYearMonth,
        String gradeLevel,
        List<Integer> gradeSteps
) {
}

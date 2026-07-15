package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record GradeSalaryStandardRequest(
        String standardYearMonth,
        String gradeLevel,
        List<Integer> gradeSteps
) {
}

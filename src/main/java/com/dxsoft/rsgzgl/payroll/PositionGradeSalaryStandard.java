package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record PositionGradeSalaryStandard(
        String standardYearMonth,
        String positionCode,
        Integer technicalGradeSalary,
        List<Integer> gradeSteps
) {
}

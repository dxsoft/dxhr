package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record JudicialPositionGradeSalaryStandard(
        String standardYearMonth,
        String positionCode,
        String positionName,
        List<Integer> gradeSteps
) {
}

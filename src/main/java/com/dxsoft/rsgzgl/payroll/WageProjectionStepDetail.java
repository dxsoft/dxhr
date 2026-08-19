package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;
import java.util.List;

public record WageProjectionStepDetail(
        String period,
        String description,
        String changeCategory,
        String positionCode,
        String positionName,
        String baseSalarySource,
        String level,
        String step,
        String levelStepDisplay,
        String levelStartYear,
        String stepStartYear,
        String salaryStandardYearMonth,
        String allowanceStandardYearMonth,
        List<PayrollPreviewComponent> components,
        BigDecimal total
) {
}

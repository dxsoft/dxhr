package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;
import java.util.List;

public record WageProjectionStepDetail(
        String period,
        String description,
        String positionCode,
        String positionName,
        String levelStepDisplay,
        String salaryStandardYearMonth,
        String allowanceStandardYearMonth,
        List<PayrollPreviewComponent> components,
        BigDecimal total
) {
}

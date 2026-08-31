package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record PayrollPreviewComponent(
        String fieldName,
        String caption,
        BigDecimal amount,
        String source
) {
}

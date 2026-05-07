package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record PayrollFieldMetadata(
        Integer id,
        Integer sequence,
        Integer fieldCategory,
        String tableName,
        String fieldName,
        String fieldType,
        Integer fieldLength,
        Integer decimalLength,
        String caption,
        String shortCaption,
        String simpleCaption,
        String enabledIn2006Policy,
        String enabled,
        String inputMode,
        String category,
        Boolean allowance,
        String association,
        Integer probationPaymentMode,
        Boolean allowanceStandard,
        String calculationMode,
        BigDecimal fixedValue,
        Boolean readOnly,
        Boolean grouped,
        Boolean counted
) {
}

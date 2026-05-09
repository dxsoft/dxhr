package com.dxsoft.rsgzgl.personnel;

import java.math.BigDecimal;

public record ChangedPersonnelRecord(
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String idCard,
        String gender,
        String birthYearMonth,
        String personnelCategory,
        String organizationType,
        String postCategory,
        String changeYear,
        String changeMonth,
        String changeType,
        String oldPositionCode,
        String oldPositionName,
        BigDecimal oldTotalAmount,
        String newPositionCode,
        String newPositionName,
        BigDecimal newTotalAmount,
        String salaryStandardYearMonth,
        String allowanceStandardYearMonth,
        String remark
) {
}

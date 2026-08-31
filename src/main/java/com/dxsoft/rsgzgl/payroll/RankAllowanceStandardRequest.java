package com.dxsoft.rsgzgl.payroll;

public record RankAllowanceStandardRequest(
        String standardYearMonth,
        String rankCode,
        String rankName,
        Integer amount,
        String category
) {
}

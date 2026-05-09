package com.dxsoft.rsgzgl.payroll;

public record RankAllowanceStandard(
        Integer id,
        String standardYearMonth,
        String rankCode,
        String rankName,
        Integer amount,
        String category
) {
}

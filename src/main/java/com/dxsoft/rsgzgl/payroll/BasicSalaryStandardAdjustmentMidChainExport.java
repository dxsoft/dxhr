package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record BasicSalaryStandardAdjustmentMidChainExport(
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String targetStandardYearMonth,
        String adjustmentHistoryId,
        List<SuccessorRecalc> successors
) {
    public record SuccessorRecalc(
            String period,
            String changeType,
            String oldSalaryStandardYearMonth,
            String newSalaryStandardYearMonth,
            Integer oldTotal,
            Integer newTotal,
            Integer difference
    ) {
    }
}

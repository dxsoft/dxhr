package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record RankAllowanceChangeMidChainExport(
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String executionPeriod,
        String adjustmentHistoryId,
        List<SuccessorRecalc> successors
) {
    public record SuccessorRecalc(
            String period,
            String changeType,
            String oldRankName,
            String newRankName,
            Integer oldRankAllowance,
            Integer newRankAllowance,
            Integer oldTotal,
            Integer newTotal,
            Integer difference
    ) {
    }
}

package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record RankAllowanceChangeBatchRollbackRequest(
        List<RankAllowanceChangeBatchRollbackItem> items
) {
    public record RankAllowanceChangeBatchRollbackItem(
            String payrollHistoryId,
            String organizationCode,
            String personCode
    ) {
    }
}

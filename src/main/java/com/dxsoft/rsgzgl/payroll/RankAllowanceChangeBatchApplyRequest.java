package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record RankAllowanceChangeBatchApplyRequest(
        List<RankAllowanceChangeBatchApplyItem> items
) {
    public record RankAllowanceChangeBatchApplyItem(
            String payrollHistoryId
    ) {
    }
}

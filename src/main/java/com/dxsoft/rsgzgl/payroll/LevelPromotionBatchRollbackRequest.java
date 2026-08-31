package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record LevelPromotionBatchRollbackRequest(
        List<LevelPromotionBatchRollbackItem> items
) {
    public record LevelPromotionBatchRollbackItem(
            String payrollHistoryId,
            String organizationCode,
            String personCode
    ) {
    }
}

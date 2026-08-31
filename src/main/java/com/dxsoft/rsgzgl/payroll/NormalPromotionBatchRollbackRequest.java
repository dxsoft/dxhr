package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record NormalPromotionBatchRollbackRequest(
        List<NormalPromotionBatchRollbackItem> items
) {
    public record NormalPromotionBatchRollbackItem(
            String payrollHistoryId,
            String organizationCode,
            String personCode
    ) {
    }
}

package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record RegularizationBatchRollbackRequest(
        List<RegularizationBatchRollbackItem> items
) {
    public record RegularizationBatchRollbackItem(
            String payrollHistoryId,
            String organizationCode,
            String personCode
    ) {
    }
}

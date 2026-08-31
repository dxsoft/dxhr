package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record BasicSalaryStandardAdjustmentBatchRollbackRequest(
        List<BasicSalaryStandardAdjustmentRollbackItem> items
) {
    public record BasicSalaryStandardAdjustmentRollbackItem(
            String payrollHistoryId,
            String organizationCode,
            String personCode
    ) {
    }
}

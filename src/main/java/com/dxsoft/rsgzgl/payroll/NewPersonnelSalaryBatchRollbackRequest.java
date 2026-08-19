package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record NewPersonnelSalaryBatchRollbackRequest(
        List<NewPersonnelSalaryBatchRollbackItem> items
) {
    public record NewPersonnelSalaryBatchRollbackItem(
            String payrollHistoryId,
            String organizationCode,
            String personCode
    ) {
    }
}

package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record NewPersonnelSalaryBatchApplyRequest(
        List<NewPersonnelSalaryBatchApplyItem> items
) {
    public record NewPersonnelSalaryBatchApplyItem(
            int uid,
            String organizationCode,
            String personCode
    ) {
    }
}

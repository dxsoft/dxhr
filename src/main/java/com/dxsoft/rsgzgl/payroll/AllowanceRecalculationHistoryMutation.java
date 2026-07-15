package com.dxsoft.rsgzgl.payroll;

record AllowanceRecalculationHistoryMutation(
        String calculationYear,
        String calculationMonth,
        String changeType,
        Integer performanceAllowance,
        Integer subsidyAllowance,
        Integer retainedAllowance,
        Integer salaryIncrease,
        Integer yearAllowance,
        Integer totalAmount,
        String allowanceStandardYearMonth) {
}

package com.dxsoft.rsgzgl.payroll;

record SalaryStandardAdjustmentHistoryMutation(
        String calculationYear,
        String calculationMonth,
        String changeType,
        Integer positionSalary,
        Integer gradeSalary,
        Integer technicalGradeSalary,
        Integer performanceAllowance,
        Integer subsidyAllowance,
        Integer retainedAllowance,
        Integer floatingSalary,
        Integer salaryIncrease,
        Integer totalAmount,
        String salaryStandardYearMonth,
        String allowanceStandardYearMonth) {
}

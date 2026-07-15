package com.dxsoft.rsgzgl.payroll;

record InternSalaryHistoryMutation(
        String calculationYear,
        String calculationMonth,
        String changeType,
        Integer internSalary,
        Integer performanceAllowance,
        Integer totalAmount) {
}

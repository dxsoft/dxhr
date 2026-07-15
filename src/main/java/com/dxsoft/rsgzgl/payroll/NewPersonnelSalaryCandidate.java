package com.dxsoft.rsgzgl.payroll;

record NewPersonnelSalaryCandidate(
        Integer uid,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String joinYearMonth,
        String joinType,
        String positionCode,
        String positionName,
        String positionStartYearMonth,
        String workStartYearMonth,
        String regularizationYearMonth,
        String educationCode,
        String educationName,
        Integer salaryYears,
        String payrollHistoryId,
        String currentChangeType) {
}

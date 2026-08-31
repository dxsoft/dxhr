package com.dxsoft.rsgzgl.payroll;

record WageReform2006Candidate(
        Integer uid,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String workStartYearMonth,
        String regularizationYearMonth,
        String positionCode,
        String positionName,
        String positionStartYearMonth,
        Integer salaryYears,
        String payrollHistoryId,
        String currentChangeType,
        String calculationYear,
        String calculationMonth) {
}

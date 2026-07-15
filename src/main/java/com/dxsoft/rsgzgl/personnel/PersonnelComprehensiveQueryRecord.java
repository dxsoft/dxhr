package com.dxsoft.rsgzgl.personnel;

public record PersonnelComprehensiveQueryRecord(
        Integer uid,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String idCard,
        String gender,
        String birthYearMonth,
        String personnelCategory,
        String organizationType,
        String postCategory,
        String workStartYearMonth,
        String regularizationYearMonth,
        String educationCode,
        String highestEducation,
        Integer salaryYears,
        String appointmentPositionCode,
        String appointmentPositionName,
        String appointmentStartYearMonth,
        String payrollPeriod,
        String payrollPositionCode,
        String payrollPositionName,
        String gradeLevel,
        String gradeStep,
        Integer totalSalary) {
}

package com.dxsoft.rsgzgl.payroll;

record PensionBaseRecord(
        Integer id,
        String organizationCode,
        String personCode,
        String name,
        String idCard,
        String year,
        String positionCode,
        String positionName,
        String gradeLevel,
        String gradeStep,
        Integer positionSalary,
        Integer gradeSalary,
        Integer technicalGradeSalary,
        Integer salaryIncrease,
        Integer floatingSalary,
        Integer performanceAllowance,
        Integer retainedAllowance,
        Integer bonusBalance,
        Integer rankAllowanceBonus,
        Integer teachingAllowance,
        Integer postAllowanceBonus,
        Integer averageSalary,
        String remark) {
}

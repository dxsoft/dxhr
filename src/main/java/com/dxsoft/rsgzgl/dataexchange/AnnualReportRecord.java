package com.dxsoft.rsgzgl.dataexchange;

import java.math.BigDecimal;

record AnnualReportRecord(
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String idCard,
        String gender,
        String birthYearMonth,
        String personnelCategory,
        String currentPosition,
        String currentJob,
        String currentGrade,
        String currentLevel,
        String period,
        String changeType,
        BigDecimal positionSalary,
        BigDecimal gradeSalary,
        BigDecimal techGradeSalary,
        BigDecimal performanceAllowance,
        BigDecimal retainedAllowance,
        BigDecimal rankAllowance,
        BigDecimal yearAllowance,
        BigDecimal teachingAllowance,
        BigDecimal improvedSalary,
        BigDecimal floatingSalary,
        BigDecimal bonusBalance,
        BigDecimal pgbc,
        BigDecimal total) {
}

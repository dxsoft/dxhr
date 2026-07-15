package com.dxsoft.rsgzgl.personnel;

record PersonnelComprehensiveQueryCriteria(
        String organizationCode,
        String keyword,
        String gender,
        String personnelCategory,
        String organizationType,
        String postCategory,
        String educationCode,
        String birthYearMonthFrom,
        String birthYearMonthTo,
        String workStartYearMonthFrom,
        String workStartYearMonthTo,
        String regularizationYearMonthFrom,
        String regularizationYearMonthTo,
        String positionCode,
        String positionCodePrefix,
        String gradeLevelFrom,
        String gradeLevelTo) {
}

package com.dxsoft.rsgzgl.personnel;

public record ExcelPersonnelImportRow(
        int rowNumber,
        String personCode,
        String name,
        String gender,
        String idCard,
        String birthYearMonth,
        String workStartYearMonth,
        String joinYearMonth,
        String regularizationYearMonth,
        String personnelCategory,
        String organizationType,
        String postCategory,
        String highestEducation,
        String school,
        String graduationDate,
        String positionLevel,
        String currentPosition,
        String positionStartYearMonth,
        String ethnicity,
        String politicalStatus,
        String archiveNumber,
        String salaryYears) {
}

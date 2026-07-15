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
        String probationAssessment,
        String highestEducation,
        String school,
        String graduationDate,
        String assessmentSummary,
        String positionLevel,
        String positionStartYearMonth,
        String annualAssessmentSummary) {
}

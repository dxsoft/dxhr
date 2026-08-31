package com.dxsoft.rsgzgl.report;

public record PersonnelReportCandidateRow(
        Integer uid,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String gender,
        String birthYearMonth,
        String personnelCategory,
        String currentPosition) {
}

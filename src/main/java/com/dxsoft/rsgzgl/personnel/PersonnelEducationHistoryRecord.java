package com.dxsoft.rsgzgl.personnel;

public record PersonnelEducationHistoryRecord(
        Integer id,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String educationCode,
        String educationName,
        String school,
        String enrollmentDate,
        String graduationDate,
        Integer studyYears,
        String educationType,
        String remark
) {
}

package com.dxsoft.rsgzgl.personnel;

public record EducationRecord(
        Integer id,
        String organizationCode,
        String personCode,
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

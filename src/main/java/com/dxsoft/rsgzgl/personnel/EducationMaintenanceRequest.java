package com.dxsoft.rsgzgl.personnel;

public record EducationMaintenanceRequest(
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

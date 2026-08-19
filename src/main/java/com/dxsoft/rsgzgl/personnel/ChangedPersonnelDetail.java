package com.dxsoft.rsgzgl.personnel;

import java.util.List;
import java.util.Map;

public record ChangedPersonnelDetail(
        PersonnelMaintenanceRecord basic,
        List<EducationRecord> education,
        List<PositionRecord> positions,
        List<AssessmentRecord> assessments,
        List<Map<String, Object>> payrollHistories,
        Map<String, Object> relatedRecords
) {
}

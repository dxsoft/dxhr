package com.dxsoft.rsgzgl.personnel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PersonnelServiceTest {

    @Test
    void defaultsInstitutionAssessmentToQualified() {
        assertThat(PersonnelService.defaultAssessmentResult(record("事业人员", "事业单位")))
                .isEqualTo("合格");
    }

    @Test
    void defaultsNonInstitutionAssessmentToCompetent() {
        assertThat(PersonnelService.defaultAssessmentResult(record("公务员", "机关")))
                .isEqualTo("称职");
    }

    @Test
    void treatsMissingCategoryAsNonInstitution() {
        assertThat(PersonnelService.defaultAssessmentResult(record(null, null)))
                .isEqualTo("称职");
    }

    @Test
    void validatesAdministrativeAssessmentResult() {
        PersonnelService.validateAssessmentResult("公务员", "机关", "称职");
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                PersonnelService.validateAssessmentResult("公务员", "机关", "合格"));
    }

    @Test
    void validatesInstitutionAssessmentResult() {
        PersonnelService.validateAssessmentResult("事业人员", "事业单位", "合格");
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                PersonnelService.validateAssessmentResult("事业人员", "事业单位", "称职"));
    }

    @Test
    void recognizesCurrentPositionFlag() {
        assertThat(PersonnelService.isCurrentPositionFlag("1")).isTrue();
        assertThat(PersonnelService.isCurrentPositionFlag("0")).isFalse();
        assertThat(PersonnelService.isCurrentPositionFlag("")).isFalse();
    }

    private static PersonnelMaintenanceRecord record(String personnelCategory, String organizationType) {
        return new PersonnelMaintenanceRecord(
                1,
                "001",
                "测试单位",
                "",
                "0001",
                "测试人员",
                "",
                "",
                "",
                personnelCategory,
                organizationType,
                "",
                "",
                "",
                0,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "");
    }
}

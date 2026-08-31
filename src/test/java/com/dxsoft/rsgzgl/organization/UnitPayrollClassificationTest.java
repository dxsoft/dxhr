package com.dxsoft.rsgzgl.organization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UnitPayrollClassificationTest {

    @Test
    void referenceCivilServiceInstitutionUsesAdministrativeTreatment() {
        assertThat(UnitPayrollClassification.usesAdministrativeTreatment("事业", "参照公务员")).isTrue();
        assertThat(UnitPayrollClassification.effectiveUnitCategory("事业", "参照公务员")).isEqualTo("行政");
    }

    @Test
    void institutionManagedPayrollStaysInstitution() {
        assertThat(UnitPayrollClassification.usesAdministrativeTreatment("事业", "事业管理")).isFalse();
        assertThat(UnitPayrollClassification.effectiveUnitCategory("事业", "事业管理  ")).isEqualTo("事业");
    }

    @Test
    void legacyNumericPayrollCategoriesUseAdministrativeRules() {
        assertThat(UnitPayrollClassification.isCivilServiceManagedPayroll("1")).isTrue();
        assertThat(UnitPayrollClassification.isCivilServiceManagedPayroll("2")).isTrue();
    }
}

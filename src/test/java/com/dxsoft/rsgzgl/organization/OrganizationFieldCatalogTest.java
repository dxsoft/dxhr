package com.dxsoft.rsgzgl.organization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrganizationFieldCatalogTest {

    @Test
    void payrollCategoryValuesForAdministrativeUnit() {
        assertThat(OrganizationFieldCatalog.payrollCategoryValuesForUnitCategory("行政"))
                .containsExactly("公务员管理", "参照公务员", "依照公务员");
    }

    @Test
    void payrollCategoryValuesForInstitutionUnit() {
        assertThat(OrganizationFieldCatalog.payrollCategoryValuesForUnitCategory("事业"))
                .containsExactly("事业管理", "参照公务员");
    }

    @Test
    void defaultPayrollCategoryValuesIncludeAllLegacyChoices() {
        assertThat(OrganizationFieldCatalog.defaultPayrollCategoryValues())
                .containsExactly("公务员管理", "参照公务员", "依照公务员", "事业管理");
    }

    @Test
    void financeSourceValuesForAdministrativeUnit() {
        assertThat(OrganizationFieldCatalog.financeSourceValuesForUnitCategory("行政"))
                .containsExactly("全额拨款");
    }

    @Test
    void financeSourceValuesForInstitutionUnit() {
        assertThat(OrganizationFieldCatalog.financeSourceValuesForUnitCategory("事业"))
                .containsExactly("全额拨款", "差额拨款", "自收自支");
    }
}

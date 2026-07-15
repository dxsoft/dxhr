package com.dxsoft.rsgzgl.dictionary;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DictionaryQueryFilterTest {

    @Test
    void administrativeXrzwExcludesInstitutionPrefixes() {
        DictionaryFilterSpec spec = DictionaryQueryFilter.forField("xrzw", "行政", "");
        assertThat(spec.treePrefix()).isEqualTo("051");
        assertThat(spec.whereClause())
                .contains("bm LIKE '051%'")
                .contains("LEFT(bm, 5) <> '05107'");
    }

    @Test
    void institutionXrzwUsesTechnicalPostSeries() {
        DictionaryFilterSpec spec = DictionaryQueryFilter.forField("XRZW", "事业", "");
        assertThat(spec.treePrefix()).isEqualTo("001");
        assertThat(spec.whereClause()).contains("bm LIKE '001%'").contains("bm >= '00107'");
    }

    @Test
    void administrativeZwjbUsesTwentySixSeries() {
        DictionaryFilterSpec spec = DictionaryQueryFilter.forField("zwjb", "行政", "");
        assertThat(spec.treePrefix()).isEqualTo("026");
        assertThat(spec.whereClause()).contains("LEFT(bm, 5) <> '02604'");
    }

    @Test
    void institutionXzzwUsesInstitutionSalaryPostRange() {
        DictionaryFilterSpec spec = DictionaryQueryFilter.forField("xzzw", "机关", "");
        assertThat(spec.treePrefix()).isEqualTo("051");
        assertThat(spec.whereClause())
                .contains("bm >= '05107'")
                .contains("bm < '05121'");
    }

    @Test
    void zjdjUsesOrganizationPropertyWhenPresent() {
        DictionaryFilterSpec spec = DictionaryQueryFilter.forField("ZJDJ", "行政", "01");
        assertThat(spec.treePrefix()).isEqualTo("05801");
        assertThat(spec.whereClause()).contains("bm LIKE '05801%'");
    }

    @Test
    void supportsKnownPositionFields() {
        assertThat(DictionaryQueryFilter.supports("xrzw")).isTrue();
        assertThat(DictionaryQueryFilter.supports("unknown")).isFalse();
    }
}

package com.dxsoft.rsgzgl.dictionary;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RankCategorySupportTest {

    @Test
    void mapsUnifiedRankCodesToLbValues() {
        assertThat(RankCategorySupport.categoryFromCode("0230107")).isEqualTo("jx");
        assertThat(RankCategorySupport.categoryFromCode("0230208")).isEqualTo("jc");
        assertThat(RankCategorySupport.categoryFromCode("0230308")).isEqualTo("sp");
        assertThat(RankCategorySupport.categoryFromCode("0230408")).isEqualTo("mt");
    }

    @Test
    void recognizesSelectableLeafCodes() {
        assertThat(RankCategorySupport.isSelectableLeaf("0230107")).isTrue();
        assertThat(RankCategorySupport.isSelectableLeaf("02301")).isFalse();
        assertThat(RankCategorySupport.isSelectableLeaf("0260325")).isFalse();
    }
}

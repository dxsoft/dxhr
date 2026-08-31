package com.dxsoft.rsgzgl.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SensitiveDataTest {

    @Test
    void masksChineseIdCard() {
        assertThat(SensitiveData.maskIdCard("413001197107110048")).isEqualTo("413001********0048");
    }

    @Test
    void keepsShortValuesReadable() {
        assertThat(SensitiveData.maskIdCard("12345678")).isEqualTo("12345678");
    }
}

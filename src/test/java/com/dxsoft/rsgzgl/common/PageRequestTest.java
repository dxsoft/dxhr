package com.dxsoft.rsgzgl.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PageRequestTest {

    @Test
    void normalizesMissingAndInvalidValues() {
        PageRequest request = PageRequest.of(-1, 0);

        assertThat(request.page()).isZero();
        assertThat(request.size()).isEqualTo(20);
        assertThat(request.offset()).isZero();
    }

    @Test
    void capsLargePageSize() {
        PageRequest request = PageRequest.of(2, 500);

        assertThat(request.page()).isEqualTo(2);
        assertThat(request.size()).isEqualTo(200);
        assertThat(request.offset()).isEqualTo(400);
    }
}

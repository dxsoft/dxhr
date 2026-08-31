package com.dxsoft.rsgzgl.security.ukey.sm2;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SM2SM3Test {

    @Test
    void sm2UserIdEncodingChangesZValueForChineseIdentity() {
        SM2SM3 digest = new SM2SM3();
        String id = "浉河区人民检察院";
        byte[] utf8 = digest.Sm2GetZ(
                new java.math.BigInteger("B1E33CFA4693D7803318E2DB897347C65733A1FD3E2469D7D55DA0C1858EA863", 16),
                new java.math.BigInteger("FC98EBC4CEC22ED137880F373F748691237BBDC91C01E526262D4F2AF648294D", 16),
                id.getBytes(StandardCharsets.UTF_8));
        byte[] gbk = digest.Sm2GetZ(
                new java.math.BigInteger("B1E33CFA4693D7803318E2DB897347C65733A1FD3E2469D7D55DA0C1858EA863", 16),
                new java.math.BigInteger("FC98EBC4CEC22ED137880F373F748691237BBDC91C01E526262D4F2AF648294D", 16),
                id.getBytes(Charset.forName("GBK")));
        assertThat(utf8).isNotEqualTo(gbk);
    }
}

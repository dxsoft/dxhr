package com.dxsoft.rsgzgl.printauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class PrintAuthorizationServiceTest {

    @Test
    void parsesFixedOffsetYearLikeVfp() {
        OptionalInt year = PrintAuthorizationService.parseAuthorizedYear("xxxxxxxx2026yyyy");
        assertTrue(year.isPresent());
        assertEquals(2026, year.getAsInt());
    }

    @Test
    void parsesYearFromLooseResponse() {
        OptionalInt year = PrintAuthorizationService.parseAuthorizedYear("status=200;authYear=2025");
        assertTrue(year.isPresent());
        assertEquals(2025, year.getAsInt());
    }

    @Test
    void base64EncodesMembershipAndNameAsUtf8() {
        assertEquals("5L+h6Ziz5biC", PrintAuthorizationService.base64Utf8("信阳市"));
        assertEquals("5biC5bqc5Yqe", PrintAuthorizationService.base64Utf8("市府办"));
        assertEquals("", PrintAuthorizationService.base64Utf8(""));
        assertEquals("", PrintAuthorizationService.base64Utf8(null));
    }
}

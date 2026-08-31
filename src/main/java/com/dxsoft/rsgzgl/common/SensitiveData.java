package com.dxsoft.rsgzgl.common;

public final class SensitiveData {

    private SensitiveData() {
    }

    public static String maskIdCard(String idCard) {
        String value = SqlText.trim(idCard);
        if (value == null || value.length() <= 8) {
            return value;
        }
        return value.substring(0, 6) + "*".repeat(value.length() - 10) + value.substring(value.length() - 4);
    }
}

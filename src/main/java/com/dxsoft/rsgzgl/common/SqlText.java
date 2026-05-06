package com.dxsoft.rsgzgl.common;

public final class SqlText {

    private SqlText() {
    }

    public static String trim(String value) {
        return value == null ? null : value.trim();
    }
}

package com.dxsoft.rsgzgl.common;

public final class SqlText {

    public static final String MYSQL_COLLATION = "utf8mb4_0900_ai_ci";

    private SqlText() {
    }

    public static String trim(String value) {
        return value == null ? null : value.trim();
    }

    public static String collate(String expression) {
        return expression + " COLLATE " + MYSQL_COLLATION;
    }
}

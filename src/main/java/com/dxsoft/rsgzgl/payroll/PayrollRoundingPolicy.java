package com.dxsoft.rsgzgl.payroll;

public record PayrollRoundingPolicy(int decimalPlaces, String roundingMethod) {
    public static PayrollRoundingPolicy defaults() {
        return new PayrollRoundingPolicy(0, "0");
    }

    public static PayrollRoundingPolicy from(String roundingMode, String roundToInteger) {
        int scale = 0;
        if (roundingMode != null && !roundingMode.isBlank()) {
            String normalized = roundingMode.trim();
            try {
                scale = Integer.parseInt(normalized);
            } catch (NumberFormatException ignored) {
                scale = "0".equals(normalized) ? 0 : 1;
            }
        }

        String method = roundToInteger == null || roundToInteger.isBlank() ? "0" : roundToInteger.trim();
        if (!"1".equals(method) && !"2".equals(method)) {
            method = "0";
        }
        return new PayrollRoundingPolicy(Math.max(0, Math.min(4, scale)), method);
    }
}

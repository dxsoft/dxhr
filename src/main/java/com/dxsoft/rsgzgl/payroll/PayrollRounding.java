package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PayrollRounding {
    private PayrollRounding() {
    }

    public static BigDecimal zround(BigDecimal value, PayrollRoundingPolicy policy) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        PayrollRoundingPolicy effective = policy == null ? PayrollRoundingPolicy.defaults() : policy;
        int scale = effective.decimalPlaces();
        BigDecimal rounded = switch (effective.roundingMethod()) {
            case "1" -> value.add(upwardOffset(scale)).setScale(scale, RoundingMode.HALF_UP);
            case "2" -> value.setScale(scale, RoundingMode.FLOOR);
            default -> value.setScale(scale, RoundingMode.HALF_UP);
        };
        if (rounded.stripTrailingZeros().scale() <= 0) {
            return rounded.setScale(0, RoundingMode.UNNECESSARY);
        }
        return rounded;
    }

    public static int zroundToInt(BigDecimal value, PayrollRoundingPolicy policy) {
        return zround(value, policy).intValue();
    }

    public static int zroundToInt(int value, PayrollRoundingPolicy policy) {
        return zround(BigDecimal.valueOf(value), policy).intValue();
    }

    public static int zroundPercent(int base, int percent, PayrollRoundingPolicy policy) {
        if (base <= 0 || percent <= 0) {
            return 0;
        }
        return zroundToInt(
                BigDecimal.valueOf(base)
                        .multiply(BigDecimal.valueOf(percent))
                        .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP),
                policy);
    }

    private static BigDecimal upwardOffset(int scale) {
        return BigDecimal.valueOf(4L, scale + 1);
    }
}

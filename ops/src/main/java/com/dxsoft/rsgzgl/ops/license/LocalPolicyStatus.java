package com.dxsoft.rsgzgl.ops.license;

import java.math.BigDecimal;

public record LocalPolicyStatus(
        boolean synced,
        BigDecimal bonusBalanceMode,
        String roundingMode,
        String roundToInteger,
        BigDecimal policeRankStartLevel
) {
    static LocalPolicyStatus from(LicenseLocalPolicy policy) {
        if (policy == null) {
            return new LocalPolicyStatus(false, null, null, null, null);
        }
        return new LocalPolicyStatus(
                true,
                policy.bonusBalanceMode(),
                empty(policy.roundingMode()),
                empty(policy.roundToInteger()),
                policy.policeRankStartLevel());
    }

    private static String empty(String value) {
        return value == null ? "" : value.trim();
    }
}

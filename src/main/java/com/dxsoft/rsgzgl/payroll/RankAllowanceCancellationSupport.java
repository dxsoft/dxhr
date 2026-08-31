package com.dxsoft.rsgzgl.payroll;

import java.util.Set;

/**
 * 警衔/等级取消约定：jx 子表保留历史记录，新增一条取消行（lb 与类别一致、sysj 为执行年月），
 * jx 字段留空或使用 {@link #CANCELLATION_SENTINELS} 中的标记值。
 */
public final class RankAllowanceCancellationSupport {

    public static final Set<String> CANCELLATION_SENTINELS = Set.of("无", "无警衔", "无等级");

    private RankAllowanceCancellationSupport() {
    }

    public static boolean isCancellation(String category, String rankName, String recordCategory) {
        if (!categoryMatches(category, recordCategory)) {
            return false;
        }
        String normalized = rankName == null ? "" : rankName.trim();
        return normalized.isEmpty() || CANCELLATION_SENTINELS.contains(normalized);
    }

    private static boolean categoryMatches(String category, String recordCategory) {
        if (category == null || category.isBlank()) {
            return false;
        }
        String lb = recordCategory == null ? "" : recordCategory.trim();
        return category.equals(lb);
    }
}

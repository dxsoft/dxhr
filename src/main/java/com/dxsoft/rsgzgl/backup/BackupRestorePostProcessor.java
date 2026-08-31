package com.dxsoft.rsgzgl.backup;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Post-restore maintenance: inspect and rebuild {@code hisbase}/{@code hisbaseb} {@code sid} chains.
 */
@Service
class BackupRestorePostProcessor {

    private final BackupRestoreProperties restoreProperties;
    private final HisbaseSidChainService sidChainService;

    BackupRestorePostProcessor(BackupRestoreProperties restoreProperties, HisbaseSidChainService sidChainService) {
        this.restoreProperties = restoreProperties;
        this.sidChainService = sidChainService;
    }

    BackupRestoreResult applyChainRepair(BackupRestoreResult result) {
        if (!restoreProperties.repairHisbaseChain()) {
            return result;
        }
        List<String> chainTables = result.restoredTables().stream()
                .filter(HisbaseSidChainSupport::isChainTable)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        if (chainTables.isEmpty()) {
            return result;
        }

        List<HisbaseSidChainRepairSummary> repairs = sidChainService.repairTables(chainTables);
        StringBuilder messageSuffix = new StringBuilder();
        for (HisbaseSidChainRepairSummary summary : repairs) {
            messageSuffix.append(formatSummaryLine(summary));
        }

        if (messageSuffix.isEmpty()) {
            return withChainRepairs(result, repairs);
        }
        return withChainRepairs(result, repairs, result.message() + messageSuffix);
    }

    private static String formatSummaryLine(HisbaseSidChainRepairSummary summary) {
        if (summary.isHealthyAfter() && summary.nullSidTipsBefore() == 0 && !summary.hadChainIssuesBefore()) {
            return " " + summary.tableName() + " 链表已校验（"
                    + summary.rowsRebuilt() + " 行 sid 已对齐，链头均为空字符串）。";
        }
        if (summary.isHealthyAfter()) {
            return " " + summary.tableName() + " 链表已修复：修复前 "
                    + summary.multiTipPersonsBefore() + " 人多链头、"
                    + summary.brokenSidRefsBefore() + " 条悬空 sid、"
                    + summary.nullSidTipsBefore() + " 条 NULL 链头；"
                    + "已重建 " + summary.rowsRebuilt() + " 行、规范 "
                    + summary.nullTipsNormalized() + " 条 NULL→''，现均正常。";
        }
        return " " + summary.tableName() + " 链表修复后仍有问题："
                + summary.multiTipPersonsAfter() + " 人多链头、"
                + summary.brokenSidRefsAfter() + " 条悬空 sid、"
                + summary.nullSidTipsAfter() + " 条 NULL 链头。";
    }

    private static BackupRestoreResult withChainRepairs(
            BackupRestoreResult result, List<HisbaseSidChainRepairSummary> repairs) {
        return withChainRepairs(result, repairs, result.message());
    }

    private static BackupRestoreResult withChainRepairs(
            BackupRestoreResult result,
            List<HisbaseSidChainRepairSummary> repairs,
            String message) {
        return new BackupRestoreResult(
                result.format(),
                result.formatLabel(),
                result.tablesRestored(),
                result.rowsRestored(),
                result.restoredTables(),
                result.skippedTables(),
                result.rowCounts(),
                result.durationMs(),
                message,
                repairs);
    }
}

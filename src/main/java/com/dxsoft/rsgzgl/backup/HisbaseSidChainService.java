package com.dxsoft.rsgzgl.backup;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;

@Service
class HisbaseSidChainService {

    private final DataSource dataSource;

    HisbaseSidChainService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    List<HisbaseSidChainRepairSummary> repairTables(Collection<String> tableNames) {
        List<String> ordered = tableNames.stream()
                .filter(HisbaseSidChainSupport::isChainTable)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        if (ordered.isEmpty()) {
            return List.of();
        }

        List<HisbaseSidChainRepairSummary> summaries = new ArrayList<>();
        Connection connection = null;
        try {
            connection = BackupJdbcSupport.openRestoreConnection(dataSource);
            for (String tableName : ordered) {
                summaries.add(repairTable(connection, tableName));
            }
            connection.commit();
            return summaries;
        } catch (SQLException ex) {
            BackupJdbcSupport.safeRollback(connection);
            throw new IllegalStateException("工资历史链表修复失败: " + BackupJdbcSupport.rootMessage(ex), ex);
        } finally {
            BackupJdbcSupport.safeClose(connection);
        }
    }

    private static HisbaseSidChainRepairSummary repairTable(Connection connection, String tableName)
            throws SQLException {
        String resolvedTable = BackupTableNameSupport.resolveTableName(connection, tableName);
        if (resolvedTable == null) {
            throw new SQLException("目标库无表 " + tableName);
        }
        HisbaseSidChainSupport.ChainIssueStats before = HisbaseSidChainSupport.inspect(connection, resolvedTable);
        int rowsRebuilt = HisbaseSidChainSupport.rebuild(connection, resolvedTable);
        int nullTipsNormalized = HisbaseSidChainSupport.normalizeNullSidTips(connection, resolvedTable);
        HisbaseSidChainSupport.ChainIssueStats after = HisbaseSidChainSupport.inspect(connection, resolvedTable);
        return new HisbaseSidChainRepairSummary(
                resolvedTable,
                before.multiTipPersons(),
                before.brokenSidRefs(),
                before.nullSidTips(),
                rowsRebuilt,
                nullTipsNormalized,
                after.multiTipPersons(),
                after.brokenSidRefs(),
                after.nullSidTips());
    }
}

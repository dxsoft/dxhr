package com.dxsoft.rsgzgl.backup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Shared JDBC batch writer for backup restore. Executes {@code executeBatch} when
 * {@code batchSize} rows accumulate; commits according to {@code commitEveryBatches}.
 */
final class BackupRestoreBatchWriter {

    private final Connection connection;
    private final PreparedStatement ps;
    private final int batchSize;
    private final int commitEveryBatches;
    private int rowsInCurrentBatch;
    private int batchesSinceCommit;
    private int totalRows;

    BackupRestoreBatchWriter(
            Connection connection, PreparedStatement ps, BackupRestoreProperties properties) {
        this(connection, ps, properties.batchSize(), properties.commitEveryBatches());
    }

    BackupRestoreBatchWriter(Connection connection, PreparedStatement ps, int batchSize, int commitEveryBatches) {
        this.connection = connection;
        this.ps = ps;
        this.batchSize = Math.max(1, batchSize);
        this.commitEveryBatches = commitEveryBatches;
    }

    void addBatch() throws SQLException {
        ps.addBatch();
        rowsInCurrentBatch++;
        totalRows++;
        if (rowsInCurrentBatch >= batchSize) {
            flushBatch();
        }
    }

    int finish() throws SQLException {
        if (rowsInCurrentBatch > 0) {
            ps.executeBatch();
            ps.clearBatch();
            rowsInCurrentBatch = 0;
            batchesSinceCommit++;
        }
        connection.commit();
        return totalRows;
    }

    int totalRows() {
        return totalRows;
    }

    private void flushBatch() throws SQLException {
        ps.executeBatch();
        ps.clearBatch();
        rowsInCurrentBatch = 0;
        batchesSinceCommit++;
        if (commitEveryBatches > 0 && batchesSinceCommit >= commitEveryBatches) {
            connection.commit();
            batchesSinceCommit = 0;
        }
    }
}

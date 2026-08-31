package com.dxsoft.rsgzgl.backup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class BackupRestoreBatchWriterTest {

    @Test
    void commitsAfterEveryBatchWhenConfigured() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        BackupRestoreBatchWriter writer = new BackupRestoreBatchWriter(connection, ps, 2, 1);
        writer.addBatch();
        writer.addBatch();
        writer.finish();

        verify(ps, times(1)).executeBatch();
        verify(connection, times(2)).commit();
    }

    @Test
    void commitsOncePerTableWhenCommitEveryBatchesIsZero() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        BackupRestoreBatchWriter writer = new BackupRestoreBatchWriter(connection, ps, 2, 0);
        writer.addBatch();
        writer.addBatch();
        writer.addBatch();
        int rows = writer.finish();

        assertEquals(3, rows);
        InOrder order = Mockito.inOrder(ps, connection);
        order.verify(ps).executeBatch();
        order.verify(ps).executeBatch();
        order.verify(connection).commit();
        verify(connection, times(1)).commit();
    }

    @Test
    void flushesPartialBatchAtFinish() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        BackupRestoreBatchWriter writer = new BackupRestoreBatchWriter(connection, ps, 5, 0);
        writer.addBatch();
        writer.addBatch();
        writer.finish();

        verify(ps, times(1)).executeBatch();
        verify(connection, times(1)).commit();
    }
}

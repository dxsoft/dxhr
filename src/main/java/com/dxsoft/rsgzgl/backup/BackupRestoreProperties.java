package com.dxsoft.rsgzgl.backup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Tunable restore throughput settings. Defaults preserve the conservative
 * batch-size / per-batch commit behavior for small-memory hosts.
 */
@Component
class BackupRestoreProperties {

    private final int batchSize;
    private final int commitEveryBatches;
    private final boolean truncateBeforeInsert;
    private final boolean repairHisbaseChain;

    BackupRestoreProperties(
            @Value("${rsgzgl.backup.restore.batch-size:200}") int batchSize,
            @Value("${rsgzgl.backup.restore.commit-every-batches:1}") int commitEveryBatches,
            @Value("${rsgzgl.backup.restore.truncate-before-insert:false}") boolean truncateBeforeInsert,
            @Value("${rsgzgl.backup.restore.repair-hisbase-chain:true}") boolean repairHisbaseChain) {
        this.batchSize = Math.max(1, batchSize);
        this.commitEveryBatches = commitEveryBatches;
        this.truncateBeforeInsert = truncateBeforeInsert;
        this.repairHisbaseChain = repairHisbaseChain;
    }

    int batchSize() {
        return batchSize;
    }

    /**
     * @return batches between commits; {@code 0} means commit once per table at finish
     */
    int commitEveryBatches() {
        return commitEveryBatches;
    }

    boolean truncateBeforeInsert() {
        return truncateBeforeInsert;
    }

    boolean repairHisbaseChain() {
        return repairHisbaseChain;
    }
}

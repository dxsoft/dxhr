package com.dxsoft.rsgzgl.backup;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Optional periodic repair: rebuild sid chains and normalize NULL tips to {@code ''}.
 */
@Component
@ConditionalOnProperty(name = "rsgzgl.maintenance.hisbase-sid-chain.enabled", havingValue = "true")
class HisbaseSidChainMaintenanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(HisbaseSidChainMaintenanceScheduler.class);

    private final HisbaseSidChainService sidChainService;

    HisbaseSidChainMaintenanceScheduler(HisbaseSidChainService sidChainService) {
        this.sidChainService = sidChainService;
    }

    @Scheduled(cron = "${rsgzgl.maintenance.hisbase-sid-chain.cron:0 0 3 * * 0}")
    void repairOnSchedule() {
        List<HisbaseSidChainRepairSummary> summaries =
                sidChainService.repairTables(HisbaseSidChainSupport.CHAIN_TABLES);
        for (HisbaseSidChainRepairSummary summary : summaries) {
            log.info(
                    "hisbase sid chain maintenance {}: rebuilt {} rows, normalized {} null tips, "
                            + "multi-tip {} -> {}, broken sid {} -> {}, null tips {} -> {}",
                    summary.tableName(),
                    summary.rowsRebuilt(),
                    summary.nullTipsNormalized(),
                    summary.multiTipPersonsBefore(),
                    summary.multiTipPersonsAfter(),
                    summary.brokenSidRefsBefore(),
                    summary.brokenSidRefsAfter(),
                    summary.nullSidTipsBefore(),
                    summary.nullSidTipsAfter());
        }
    }
}

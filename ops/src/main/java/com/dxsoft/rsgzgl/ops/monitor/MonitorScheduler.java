package com.dxsoft.rsgzgl.ops.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class MonitorScheduler {

    private static final Logger log = LoggerFactory.getLogger(MonitorScheduler.class);
    private final MonitorService monitorService;

    MonitorScheduler(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @Scheduled(fixedDelayString = "${rsgzgl.ops.monitor.interval-ms:30000}", initialDelay = 3000)
    void tick() {
        try {
            monitorService.collectAndStore();
        } catch (Exception ex) {
            log.warn("定时采集监控失败：{}", ex.getMessage());
        }
    }
}

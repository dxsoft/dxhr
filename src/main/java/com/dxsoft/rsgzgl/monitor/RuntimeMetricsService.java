package com.dxsoft.rsgzgl.monitor;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
class RuntimeMetricsService {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final TomcatThreadSampler tomcatThreads;

    RuntimeMetricsService(DataSource dataSource, JdbcTemplate jdbcTemplate, TomcatThreadSampler tomcatThreads) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.tomcatThreads = tomcatThreads;
    }

    RuntimeMetrics collect() {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long gcCount = 0;
        long gcTime = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            gcCount += Math.max(0, gc.getCollectionCount());
            gcTime += Math.max(0, gc.getCollectionTime());
        }
        int[] hikari = hikari();
        int[] tomcat = tomcatThreads.snapshot();
        String db = dbStatus();
        return new RuntimeMetrics(
                "UP".equals(db) ? "OK" : "CRIT",
                db,
                heap.getUsed(),
                heap.getMax() > 0 ? heap.getMax() : heap.getCommitted(),
                gcCount,
                gcTime,
                ManagementFactory.getThreadMXBean().getThreadCount(),
                hikari[0],
                hikari[1],
                hikari[2],
                hikari[3],
                tomcat[0],
                tomcat[1],
                tomcat[2],
                ManagementFactory.getRuntimeMXBean().getUptime());
    }

    private String dbStatus() {
        try {
            Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return one != null && one == 1 ? "UP" : "DOWN";
        } catch (Exception ex) {
            return "DOWN";
        }
    }

    private int[] hikari() {
        if (!(dataSource instanceof HikariDataSource hikari)) {
            return new int[] {0, 0, 0, 0};
        }
        HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
        if (pool == null) {
            return new int[] {0, 0, 0, hikari.getMaximumPoolSize()};
        }
        return new int[] {
                pool.getActiveConnections(),
                pool.getIdleConnections(),
                pool.getThreadsAwaitingConnection(),
                hikari.getMaximumPoolSize()
        };
    }

}

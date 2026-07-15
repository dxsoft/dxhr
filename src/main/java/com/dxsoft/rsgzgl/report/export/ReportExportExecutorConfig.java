package com.dxsoft.rsgzgl.report.export;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ReportExportExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    ExecutorService reportExportExecutor() {
        return Executors.newFixedThreadPool(2);
    }
}

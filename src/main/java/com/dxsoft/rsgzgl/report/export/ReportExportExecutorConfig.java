package com.dxsoft.rsgzgl.report.export;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

@Configuration
class ReportExportExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    ExecutorService reportExportExecutor() {
        AtomicInteger sequence = new AtomicInteger();
        ExecutorService delegate = Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("report-export-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        // Preserve the submitting user's SecurityContext on worker threads
        // (payrollChangeComparisons checks organization access via AccessControlService).
        return new DelegatingSecurityContextExecutorService(delegate);
    }

    /**
     * Dedicated pool for OpenHTMLToPDF chunk rendering. Kept separate from {@link #reportExportExecutor()}
     * so async export jobs do not deadlock waiting for PDF worker threads.
     */
    @Bean(destroyMethod = "shutdown")
    ExecutorService reportPdfRenderExecutor() {
        int threads = Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors()));
        AtomicInteger sequence = new AtomicInteger();
        return Executors.newFixedThreadPool(threads, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("report-pdf-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }
}

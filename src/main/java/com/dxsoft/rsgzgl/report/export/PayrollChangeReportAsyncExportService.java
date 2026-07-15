package com.dxsoft.rsgzgl.report.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PayrollChangeReportAsyncExportService {

    private static final Duration JOB_TTL = Duration.ofHours(2);

    private final PayrollChangeReportExportService exportService;
    private final ExecutorService reportExportExecutor;
    private final Map<String, ExportJob> jobs = new ConcurrentHashMap<>();

    PayrollChangeReportAsyncExportService(
            PayrollChangeReportExportService exportService,
            ExecutorService reportExportExecutor) {
        this.exportService = exportService;
        this.reportExportExecutor = reportExportExecutor;
    }

    public PayrollChangeReportExportJobView submit(PayrollChangeReportExportJobRequest request) {
        if (request == null || request.target() == null || request.exportRequest() == null) {
            throw new IllegalArgumentException("导出任务参数不完整");
        }
        int recordCount = request.exportRequest().payrollHistoryIds() == null
                ? 0
                : (int) request.exportRequest().payrollHistoryIds().stream()
                        .map(String::trim)
                        .filter(id -> !id.isEmpty())
                        .count();
        if (recordCount == 0) {
            throw new IllegalArgumentException("请至少选择一条工资变动记录");
        }

        String jobId = UUID.randomUUID().toString();
        ExportJob job = new ExportJob(
                jobId,
                request.target(),
                request.exportRequest(),
                recordCount,
                PayrollChangeReportExportJobStatus.PENDING,
                null,
                null,
                null,
                null,
                Instant.now(),
                null);
        jobs.put(jobId, job);
        reportExportExecutor.submit(() -> runJob(jobId));
        return toView(job);
    }

    public PayrollChangeReportExportJobView getJob(String jobId) {
        ExportJob job = requireJob(jobId);
        return toView(job);
    }

    public ResponseEntity<byte[]> downloadJob(String jobId) {
        ExportJob job = requireJob(jobId);
        if (job.status() != PayrollChangeReportExportJobStatus.SUCCEEDED || job.resultPath() == null) {
            throw new IllegalStateException("导出任务尚未完成");
        }
        try {
            byte[] bytes = Files.readAllBytes(job.resultPath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(job.contentType()));
            headers.setContentDispositionFormData("attachment", job.fileName());
            headers.setContentLength(bytes.length);
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (IOException exception) {
            throw new IllegalStateException("读取导出结果失败", exception);
        }
    }

    @Scheduled(fixedDelay = 1_800_000)
    void cleanupExpiredJobs() {
        Instant cutoff = Instant.now().minus(JOB_TTL);
        jobs.entrySet().removeIf(entry -> {
            ExportJob job = entry.getValue();
            if (job.createdAt().isAfter(cutoff)) {
                return false;
            }
            deleteResultFile(job.resultPath());
            return true;
        });
    }

    private void runJob(String jobId) {
        ExportJob current = jobs.get(jobId);
        if (current == null) {
            return;
        }
        jobs.put(jobId, current.withStatus(PayrollChangeReportExportJobStatus.RUNNING, null, null, null));
        try {
            PayrollChangeReportArtifact artifact = exportService.exportArtifact(current.target(), current.exportRequest());
            Path resultPath = Files.createTempFile("payroll-change-export-" + jobId + "-", "." + current.target().extension());
            Files.write(resultPath, artifact.bytes());
            jobs.put(jobId, jobs.get(jobId).withStatus(
                    PayrollChangeReportExportJobStatus.SUCCEEDED,
                    artifact.fileName(),
                    artifact.contentType(),
                    resultPath));
        } catch (RuntimeException | IOException exception) {
            String message = exception.getMessage() == null ? "导出失败" : exception.getMessage();
            jobs.put(jobId, jobs.get(jobId).withStatus(
                    PayrollChangeReportExportJobStatus.FAILED,
                    null,
                    null,
                    null,
                    message));
        }
    }

    private ExportJob requireJob(String jobId) {
        ExportJob job = jobs.get(jobId);
        if (job == null) {
            throw new IllegalArgumentException("导出任务不存在或已过期");
        }
        return job;
    }

    private PayrollChangeReportExportJobView toView(ExportJob job) {
        return new PayrollChangeReportExportJobView(
                job.jobId(),
                job.target(),
                job.status(),
                job.fileName(),
                job.contentType(),
                job.recordCount(),
                job.errorMessage(),
                job.createdAt(),
                job.completedAt());
    }

    private void deleteResultFile(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best effort cleanup for temporary export files.
        }
    }

    private record ExportJob(
            String jobId,
            PayrollChangeReportExportTarget target,
            PayrollChangeReportExportRequest exportRequest,
            int recordCount,
            PayrollChangeReportExportJobStatus status,
            String fileName,
            String contentType,
            Path resultPath,
            String errorMessage,
            Instant createdAt,
            Instant completedAt) {

        ExportJob withStatus(
                PayrollChangeReportExportJobStatus nextStatus,
                String nextFileName,
                String nextContentType,
                Path nextResultPath) {
            return withStatus(nextStatus, nextFileName, nextContentType, nextResultPath, errorMessage);
        }

        ExportJob withStatus(
                PayrollChangeReportExportJobStatus nextStatus,
                String nextFileName,
                String nextContentType,
                Path nextResultPath,
                String nextErrorMessage) {
            Instant doneAt = nextStatus == PayrollChangeReportExportJobStatus.SUCCEEDED
                    || nextStatus == PayrollChangeReportExportJobStatus.FAILED
                    ? Instant.now()
                    : completedAt;
            return new ExportJob(
                    jobId,
                    target,
                    exportRequest,
                    recordCount,
                    nextStatus,
                    nextFileName,
                    nextContentType,
                    nextResultPath,
                    nextErrorMessage,
                    createdAt,
                    doneAt);
        }
    }
}

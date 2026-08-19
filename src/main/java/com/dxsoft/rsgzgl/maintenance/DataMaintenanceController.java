package com.dxsoft.rsgzgl.maintenance;

import com.dxsoft.rsgzgl.backup.BackupInspectResult;
import com.dxsoft.rsgzgl.backup.BackupRestoreResult;
import com.dxsoft.rsgzgl.backup.BackupTableScopes;
import com.dxsoft.rsgzgl.backup.DataBackupService;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/data-maintenance")
public class DataMaintenanceController {

    private final DataMaintenanceService dataMaintenanceService;
    private final DataBackupService dataBackupService;

    DataMaintenanceController(
            DataMaintenanceService dataMaintenanceService,
            DataBackupService dataBackupService) {
        this.dataMaintenanceService = dataMaintenanceService;
        this.dataBackupService = dataBackupService;
    }

    @GetMapping("/diagnostics")
    DataMaintenanceDiagnostics diagnostics() {
        return dataMaintenanceService.diagnostics();
    }

    @PostMapping("/purge-audit-logs")
    int purgeAuditLogs(@RequestParam(defaultValue = "90") int keepDays) {
        return dataMaintenanceService.purgeAuditLogs(keepDays);
    }

    @PostMapping("/purge-orphan-markers")
    int purgeOrphanAppRecordMarkers() {
        return dataMaintenanceService.purgeOrphanAppRecordMarkers();
    }

    @GetMapping("/backup/scopes")
    List<BackupTableScopes.ScopeDescriptor> backupScopes() {
        return dataBackupService.listScopes();
    }

    @PostMapping("/backup/export")
    ResponseEntity<StreamingResponseBody> exportBackup(
            @RequestParam(required = false) List<String> scopes) {
        DataBackupService.BackupDownload download = dataBackupService.createBackup(scopes);
        long size;
        try {
            size = Files.size(download.path());
        } catch (Exception ex) {
            try {
                Files.deleteIfExists(download.path());
            } catch (Exception ignored) {
                // ignore
            }
            throw new IllegalStateException("无法读取备份文件: " + ex.getMessage(), ex);
        }
        StreamingResponseBody body = outputStream -> {
            try (InputStream in = Files.newInputStream(download.path())) {
                in.transferTo(outputStream);
            } finally {
                Files.deleteIfExists(download.path());
            }
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.filename(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentLength(size)
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(body);
    }

    @PostMapping("/backup/inspect")
    BackupInspectResult inspectBackup(@RequestParam("file") MultipartFile file) {
        return dataBackupService.inspect(file);
    }

    @PostMapping("/backup/restore")
    BackupRestoreResult restoreBackup(
            @RequestParam("file") MultipartFile file,
            @RequestParam String confirmPhrase,
            @RequestParam(required = false) List<String> scopes) {
        return dataBackupService.restore(file, confirmPhrase, scopes);
    }
}

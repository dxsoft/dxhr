package com.dxsoft.rsgzgl.backup;

import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DataBackupService {

    private static final String CONFIRM_PHRASE = "数据恢复";
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final AccessControlService accessControlService;
    private final OperationLogService operationLogService;
    private final BackupPackageInspector inspector;
    private final NewFormatBackupService newFormatBackupService;
    private final LegacyDbfRestoreService legacyDbfRestoreService;

    DataBackupService(
            AccessControlService accessControlService,
            OperationLogService operationLogService,
            BackupPackageInspector inspector,
            NewFormatBackupService newFormatBackupService,
            LegacyDbfRestoreService legacyDbfRestoreService) {
        this.accessControlService = accessControlService;
        this.operationLogService = operationLogService;
        this.inspector = inspector;
        this.newFormatBackupService = newFormatBackupService;
        this.legacyDbfRestoreService = legacyDbfRestoreService;
    }

    public List<BackupTableScopes.ScopeDescriptor> listScopes() {
        requirePermission();
        return BackupTableScopes.descriptors();
    }

    public BackupDownload createBackup(Collection<String> scopeIds) {
        requirePermission();
        try {
            Path path = newFormatBackupService.createBackupArchive(scopeIds);
            long size = Files.size(path);
            String scopePart = scopeSuffix(scopeIds);
            String filename = "人事工资信息备份" + scopePart + "-" + FILE_TS.format(LocalDateTime.now()) + ".rsbak";
            operationLogService.record(
                    "DATA_BACKUP",
                    "database",
                    filename,
                    "导出新系统备份包 " + filename + "，大小 " + size + " 字节，范围="
                            + BackupTableScopes.normalizeScopeIds(scopeIds));
            return new BackupDownload(filename, path);
        } catch (IOException | SQLException ex) {
            throw new IllegalStateException("创建备份失败: " + ex.getMessage(), ex);
        }
    }

    public BackupInspectResult inspect(MultipartFile file) {
        requirePermission();
        Path temp = null;
        try {
            temp = storeUpload(file);
            return inspector.inspect(temp);
        } catch (IOException ex) {
            throw new IllegalStateException("无法读取备份包: " + ex.getMessage(), ex);
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // ignore
                }
            }
        }
    }

    public BackupRestoreResult restore(MultipartFile file, String confirmPhrase, Collection<String> scopeIds) {
        requirePermission();
        if (!CONFIRM_PHRASE.equals(trimToNull(confirmPhrase))) {
            throw new IllegalArgumentException("确认短语不正确，请输入「" + CONFIRM_PHRASE + "」。");
        }
        Path archive = null;
        Path extractDir = null;
        try {
            archive = storeUpload(file);
            extractDir = Files.createTempDirectory("rsgzgl-bak-restore-");
            inspector.unzip(archive, extractDir);
            BackupInspectResult inspect = inspector.inspectExtracted(extractDir);
            BackupRestoreResult result = switch (inspect.format()) {
                case NEW -> newFormatBackupService.restore(extractDir, scopeIds);
                case LEGACY -> legacyDbfRestoreService.restore(extractDir, scopeIds);
                case UNKNOWN -> throw new IllegalArgumentException(inspect.message());
            };
            operationLogService.record(
                    "DATA_RESTORE",
                    "database",
                    inspect.format().name(),
                    result.message() + " 标识=" + inspect.markerFile()
                            + "，范围=" + BackupTableScopes.normalizeScopeIds(scopeIds)
                            + "，跳过=" + result.skippedTables().size());
            return result;
        } catch (IOException | SQLException ex) {
            throw new IllegalStateException("数据恢复失败: " + BackupJdbcSupport.rootMessage(ex), ex);
        } finally {
            if (archive != null) {
                try {
                    Files.deleteIfExists(archive);
                } catch (IOException ignored) {
                    // ignore
                }
            }
            BackupPackageInspector.deleteRecursively(extractDir);
        }
    }

    private Path storeUpload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择备份文件。");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!(original.endsWith(".zl") || original.endsWith(".zip") || original.endsWith(".rsbak"))) {
            throw new IllegalArgumentException("仅支持 .zl / .zip / .rsbak 备份文件。");
        }
        Path temp = Files.createTempFile("rsgzgl-upload-", ".zip");
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return temp;
    }

    private void requirePermission() {
        if (!accessControlService.hasPermission("DATA_MAINTENANCE")) {
            throw new IllegalStateException("当前用户没有数据维护权限。");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String scopeSuffix(Collection<String> scopeIds) {
        var scopes = BackupTableScopes.normalizeScopeIds(scopeIds);
        if (scopes.isEmpty() || scopes.contains(BackupTableScopes.ALL)) {
            return "";
        }
        String joined = scopes.stream()
                .map(BackupTableScopes::labelOf)
                .collect(Collectors.joining("+"));
        return "-" + joined.replaceAll("[\\\\/:*?\"<>|]", "");
    }

    public record BackupDownload(String filename, Path path) {
    }
}

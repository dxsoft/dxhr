package com.dxsoft.rsgzgl.personnel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
class SubrecordAttachmentStorage {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "jpg", "jpeg", "png", "gif", "bmp", "zip", "rar", "7z");

    private final Path rootDirectory;
    private final long maxFileSizeBytes;

    SubrecordAttachmentStorage(
            @Value("${rsgzgl.attachments.storage-dir:./data/attachments}") String storageDir,
            @Value("${rsgzgl.attachments.max-file-size-bytes:20971520}") long maxFileSizeBytes) throws IOException {
        this.rootDirectory = Path.of(storageDir).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeBytes;
        Files.createDirectories(this.rootDirectory);
    }

    StoredFile store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的附件。");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException("附件大小不能超过 " + (maxFileSizeBytes / 1024 / 1024) + " MB。");
        }
        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        String extension = extensionOf(originalName);
        if (extension.isBlank() || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的附件类型，请上传 PDF、Word、Excel、图片或压缩包。");
        }
        String storedName = UUID.randomUUID() + "." + extension;
        Path target = rootDirectory.resolve(storedName);
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target);
        }
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }
        return new StoredFile(storedName, originalName, contentType, file.getSize());
    }

    Resource load(String storedName) {
        Path path = resolveStoredPath(storedName);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("附件文件不存在或已被删除。");
        }
        return new FileSystemResource(path);
    }

    PreviewPayload loadForPreview(String storedName, String originalName, String storedContentType) throws IOException {
        Path path = resolveStoredPath(storedName);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("附件文件不存在或已被删除。");
        }
        if (AttachmentImagePreviewSupport.shouldScale(path, originalName)) {
            Path cachePath = AttachmentImagePreviewSupport.previewCachePath(rootDirectory, storedName);
            Path previewPath = AttachmentImagePreviewSupport.ensurePreview(path, cachePath, originalName);
            return new PreviewPayload(new FileSystemResource(previewPath), "image/jpeg");
        }
        return new PreviewPayload(
                new FileSystemResource(path),
                resolveContentType(originalName, storedContentType));
    }

    void delete(String storedName) throws IOException {
        if (storedName == null || storedName.isBlank()) {
            return;
        }
        Files.deleteIfExists(resolveStoredPath(storedName));
    }

    private Path resolveStoredPath(String storedName) {
        Path resolved = rootDirectory.resolve(storedName).normalize();
        if (!resolved.startsWith(rootDirectory)) {
            throw new IllegalArgumentException("非法附件路径。");
        }
        return resolved;
    }

    private static String sanitizeOriginalName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "attachment";
        }
        String name = originalFilename.replace("\\", "/");
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("[\\x00-\\x1f\"<>|:*?\\\\/]", "_").trim();
        return name.isBlank() ? "attachment" : name;
    }

    static String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase();
    }

    static String resolveContentType(String originalName, String storedContentType) {
        if (storedContentType != null && !storedContentType.isBlank()
                && !"application/octet-stream".equalsIgnoreCase(storedContentType)) {
            return storedContentType;
        }
        return mimeTypeFromExtension(extensionOf(originalName));
    }

    private static String mimeTypeFromExtension(String extension) {
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "zip" -> "application/zip";
            case "rar" -> "application/vnd.rar";
            case "7z" -> "application/x-7z-compressed";
            default -> "application/octet-stream";
        };
    }

    record StoredFile(String storedName, String originalName, String contentType, long fileSize) {
    }

    record PreviewPayload(Resource resource, String contentType) {
    }
}

package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.security.AccessControlService;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MobileAttachmentUploadService {

    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final SubrecordAttachmentStorage attachmentStorage;
    private final AccessControlService accessControlService;
    private final MobileAttachmentUploadSessionRepository sessionRepository;
    private final String configuredPublicBaseUrl;

    MobileAttachmentUploadService(
            SubrecordAttachmentStorage attachmentStorage,
            AccessControlService accessControlService,
            MobileAttachmentUploadSessionRepository sessionRepository,
            @org.springframework.beans.factory.annotation.Value("${rsgzgl.attachments.mobile-upload-public-base-url:}")
            String configuredPublicBaseUrl) {
        this.attachmentStorage = attachmentStorage;
        this.accessControlService = accessControlService;
        this.sessionRepository = sessionRepository;
        this.configuredPublicBaseUrl = configuredPublicBaseUrl;
    }

    MobileAttachmentUploadNetworkHints networkHints(
            String scheme,
            int port,
            String requestHost,
            String browserOrigin) {
        requireWritePermission();
        String suggested = LocalNetworkAddressSupport.resolveSuggestedPublicBaseUrl(
                configuredPublicBaseUrl,
                browserOrigin,
                scheme,
                requestHost,
                port);
        if (suggested == null) {
            return new MobileAttachmentUploadNetworkHints(null, List.of(), false);
        }
        boolean lanFallback = !LocalNetworkAddressSupport.isPublicBaseUrl(suggested);
        List<String> addresses = lanFallback ? LocalNetworkAddressSupport.privateIpv4Addresses() : List.of();
        return new MobileAttachmentUploadNetworkHints(suggested, addresses, true);
    }

    @Transactional
    MobileAttachmentUploadSessionResponse createSession(MobileAttachmentUploadSessionRequest request) {
        requireWritePermission();
        if (request.type() == null || request.type().isBlank()) {
            throw new IllegalArgumentException("附件类型不能为空。");
        }
        if (request.uid() <= 0) {
            throw new IllegalArgumentException("人员信息无效。");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        String baseUrl = normalizeBaseUrl(request.publicBaseUrl());
        Instant expiresAt = Instant.now().plus(SESSION_TTL);
        MobileAttachmentUploadSession session = new MobileAttachmentUploadSession(
                token,
                request.type().trim(),
                request.uid(),
                emptyToNull(request.recordId()),
                baseUrl,
                expiresAt,
                List.of());
        sessionRepository.insertSession(session);
        return new MobileAttachmentUploadSessionResponse(token, buildUploadUrl(baseUrl, token), expiresAt);
    }

    MobileAttachmentUploadSession getSession(String token) {
        return requireValidSession(token);
    }

    List<MobileAttachmentUploadFile> listFiles(String token, boolean unconsumedOnly) {
        MobileAttachmentUploadSession session = requireValidSession(token);
        return session.files().stream()
                .filter(file -> !unconsumedOnly || !file.consumed())
                .toList();
    }

    @Transactional
    MobileAttachmentUploadFile upload(String token, MultipartFile file) throws IOException {
        MobileAttachmentUploadSession session = requireValidSession(token);
        String extension = SubrecordAttachmentStorage.extensionOf(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (!List.of("jpg", "jpeg", "png", "gif", "bmp").contains(extension)) {
            throw new IllegalArgumentException("手机拍照仅支持上传图片。");
        }
        SubrecordAttachmentStorage.StoredFile stored = attachmentStorage.store(file);
        MobileAttachmentUploadFile uploaded = new MobileAttachmentUploadFile(
                UUID.randomUUID().toString().replace("-", ""),
                stored.storedName(),
                stored.originalName(),
                stored.contentType(),
                stored.fileSize(),
                Instant.now(),
                false);
        sessionRepository.insertFile(session.token(), uploaded);
        return uploaded;
    }

    Resource download(String token, String fileId) {
        MobileAttachmentUploadSession session = requireValidSession(token);
        MobileAttachmentUploadFile file = findFile(session, fileId);
        return attachmentStorage.load(file.storedName());
    }

    MobileAttachmentUploadFile findFile(String token, String fileId) {
        return findFile(requireValidSession(token), fileId);
    }

    @Transactional
    void consume(String token, String fileId) {
        requireWritePermission();
        requireValidSession(token);
        sessionRepository.markFileConsumedIfPending(token, fileId);
    }

    @Transactional
    void closeSession(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        List<MobileAttachmentUploadFile> files = sessionRepository.deleteSession(token.trim());
        files.stream()
                .filter(file -> !file.consumed())
                .forEach(file -> {
                    try {
                        attachmentStorage.delete(file.storedName());
                    } catch (IOException ignored) {
                        // Best effort cleanup for expired mobile upload sessions.
                    }
                });
    }

    private MobileAttachmentUploadSession requireValidSession(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("上传会话无效。");
        }
        MobileAttachmentUploadSession session = sessionRepository.findByToken(token.trim())
                .orElseThrow(() -> new IllegalArgumentException("上传会话不存在或已失效，请在电脑端重新生成二维码。"));
        if (Instant.now().isAfter(session.expiresAt())) {
            closeSession(session.token());
            throw new IllegalArgumentException("上传会话已过期，请在电脑端重新生成二维码。");
        }
        return session;
    }

    private static MobileAttachmentUploadFile findFile(MobileAttachmentUploadSession session, String fileId) {
        return session.files().stream()
                .filter(file -> file.id().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("上传文件不存在。"));
    }

    private static String buildUploadUrl(String baseUrl, String token) {
        return baseUrl + "/mobile-upload.html?token=" + token;
    }

    private static String normalizeBaseUrl(String publicBaseUrl) {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            throw new IllegalArgumentException("请提供手机可访问的服务地址。");
        }
        String trimmed = publicBaseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void requireWritePermission() {
        if (!accessControlService.hasPermission("PERSONNEL_WRITE")) {
            throw new AccessDeniedException("当前用户没有附件上传权限。");
        }
    }
}

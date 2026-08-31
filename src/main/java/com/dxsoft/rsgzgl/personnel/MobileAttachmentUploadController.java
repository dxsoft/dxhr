package com.dxsoft.rsgzgl.personnel;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mobile-attachment-sessions")
class MobileAttachmentUploadController {

    private final MobileAttachmentUploadService mobileAttachmentUploadService;

    MobileAttachmentUploadController(MobileAttachmentUploadService mobileAttachmentUploadService) {
        this.mobileAttachmentUploadService = mobileAttachmentUploadService;
    }

    @GetMapping("/network-hints")
    MobileAttachmentUploadNetworkHints networkHints(
            HttpServletRequest request,
            @RequestParam(required = false) Integer port,
            @RequestParam(required = false) String origin) {
        int resolvedPort = port != null && port > 0 ? port : request.getServerPort();
        return mobileAttachmentUploadService.networkHints(
                request.getScheme(),
                resolvedPort,
                request.getServerName(),
                origin);
    }

    @PostMapping
    MobileAttachmentUploadSessionResponse create(@RequestBody MobileAttachmentUploadSessionRequest request) {
        return mobileAttachmentUploadService.createSession(request);
    }

    @GetMapping("/{token}")
    MobileAttachmentUploadSession session(@PathVariable String token) {
        MobileAttachmentUploadSession session = mobileAttachmentUploadService.getSession(token);
        return new MobileAttachmentUploadSession(
                session.token(),
                session.type(),
                session.uid(),
                session.recordId(),
                session.publicBaseUrl(),
                session.expiresAt(),
                List.of());
    }

    @PostMapping("/{token}/files")
    MobileAttachmentUploadFile upload(@PathVariable String token, @RequestParam("file") MultipartFile file)
            throws Exception {
        return mobileAttachmentUploadService.upload(token, file);
    }

    @GetMapping("/{token}/files")
    List<MobileAttachmentUploadFile> files(
            @PathVariable String token,
            @RequestParam(defaultValue = "false") boolean unconsumed) {
        return mobileAttachmentUploadService.listFiles(token, unconsumed);
    }

    @GetMapping("/{token}/files/{fileId}/download")
    ResponseEntity<Resource> download(@PathVariable String token, @PathVariable String fileId) {
        Resource resource = mobileAttachmentUploadService.download(token, fileId);
        MobileAttachmentUploadFile file = mobileAttachmentUploadService.findFile(token, fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.originalName() + "\"")
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(resource);
    }

    @PostMapping("/{token}/files/{fileId}/consume")
    void consume(@PathVariable String token, @PathVariable String fileId) {
        mobileAttachmentUploadService.consume(token, fileId);
    }
}

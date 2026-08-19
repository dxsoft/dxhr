package com.dxsoft.rsgzgl.license;

import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/license")
public class LicenseController {

    private final LicenseService licenseService;

    LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping("/status")
    LicenseStatus status() {
        return licenseService.status();
    }

    @GetMapping("/issue-enabled")
    java.util.Map<String, Boolean> issueEnabled() {
        return java.util.Map.of("enabled", licenseService.isIssueEnabled());
    }

    @GetMapping("/issue-preview")
    LicenseIssuePreview issuePreview(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false, defaultValue = "true") Boolean includeSubordinates,
            @RequestParam(required = false, defaultValue = "false") Boolean includeAllOrganizations) {
        return licenseService.previewIssue(organizationCode, includeSubordinates, includeAllOrganizations);
    }

    @GetMapping("/orgs-export")
    ResponseEntity<byte[]> exportOrganizations() {
        byte[] body = licenseService.exportOrganizationsForOps();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("license-orgs-v1.json", StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @PostMapping("/import")
    LicenseImportResult importLicense(@RequestParam("file") MultipartFile file) {
        return licenseService.importPackage(file);
    }

    @PostMapping("/issue")
    ResponseEntity<byte[]> issue(@RequestBody LicenseIssueRequest request) {
        byte[] body = licenseService.issuePackage(request);
        String code = request.organizationCode() == null ? "license" : request.organizationCode().trim();
        String filename = "单位授权-" + code + ".rsauth.json";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}

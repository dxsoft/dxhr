package com.dxsoft.rsgzgl.ops.license;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/license")
public class OpsLicenseController {

    private final OpsLicenseService service;

    OpsLicenseController(OpsLicenseService service) {
        this.service = service;
    }

    @GetMapping("/orgs")
    List<LicenseOrgRepository.LicenseOrgRow> orgs(@RequestParam(required = false) String keyword) {
        return service.listOrgs(keyword);
    }

    @PutMapping("/orgs")
    LicenseOrgRepository.LicenseOrgRow saveOrg(@RequestBody LicenseOrgRequest request) {
        return service.saveOrg(request);
    }

    @DeleteMapping("/orgs/{code}")
    void deleteOrg(@PathVariable String code) {
        service.deleteOrg(code);
    }

    @PostMapping("/orgs/import-csv")
    Map<String, Object> importCsv(@RequestBody Map<String, String> body) {
        int saved = service.importCsv(body == null ? null : body.get("csv"));
        return Map.of("saved", saved);
    }

    /**
     * 路径刻意不用 /orgs/...，避免被 DELETE /orgs/{code} 抢匹配成 405。
     */
    @PostMapping("/import-orgs-json")
    SeedImportResult importOrgsJson(@RequestBody Map<String, String> body) {
        return service.importOrgsJson(body == null ? null : body.get("json"));
    }

    @PostMapping(value = "/import-seed-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    SeedImportResult importSeedFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择签发种子 JSON 文件。");
        }
        return service.importOrgsJson(new String(file.getBytes(), StandardCharsets.UTF_8));
    }

    @GetMapping("/local-policy")
    LocalPolicyStatus localPolicy() {
        return service.localPolicyStatus();
    }

    @PostMapping("/issue")
    ResponseEntity<byte[]> issue(@RequestBody OpsLicenseIssueRequest request) {
        byte[] body = service.issue(request);
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

    @GetMapping("/issue-logs")
    List<LicenseOrgRepository.IssueLogRow> issueLogs(@RequestParam(required = false) Integer limit) {
        return service.issueLogs(limit == null ? 50 : limit);
    }
}

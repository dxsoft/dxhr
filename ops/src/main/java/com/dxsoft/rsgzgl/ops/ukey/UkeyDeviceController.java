package com.dxsoft.rsgzgl.ops.ukey;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
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

@RestController
@RequestMapping("/api/ukey")
public class UkeyDeviceController {

    private final UkeyDeviceService service;

    UkeyDeviceController(UkeyDeviceService service) {
        this.service = service;
    }

    @GetMapping("/devices")
    List<UkeyDeviceView> list(@RequestParam(required = false) String keyword) {
        return service.list(keyword);
    }

    @PostMapping("/devices")
    UkeyDeviceView register(@RequestBody UkeyDeviceRegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/enc-key")
    Map<String, String> generateEncKey() {
        return service.generateEncKey();
    }

    @GetMapping("/bindings/export")
    ResponseEntity<byte[]> export(@RequestParam(required = false) String keyword) {
        byte[] body = service.exportBindings(keyword);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("ukey-bind-v2.json", StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @GetMapping("/me")
    Map<String, String> me(org.springframework.security.core.Authentication authentication) {
        return Map.of("username", authentication == null ? "" : authentication.getName());
    }
}

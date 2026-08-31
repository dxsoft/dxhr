package com.dxsoft.rsgzgl.ops.security;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ops")
public class OpsPasswordController {

    private final OpsPasswordService service;

    OpsPasswordController(OpsPasswordService service) {
        this.service = service;
    }

    @PostMapping("/password")
    Map<String, String> changePassword(Authentication authentication, @RequestBody OpsPasswordChangeRequest request) {
        service.changePassword(authentication == null ? "" : authentication.getName(), request);
        return Map.of("message", "密码已更新，请使用新密码登录。");
    }
}

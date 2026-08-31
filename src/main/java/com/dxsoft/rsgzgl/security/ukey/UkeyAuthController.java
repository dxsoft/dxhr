package com.dxsoft.rsgzgl.security.ukey;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/ukey")
class UkeyAuthController {

    private final UkeyAuthService ukeyAuthService;

    UkeyAuthController(UkeyAuthService ukeyAuthService) {
        this.ukeyAuthService = ukeyAuthService;
    }

    @GetMapping("/options")
    Map<String, Object> options() {
        return ukeyAuthService.options();
    }

    @GetMapping("/preauth-status")
    Map<String, Object> preauthStatus(HttpServletRequest request) {
        return ukeyAuthService.preauthStatus(request);
    }

    @PostMapping("/cancel-preauth")
    Map<String, Object> cancelPreauth(HttpServletRequest request) {
        return ukeyAuthService.cancelPreauth(request);
    }

    @GetMapping("/challenge")
    Map<String, String> challenge() {
        UkeyChallengeStore.ChallengeIssue issue = ukeyAuthService.createChallenge();
        return Map.of(
                "challengeId", issue.challengeId(),
                "rnd", issue.rnd());
    }

    @PostMapping("/login")
    Map<String, String> login(
            @RequestBody UkeyAuthService.UkeyLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        UkeyAuthService.LoginResult result = ukeyAuthService.login(request, httpRequest, httpResponse);
        return Map.of(
                "username", result.username(),
                "displayName", result.displayName());
    }

    @PostMapping("/verify-step")
    Map<String, String> verifyStep(
            @RequestBody UkeyAuthService.UkeyLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        UkeyAuthService.LoginResult result = ukeyAuthService.verifyStep(request, httpRequest, httpResponse);
        return Map.of(
                "username", result.username(),
                "displayName", result.displayName());
    }
}

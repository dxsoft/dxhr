package com.dxsoft.rsgzgl.security.ukey;

import com.dxsoft.rsgzgl.common.SqlText;
import com.dxsoft.rsgzgl.security.AppUserDetailsService;
import com.dxsoft.rsgzgl.security.AppUserPrincipal;
import com.dxsoft.rsgzgl.security.SecurityAuditService;
import com.dxsoft.rsgzgl.security.ukey.enc.SoftKeyStrEnc;
import com.dxsoft.rsgzgl.security.ukey.sm2.SM2SM3;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
class UkeyAuthService {

    private final UkeyRequirementEvaluator requirementEvaluator;
    private final UkeyChallengeStore challengeStore;
    private final AppUserDetailsService userDetailsService;
    private final SecurityAuditService auditService;
    private final ApplicationEventPublisher eventPublisher;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    UkeyAuthService(
            UkeyRequirementEvaluator requirementEvaluator,
            UkeyChallengeStore challengeStore,
            AppUserDetailsService userDetailsService,
            SecurityAuditService auditService,
            ApplicationEventPublisher eventPublisher) {
        this.requirementEvaluator = requirementEvaluator;
        this.challengeStore = challengeStore;
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    Map<String, Object> options() {
        return Map.of(
                "enabled", requirementEvaluator.ukeyEnabled(),
                "globalRequired", requirementEvaluator.globalRequired());
    }

    Map<String, Object> preauthStatus(HttpServletRequest request) {
        String username = UkeyPreAuth.usernameIfValid(request.getSession(false));
        if (username == null) {
            return Map.of("pending", false);
        }
        return Map.of("pending", true, "username", username);
    }

    Map<String, Object> cancelPreauth(HttpServletRequest request) {
        UkeyPreAuth.clear(request.getSession(false));
        return Map.of("pending", false);
    }

    UkeyChallengeStore.ChallengeIssue createChallenge() {
        ensureEnabled();
        return challengeStore.issue();
    }

    LoginResult login(
            UkeyLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        ensureEnabled();
        String keyId = requireText(request.keyId(), "缺少 UKey 芯片 ID");
        String challengeId = requireText(request.challengeId(), "缺少登录挑战");
        String mode = normalizeMode(request.mode());

        String rnd = challengeStore.consume(challengeId);
        if (rnd == null) {
            auditFailure(keyId, "挑战无效或已过期");
            throw new IllegalArgumentException("登录挑战无效或已过期，请重试");
        }

        Map<String, Object> binding = userDetailsService.findUkeyBinding(keyId);
        if (binding == null) {
            auditFailure(keyId, "UKey 未绑定用户");
            throw new IllegalArgumentException("该 UKey 尚未绑定系统用户，请联系管理员");
        }

        Integer userRequired = integerOrNull(binding.get("ukey_required"));
        if (requirementEvaluator.effectiveRequire(userRequired)) {
            auditFailure(keyId, "该用户需先密码再 UKey，拒绝仅 UKey 登录");
            throw new IllegalArgumentException("该账号已启用双认证，请先使用账号密码登录，再插入 UKey");
        }

        verifyCrypto(keyId, rnd, mode, request, binding);

        AppUserPrincipal principal = userDetailsService.loadUserByUkeyId(keyId);
        if (!principal.isEnabled()) {
            auditFailure(principal.getUsername(), "用户已停用");
            throw new IllegalArgumentException("用户已停用");
        }

        return establishSession(principal, httpRequest, httpResponse);
    }

    LoginResult verifyStep(
            UkeyLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        ensureEnabled();
        HttpSession session = httpRequest.getSession(false);
        String preAuthUsername = UkeyPreAuth.usernameIfValid(session);
        if (preAuthUsername == null) {
            throw new IllegalArgumentException("预认证已过期，请重新输入账号密码");
        }

        String keyId = requireText(request.keyId(), "缺少 UKey 芯片 ID");
        String challengeId = requireText(request.challengeId(), "缺少登录挑战");
        String mode = normalizeMode(request.mode());

        String rnd = challengeStore.consume(challengeId);
        if (rnd == null) {
            auditFailure(preAuthUsername, "挑战无效或已过期");
            throw new IllegalArgumentException("登录挑战无效或已过期，请重试");
        }

        Map<String, Object> binding = userDetailsService.findUkeyBindingByUsername(preAuthUsername);
        if (binding == null) {
            auditFailure(preAuthUsername, "预认证用户不存在");
            throw new IllegalArgumentException("预认证用户不存在，请重新登录");
        }

        String boundKeyId = SqlText.trim((String) binding.get("ukey_id"));
        if (isBlank(boundKeyId)) {
            UkeyPreAuth.clear(session);
            auditFailure(preAuthUsername, "未绑定 UKey");
            throw new IllegalArgumentException("该账号未绑定 UKey，请联系管理员");
        }
        if (!boundKeyId.equals(keyId)) {
            auditFailure(preAuthUsername, "插入的 UKey 与绑定不一致");
            throw new IllegalArgumentException("插入的 UKey 与该账号绑定不一致");
        }

        verifyCrypto(keyId, rnd, mode, request, binding);

        AppUserPrincipal principal = (AppUserPrincipal) userDetailsService.loadUserByUsername(preAuthUsername);
        if (!principal.isEnabled()) {
            auditFailure(principal.getUsername(), "用户已停用");
            throw new IllegalArgumentException("用户已停用");
        }

        UkeyPreAuth.clear(session);
        return establishSession(principal, httpRequest, httpResponse);
    }

    private void verifyCrypto(
            String keyId,
            String rnd,
            String mode,
            UkeyLoginRequest request,
            Map<String, Object> binding) {
        if ("enc".equals(mode)) {
            verifyEnc(keyId, rnd, request.encData(), binding);
        } else if ("sm2".equals(mode)) {
            verifySm2(keyId, rnd, request.sm2UserId(), request.signature(), binding);
        } else {
            throw new IllegalArgumentException("不支持的 UKey 登录模式：" + mode);
        }
    }

    private LoginResult establishSession(
            AppUserPrincipal principal,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        eventPublisher.publishEvent(new AuthenticationSuccessEvent(authentication));
        return new LoginResult(principal.getUsername(), principal.displayName());
    }

    private void verifySm2(
            String keyId,
            String rnd,
            String sm2UserId,
            String signature,
            Map<String, Object> binding) {
        if (!allowsMode(binding, "SM2")) {
            auditFailure(keyId, "绑定不允许 SM2 登录");
            throw new IllegalArgumentException("该 UKey 未配置 SM2 登录");
        }
        String storedSm2UserId = SqlText.trim((String) binding.get("sm2_user_id"));
        String pubX = SqlText.trim((String) binding.get("sm2_pubkey_x"));
        String pubY = SqlText.trim((String) binding.get("sm2_pubkey_y"));
        if (isBlank(storedSm2UserId) || isBlank(pubX) || isBlank(pubY)) {
            auditFailure(keyId, "UKey 绑定公钥不完整");
            throw new IllegalArgumentException("该用户的 SM2 公钥或身份未配置完整");
        }
        requireText(sm2UserId, "缺少 SM2 用户身份");
        requireText(signature, "缺少 SM2 签名");
        if (!storedSm2UserId.equals(sm2UserId.trim())) {
            auditFailure(keyId, "SM2 身份与绑定不一致");
            throw new IllegalArgumentException("UKey 内身份与系统绑定不一致");
        }
        boolean verified;
        try {
            verified = verifySm2Signature(storedSm2UserId, rnd, pubX, pubY, signature.trim());
        } catch (RuntimeException ex) {
            auditFailure(keyId, "验签异常：" + ex.getMessage());
            throw new IllegalArgumentException("SM2 验签失败");
        }
        if (!verified) {
            auditFailure(keyId, "SM2 验签失败");
            throw new IllegalArgumentException("UKey 签名校验失败");
        }
    }

    private boolean verifySm2Signature(
            String sm2UserId, String rnd, String pubX, String pubY, String signature) {
        if (signature == null || signature.length() < 128) {
            return false;
        }
        // SoftKey / 国密 UKey 对中文身份常用 GBK 参与 Z 值；Linux 服务端默认 UTF-8 会导致验签失败。
        java.nio.charset.Charset gbk = java.nio.charset.Charset.forName("GBK");
        if (SM2SM3.YtVerfiy(sm2UserId, rnd, pubX, pubY, signature, gbk)) {
            return true;
        }
        return SM2SM3.YtVerfiy(sm2UserId, rnd, pubX, pubY, signature, java.nio.charset.StandardCharsets.UTF_8)
                || SM2SM3.YtVerfiy(sm2UserId, rnd, pubX, pubY, signature);
    }

    private void verifyEnc(String keyId, String rnd, String encData, Map<String, Object> binding) {
        if (!allowsMode(binding, "ENC")) {
            auditFailure(keyId, "绑定不允许增强算法登录");
            throw new IllegalArgumentException("该 UKey 未配置增强算法登录");
        }
        String encKey = SqlText.trim((String) binding.get("enc_algo_key"));
        if (isBlank(encKey)) {
            auditFailure(keyId, "增强算法密钥未配置");
            throw new IllegalArgumentException("该用户未配置增强算法密钥");
        }
        requireText(encData, "缺少增强算法加密结果");
        boolean verified;
        try {
            verified = SoftKeyStrEnc.matches(rnd, encKey, encData.trim());
        } catch (RuntimeException ex) {
            auditFailure(keyId, "增强算法验算异常：" + ex.getMessage());
            throw new IllegalArgumentException("增强算法校验失败");
        }
        if (!verified) {
            auditFailure(keyId, "增强算法校验失败");
            throw new IllegalArgumentException("UKey 增强算法校验失败");
        }
    }

    private boolean allowsMode(Map<String, Object> binding, String mode) {
        String modes = SqlText.trim((String) binding.get("ukey_auth_modes"));
        if (!isBlank(modes)) {
            String upper = modes.toUpperCase(Locale.ROOT);
            return "BOTH".equals(upper) || mode.equalsIgnoreCase(upper);
        }
        if ("SM2".equals(mode)) {
            return !isBlank(SqlText.trim((String) binding.get("sm2_pubkey_x")))
                    && !isBlank(SqlText.trim((String) binding.get("sm2_pubkey_y")))
                    && !isBlank(SqlText.trim((String) binding.get("sm2_user_id")));
        }
        return !isBlank(SqlText.trim((String) binding.get("enc_algo_key")));
    }

    private void ensureEnabled() {
        if (!requirementEvaluator.ukeyEnabled()) {
            throw new IllegalStateException("UKey 登录未启用");
        }
    }

    private String normalizeMode(String mode) {
        return mode == null || mode.isBlank()
                ? "sm2"
                : mode.trim().toLowerCase(Locale.ROOT);
    }

    private String requireText(String value, String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Integer integerOrNull(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
            return null;
        }
        return Integer.parseInt(text);
    }

    private void auditFailure(String actor, String reason) {
        auditService.recordAs(
                actor == null || actor.isBlank() ? "UNKNOWN" : actor,
                "LOGIN_FAILURE",
                "USER",
                actor == null ? "UNKNOWN" : actor,
                "UKey 登录失败：" + reason);
    }

    record UkeyLoginRequest(
            String challengeId,
            String keyId,
            String mode,
            String sm2UserId,
            String signature,
            String encData) {
    }

    record LoginResult(String username, String displayName) {
    }
}

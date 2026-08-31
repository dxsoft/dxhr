package com.dxsoft.rsgzgl.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
class UkeyBindingImportService {

    private final SecurityAdminRepository repository;
    private final SecurityAuditService auditService;

    UkeyBindingImportService(SecurityAdminRepository repository, SecurityAuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    ImportResult importBindings(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择 UKey 绑定包文件");
        }
        String json;
        try {
            json = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalArgumentException("无法读取绑定包：" + ex.getMessage());
        }
        String format = textField(json, "format");
        if (!"ukey-bind-v1".equals(format) && !"ukey-bind-v2".equals(format)) {
            throw new IllegalArgumentException("不支持的绑定包格式，需要 ukey-bind-v1 或 ukey-bind-v2");
        }
        List<String> deviceBlocks = splitObjects(arrayBlock(json, "devices"));
        if (deviceBlocks.isEmpty()) {
            throw new IllegalArgumentException("绑定包中没有设备记录");
        }
        int success = 0;
        List<String> failures = new ArrayList<>();
        for (String block : deviceBlocks) {
            String ukeyId = textField(block, "ukeyId");
            String sm2UserId = blankToNull(textField(block, "sm2UserId"));
            String pubX = blankToNull(textField(block, "sm2PubkeyX"));
            String pubY = blankToNull(textField(block, "sm2PubkeyY"));
            String encKey = blankToNull(textField(block, "encAlgoKey"));
            String modes = blankToNull(textField(block, "authModes"));
            String username = textField(block, "username");
            if (blank(ukeyId)) {
                failures.add("跳过无芯片 ID 的记录");
                continue;
            }
            boolean hasSm2 = sm2UserId != null && pubX != null && pubY != null
                    && pubX.length() >= 64 && pubY.length() >= 64;
            boolean hasEnc = encKey != null && encKey.length() == 32;
            if (!hasSm2 && !hasEnc) {
                failures.add("跳过不完整记录：" + ukeyId + "（需 SM2 公钥或 32 位增强密钥）");
                continue;
            }
            Long userId = null;
            if (!blank(username)) {
                userId = repository.findUserIdByUsername(username.trim());
                if (userId == null) {
                    failures.add("用户不存在：" + username.trim() + "（芯片 " + ukeyId + "）");
                    continue;
                }
            } else {
                userId = repository.findUserIdByUkeyId(ukeyId.trim());
                if (userId == null) {
                    failures.add("未指定 username 且无已绑定该芯片的用户：" + ukeyId);
                    continue;
                }
            }
            Long occupied = repository.findUserIdByUkeyId(ukeyId.trim());
            if (occupied != null && !occupied.equals(userId)) {
                failures.add("芯片已绑定其他用户：" + ukeyId);
                continue;
            }
            if (modes == null) {
                if (hasSm2 && hasEnc) {
                    modes = "BOTH";
                } else if (hasEnc) {
                    modes = "ENC";
                } else {
                    modes = "SM2";
                }
            } else {
                modes = modes.toUpperCase(Locale.ROOT);
            }
            repository.updateUserUkeyBinding(
                    userId,
                    ukeyId.trim(),
                    hasSm2 ? sm2UserId.trim() : null,
                    hasSm2 ? pubX.trim().toUpperCase(Locale.ROOT) : null,
                    hasSm2 ? pubY.trim().toUpperCase(Locale.ROOT) : null,
                    hasEnc ? encKey.trim().toUpperCase(Locale.ROOT) : null,
                    modes);
            success++;
        }
        auditService.record(
                "IMPORT_UKEY_BINDINGS",
                "USER",
                "batch",
                "导入 UKey 绑定包：成功 " + success + "，失败 " + failures.size());
        return new ImportResult(success, failures.size(), failures);
    }

    private static String blankToNull(String value) {
        return blank(value) ? null : value.trim();
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String textField(String json, String name) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
                .matcher(json);
        if (!matcher.find()) {
            return "";
        }
        return matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String arrayBlock(String json, String name) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:\\s*\\[").matcher(json);
        if (!matcher.find()) {
            return "[]";
        }
        return extractBalanced(json, matcher.end() - 1, '[', ']');
    }

    private static String extractBalanced(String text, int start, char open, char close) {
        int depth = 0;
        boolean inString = false;
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '"' && (i == 0 || text.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (inString) {
                continue;
            }
            if (ch == open) {
                depth++;
            } else if (ch == close) {
                depth--;
                if (depth == 0) {
                    return text.substring(start, i + 1);
                }
            }
        }
        return "";
    }

    private static List<String> splitObjects(String arrayJson) {
        List<String> items = new ArrayList<>();
        int depth = 0;
        boolean inString = false;
        int start = -1;
        for (int i = 0; i < arrayJson.length(); i++) {
            char ch = arrayJson.charAt(i);
            if (ch == '"' && (i == 0 || arrayJson.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (inString) {
                continue;
            }
            if (ch == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    items.add(arrayJson.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return items;
    }

    record ImportResult(int successCount, int failureCount, List<String> failures) {
    }
}

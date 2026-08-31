package com.dxsoft.rsgzgl.ops.ukey;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class UkeyDeviceService {

    private final UkeyDeviceRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    UkeyDeviceService(UkeyDeviceRepository repository) {
        this.repository = repository;
    }

    public List<UkeyDeviceView> list(String keyword) {
        return repository.findAll(keyword);
    }

    public Map<String, String> generateEncKey() {
        byte[] raw = new byte[16];
        secureRandom.nextBytes(raw);
        return Map.of("encAlgoKey", HexFormat.of().withUpperCase().formatHex(raw));
    }

    public UkeyDeviceView register(UkeyDeviceRegisterRequest request) {
        require(request.chipId(), "缺少芯片 ID");
        boolean hasSm2 = notBlank(request.sm2UserId())
                && notBlank(request.pubkeyX())
                && notBlank(request.pubkeyY())
                && request.pubkeyX().trim().length() >= 64
                && request.pubkeyY().trim().length() >= 64;
        boolean hasEnc = notBlank(request.encAlgoKey()) && request.encAlgoKey().trim().length() == 32;
        if (!hasSm2 && !hasEnc) {
            throw new IllegalArgumentException("请提供 SM2 公钥身份，或 32 位增强算法密钥");
        }
        String modes = blankToNull(request.authModes());
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
            if (!List.of("SM2", "ENC", "BOTH").contains(modes)) {
                throw new IllegalArgumentException("authModes 仅支持 SM2 / ENC / BOTH");
            }
        }
        return repository.upsert(new UkeyDeviceRegisterRequest(
                request.chipId(),
                hasSm2 ? request.sm2UserId() : null,
                hasSm2 ? request.pubkeyX() : null,
                hasSm2 ? request.pubkeyY() : null,
                hasEnc ? request.encAlgoKey() : null,
                modes,
                request.username(),
                request.orgCode(),
                request.note()));
    }

    public byte[] exportBindings(String keyword) {
        List<UkeyBindExportItem> items = repository.findAll(keyword).stream()
                .filter(d -> "ACTIVE".equalsIgnoreCase(d.status()))
                .map(d -> new UkeyBindExportItem(
                        d.chipId(),
                        d.sm2UserId(),
                        d.pubkeyX(),
                        d.pubkeyY(),
                        d.encAlgoKey(),
                        d.authModes(),
                        d.username(),
                        d.note(),
                        d.orgCode()))
                .toList();
        return toJson(new UkeyBindExportDocument(UkeyBindExportDocument.FORMAT, items))
                .getBytes(StandardCharsets.UTF_8);
    }

    private static String toJson(UkeyBindExportDocument doc) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"format\": ").append(quote(doc.format())).append(",\n");
        sb.append("  \"devices\": [\n");
        List<UkeyBindExportItem> devices = doc.devices() == null ? List.of() : doc.devices();
        for (int i = 0; i < devices.size(); i++) {
            UkeyBindExportItem d = devices.get(i);
            sb.append("    {\n");
            sb.append("      \"ukeyId\": ").append(quote(d.ukeyId())).append(",\n");
            sb.append("      \"authModes\": ").append(quote(nullToEmpty(d.authModes()))).append(",\n");
            sb.append("      \"encAlgoKey\": ").append(quote(nullToEmpty(d.encAlgoKey()))).append(",\n");
            sb.append("      \"sm2UserId\": ").append(quote(nullToEmpty(d.sm2UserId()))).append(",\n");
            sb.append("      \"sm2PubkeyX\": ").append(quote(nullToEmpty(d.sm2PubkeyX()))).append(",\n");
            sb.append("      \"sm2PubkeyY\": ").append(quote(nullToEmpty(d.sm2PubkeyY()))).append(",\n");
            sb.append("      \"username\": ").append(quote(nullToEmpty(d.username()))).append(",\n");
            sb.append("      \"note\": ").append(quote(nullToEmpty(d.note()))).append(",\n");
            sb.append("      \"orgCode\": ").append(quote(nullToEmpty(d.orgCode()))).append("\n");
            sb.append("    }").append(i + 1 < devices.size() ? "," : "").append("\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String quote(String value) {
        return "\"" + nullToEmpty(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r") + "\"";
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String blankToNull(String value) {
        return notBlank(value) ? value.trim() : null;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static void require(String value, String message) {
        if (!notBlank(value)) {
            throw new IllegalArgumentException(message);
        }
    }
}

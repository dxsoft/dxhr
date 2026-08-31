package com.dxsoft.rsgzgl.license;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class LicenseCrypto {

    private LicenseCrypto() {
    }

    static String canonicalPayload(LicensePackageDocument doc) {
        StringBuilder sb = new StringBuilder();
        sb.append("format=").append(nullToEmpty(doc.format())).append('\n');
        sb.append("issuedAt=").append(nullToEmpty(doc.issuedAt())).append('\n');
        sb.append("expiresAt=").append(nullToEmpty(doc.expiresAt())).append('\n');
        sb.append("issuer=").append(nullToEmpty(doc.issuer())).append('\n');
        LicenseSubject subject = doc.subject();
        if (subject != null) {
            sb.append("subjectCode=").append(nullToEmpty(subject.organizationCode())).append('\n');
            sb.append("subjectName=").append(nullToEmpty(subject.organizationName())).append('\n');
            sb.append("subjectLevel=").append(nullToEmpty(subject.organizationLevel())).append('\n');
            sb.append("subjectCity=").append(nullToEmpty(subject.city())).append('\n');
            sb.append("subjectSupervisor=").append(nullToEmpty(subject.supervisor())).append('\n');
        }
        List<LicenseOrganization> orgs = doc.organizations() == null ? List.of() : doc.organizations();
        for (LicenseOrganization org : orgs) {
            sb.append("org=")
                    .append(nullToEmpty(org.organizationCode())).append('|')
                    .append(nullToEmpty(org.name())).append('|')
                    .append(nullToEmpty(org.shortName())).append('|')
                    .append(nullToEmpty(org.property())).append('|')
                    .append(nullToEmpty(org.category())).append('|')
                    .append(nullToEmpty(org.payrollCategory())).append('|')
                    .append(nullToEmpty(org.organizationLevel()))
                    .append('\n');
        }
        // Only include when declared — keeps old packages verifiable.
        if (doc.ukeyEnabled() != null) {
            sb.append("ukeyEnabled=").append(doc.ukeyEnabled()).append('\n');
        }
        if (doc.ukeyRequired() != null) {
            sb.append("ukeyRequired=").append(doc.ukeyRequired()).append('\n');
        }
        appendLocalPolicyCanonical(sb, doc.localPolicy());
        return sb.toString();
    }

    private static void appendLocalPolicyCanonical(StringBuilder sb, LicenseLocalPolicy policy) {
        if (policy == null) {
            return;
        }
        sb.append("policyActiveStaffFlag=").append(policy.activeStaffFlag() == null ? "" : policy.activeStaffFlag()).append('\n');
        sb.append("policyApprovalFlag=").append(nullToEmpty(policy.approvalFlag())).append('\n');
        sb.append("policyPayrollTitle=").append(nullToEmpty(policy.payrollTitle())).append('\n');
        sb.append("policyRoundingMode=").append(nullToEmpty(policy.roundingMode())).append('\n');
        sb.append("policyRoundToInteger=").append(nullToEmpty(policy.roundToInteger())).append('\n');
        sb.append("policyPoliceAllowanceCaption=").append(nullToEmpty(policy.policeAllowanceCaption())).append('\n');
        sb.append("policySubsidyCaption=").append(nullToEmpty(policy.subsidyCaption())).append('\n');
        sb.append("policyApprovalMode=").append(nullToEmpty(policy.approvalMode())).append('\n');
        sb.append("policyUnitApprovalCategory=").append(nullToEmpty(policy.unitApprovalCategory())).append('\n');
        sb.append("policyPoliceRankStartLevel=").append(decimalText(policy.policeRankStartLevel())).append('\n');
        sb.append("policyRetiredGradeStep=").append(nullToEmpty(policy.retiredGradeStep())).append('\n');
        sb.append("policyInternSalaryMode=").append(decimalText(policy.internSalaryMode())).append('\n');
        sb.append("policyBonusBalanceMode=").append(decimalText(policy.bonusBalanceMode())).append('\n');
        sb.append("policyFloatingSalaryMode=").append(decimalText(policy.floatingSalaryMode())).append('\n');
        sb.append("policyPayGradeRetentionMode=").append(decimalText(policy.payGradeRetentionMode())).append('\n');
        sb.append("policyBackupPath=").append(nullToEmpty(policy.backupPath())).append('\n');
        sb.append("policyPositionChangeIncludeTechnicalGrade=").append(nullToEmpty(policy.positionChangeIncludeTechnicalGrade())).append('\n');
        sb.append("policyRankChangeIncludeTechnicalGrade=").append(nullToEmpty(policy.rankChangeIncludeTechnicalGrade())).append('\n');
        sb.append("policyAutoBackup=").append(decimalText(policy.autoBackup())).append('\n');
        sb.append("policyConfirmBeforeAction=").append(decimalText(policy.confirmBeforeAction())).append('\n');
        sb.append("policyCheckUpdate=").append(decimalText(policy.checkUpdate())).append('\n');
    }

    static String hmacSha256Hex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception ex) {
            throw new IllegalStateException("无法计算授权签名: " + ex.getMessage(), ex);
        }
    }

    static String fingerprint(String canonicalPayload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] raw = digest.digest(canonicalPayload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw).substring(0, 32).toUpperCase(Locale.ROOT);
        } catch (Exception ex) {
            throw new IllegalStateException("无法计算授权指纹: " + ex.getMessage(), ex);
        }
    }

    static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        byte[] a = left.getBytes(StandardCharsets.UTF_8);
        byte[] b = right.getBytes(StandardCharsets.UTF_8);
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    static String toOrgsExportJson(
            String format,
            String exportedAt,
            String city,
            String supervisor,
            List<LicenseOrganization> orgs) {
        return toOrgsExportJson(format, exportedAt, city, supervisor, orgs, null);
    }

    static String toOrgsExportJson(
            String format,
            String exportedAt,
            String city,
            String supervisor,
            List<LicenseOrganization> orgs,
            LicenseLocalPolicy localPolicy) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"format\": ").append(quote(format)).append(",\n");
        sb.append("  \"exportedAt\": ").append(quote(exportedAt)).append(",\n");
        sb.append("  \"city\": ").append(quote(city)).append(",\n");
        sb.append("  \"supervisor\": ").append(quote(supervisor)).append(",\n");
        sb.append("  \"organizations\": [\n");
        List<LicenseOrganization> list = orgs == null ? List.of() : orgs;
        for (int i = 0; i < list.size(); i++) {
            LicenseOrganization org = list.get(i);
            sb.append("    {\n");
            sb.append("      \"organizationCode\": ").append(quote(org.organizationCode())).append(",\n");
            sb.append("      \"name\": ").append(quote(org.name())).append(",\n");
            sb.append("      \"shortName\": ").append(quote(org.shortName())).append(",\n");
            sb.append("      \"property\": ").append(quote(org.property())).append(",\n");
            sb.append("      \"category\": ").append(quote(org.category())).append(",\n");
            sb.append("      \"payrollCategory\": ").append(quote(org.payrollCategory())).append(",\n");
            sb.append("      \"allowanceStandard\": ").append(quote(org.allowanceStandard())).append(",\n");
            sb.append("      \"personnelQuota\": ").append(org.personnelQuota() == null ? "null" : org.personnelQuota()).append(",\n");
            sb.append("      \"establishmentCount\": ").append(org.establishmentCount() == null ? "null" : org.establishmentCount()).append(",\n");
            sb.append("      \"actualCount\": ").append(org.actualCount() == null ? "null" : org.actualCount()).append(",\n");
            sb.append("      \"organizationLevel\": ").append(quote(org.organizationLevel())).append(",\n");
            sb.append("      \"systemCategory\": ").append(quote(org.systemCategory())).append(",\n");
            sb.append("      \"performanceAllowanceEnabled\": ").append(org.performanceAllowanceEnabled() == null ? "null" : org.performanceAllowanceEnabled()).append(",\n");
            sb.append("      \"performanceCategory\": ").append(org.performanceCategory() == null ? "null" : org.performanceCategory()).append(",\n");
            sb.append("      \"performanceRatio\": ").append(quote(org.performanceRatio())).append(",\n");
            sb.append("      \"yearAllowanceCategory\": ").append(org.yearAllowanceCategory() == null ? "null" : org.yearAllowanceCategory()).append(",\n");
            sb.append("      \"financeSource\": ").append(quote(org.financeSource())).append(",\n");
            sb.append("      \"housingFundWithheld\": ").append(quote(org.housingFundWithheld())).append(",\n");
            sb.append("      \"pensionWithheld\": ").append(quote(org.pensionWithheld())).append(",\n");
            sb.append("      \"city\": ").append(quote(city)).append(",\n");
            sb.append("      \"supervisor\": ").append(quote(supervisor)).append("\n");
            sb.append("    }").append(i + 1 < list.size() ? "," : "").append("\n");
        }
        sb.append("  ]");
        if (localPolicy != null) {
            sb.append(",\n  \"localPolicy\": ").append(localPolicyJson(localPolicy));
        }
        sb.append("\n}\n");
        return sb.toString();
    }

    static String toJson(LicensePackageDocument doc) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"format\": ").append(quote(doc.format())).append(",\n");
        sb.append("  \"issuedAt\": ").append(quote(doc.issuedAt())).append(",\n");
        sb.append("  \"expiresAt\": ").append(doc.expiresAt() == null || doc.expiresAt().isBlank()
                ? "null" : quote(doc.expiresAt())).append(",\n");
        sb.append("  \"issuer\": ").append(quote(doc.issuer())).append(",\n");
        LicenseSubject subject = doc.subject();
        sb.append("  \"subject\": {\n");
        sb.append("    \"organizationCode\": ").append(quote(subject.organizationCode())).append(",\n");
        sb.append("    \"organizationName\": ").append(quote(subject.organizationName())).append(",\n");
        sb.append("    \"organizationLevel\": ").append(quote(subject.organizationLevel())).append(",\n");
        sb.append("    \"city\": ").append(quote(subject.city())).append(",\n");
        sb.append("    \"supervisor\": ").append(quote(subject.supervisor())).append("\n");
        sb.append("  },\n");
        sb.append("  \"organizations\": [\n");
        List<LicenseOrganization> orgs = doc.organizations() == null ? List.of() : doc.organizations();
        for (int i = 0; i < orgs.size(); i++) {
            LicenseOrganization org = orgs.get(i);
            sb.append("    {\n");
            sb.append("      \"organizationCode\": ").append(quote(org.organizationCode())).append(",\n");
            sb.append("      \"name\": ").append(quote(org.name())).append(",\n");
            sb.append("      \"shortName\": ").append(quote(org.shortName())).append(",\n");
            sb.append("      \"property\": ").append(quote(org.property())).append(",\n");
            sb.append("      \"category\": ").append(quote(org.category())).append(",\n");
            sb.append("      \"payrollCategory\": ").append(quote(org.payrollCategory())).append(",\n");
            sb.append("      \"allowanceStandard\": ").append(quote(org.allowanceStandard())).append(",\n");
            sb.append("      \"personnelQuota\": ").append(org.personnelQuota() == null ? "null" : org.personnelQuota()).append(",\n");
            sb.append("      \"establishmentCount\": ").append(org.establishmentCount() == null ? "null" : org.establishmentCount()).append(",\n");
            sb.append("      \"actualCount\": ").append(org.actualCount() == null ? "null" : org.actualCount()).append(",\n");
            sb.append("      \"organizationLevel\": ").append(quote(org.organizationLevel())).append(",\n");
            sb.append("      \"systemCategory\": ").append(quote(org.systemCategory())).append(",\n");
            sb.append("      \"performanceAllowanceEnabled\": ").append(org.performanceAllowanceEnabled() == null ? "null" : org.performanceAllowanceEnabled()).append(",\n");
            sb.append("      \"performanceCategory\": ").append(org.performanceCategory() == null ? "null" : org.performanceCategory()).append(",\n");
            sb.append("      \"performanceRatio\": ").append(quote(org.performanceRatio())).append(",\n");
            sb.append("      \"yearAllowanceCategory\": ").append(org.yearAllowanceCategory() == null ? "null" : org.yearAllowanceCategory()).append(",\n");
            sb.append("      \"financeSource\": ").append(quote(org.financeSource())).append(",\n");
            sb.append("      \"housingFundWithheld\": ").append(quote(org.housingFundWithheld())).append(",\n");
            sb.append("      \"pensionWithheld\": ").append(quote(org.pensionWithheld())).append("\n");
            sb.append("    }").append(i + 1 < orgs.size() ? "," : "").append("\n");
        }
        sb.append("  ],\n");
        if (doc.localPolicy() != null) {
            sb.append("  \"localPolicy\": ").append(localPolicyJson(doc.localPolicy())).append(",\n");
        }
        if (doc.ukeyEnabled() != null) {
            sb.append("  \"ukeyEnabled\": ").append(doc.ukeyEnabled()).append(",\n");
        }
        if (doc.ukeyRequired() != null) {
            sb.append("  \"ukeyRequired\": ").append(doc.ukeyRequired()).append(",\n");
        }
        sb.append("  \"signature\": ").append(quote(doc.signature())).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    static LicensePackageDocument parseJson(String json) {
        String format = textField(json, "format");
        String issuedAt = textField(json, "issuedAt");
        String expiresAt = nullableTextField(json, "expiresAt");
        String issuer = textField(json, "issuer");
        String signature = textField(json, "signature");
        Boolean ukeyEnabled = booleanField(json, "ukeyEnabled");
        Boolean ukeyRequired = booleanField(json, "ukeyRequired");
        String subjectBlock = objectBlock(json, "subject");
        LicenseSubject subject = new LicenseSubject(
                textField(subjectBlock, "organizationCode"),
                textField(subjectBlock, "organizationName"),
                textField(subjectBlock, "organizationLevel"),
                textField(subjectBlock, "city"),
                textField(subjectBlock, "supervisor"));
        List<LicenseOrganization> organizations = new ArrayList<>();
        String orgsBlock = arrayBlock(json, "organizations");
        for (String item : splitObjects(orgsBlock)) {
            organizations.add(new LicenseOrganization(
                    textField(item, "organizationCode"),
                    textField(item, "name"),
                    textField(item, "shortName"),
                    textField(item, "property"),
                    textField(item, "category"),
                    textField(item, "payrollCategory"),
                    textField(item, "allowanceStandard"),
                    intField(item, "personnelQuota"),
                    intField(item, "establishmentCount"),
                    intField(item, "actualCount"),
                    textField(item, "organizationLevel"),
                    textField(item, "systemCategory"),
                    intField(item, "performanceAllowanceEnabled"),
                    intField(item, "performanceCategory"),
                    textField(item, "performanceRatio"),
                    intField(item, "yearAllowanceCategory"),
                    textField(item, "financeSource"),
                    textField(item, "housingFundWithheld"),
                    textField(item, "pensionWithheld")));
        }
        LicenseLocalPolicy localPolicy = parseLocalPolicy(objectBlock(json, "localPolicy"));
        return new LicensePackageDocument(
                format, issuedAt, expiresAt, issuer, subject, organizations, localPolicy,
                ukeyEnabled, ukeyRequired, signature);
    }

    private static String localPolicyJson(LicenseLocalPolicy policy) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("    \"activeStaffFlag\": ").append(policy.activeStaffFlag() == null ? "null" : policy.activeStaffFlag()).append(",\n");
        sb.append("    \"approvalFlag\": ").append(quote(policy.approvalFlag())).append(",\n");
        sb.append("    \"payrollTitle\": ").append(quote(policy.payrollTitle())).append(",\n");
        sb.append("    \"roundingMode\": ").append(quote(policy.roundingMode())).append(",\n");
        sb.append("    \"roundToInteger\": ").append(quote(policy.roundToInteger())).append(",\n");
        sb.append("    \"policeAllowanceCaption\": ").append(quote(policy.policeAllowanceCaption())).append(",\n");
        sb.append("    \"subsidyCaption\": ").append(quote(policy.subsidyCaption())).append(",\n");
        sb.append("    \"approvalMode\": ").append(quote(policy.approvalMode())).append(",\n");
        sb.append("    \"unitApprovalCategory\": ").append(quote(policy.unitApprovalCategory())).append(",\n");
        sb.append("    \"policeRankStartLevel\": ").append(decimalJson(policy.policeRankStartLevel())).append(",\n");
        sb.append("    \"retiredGradeStep\": ").append(quote(policy.retiredGradeStep())).append(",\n");
        sb.append("    \"internSalaryMode\": ").append(decimalJson(policy.internSalaryMode())).append(",\n");
        sb.append("    \"bonusBalanceMode\": ").append(decimalJson(policy.bonusBalanceMode())).append(",\n");
        sb.append("    \"floatingSalaryMode\": ").append(decimalJson(policy.floatingSalaryMode())).append(",\n");
        sb.append("    \"payGradeRetentionMode\": ").append(decimalJson(policy.payGradeRetentionMode())).append(",\n");
        sb.append("    \"backupPath\": ").append(quote(policy.backupPath())).append(",\n");
        sb.append("    \"positionChangeIncludeTechnicalGrade\": ").append(quote(policy.positionChangeIncludeTechnicalGrade())).append(",\n");
        sb.append("    \"rankChangeIncludeTechnicalGrade\": ").append(quote(policy.rankChangeIncludeTechnicalGrade())).append(",\n");
        sb.append("    \"autoBackup\": ").append(decimalJson(policy.autoBackup())).append(",\n");
        sb.append("    \"confirmBeforeAction\": ").append(decimalJson(policy.confirmBeforeAction())).append(",\n");
        sb.append("    \"checkUpdate\": ").append(decimalJson(policy.checkUpdate())).append("\n");
        sb.append("  }");
        return sb.toString();
    }

    private static LicenseLocalPolicy parseLocalPolicy(String block) {
        if (block == null || block.isBlank() || "{}".equals(block.trim())) {
            return null;
        }
        return new LicenseLocalPolicy(
                intField(block, "activeStaffFlag"),
                textField(block, "approvalFlag"),
                textField(block, "payrollTitle"),
                textField(block, "roundingMode"),
                textField(block, "roundToInteger"),
                textField(block, "policeAllowanceCaption"),
                textField(block, "subsidyCaption"),
                textField(block, "approvalMode"),
                textField(block, "unitApprovalCategory"),
                decimalField(block, "policeRankStartLevel"),
                textField(block, "retiredGradeStep"),
                decimalField(block, "internSalaryMode"),
                decimalField(block, "bonusBalanceMode"),
                decimalField(block, "floatingSalaryMode"),
                decimalField(block, "payGradeRetentionMode"),
                textField(block, "backupPath"),
                textField(block, "positionChangeIncludeTechnicalGrade"),
                textField(block, "rankChangeIncludeTechnicalGrade"),
                decimalField(block, "autoBackup"),
                decimalField(block, "confirmBeforeAction"),
                decimalField(block, "checkUpdate"));
    }

    private static java.math.BigDecimal decimalField(String json, String name) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:\\s*(null|-?\\d+(?:\\.\\d+)?)")
                .matcher(json);
        if (!matcher.find() || "null".equals(matcher.group(1))) {
            return null;
        }
        return new java.math.BigDecimal(matcher.group(1));
    }

    private static String decimalJson(java.math.BigDecimal value) {
        return value == null ? "null" : value.stripTrailingZeros().toPlainString();
    }

    private static String decimalText(java.math.BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private static String textField(String json, String name) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
                .matcher(json);
        if (!matcher.find()) {
            return "";
        }
        return unescape(matcher.group(1));
    }

    private static String nullableTextField(String json, String name) {
        if (Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:\\s*null").matcher(json).find()) {
            return null;
        }
        String value = textField(json, name);
        return value.isBlank() ? null : value;
    }

    private static Integer intField(String json, String name) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:\\s*(null|-?\\d+)")
                .matcher(json);
        if (!matcher.find() || "null".equals(matcher.group(1))) {
            return null;
        }
        return Integer.valueOf(matcher.group(1));
    }

    /** Missing field → null (legacy packages). */
    private static Boolean booleanField(String json, String name) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:\\s*(true|false|null)")
                .matcher(json);
        if (!matcher.find() || "null".equals(matcher.group(1))) {
            return null;
        }
        return Boolean.valueOf(matcher.group(1));
    }

    private static String objectBlock(String json, String name) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:\\s*\\{").matcher(json);
        if (!matcher.find()) {
            return "{}";
        }
        return extractBalanced(json, matcher.end() - 1, '{', '}');
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

    private static String quote(String value) {
        return "\"" + escape(nullToEmpty(value)) + "\"";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r");
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}

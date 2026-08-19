package com.dxsoft.rsgzgl.dictionary;

import java.util.Locale;
import java.util.Set;

final class DictionaryQueryFilter {

    private static final Set<String> FILTERED_FIELDS = Set.of(
            "RYFL", "GWFL", "XRZW", "ZWJB", "XZZW", "ZJDJ", "ZWGW1", "ZWGW2");

    private DictionaryQueryFilter() {
    }

    static boolean supports(String fieldName) {
        return fieldName != null && FILTERED_FIELDS.contains(normalizeField(fieldName));
    }

    static DictionaryFilterSpec forField(String fieldName, String unitCategory, String organizationProperty) {
        String field = normalizeField(fieldName);
        boolean administrative = "行政".equals(trim(unitCategory));
        String dwsx = trim(organizationProperty);
        return switch (field) {
            case "RYFL" -> administrative ? ryflAdministrative() : ryflInstitution();
            case "GWFL" -> administrative ? gwflAdministrative() : gwflInstitution();
            case "XRZW" -> administrative ? xrzwAdministrative() : xrzwInstitution();
            case "ZWJB" -> administrative ? zwjbAdministrative() : zwjbInstitution();
            case "XZZW" -> administrative ? xzzwAdministrative() : xzzwInstitution();
            case "ZJDJ" -> zjdj(dwsx);
            case "ZWGW1", "ZWGW2" -> administrative ? zwgwAdministrative(dwsx) : zwgwInstitution(dwsx);
            default -> null;
        };
    }

    private static DictionaryFilterSpec ryflAdministrative() {
        return new DictionaryFilterSpec("014",
                "bm LIKE '014%' AND LENGTH(TRIM(bm)) = 5 AND bm <= '01406'");
    }

    private static DictionaryFilterSpec ryflInstitution() {
        return new DictionaryFilterSpec("014",
                "bm LIKE '014%' AND LENGTH(TRIM(bm)) = 5 AND bm >= '01407'");
    }

    private static DictionaryFilterSpec gwflAdministrative() {
        return new DictionaryFilterSpec("051",
                "LENGTH(TRIM(bm)) = 5 AND LEFT(bm, 5) IN ("
                        + "'05101','05102','05103','05105','05106',"
                        + "'05121','05122','05123','05124','05125',"
                        + "'05126','05127','05128')");
    }

    private static DictionaryFilterSpec gwflInstitution() {
        return new DictionaryFilterSpec("051",
                "bm LIKE '051%' AND LENGTH(TRIM(bm)) = 5 AND bm > '05106' AND bm < '05121'");
    }

    private static DictionaryFilterSpec xrzwAdministrative() {
        return excludeFiveDigitPrefixes("051", "05104", "05107", "05108", "05109", "05110");
    }

    private static DictionaryFilterSpec xrzwInstitution() {
        return new DictionaryFilterSpec("001", "bm LIKE '001%' AND bm >= '00107'");
    }

    private static DictionaryFilterSpec zwjbAdministrative() {
        return excludeFiveDigitPrefixes("026", "02604", "02607", "02608", "02609", "02610");
    }

    private static DictionaryFilterSpec zwjbInstitution() {
        return new DictionaryFilterSpec("026",
                "bm LIKE '026%' AND LENGTH(TRIM(bm)) > 3 AND bm >= '02607' AND bm < '02621'");
    }

    private static DictionaryFilterSpec xzzwAdministrative() {
        return excludeFiveDigitPrefixes("051", "05104", "05107", "05108", "05109", "05110");
    }

    private static DictionaryFilterSpec xzzwInstitution() {
        return new DictionaryFilterSpec("051",
                "bm LIKE '051%' AND LENGTH(TRIM(bm)) > 3 AND bm >= '05107' AND bm < '05121'");
    }

    private static DictionaryFilterSpec zjdj(String dwsx) {
        String safeDwsx = sanitizeCodeSuffix(dwsx);
        if (!safeDwsx.isEmpty()) {
            return new DictionaryFilterSpec("058" + safeDwsx,
                    "bm LIKE '058" + safeDwsx + "%' AND LENGTH(TRIM(bm)) > 5");
        }
        return new DictionaryFilterSpec("058", "bm LIKE '058%' AND LENGTH(TRIM(bm)) > 3");
    }

    private static DictionaryFilterSpec zwgwAdministrative(String dwsx) {
        String safeDwsx = sanitizeCodeSuffix(dwsx);
        if (!safeDwsx.isEmpty()) {
            return new DictionaryFilterSpec("051" + safeDwsx,
                    "bm LIKE '051" + safeDwsx + "%' AND LENGTH(TRIM(bm)) > 5");
        }
        return new DictionaryFilterSpec("051",
                "bm LIKE '051%' AND LENGTH(TRIM(bm)) > 3 AND LEFT(bm, 5) <> '05104'");
    }

    private static DictionaryFilterSpec zwgwInstitution(String dwsx) {
        String safeDwsx = sanitizeCodeSuffix(dwsx);
        if ("05".equals(safeDwsx)) {
            return new DictionaryFilterSpec("051",
                    "bm LIKE '051%' AND LENGTH(TRIM(bm)) >= 7 AND bm >= '05105' AND bm < '05121'");
        }
        // 普通专技(10)与义务教育专技(11)并列可选，便于单位逐步改用 11xx
        if ("10".equals(safeDwsx)) {
            return new DictionaryFilterSpec("05110",
                    "(bm LIKE '05110%' OR bm LIKE '05111%') AND LENGTH(TRIM(bm)) >= 7");
        }
        if ("11".equals(safeDwsx)) {
            return new DictionaryFilterSpec("05111",
                    "bm LIKE '05111%' AND LENGTH(TRIM(bm)) >= 7");
        }
        if (!safeDwsx.isEmpty()) {
            return new DictionaryFilterSpec("051" + safeDwsx,
                    "bm LIKE '051" + safeDwsx + "%' AND LENGTH(TRIM(bm)) >= 7");
        }
        return new DictionaryFilterSpec("051",
                "bm LIKE '051%' AND LENGTH(TRIM(bm)) >= 5 AND bm >= '05107' AND bm < '05121'");
    }

    private static DictionaryFilterSpec excludeFiveDigitPrefixes(String rootPrefix, String... excludedPrefixes) {
        StringBuilder clause = new StringBuilder()
                .append("bm LIKE '")
                .append(rootPrefix)
                .append("%' AND LENGTH(TRIM(bm)) > 3");
        for (String excludedPrefix : excludedPrefixes) {
            clause.append(" AND LEFT(bm, 5) <> '").append(excludedPrefix).append("'");
        }
        return new DictionaryFilterSpec(rootPrefix, clause.toString());
    }

    private static String normalizeField(String fieldName) {
        return trim(fieldName).toUpperCase(Locale.ROOT);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String sanitizeCodeSuffix(String value) {
        String trimmed = trim(value);
        if (trimmed.isEmpty() || !trimmed.chars().allMatch(Character::isDigit)) {
            return "";
        }
        return trimmed;
    }
}

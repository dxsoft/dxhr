package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.organization.UnitPayrollClassification;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Maps dryjbxx columns to maintenance form fields and default metadata. */
final class PersonnelBasicFieldRegistry {

    record FieldBinding(String requestProperty, String elementId, String defaultCategory) {
    }

    private static final Map<String, FieldBinding> BINDINGS = buildBindings();

    private PersonnelBasicFieldRegistry() {
    }

    static Optional<FieldBinding> bindingForColumn(String columnName) {
        if (columnName == null || columnName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BINDINGS.get(columnName.trim().toLowerCase()));
    }

    static Optional<String> columnForRequestProperty(String requestProperty) {
        if (requestProperty == null || requestProperty.isBlank()) {
            return Optional.empty();
        }
        return BINDINGS.entrySet().stream()
                .filter(entry -> requestProperty.equals(entry.getValue().requestProperty()))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    static List<FieldMetadataRecord> defaultMetadata(String unitProperty) {
        String specific = resolveFldjbxxCategory(unitProperty);
        return BINDINGS.entrySet().stream()
                .map(entry -> new FieldMetadataRecord(
                        entry.getKey(),
                        entry.getKey(),
                        entry.getValue().defaultCategory() == null ? "00" : entry.getValue().defaultCategory(),
                        false,
                        false,
                        true,
                        BINDINGS.size()))
                .filter(field -> "00".equals(field.category()) || specific.equals(field.category()))
                .toList();
    }

    static String resolveFldjbxxCategory(String unitProperty) {
        return UnitPayrollClassification.usesAdministrativeTreatment(unitProperty, null) ? "01" : "10";
    }

    static String normalizeUnitProperty(String unitProperty) {
        if (unitProperty == null || unitProperty.isBlank()) {
            return "事业";
        }
        return unitProperty.trim();
    }

    private static Map<String, FieldBinding> buildBindings() {
        Map<String, FieldBinding> bindings = new LinkedHashMap<>();
        bindings.put("dwbm", new FieldBinding("organizationCode", "maint-organization-name", "00"));
        bindings.put("grbm", new FieldBinding("personCode", "maint-person-code", "00"));
        bindings.put("xm", new FieldBinding("name", "maint-name", "00"));
        bindings.put("sfzh", new FieldBinding("idCard", "maint-id-card", "00"));
        bindings.put("xb", new FieldBinding("gender", "maint-gender", "00"));
        bindings.put("csny", new FieldBinding("birthYearMonth", "maint-birth-year-month", "00"));
        bindings.put("ryfl", new FieldBinding("personnelCategory", "maint-personnel-category", "00"));
        bindings.put("dwsx", new FieldBinding("organizationType", "maint-organization-type", "00"));
        bindings.put("gwfl", new FieldBinding("postCategory", "maint-post-category", "00"));
        bindings.put("cjgzny", new FieldBinding("workStartYearMonth", "maint-work-start", "00"));
        bindings.put("jrny", new FieldBinding("joinYearMonth", "maint-join-year-month", "00"));
        bindings.put("jrfs", new FieldBinding("joinType", "maint-join-type", "00"));
        bindings.put("zzny", new FieldBinding("regularizationYearMonth", "maint-regularization", "00"));
        bindings.put("gznx", new FieldBinding("salaryYears", "maint-salary-years", "00"));
        bindings.put("xlbm", new FieldBinding("educationCode", "maint-education-code", "00"));
        bindings.put("zgxl", new FieldBinding("highestEducation", "maint-highest-education", "00"));
        bindings.put("zwjb", new FieldBinding("currentPositionLevel", "maint-position-level", "00"));
        bindings.put("zjbm", new FieldBinding("currentRankCode", "maint-rank-code", "00"));
        bindings.put("xrzw", new FieldBinding("currentPosition", "maint-current-position", "00"));
        bindings.put("srny", new FieldBinding("currentPositionStartYearMonth", "maint-position-start", "00"));
        bindings.put("mz", new FieldBinding("ethnicity", "maint-ethnicity", "00"));
        bindings.put("zzmm", new FieldBinding("politicalStatus", "maint-political-status", "00"));
        bindings.put("dah", new FieldBinding("archiveNumber", "maint-archive-number", "00"));
        return Map.copyOf(bindings);
    }
}

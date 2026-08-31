package com.dxsoft.rsgzgl.organization;

import java.util.List;

final class OrganizationFieldCatalog {

    private OrganizationFieldCatalog() {
    }

    static List<OrganizationFieldOption> yearAllowanceCategories() {
        return List.of(
                new OrganizationFieldOption("0", ""),
                new OrganizationFieldOption("1", "一类"),
                new OrganizationFieldOption("2", "二类"),
                new OrganizationFieldOption("3", "三类"),
                new OrganizationFieldOption("4", "四类"));
    }

    static List<OrganizationFieldOption> performanceEnabledOptions() {
        return List.of(
                new OrganizationFieldOption("0", "否"),
                new OrganizationFieldOption("1", "是"));
    }

    static List<OrganizationFieldOption> performanceCategories() {
        return List.of(
                new OrganizationFieldOption("0", "无"),
                new OrganizationFieldOption("1", "公务员"),
                new OrganizationFieldOption("2", "义务教育学校"),
                new OrganizationFieldOption("3", "公共卫生事业单位"),
                new OrganizationFieldOption("4", "基层卫生事业单位"),
                new OrganizationFieldOption("5", "其它事业单位"),
                new OrganizationFieldOption("6", "工商质检药监"));
    }

    static String performanceCategoryLabel(Integer value) {
        if (value == null) {
            return "";
        }
        return performanceCategories().stream()
                .filter(option -> option.value().equals(String.valueOf(value)))
                .map(OrganizationFieldOption::label)
                .findFirst()
                .orElse(String.valueOf(value));
    }

    static List<OrganizationFieldOption> organizationLevels() {
        return List.of(
                new OrganizationFieldOption("正厅级", "正厅级"),
                new OrganizationFieldOption("副厅级", "副厅级"),
                new OrganizationFieldOption("正处级", "正处级"),
                new OrganizationFieldOption("副处级", "副处级"),
                new OrganizationFieldOption("正科级", "正科级"),
                new OrganizationFieldOption("副科级", "副科级"),
                new OrganizationFieldOption("股级", "股级"));
    }

    static List<OrganizationFieldOption> systemCategories() {
        return defaultSystemCategories().stream()
                .map(value -> new OrganizationFieldOption(value, value))
                .toList();
    }

    static List<String> defaultSystemCategories() {
        return List.of(
                "党委",
                "人大",
                "政府",
                "政协",
                "法院",
                "检察院",
                "民主党派",
                "群众团体",
                "其它");
    }

    /** Legacy dwbm.gzczbz values (VFP numeric 0–3 mapped to text). */
    static List<String> defaultPayrollCategoryValues() {
        return List.of("公务员管理", "参照公务员", "依照公务员", "事业管理");
    }

    /**
     * Options shown in dwxx by unit category ({@code dwbz}):
     * 行政 → 公务员管理、参照公务员；事业 → 事业管理、参照公务员。
     */
    static List<String> payrollCategoryValuesForUnitCategory(String unitCategory) {
        String category = unitCategory == null ? "" : unitCategory.trim();
        if ("行政".equals(category)) {
            return List.of("公务员管理", "参照公务员", "依照公务员");
        }
        if ("事业".equals(category)) {
            return List.of("事业管理", "参照公务员");
        }
        return defaultPayrollCategoryValues();
    }

    static List<OrganizationFieldOption> payrollCategories() {
        return defaultPayrollCategoryValues().stream()
                .map(value -> new OrganizationFieldOption(value, value))
                .toList();
    }

    /** Legacy dwbm.jfly values (VFP numeric 0–2 mapped to text). */
    static List<String> defaultFinanceSourceValues() {
        return List.of("全额拨款", "差额拨款", "自收自支");
    }

    /**
     * Options shown in dwxx by unit category ({@code dwbz}):
     * 行政 → 全额拨款；事业 → 全额拨款、差额拨款、自收自支。
     */
    static List<String> financeSourceValuesForUnitCategory(String unitCategory) {
        String category = unitCategory == null ? "" : unitCategory.trim();
        if ("行政".equals(category)) {
            return List.of("全额拨款");
        }
        if ("事业".equals(category)) {
            return List.of("全额拨款", "差额拨款", "自收自支");
        }
        return defaultFinanceSourceValues();
    }
}

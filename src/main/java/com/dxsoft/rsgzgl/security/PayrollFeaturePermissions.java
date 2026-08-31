package com.dxsoft.rsgzgl.security;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Fine-grained read/write permissions for payroll change modules. */
public final class PayrollFeaturePermissions {

    public record Feature(
            String apiPath,
            String permissionPrefix,
            String readLabel,
            String writeLabel,
            String menuCode) {

        public String readPermission() {
            return permissionPrefix + "_READ";
        }

        public String writePermission() {
            return permissionPrefix + "_WRITE";
        }
    }

    public static final String LEGACY_READ = "PAYROLL_READ";
    public static final String LEGACY_WRITE = "PAYROLL_WRITE";

    private static final List<Feature> FEATURES = List.of(
            feature("normal-promotions", "NORMAL_PROMOTION", "正常档次/薪级晋升查询", "正常档次/薪级晋升办理", "NORMAL_PROMOTION"),
            feature("level-promotions", "LEVEL_PROMOTION", "级别晋升查询", "级别晋升办理", "LEVEL_PROMOTION"),
            feature("reform-level-rollings", "LEVEL_PROMOTION", "级别晋升查询", "级别晋升办理", null),
            feature("position-change-promotions", "POSITION_CHANGE_PROMOTION", "职务变化晋升查询", "职务变化晋升办理", "POSITION_CHANGE_PROMOTION"),
            feature("disciplinary-demotion-promotions", "DISCIPLINARY_DEMOTION_PROMOTION", "处分降职办理查询", "处分降职办理", "DISCIPLINARY_DEMOTION_PROMOTION"),
            feature("new-personnel-salary-determinations", "NEW_PERSONNEL_SALARY", "新进定资查询", "新进定资办理", "NEW_PERSONNEL_SALARY"),
            feature("regularizations", "REGULARIZATION", "转正定级查询", "转正定级办理", "REGULARIZATION"),
            feature("education-promotions", "EDUCATION_PROMOTION", "学历晋升查询", "学历晋升办理", "EDUCATION_PROMOTION"),
            feature("teaching-allowance-adjustments", "TEACHING_ALLOWANCE_ADJUSTMENT", "调整教护龄津贴查询", "调整教护龄津贴办理", "TEACHING_ALLOWANCE_ADJUSTMENT"),
            feature("floating-to-fixed-conversions", "FLOATING_TO_FIXED", "浮动固定查询", "浮动固定办理", "FLOATING_TO_FIXED"),
            feature("other-payroll-changes", "OTHER_PAYROLL_CHANGE", "其它情况工资变动查询", "其它情况工资变动办理", "OTHER_PAYROLL_CHANGE"),
            feature("regularization-high-grades", "REGULARIZATION_HIGH_GRADE", "转正高定档次薪级查询", "转正高定档次薪级办理", "REGULARIZATION_HIGH_GRADE"),
            feature("wage-reforms-2006", "WAGE_REFORM_2006", "2006年工资套改查询", "2006年工资套改办理", "WAGE_REFORM_2006"),
            feature("prosecution-allowance-adjustments", "PROSECUTION_ALLOWANCE_ADJUSTMENT", "调整检察津贴查询", "调整检察津贴办理", "PROSECUTION_ALLOWANCE_ADJUSTMENT"),
            feature("judicial-allowance-adjustments", "JUDICIAL_ALLOWANCE_ADJUSTMENT", "调整审判津贴查询", "调整审判津贴办理", "JUDICIAL_ALLOWANCE_ADJUSTMENT"),
            feature("police-allowance-adjustments", "POLICE_ALLOWANCE_ADJUSTMENT", "调整警衔津贴查询", "调整警衔津贴办理", "POLICE_ALLOWANCE_ADJUSTMENT"),
            feature("supervision-allowance-adjustments", "SUPERVISION_ALLOWANCE_ADJUSTMENT", "调整监察津贴查询", "调整监察津贴办理", "SUPERVISION_ALLOWANCE_ADJUSTMENT"),
            feature("police-rank-change-promotions", "POLICE_RANK_CHANGE_PROMOTION", "警衔变化晋升查询", "警衔变化晋升办理", "POLICE_RANK_CHANGE_PROMOTION"),
            feature("prosecution-rank-change-promotions", "PROSECUTION_RANK_CHANGE_PROMOTION", "检察官等级变化晋升查询", "检察官等级变化晋升办理", "PROSECUTION_RANK_CHANGE_PROMOTION"),
            feature("judicial-rank-change-promotions", "JUDICIAL_RANK_CHANGE_PROMOTION", "法官等级变化晋升查询", "法官等级变化晋升办理", "JUDICIAL_RANK_CHANGE_PROMOTION"),
            feature("supervision-rank-change-promotions", "SUPERVISION_RANK_CHANGE_PROMOTION", "监察等级变化晋升查询", "监察等级变化晋升办理", "SUPERVISION_RANK_CHANGE_PROMOTION"),
            feature("intern-salary-changes", "INTERN_SALARY_CHANGE", "见习工资变动查询", "见习工资变动办理", "INTERN_SALARY_CHANGE"),
            feature("basic-salary-standard-adjustments", "BASIC_SALARY_STANDARD_ADJUSTMENT", "调整基本工资标准查询", "调整基本工资标准办理", "BASIC_SALARY_STANDARD_ADJUSTMENT"),
            feature("civil-allowance-standard-adjustments", "CIVIL_ALLOWANCE_STANDARD_ADJUSTMENT", "调整公务员津贴补贴查询", "调整公务员津贴补贴办理", "CIVIL_ALLOWANCE_STANDARD_ADJUSTMENT"),
            feature("performance-standard-adjustments", "PERFORMANCE_STANDARD_ADJUSTMENT", "调整绩效工资标准查询", "调整绩效工资标准办理", "PERFORMANCE_STANDARD_ADJUSTMENT"),
            feature("performance-ratio-adjustments", "PERFORMANCE_RATIO_ADJUSTMENT", "调整绩效比例查询", "调整绩效比例办理", "PERFORMANCE_RATIO_ADJUSTMENT"),
            feature("allowance-recalculations", "ALLOWANCE_RECALCULATION", "重算津补贴查询", "重算津补贴办理", "ALLOWANCE_RECALCULATION"),
            feature("monthly-average-salaries", "MONTHLY_AVERAGE_SALARY", "月平均工资计算查询", "月平均工资计算办理", "MONTHLY_AVERAGE_SALARY"),
            feature("salary-standard-adjustments", "SALARY_STANDARD_ADJUSTMENT", "工资标准调标查询", "工资标准调标办理", null));

    private PayrollFeaturePermissions() {
    }

    public static List<Feature> all() {
        return FEATURES;
    }

    public static Optional<Feature> byApiPath(String apiPath) {
        return FEATURES.stream().filter(feature -> feature.apiPath().equals(apiPath)).findFirst();
    }

    public static Optional<Feature> byWritePermission(String writePermission) {
        return FEATURES.stream().filter(feature -> feature.writePermission().equals(writePermission)).findFirst();
    }

    public static String[] readAuthorities(Feature feature) {
        return new String[] {feature.readPermission(), LEGACY_READ};
    }

    public static String[] writeAuthorities(Feature feature) {
        return new String[] {feature.writePermission()};
    }

    public static List<Feature> distinctPermissionFeatures() {
        Map<String, Feature> byPrefix = new LinkedHashMap<>();
        for (Feature feature : FEATURES) {
            byPrefix.putIfAbsent(feature.permissionPrefix(), feature);
        }
        return new ArrayList<>(byPrefix.values());
    }

    public static List<Feature> menuFeatures() {
        return FEATURES.stream().filter(feature -> feature.menuCode() != null).toList();
    }

    private static Feature feature(
            String apiPath, String permissionPrefix, String readLabel, String writeLabel, String menuCode) {
        return new Feature(apiPath, permissionPrefix, readLabel, writeLabel, menuCode);
    }

    public static boolean isLegacyWriteAuthority(String authority) {
        return LEGACY_WRITE.equals(authority)
                || FEATURES.stream().anyMatch(feature -> feature.writePermission().equals(authority));
    }

    public static String[] allWritePermissions() {
        return FEATURES.stream().map(Feature::writePermission).distinct().toArray(String[]::new);
    }
}

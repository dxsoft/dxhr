package com.dxsoft.rsgzgl.workflow;

import java.util.Locale;
import java.util.Set;

public enum PayrollWorkflowModule {
    NEW_PERSONNEL_SALARY("新增人员定资", "new-personnel-salary", Set.of("见习工资", "新进工资", "调入定资", "转业定资", "退伍定资")),
    EDUCATION_PROMOTION("学历晋升", "education-promotion", Set.of("学历变化", "学历变动", "学历晋升")),
    POSITION_CHANGE_PROMOTION("职务变化", "position-change-promotion", Set.of("职务变化", "职级晋升", "同序列职务变化", "事业岗位", "事业岗位变动", "转换序列", "法检套改", "警员套改", "警务套改", "职级套改")),
    NORMAL_PROMOTION("正常档次/薪级", "normal-promotion", Set.of("正常档次", "正常薪级", "正常晋档")),
    LEVEL_PROMOTION("正常级别", "level-promotion", Set.of("正常级别", "级别滚动")),
    POLICE_RANK_CHANGE_PROMOTION("警衔变化", "police-rank-change-promotion", Set.of("警衔变化")),
    PROSECUTION_RANK_CHANGE_PROMOTION("检察等级", "prosecution-rank-change-promotion", Set.of("检察等级")),
    JUDICIAL_RANK_CHANGE_PROMOTION("法官等级", "judicial-rank-change-promotion", Set.of("法官等级")),
    SUPERVISION_RANK_CHANGE_PROMOTION("监察等级", "supervision-rank-change-promotion", Set.of("监察等级")),
    REGULARIZATION("转正定级", "regularization", Set.of("转正定级"));

    private final String label;
    private final String actionTab;
    private final Set<String> changeTypes;

    PayrollWorkflowModule(String label, String actionTab, Set<String> changeTypes) {
        this.label = label;
        this.actionTab = actionTab;
        this.changeTypes = changeTypes;
    }

    public String label() {
        return label;
    }

    public String actionTab() {
        return actionTab;
    }

    public Set<String> changeTypes() {
        return changeTypes;
    }

    public static PayrollWorkflowModule fromChangeType(String changeType) {
        if (changeType == null || changeType.isBlank()) {
            return null;
        }
        String normalized = changeType.trim();
        for (PayrollWorkflowModule module : values()) {
            if (module.changeTypes.contains(normalized)) {
                return module;
            }
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        for (PayrollWorkflowModule module : values()) {
            for (String candidate : module.changeTypes) {
                if (lower.contains(candidate.toLowerCase(Locale.ROOT))) {
                    return module;
                }
            }
        }
        return null;
    }
}

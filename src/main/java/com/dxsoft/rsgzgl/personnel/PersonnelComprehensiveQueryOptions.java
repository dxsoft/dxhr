package com.dxsoft.rsgzgl.personnel;

import java.util.List;

public record PersonnelComprehensiveQueryOptions(
        List<CodeNameOption> personnelCategories,
        List<CodeNameOption> organizationTypes,
        List<CodeNameOption> postCategories,
        List<CodeNameOption> educations,
        List<CodeNameOption> positions) {

    public record CodeNameOption(String code, String name) {
    }
}

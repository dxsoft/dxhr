package com.dxsoft.rsgzgl.ops.license;

import java.util.List;

public record OpsLicenseIssueRequest(
        String organizationCode,
        String organizationName,
        String organizationLevel,
        String city,
        String supervisor,
        String expiresAt,
        String issuer,
        List<String> organizationCodes,
        /** null 签发时默认 true */
        Boolean ukeyEnabled,
        /** null 签发时默认 false */
        Boolean ukeyRequired,
        /** null 或 true：按编码前缀包含下属单位 */
        Boolean includeSubordinates,
        /** true：写入单位目录全部单位（显式全选） */
        Boolean includeAllOrganizations
) {
}

package com.dxsoft.rsgzgl.license;

public record LicenseIssueRequest(
        String organizationCode,
        String organizationName,
        String organizationLevel,
        String city,
        String supervisor,
        String expiresAt,
        String issuer,
        /** 为空时从本地 dwbm 加载 */
        java.util.List<LicenseOrganization> organizations,
        /** 从本地库按主体编码加载时是否包含前缀下属，默认 true；与 includeAllOrganizations 互斥时以后者为准 */
        Boolean includeSubordinates,
        /** 为 true 时写入本地 dwbm 全部单位作为初始种子，不要求挂在主体编码下 */
        Boolean includeAllOrganizations,
        /** 授权是否启用 UKey；null 签发时默认 true */
        Boolean ukeyEnabled,
        /** 授权是否默认要求双认证 UKey；null 签发时默认 false */
        Boolean ukeyRequired
) {
}

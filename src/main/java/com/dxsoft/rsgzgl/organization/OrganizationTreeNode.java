package com.dxsoft.rsgzgl.organization;

public record OrganizationTreeNode(
        String code,
        String name,
        String shortName,
        String parentCode
) {
}

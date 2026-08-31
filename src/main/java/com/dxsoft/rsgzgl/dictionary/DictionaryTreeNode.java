package com.dxsoft.rsgzgl.dictionary;

public record DictionaryTreeNode(
        String code,
        String value,
        String name,
        String parentCode,
        Boolean enabled
) {
}

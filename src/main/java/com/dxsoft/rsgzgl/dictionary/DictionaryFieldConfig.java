package com.dxsoft.rsgzgl.dictionary;

public record DictionaryFieldConfig(
        String tableName,
        String fieldName,
        String caption,
        String dictionaryPrefix
) {
}

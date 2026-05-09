package com.dxsoft.rsgzgl.dictionary;

public record DictionaryEntry(
        String code,
        String name,
        String parentCode,
        Integer systemFlag,
        Integer enabledFlag
) {
}

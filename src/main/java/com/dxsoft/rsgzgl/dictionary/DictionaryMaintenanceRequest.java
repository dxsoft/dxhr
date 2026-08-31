package com.dxsoft.rsgzgl.dictionary;

public record DictionaryMaintenanceRequest(
        String code,
        String name,
        String parentCode,
        Integer systemFlag,
        Integer enabledFlag
) {
}

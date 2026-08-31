package com.dxsoft.rsgzgl.common;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> of(List<T> content, PageRequest pageRequest, long totalElements) {
        int totalPages = pageRequest.size() == 0
                ? 0
                : (int) Math.ceil((double) totalElements / pageRequest.size());
        return new PageResponse<>(content, pageRequest.page(), pageRequest.size(), totalElements, totalPages);
    }
}

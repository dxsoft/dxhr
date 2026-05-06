package com.dxsoft.rsgzgl.common;

public record PageRequest(int page, int size) {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 200;

    public static PageRequest of(Integer page, Integer size) {
        int normalizedPage = page == null || page < 0 ? 0 : page;
        int normalizedSize = size == null || size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return new PageRequest(normalizedPage, normalizedSize);
    }

    public int offset() {
        return page * size;
    }
}

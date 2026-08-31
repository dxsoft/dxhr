package com.dxsoft.rsgzgl.personnel;

public final class PositionChangeReasons {

    public static final String NORMAL = "正常任免";
    public static final String DEMOTION_DISCIPLINARY = "降职处分";
    public static final String DISMISSAL_DISCIPLINARY = "撤职处分";
    public static final String OTHER = "其他";

    private PositionChangeReasons() {
    }

    public static boolean isDisciplinary(String positionChangeReason) {
        String normalized = positionChangeReason == null ? "" : positionChangeReason.trim();
        return DEMOTION_DISCIPLINARY.equals(normalized) || DISMISSAL_DISCIPLINARY.equals(normalized);
    }
}

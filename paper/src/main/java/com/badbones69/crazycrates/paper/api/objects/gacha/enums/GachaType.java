package com.badbones69.crazycrates.paper.api.objects.gacha.enums;

public enum GachaType {
    NORMAL,
    FATE_POINT,
    OVERRIDE;

    public static GachaType getType(boolean isFatePoint, boolean isOverride) {
        if (isFatePoint) return FATE_POINT;
        else if (isOverride) return OVERRIDE;
        else return NORMAL;
    }
}

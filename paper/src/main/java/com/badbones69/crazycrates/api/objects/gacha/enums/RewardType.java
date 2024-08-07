package com.badbones69.crazycrates.api.objects.gacha.enums;

public enum RewardType {
    STANDARD,
    LIMITED,
    EXTRA_REWARD,
    SHOP;

    public static RewardType fromString(String string) {
        for (RewardType rewardType : values()) {
            if (rewardType.name().equalsIgnoreCase(string)) {
                return rewardType;
            }
        }
        return null;
    }
}

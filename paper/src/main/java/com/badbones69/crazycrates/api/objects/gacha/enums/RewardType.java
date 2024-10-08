package com.badbones69.crazycrates.api.objects.gacha.enums;

import lombok.Getter;

@Getter
public enum RewardType {
    STANDARD("Standard"),
    LIMITED("Limited"),
    EXTRA_REWARD("Extra Reward"),
    SHOP("Shop");

    private final String name;

    RewardType(String name) {
        this.name = name;
    }

    public static RewardType fromString(String string) {
        for (RewardType rewardType : values()) {
            if (rewardType.name().equalsIgnoreCase(string)) {
                return rewardType;
            }
        }
        return null;
    }
}

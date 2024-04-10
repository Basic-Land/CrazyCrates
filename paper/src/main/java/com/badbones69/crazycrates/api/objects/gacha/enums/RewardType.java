package com.badbones69.crazycrates.api.objects.gacha.enums;

import lombok.Getter;

@Getter
public enum RewardType {
    STANDARD("AllItems"),
    LIMITED("AllItems"),
    EXTRA_REWARD("ExtraRewards");

    private final String tableName;

    RewardType(String tableName) {
        this.tableName = tableName;
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

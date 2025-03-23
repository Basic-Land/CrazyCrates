package com.badbones69.crazycrates.paper.api.objects.gacha.enums;

import lombok.Getter;

import java.util.Arrays;

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
        return Arrays.stream(values())
                .filter(rewardType -> rewardType.name().equalsIgnoreCase(string))
                .findFirst()
                .orElse(null);
    }
}

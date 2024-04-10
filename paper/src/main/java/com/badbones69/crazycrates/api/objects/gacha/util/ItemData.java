package com.badbones69.crazycrates.api.objects.gacha.util;

import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import org.bukkit.inventory.ItemStack;

public record ItemData(String rewardName, Rarity rarity, RewardType type, ItemStack itemStack) {
    @Override
    public String toString() {
        return "ItemData{" +
                "rewardName='" + rewardName + '\'' +
                ", rarity=" + rarity +
                ", type=" + type +
                ", itemStack=" + itemStack +
                '}';
    }
}

package com.badbones69.crazycrates.api.objects.gacha.util;

import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import cz.basicland.blibs.spigot.utils.item.CustomItemStack;

public record ItemData(Integer id, Rarity rarity, RewardType type, CustomItemStack itemStack) {
    @Override
    public String toString() {
        return "ItemData{" +
                "id=" + id +
                ", rarity=" + rarity +
                ", type='" + type + '\'' +
                ", itemStack=" + itemStack +
                '}';
    }
}

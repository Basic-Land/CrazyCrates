package com.badbones69.crazycrates.api.objects.gacha.shop;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ShopItem(ItemStack stack,
                       int price,
                       int limit,
                       int place)
        implements Comparable<ShopItem> {
    @Override
    public int compareTo(@NotNull ShopItem o) {
        return Integer.compare(place, o.place);
    }
}

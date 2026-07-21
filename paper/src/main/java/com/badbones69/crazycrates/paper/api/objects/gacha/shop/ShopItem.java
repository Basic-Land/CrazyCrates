package com.badbones69.crazycrates.paper.api.objects.gacha.shop;

import com.badbones69.crazycrates.paper.api.objects.gacha.enums.CurrencyType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ShopItem(ItemStack stack,
                       int price,
                       int limit,
                       int place,
                       int id,
                       String crate,
                       CurrencyType currencyType)
        implements Comparable<ShopItem> {

    @Override
    public int compareTo(@NotNull ShopItem o) {
        return Integer.compare(place, o.place);
    }

    public boolean isUnlimited() {
        return limit == -1;
    }

    @Override
    public String toString() {
        return "ShopItem{" +
                "stack=" + stack +
                ", price=" + price +
                ", limit=" + limit +
                ", place=" + place +
                ", id=" + id +
                ", crate='" + crate + '\'' +
                '}';
    }
}

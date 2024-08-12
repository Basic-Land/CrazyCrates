package com.badbones69.crazycrates.api.objects.gacha.shop;

import com.badbones69.crazycrates.api.objects.gacha.enums.CurrencyType;
import com.badbones69.crazycrates.api.objects.gacha.enums.ShopID;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ShopData(ShopID shopID,
                       CurrencyType currencyType,
                       String shopName,
                       List<ShopItem> items)
        implements Comparable<ShopData> {

    @Override
    public String toString() {
        return "ShopData{" +
                "shopID=" + shopID +
                ", currencyType=" + currencyType +
                ", shopName='" + shopName + '\'' +
                ", items=" + items +
                '}';
    }

    @Override
    public int compareTo(@NotNull ShopData o) {
        return shopID.compareTo(o.shopID);
    }
}

package com.badbones69.crazycrates.api.objects.gacha.shop;

import com.badbones69.crazycrates.api.objects.gacha.enums.CurrencyType;
import com.badbones69.crazycrates.api.objects.gacha.enums.ShopID;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public record ShopData(ShopID shopID,
                       CurrencyType currencyType,
                       String shopName,
                       List<ShopItem> items)
        implements Comparable<ShopData> {

    public Stream<ShopItem> sorted() {
        return items.stream().sorted();
    }

    public ShopItem getItemByDatabaseID(int id) {
        return items.stream().filter(item -> item.id() == id).findFirst().orElse(null);
    }

    public ShopItem getItemByPlace(int place) {
        return items.stream().filter(item -> item.place() == place).findFirst().orElse(null);
    }

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

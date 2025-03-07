package com.badbones69.crazycrates.api.objects.gacha.enums;

import java.util.Arrays;

public enum ShopID {
    VOTE_SHOP, // base
    MYSTIC_SHOP, // common
    STELLAR_SHOP; // rare

    public static ShopID getShopID(String name) {
        return Arrays.stream(values())
                .filter(shopID -> shopID.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}

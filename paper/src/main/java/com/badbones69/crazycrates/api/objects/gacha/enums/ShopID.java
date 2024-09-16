package com.badbones69.crazycrates.api.objects.gacha.enums;

public enum ShopID {
    VOTE_SHOP, // base
    MYSTIC_SHOP, // common
    STELLAR_SHOP; // rare

    public static ShopID getShopID(String name) {
        for (ShopID shopID : values()) {
            if (shopID.name().equalsIgnoreCase(name)) {
                return shopID;
            }
        }
        return null;
    }
}

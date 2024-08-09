package com.badbones69.crazycrates.api.objects.gacha.enums;

public enum CurrencyType {
    VOTE_TOKENS, // base
    MYSTIC_TOKENS, // common
    STELLAR_SHARDS, // rare
    PREMIUM_CURRENCY;

    public static CurrencyType getCurrencyType(String name) {
        for (CurrencyType currencyType : values()) {
            if (currencyType.name().equalsIgnoreCase(name) && !currencyType.equals(PREMIUM_CURRENCY)) {
                return currencyType;
            }
        }
        return null;
    }
}

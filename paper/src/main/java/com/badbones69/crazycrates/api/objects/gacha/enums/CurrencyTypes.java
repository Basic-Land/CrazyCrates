package com.badbones69.crazycrates.api.objects.gacha.enums;

public enum CurrencyTypes {
    VOTE_TOKENS, // base
    MYSTIC_TOKENS, // common
    STELLAR_SHARDS, // rare
    PREMIUM_CURRENCY; //shop

    public static CurrencyTypes getCurrencyType(String name) {
        for (CurrencyTypes currencyType : values()) {
            if (currencyType.name().equalsIgnoreCase(name) && !currencyType.equals(PREMIUM_CURRENCY)) {
                return currencyType;
            }
        }
        return null;
    }
}

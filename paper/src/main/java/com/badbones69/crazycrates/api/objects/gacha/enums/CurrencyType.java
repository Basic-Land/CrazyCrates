package com.badbones69.crazycrates.api.objects.gacha.enums;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum CurrencyType {
    VOTE_TOKENS("votetokeny", "vote tokenů"), // base
    MYSTIC_TOKENS("mystictokeny", "mystic tokenů"), // common - rarity
    STELLAR_SHARDS("stellarshardy", "stellar shardů"), // rare - epic and above
    PREMIUM_CURRENCY("premiummena", "premium měny"); // premium

    private final String name, fallback;

    CurrencyType(String name, String fallback) {
        this.name = name;
        this.fallback = fallback;
    }

    public String translateMM() {
        return MiniMessage.miniMessage().serialize(Component.translatable(name, fallback));
    }

    public static CurrencyType getCurrencyType(String name) {
        for (CurrencyType currencyType : values()) {
            if (currencyType.name().equalsIgnoreCase(name) && !currencyType.equals(PREMIUM_CURRENCY)) {
                return currencyType;
            }
        }
        return null;
    }
}

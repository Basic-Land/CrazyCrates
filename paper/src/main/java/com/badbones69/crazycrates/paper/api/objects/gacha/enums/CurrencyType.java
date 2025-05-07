package com.badbones69.crazycrates.paper.api.objects.gacha.enums;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Arrays;

@Getter
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
        return Arrays.stream(values()).filter(currencyType -> currencyType.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static CurrencyType getFromName(String name) {
        return Arrays.stream(values()).filter(currencyType -> currencyType.name.equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}

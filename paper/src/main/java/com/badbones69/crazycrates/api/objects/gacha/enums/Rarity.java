package com.badbones69.crazycrates.api.objects.gacha.enums;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@Getter
public enum Rarity {
    COMMON(NamedTextColor.GRAY),
    UNCOMMON(NamedTextColor.DARK_AQUA),
    RARE(NamedTextColor.YELLOW),
    EPIC(NamedTextColor.DARK_PURPLE),
    LEGENDARY(TextColor.fromHexString("#ff8259")),
    EXTRA_REWARD(NamedTextColor.BLACK);

    private final TextColor color;

    Rarity(TextColor color) {
        this.color = color;
    }

    public static Rarity getRarity(String name) {
        for (Rarity rarity : values()) {
            if (rarity.name().equalsIgnoreCase(name)) {
                return rarity;
            }
        }
        return null;
    }
}

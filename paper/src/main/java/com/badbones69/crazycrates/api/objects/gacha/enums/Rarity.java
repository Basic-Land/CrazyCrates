package com.badbones69.crazycrates.api.objects.gacha.enums;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@Getter
public enum Rarity {
    COMMON(NamedTextColor.GRAY, 1000002),
    UNCOMMON(NamedTextColor.DARK_AQUA, 1000003),
    RARE(NamedTextColor.YELLOW, 1000004),
    EPIC(NamedTextColor.DARK_PURPLE, 1000005),
    LEGENDARY(TextColor.fromHexString("#ffc13d"), 1000006),
    EXTRA_REWARD(NamedTextColor.BLACK, 1000007);

    private final TextColor color;
    private final int modelData;

    Rarity(TextColor color, int modelData) {
        this.color = color;
        this.modelData = modelData;
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

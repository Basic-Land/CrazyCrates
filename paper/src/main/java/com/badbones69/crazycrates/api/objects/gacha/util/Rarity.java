package com.badbones69.crazycrates.api.objects.gacha.util;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum Rarity {
    COMMON(NamedTextColor.GRAY),
    UNCOMMON(NamedTextColor.DARK_AQUA),
    RARE(NamedTextColor.YELLOW),
    EPIC(NamedTextColor.DARK_PURPLE),
    LEGENDARY(TextColor.fromHexString("#ff8259"));

    @Getter
    private final TextColor color;

    Rarity(TextColor color) {
        this.color = color;
    }
}

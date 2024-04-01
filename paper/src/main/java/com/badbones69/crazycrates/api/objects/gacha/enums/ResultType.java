package com.badbones69.crazycrates.api.objects.gacha.enums;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@Getter
public enum ResultType {
    LOST(NamedTextColor.RED),
    WON(NamedTextColor.GOLD),
    WON_OF_RATE_UP(TextColor.fromHexString("#8c26ad")),
    GUARANTEED(NamedTextColor.BLUE);
    private final TextColor color;

    ResultType(TextColor color) {
        this.color = color;
    }
}

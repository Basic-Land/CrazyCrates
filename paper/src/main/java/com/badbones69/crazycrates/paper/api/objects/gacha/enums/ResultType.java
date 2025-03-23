package com.badbones69.crazycrates.paper.api.objects.gacha.enums;

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

    public String getNext() {
        return switch (this) {
            case LOST -> "c";
            case GUARANTEED, WON -> "b";
            default -> "";
        };
    }

    public boolean isWon() {
        return this == WON;
    }

    public boolean isLost() {
        return this == LOST;
    }

    public boolean isGuaranteed() {
        return this == GUARANTEED;
    }

    public boolean isWonOfRateUp() {
        return this == WON_OF_RATE_UP;
    }
}

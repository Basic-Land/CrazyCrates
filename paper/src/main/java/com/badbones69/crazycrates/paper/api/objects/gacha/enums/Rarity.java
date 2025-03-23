package com.badbones69.crazycrates.paper.api.objects.gacha.enums;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.Arrays;

@Getter
public enum Rarity {
    COMMON(NamedTextColor.GRAY, 1000002, "Common"),
    UNCOMMON(NamedTextColor.DARK_AQUA, 1000003, "Uncommon"),
    RARE(NamedTextColor.YELLOW, 1000004, "Rare"),
    EPIC(NamedTextColor.DARK_PURPLE, 1000005, "Epic"),
    LEGENDARY(TextColor.fromHexString("#ffc13d"), 1000006, "Legendary"),
    EXTRA_REWARD(NamedTextColor.BLACK, 1000007, "Extra Reward");

    private final TextColor color;
    private final int modelData;
    private final String name;

    Rarity(TextColor color, int modelData, String name) {
        this.color = color;
        this.modelData = modelData;
        this.name = name;
    }

    public NumberType getNumberType() {
        return switch (this) {
            case LEGENDARY -> NumberType.LEGENDARY_PITY;
            case EPIC -> NumberType.EPIC_PITY;
            case RARE -> NumberType.RARE_PITY;
            case UNCOMMON -> NumberType.UNCOMMON_PITY;
            default -> NumberType.NONE;
        };
    }

    public static Rarity getRarity(String name) {
        return Arrays.stream(values())
                .filter(rarity -> rarity.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean isLegendary() {
        return this == LEGENDARY;
    }
}

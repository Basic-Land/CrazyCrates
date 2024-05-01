package com.badbones69.crazycrates.api.objects.gacha.ultimatemenu;

import com.badbones69.crazycrates.CrazyCrates;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.UUID;

@UtilityClass
public final class ComponentBuilder {
    private static final Key KEY = Key.key("minecraft", "spaces");
    private static final TextColor COLOR = NamedTextColor.WHITE;
    private static final Component SPACE_BACK = Component.translatable("space.-45", "").font(KEY);
    private static final Component SPACE_NEGATIVE = Component.translatable("space.-1", "").font(KEY);
    private static final Component SPACE_PAGE = Component.translatable("space.223", "").font(KEY);
    private static final Component FILL = Component.translatable("fill", "").color(COLOR).append(SPACE_NEGATIVE);
    private static final Component FILL_ = Component.translatable("fill_", "").color(COLOR).append(SPACE_NEGATIVE);

    public static Component trans(UUID uniqueId, String crateName, int mysticTokens, int stellarShards) {
        TextComponent.Builder builder = Component.text();
        int virtualKeys = CrazyCrates.getPlugin(CrazyCrates.class).getUserManager().getVirtualKeys(uniqueId, crateName);

        crateName = crateName + " ";

        int spaceSize = getSize(crateName) + 62;
        builder.append(Component.text(crateName));
        builder.append(Component.translatable("space.-" + spaceSize, "").font(KEY));

        String mystic = String.valueOf(mysticTokens);
        String stellar = String.valueOf(stellarShards);
        String virtual = String.valueOf(virtualKeys);

        int mysticSpace = mystic.length() * 6;
        int stellarSpace = stellar.length() * 6;
        int virtualSpace = virtual.length() * 6;

        for (int i = 0; i < mystic.length(); i++) {
            builder.append(sw(mystic.charAt(i), true));
        }

        down(builder, stellar, mysticSpace, FILL, SPACE_BACK);

        down(builder, virtual, stellarSpace, FILL_, SPACE_PAGE);

        while (virtualSpace < 45) {
            builder.append(FILL_);
            virtualSpace += 3;
        }

        return builder.build();
    }

    private static void down(TextComponent.Builder builder, String amount, int spaceLength, Component filler, Component space) {
        while (spaceLength < 45) {
            builder.append(filler);
            spaceLength += 3;
        }

        builder.append(space);

        for (int i = 0; i < amount.length(); i++) {
            builder.append(sw(amount.charAt(i), false));
        }
    }

    private static Component sw(char c, boolean top) {
        return Component.translatable(charSwap(c, top), "").color(COLOR).append(SPACE_NEGATIVE);
    }

    private static String charSwap(char c, boolean top) {
        return switch (c) {
            case '0' -> "num0";
            case '1' -> "num1";
            case '2' -> "num2";
            case '3' -> "num3";
            case '4' -> "num4";
            case '5' -> "num5";
            case '6' -> "num6";
            case '7' -> "num7";
            case '8' -> "num8";
            case '9' -> "num9";
            default -> c + "";
        } + (top ? "" : "_");
    }

    public static int getSize(String name) {
        int size = 0;
        boolean color = false;
        for (char c : name.toCharArray()) {
            if (c == '&') {
                color = true;
                continue;
            }

            if (color) {
                color = false;
                continue;
            }

            size += switch (c) {
                case 'q','w','e','r','z','u','o',
                     'p','a','s','d','g','h','j',
                     'y','x','c','v','b','n','m',
                     'Q','W','E','R','T','Z','U',
                     'O','P','A','S','D','F','G',
                     'H','J','K','L','Y','X','C',
                     'V','B','N','M','0','1','2',
                     '3','4','5','6','7','8','9',
                     '/','%','ě','š','č','ř','ž',
                     'ý','á','í','é','Ě','Š','Č',
                     'Ř','Ž','Ý','Á','Í','É' -> 6;
                case 't', 'I', ' ' -> 4;
                case 'i', ':' -> 2;
                case 'f','k' -> 5;
                case 'l', '│' -> 3;
                default -> 0;
            };
        }
        return size;
    }
}

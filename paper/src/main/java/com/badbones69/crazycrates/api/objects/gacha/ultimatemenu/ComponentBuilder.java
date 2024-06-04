package com.badbones69.crazycrates.api.objects.gacha.ultimatemenu;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@UtilityClass
public final class ComponentBuilder {
    private static final Key KEY = Key.key("minecraft", "spaces");
    private static final TextColor COLOR = NamedTextColor.WHITE;
    private static final Component SPACE_BACK = translatable("space.-45", "").font(KEY);
    private static final Component SPACE_NEGATIVE = translatable("space.-1", "").font(KEY);
    private static final Component SPACE_PAGE = translatable("space.223", "").font(KEY);
    private static final Component SPACE_TIME = translatable("space.-335", "").font(KEY);
    private static final Component FILL_TOP = translatable("fill_top", "").color(COLOR).append(SPACE_NEGATIVE);
    private static final Component FILL_DOWN = translatable("fill_down", "").color(COLOR).append(SPACE_NEGATIVE);
    private static final Component FILL_TIME = translatable("fill_time", "").color(COLOR).append(SPACE_NEGATIVE);

    public static Component trans(UUID uniqueId, CrateSettings crateSettings, int mysticTokens, int stellarShards) {
        TextComponent.Builder builder = text();
        String crateName = crateSettings.getCrateName();

        int virtualKeys = CrazyCrates.getPlugin(CrazyCrates.class).getUserManager().getVirtualKeys(uniqueId, crateName);

        crateName = crateName + " ";

        int spaceSize = getSize(crateName) + 62;
        builder.append(text(crateName));
        builder.append(translatable("space.-" + spaceSize, "").font(KEY));

        String mystic = String.valueOf(mysticTokens);
        String stellar = String.valueOf(stellarShards);
        String virtual = String.valueOf(virtualKeys);
        String time = crateSettings.getBannerPackage().getRemainingDuration();

        int mysticSpace = mystic.length() * 6;
        int stellarSpace = stellar.length() * 6;
        int virtualSpace = virtual.length() * 6;
        int timeSpace = getSize(time) - 2;

        for (int i = 0; i < mystic.length(); i++) {
            builder.append(sw(mystic.charAt(i), NumberType.TOP));
        }

        down(builder, stellar, mysticSpace, FILL_TOP, SPACE_BACK);

        down(builder, virtual, stellarSpace, FILL_DOWN, SPACE_PAGE);

        while (virtualSpace < 45) {
            builder.append(FILL_DOWN);
            virtualSpace += 3;
        }

        builder.append(SPACE_TIME);

        int middle = Math.ceilDiv(69 - timeSpace, 2);
        int left = middle;
        int right = middle;

        while (--left > 0) {
            builder.append(FILL_TIME);
        }

        for (int i = 0; i < time.length(); i++) {
            char c = time.charAt(i);
            builder.append(sw(c, NumberType.TIME));
            if (c == ' ') {
                builder.append(FILL_TIME).append(FILL_TIME);
            }
        }

        while (right-- > 0) {
            builder.append(FILL_TIME);
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
            builder.append(sw(amount.charAt(i), NumberType.DOWN));
        }
    }

    private static Component sw(char c, NumberType numberType) {
        return translatable(charSwap(c, numberType), "").color(COLOR).append(SPACE_NEGATIVE);
    }

    private static String charSwap(char c, NumberType numberType) {
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
            case 'd' -> "days";
            case 'h' -> "hours";
            case 'm' -> "minutes";
            case ' ' -> "fill";
            default -> c + "";
        } + switch (numberType) {
            case TOP -> "_top";
            case DOWN -> "_down";
            case TIME -> "_time";
        };
    }

    public static int getSize(String name) {
        int size = 0;
        boolean color = false;

        for (char c : name.toCharArray()) {
            if (c == '<') {
                color = true;
                continue;
            }

            if (c == '>') {
                color = false;
                continue;
            }

            if (color) {
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
                     'ý','á','é','Ě','Š','Č',
                     'Ř','Ž','Ý','Á','É' -> 6;
                case 't', 'I', ' ', 'Í' -> 4;
                case 'i', ':', '.' -> 2;
                case 'f','k' -> 5;
                case 'l', '│', 'í' -> 3;
                default -> 0;
            };
        }
        return size;
    }
}

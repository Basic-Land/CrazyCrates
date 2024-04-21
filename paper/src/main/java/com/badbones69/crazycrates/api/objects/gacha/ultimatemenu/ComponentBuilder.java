package com.badbones69.crazycrates.api.objects.gacha.ultimatemenu;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.text.DecimalFormat;

public class ComponentBuilder {
    public static Component trans(String crateName, int mysticTokens, int stellarShards) {
        TextComponent.Builder builder = Component.text();

        int space = 210 - getSize(crateName + " ");

        builder.append(Component.text(crateName + " "));
        builder.append(Component.translatable("space." + space, "").style(style -> style.font(Key.key("minecraft", "spaces"))));

        String mystic = formatShort(mysticTokens);
        String stellar = formatShort(stellarShards);

        for (int i = 0; i < mystic.length(); i++) {
            builder.append(sw(mystic.charAt(i)));
        }

        builder.append(Component.space());

        for (int i = 0; i < stellar.length(); i++) {
            builder.append(sw(stellar.charAt(i)));
        }

        return builder.build();
    }

    private static Component sw(char c) {
        return Component.translatable(charSwap(c), "").color(NamedTextColor.WHITE);
    }

    private static String charSwap(char c) {
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
            case ',' -> "comma";
            case 'k' -> "letterK";
            case 'M' -> "letterM";
            default -> c + "";
        };
    }

    private static int getSize(String name) {
        int size = 0;
        for (char c : name.toCharArray()) {
            size += switch (c) {
                case 'q','w','e','r','z','u','o',
                     'p','a','s','d','g','h','j',
                     'y','x','c','v','b','n','m','Q',
                     'W','E','R','T','Z','U','O','P',
                     'A','S','D','F','G','H','J','K','L',
                     'Y','X','C','V','B','N','M','0','1',
                     '2','3','4','5','6','7','8','9' -> 6;
                case 't', 'I', ' ' -> 4;
                case 'i' -> 2;
                case 'f','k' -> 5;
                case 'l' -> 3;
                default -> 0;
            };
        }
        return size;
    }

    private static String formatShort(int number) {
        if (number < 1000 || number > 1e9) return String.valueOf(number);
        String[] units = new String[] { "", "k", "M"};
        int digitGroups = (int) (Math.log10(number) / Math.log10(1000));
        return new DecimalFormat("#,##0.00").format(number / Math.pow(1000, digitGroups)) + units[digitGroups];
    }
}

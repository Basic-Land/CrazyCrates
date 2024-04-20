package com.badbones69.crazycrates.api.objects.gacha.ultimatemenu;

import com.badbones69.crazycrates.api.builders.types.items.UltimateMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class ComponentBuilder {
    public static Component trans(int mysticTokens, int stellarShards) {
        TextComponent.Builder builder = Component.text().color(NamedTextColor.WHITE);

        builder.append(Component.text("Gacha Crate Mystic: "));

        String mystic = formatShort(mysticTokens);
        String stellar = formatShort(stellarShards);

        for (int i = 0; i < mystic.length(); i++) {
            builder.append(sw(mystic.charAt(i)));
        }

        builder.append(Component.text(", Stellar: "));

        for (int i = 0; i < stellar.length(); i++) {
            builder.append(sw(stellar.charAt(i)));
        }

        return builder.build();
    }

    private static Component sw(char c) {
        return Component.translatable(charSwap(c), String.valueOf(c));
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
            default -> c + "";
        };
    }

    private static String formatShort(int number) {
        String[] units = new String[] { "", "k", "M", "B", "T"};
        int digitGroups = (int) (Math.log10(number) / Math.log10(1000));
        return new DecimalFormat("#,##0.00").format(number / Math.pow(1000, digitGroups)) + units[digitGroups];
    }

    public static void open(Player player) {
        UltimateMenu menu = new UltimateMenu(player, trans(1234567890, 1234567890));
        player.openInventory(menu.build().getInventory());
    }
}

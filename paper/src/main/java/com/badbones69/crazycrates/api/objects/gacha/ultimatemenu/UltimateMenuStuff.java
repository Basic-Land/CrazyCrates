package com.badbones69.crazycrates.api.objects.gacha.ultimatemenu;

import com.ryderbelserion.vital.util.builders.items.ItemBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;

public class UltimateMenuStuff {
    public static final ItemBuilder SELECTED = getItem(Material.GREEN_STAINED_GLASS_PANE, 1000002)
            .addDisplayLore("&7Právě vybraná truhla");
    public static final ItemBuilder UNSELECTED = getItem(Material.RED_STAINED_GLASS_PANE, 1000002)
            .addDisplayLore("&7Kliknutím vyberete tuto truhlu");
    public static final ItemBuilder MAIN_MENU = getItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1000001);
    public static final ItemBuilder BANNER = getItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1000002);
    public static final ItemBuilder ARROW_LEFT = getItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1000003);
    public static final ItemBuilder ARROW_RIGHT = getItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1000004);
    public static final ItemBuilder MAIN_MENU_NAME = getItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 0);
    public static final ItemBuilder BOOK = getItem(Material.BOOK, 10)
            .setDisplayName("&f&lHistorie truhly")
            .addDisplayLore("&7Kliknutím zobrazíš historii truhly");
    public static final ItemBuilder PAPER = getItem(Material.PAPER, 13)
            .setDisplayName("&f&lPreview Itemu")
            .addDisplayLore("&7Kliknutím zobrazíš všechny detaily")
            .addDisplayLore("&7o itemech v truhle");
    public static final ItemBuilder SHOP = getItem(Material.EMERALD, 1000001)
            .setDisplayName("&f&lObchod")
            .addDisplayLore("&7Kliknutím otevřeš obchod");
    public static final ItemBuilder BACK_ITEM = getItem(Material.ARROW, 1000002)
            .setDisplayName("&f&lZpět")
            .addDisplayLore("&7Kliknutím se vrátíš o stránku zpět");
    public static final ItemBuilder FORWARD = getItem(Material.ARROW, 1000002)
            .setDisplayName("&f&lVpřed")
            .addDisplayLore("&7Kliknutím se dostaneš o stránku vpřed");
    public static final ItemBuilder BUILDER_X1 = getItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1000001)
            .setDisplayName("&f&l1x")
            .addDisplayLore("&7Kliknutím otevřeš truhlu 1x");
    public static final ItemBuilder BUILDER_X10 = getItem(Material.BLUE_STAINED_GLASS_PANE, 1000001)
            .setDisplayName("&f&l10x")
            .addDisplayLore("&7Kliknutím otevřeš truhlu 10x");

    private static ItemBuilder getItem(Material material, int modelData) {
        return new ItemBuilder(material).setCustomModelData(modelData).setDisplayName("&f");
    }

    public static final Sound CLICK = Sound.sound(Key.key("crate", "click"), Sound.Source.MASTER, 1f, 1f);
    public static final Sound BACK = Sound.sound(Key.key("crate", "back"), Sound.Source.MASTER, 1f, 1f);
    public static final Sound ERROR = Sound.sound(Key.key("crate", "error"), Sound.Source.MASTER, 1f, 1f);
    public static final Sound CRATE = Sound.sound(Key.key("crate", "crate"), Sound.Source.MASTER, 1f, 1f);

}

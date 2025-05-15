package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu;

import com.badbones69.crazycrates.paper.api.builders.LegacyItemBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.inventory.ItemType;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ItemRepo {
    public static final LegacyItemBuilder SELECTED = getItem(ItemType.GREEN_STAINED_GLASS_PANE, -1)
            .addDisplayLore("<gray>Právě vybraná truhla");
    public static final LegacyItemBuilder UNSELECTED = getItem(ItemType.RED_STAINED_GLASS_PANE, -1)
            .addDisplayLore("<gray>Kliknutím vyberete tuto truhlu");
    public static final LegacyItemBuilder MAIN_MENU = getItem(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE, 1000001);
    public static final LegacyItemBuilder BANNER = getItem(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE, -1);
    public static final LegacyItemBuilder MAIN_MENU_NAME = getItem(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE, -1);
    public static final LegacyItemBuilder BOOK = getItem(ItemType.BOOK, 10)
            .setDisplayName("<white><b>Historie truhly")
            .addDisplayLore("<gray>Kliknutím zobrazíš historii truhly");
    public static final LegacyItemBuilder PAPER = getItem(ItemType.PAPER, 13)
            .setDisplayName("<white><b>Preview Itemu")
            .addDisplayLore("<gray>Kliknutím zobrazíš všechny detaily")
            .addDisplayLore("<gray>o itemech v truhle");
    public static final LegacyItemBuilder SHOP = getItem(ItemType.EMERALD, 1000001)
            .setDisplayName("<white><b>Obchod")
            .addDisplayLore("<gray>Kliknutím otevřeš obchod");
    public static final LegacyItemBuilder BACK_ITEM = getItem(ItemType.ARROW, 1000003)
            .setDisplayName("<white><b>Zpět")
            .addDisplayLore("<gray>Kliknutím se vrátíš o stránku zpět");
    public static final LegacyItemBuilder FORWARD = getItem(ItemType.ARROW, 1000004)
            .setDisplayName("<white><b>Vpřed")
            .addDisplayLore("<gray>Kliknutím se dostaneš o stránku vpřed");
    public static final LegacyItemBuilder BUILDER_X1 = getItem(ItemType.LIGHT_BLUE_STAINED_GLASS_PANE, 1000001)
            .setDisplayName("<white><b>1x")
            .addDisplayLore("<gray>Kliknutím otevřeš truhlu 1x");
    public static final LegacyItemBuilder BUILDER_X10 = getItem(ItemType.BLUE_STAINED_GLASS_PANE, 1000001)
            .setDisplayName("<white><b>10x")
            .addDisplayLore("<gray>Kliknutím otevřeš truhlu 10x");
    public static final LegacyItemBuilder PREMIUM_SHOP = getItem(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE, 1000008);
    public static final LegacyItemBuilder STORE_MENU = getItem(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE, 1000010);


    public static final LegacyItemBuilder SHOP_BANNER = getItem(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE, 1000007);
    public static final LegacyItemBuilder SHOP_SELECTED = getItem(ItemType.GREEN_STAINED_GLASS_PANE, -1)
            .addDisplayLore("<gray>Právě vybraný obchod");
    public static final LegacyItemBuilder SHOP_UNSELECTED = getItem(ItemType.RED_STAINED_GLASS_PANE, -1)
            .addDisplayLore("<gray>Kliknutím vyberete tento obchod");
    public static final LegacyItemBuilder MAIN_MENU_SHOP = getItem(ItemType.CHEST, 1000008)
            .setDisplayName("<green><b>Hlavní Menu");


    public static final LegacyItemBuilder SHOP_BACK_MENU = getItem(ItemType.RED_STAINED_GLASS_PANE, 1000001)
            .setDisplayName("<white><b>Zpět")
            .setDisplayLore(List.of(
                    "<white>Kliknutím se vrátíš zpět",
                    "<white>na hlavní menu"
            ));
    public static final LegacyItemBuilder SHOP_VOTE_PREMIUM_YES = getItem(ItemType.GREEN_STAINED_GLASS_PANE, 1000003)
            .setDisplayName("<white><b>Potvrdit")
            .setDisplayLore(List.of(
                    "<white>Kliknutím si koupíš <yellow>{keys}</yellow> otevření",
                    "<white>za <yellow>{premium}</yellow>{currency}"
            ));

    public static final LegacyItemBuilder OPEN_STORE = getItem(ItemType.GREEN_STAINED_GLASS_PANE, 1000003)
            .setDisplayName("<white><b>Potvrdit")
            .setDisplayLore(List.of(
                    "<white>Kliknutím obdržíš link",
                    "<white>do chatu na webový obchod"
            ));

    public static final LegacyItemBuilder BORDER = getItem(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE, 1000011);

    public static final LegacyItemBuilder PREVIEW_HEAD = getItem(ItemType.PLAYER_HEAD, 1000001)
            .setDisplayName("<green><b>Bonus pity cena")
            .addDisplayLore("<gray>Klikni pro otevření")
            .addDisplayLore("<gray>bonusového výběru ceny")
            .addDisplayLore("<gray>po dostatku otevření");

    public static final LegacyItemBuilder PREVIEW_INFO = getItem(ItemType.PAPER, 11)
            .setDisplayName("Info")
            .addDisplayLore("<gray>Zde najdeš informace o")
            .addDisplayLore("<gray>itemech a jejich šancích");

    public static final LegacyItemBuilder MAIN_MENU_BACK = getItem(ItemType.CHEST, 1000001)
            .setDisplayName("<green><b>Hlavní Menu");


    public static final Sound CLICK = Sound.sound(Key.key("crate", "click"), Sound.Source.MASTER, 1f, 1f);
    public static final Sound BACK = Sound.sound(Key.key("crate", "back"), Sound.Source.MASTER, 1f, 1f);
    public static final Sound ERROR = Sound.sound(Key.key("crate", "error"), Sound.Source.MASTER, 1f, 1f);
    public static final Sound CRATE = Sound.sound(Key.key("crate", "crate"), Sound.Source.MASTER, 1f, 1f);
    public static final Sound OPEN = Sound.sound(Key.key("crate", "open"), Sound.Source.MASTER, 1f, 1f);

    private static LegacyItemBuilder getItem(ItemType material, int modelData) {
        return new LegacyItemBuilder(material).setCustomModelData(modelData).setDisplayName("<white>");
    }

}

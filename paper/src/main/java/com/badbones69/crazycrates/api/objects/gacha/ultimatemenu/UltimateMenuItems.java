package com.badbones69.crazycrates.api.objects.gacha.ultimatemenu;

import com.badbones69.crazycrates.api.builders.ItemBuilder;
import org.bukkit.Material;

public class UltimateMenuItems {
    public static final ItemBuilder SELECTED = getItem(Material.GREEN_STAINED_GLASS_PANE, 1000002);
    public static final ItemBuilder UNSELECTED = getItem(Material.RED_STAINED_GLASS_PANE, 1000002);
    public static final ItemBuilder MAIN_MENU = getItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1000001);
    public static final ItemBuilder BANNER = getItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1000002);
    public static final ItemBuilder ARROW_LEFT = getItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1000003);
    public static final ItemBuilder ARROW_RIGHT = getItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1000004);
    public static final ItemBuilder BOOK = getItem(Material.BOOK, 10);
    public static final ItemBuilder PAPER = getItem(Material.PAPER, 13);
    public static final ItemBuilder SHOP = getItem(Material.EMERALD, 1000001);
    public static final ItemBuilder BACK = getItem(Material.ARROW, 1000002);
    public static final ItemBuilder FORWARD = getItem(Material.ARROW, 1000002);
    public static final ItemBuilder BUILDER_X1 = getItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1000001);
    public static final ItemBuilder BUILDER_X10 = getItem(Material.BLUE_STAINED_GLASS_PANE, 1000001);

    private static ItemBuilder getItem(Material material, int modelData) {
        return new ItemBuilder().setMaterial(material).setCustomModelData(modelData).setHasCustomModelData(true).setName("&f");
    }
}

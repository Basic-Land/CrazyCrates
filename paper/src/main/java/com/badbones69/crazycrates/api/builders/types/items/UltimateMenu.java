package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuItems;
import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public class UltimateMenu extends InventoryBuilder {
    private final ItemStack[] contents;
    private int totalPageAmount = 1;
    private int currentPage = 1;

    public UltimateMenu(Player player, Component trans) {
        super(player, 54, trans);
        PlayerInventory playerInventory = player.getInventory();
        contents = playerInventory.getContents();
        playerInventory.clear();
    }

    @Override
    public InventoryBuilder build() {
        List<List<CrateSettings>> gachaCrates = Lists.partition(plugin.getCrateManager().getDatabaseManager().getCrateSettings(), 3);
        totalPageAmount = gachaCrates.size();

        setTopCrates(gachaCrates);

        setTextureGlass();

        setItemsPlayerInv(totalPageAmount, currentPage);


        return this;
    }

    private void setTopCrates(List<List<CrateSettings>> gachaCrates) {
        int slot = 0;
        boolean first = false;

        ItemBuilder selectedMain = UltimateMenuItems.SELECTED;
        selectedMain.addLore("&7Právě vybraná truhla");

        ItemBuilder unselectedMain = UltimateMenuItems.UNSELECTED;
        unselectedMain.addLore("&7Kliknutím vyberete tuto truhlu");

        for (List<CrateSettings> settings : gachaCrates) {
            for (CrateSettings setting : settings) {
                if (setting == null) continue;

                selectedMain.setName("&a&l" + setting.getCrateName());
                unselectedMain.setName("&c&l" + setting.getCrateName());

                if (!first) {
                    first = true;
                    selectedMain.setCustomModelData(1000002);
                    getInventory().setItem(slot, selectedMain.build());

                    selectedMain.setCustomModelData(1000003);
                    getInventory().setItem(slot + 1, selectedMain.build());
                    getInventory().setItem(slot + 2, selectedMain.build());
                } else {
                    unselectedMain.setCustomModelData(1000002);
                    getInventory().setItem(slot, unselectedMain.build());

                    unselectedMain.setCustomModelData(1000001);
                    getInventory().setItem(slot + 1, unselectedMain.build());
                    getInventory().setItem(slot + 2, unselectedMain.build());
                }
                slot += 3;
            }
        }
    }

    private void setTextureGlass() {
        PlayerInventory playerInventory = getPlayer().getInventory();
        playerInventory.setItem(27, UltimateMenuItems.MAIN_MENU.build());
        playerInventory.setItem(28, UltimateMenuItems.BANNER.build());
        playerInventory.setItem(30, UltimateMenuItems.ARROW_LEFT.build());
        playerInventory.setItem(31, UltimateMenuItems.ARROW_RIGHT.build());
    }

    private void setItemsPlayerInv(int totalPageAmount, int currentPage) {
        PlayerInventory playerInventory = getPlayer().getInventory();

        playerInventory.setItem(0, UltimateMenuItems.BOOK.build());
        playerInventory.setItem(1, UltimateMenuItems.PAPER.build());
        playerInventory.setItem(2, UltimateMenuItems.SHOP.build());

        if (totalPageAmount > 1) {
            if (currentPage > 0) {
                playerInventory.setItem(3, UltimateMenuItems.BACK.build());
            }

            if (currentPage < totalPageAmount - 1) {
                playerInventory.setItem(4, UltimateMenuItems.FORWARD.build());
            }
        }

        ItemStack x1 = UltimateMenuItems.BUILDER_X1.build();
        playerInventory.setItem(5, x1);
        playerInventory.setItem(6, x1);

        ItemStack x10 = UltimateMenuItems.BUILDER_X10.build();
        playerInventory.setItem(7, x10);
        playerInventory.setItem(8, x10);
    }

    private ItemBuilder getItem(Material material, int modelData) {
        return new ItemBuilder().setMaterial(material).setCustomModelData(modelData).setHasCustomModelData(true).setName("&f");
    }

    public static class TestMenuListener implements Listener {
        @EventHandler
        public void Click(InventoryClickEvent e) {
            if (e.getInventory().getHolder() instanceof UltimateMenu) {
                e.setCancelled(true);
            }
        }

        @EventHandler
        public void Close(InventoryCloseEvent e) {
            if (e.getInventory().getHolder() instanceof UltimateMenu testMenu) {
                Player player = (Player) e.getPlayer();
                player.getInventory().setContents(testMenu.contents);
            }
        }

        @EventHandler
        public void pickUpItem(EntityPickupItemEvent e) {
            if (e.getEntity() instanceof Player player && player.getOpenInventory().getTopInventory().getHolder() instanceof UltimateMenu) {
                e.setCancelled(true);
            }
        }
    }
}

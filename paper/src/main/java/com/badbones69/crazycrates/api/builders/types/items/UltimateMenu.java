package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.ComponentBuilder;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuItems;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import us.crazycrew.crazycrates.api.enums.types.KeyType;

import java.util.List;

public class UltimateMenu extends InventoryBuilder {
    private final int selectedCrate;
    private final int totalPageAmount;
    private final List<List<CrateSettings>> gachaCrates;
    private int currentPage;

    public UltimateMenu(List<List<CrateSettings>> gachaCrates, Player player, Component trans, int page, int selectedCrate) {
        super(gachaCrates.get(page).get(selectedCrate).getCrate(), player, 54, trans);
        this.gachaCrates = gachaCrates;
        this.currentPage = page;
        this.selectedCrate = selectedCrate;

        this.totalPageAmount = gachaCrates.size();

        player.getInventory().clear();
    }

    public UltimateMenu(UltimateMenu ultimateMenu, Component title, int selectedCrate) {
        this(ultimateMenu.gachaCrates, ultimateMenu.getPlayer(), title, ultimateMenu.currentPage, selectedCrate);
    }


    @Override
    public InventoryBuilder build() {
        setTopCrates();

        setTextureGlass();

        setItemsPlayerInv();

        return this;
    }

    private void setTopCrates() {
        int slot = 0;
        int crate = 0;

        ItemBuilder selectedMain = UltimateMenuItems.SELECTED;
        ItemBuilder unselectedMain = UltimateMenuItems.UNSELECTED;
        ItemBuilder mainCrate = UltimateMenuItems.MAIN_MENU_NAME;

        for (CrateSettings setting : gachaCrates.get(currentPage)) {
            if (setting == null) continue;

            selectedMain.setName("&a&l" + setting.getCrateName());
            unselectedMain.setName("&c&l" + setting.getCrateName());

            if (crate == selectedCrate) {
                selectedMain.setCustomModelData(1000002);
                getInventory().setItem(slot, selectedMain.build());

                selectedMain.setCustomModelData(1000003);
                getInventory().setItem(slot + 1, selectedMain.build());
                getInventory().setItem(slot + 2, selectedMain.build());

                int model = setting.getModelDataMainMenu();
                mainCrate.setCustomModelData(model);
                ItemStack item = mainCrate.build();
                getPlayer().getInventory().setItem(29, item);
            } else {
                unselectedMain.setCustomModelData(1000002);
                getInventory().setItem(slot, unselectedMain.build());

                unselectedMain.setCustomModelData(1000001);
                getInventory().setItem(slot + 1, unselectedMain.build());
                getInventory().setItem(slot + 2, unselectedMain.build());
            }

            crate++;
            slot += 3;
        }
    }

    private void setTextureGlass() {
        PlayerInventory playerInventory = getPlayer().getInventory();
        playerInventory.setItem(27, UltimateMenuItems.MAIN_MENU.build());
        playerInventory.setItem(28, UltimateMenuItems.BANNER.build());
    }

    private void setItemsPlayerInv() {
        PlayerInventory playerInventory = getPlayer().getInventory();

        playerInventory.setItem(0, UltimateMenuItems.BOOK.build());
        playerInventory.setItem(1, UltimateMenuItems.PAPER.build());
        playerInventory.setItem(2, UltimateMenuItems.SHOP.build());

        if (totalPageAmount > 1) {
            if (currentPage > 0) {
                playerInventory.setItem(3, UltimateMenuItems.BACK.build());
                playerInventory.setItem(30, UltimateMenuItems.ARROW_LEFT.build());
            }

            if (currentPage < totalPageAmount - 1) {
                playerInventory.setItem(4, UltimateMenuItems.FORWARD.build());
                playerInventory.setItem(31, UltimateMenuItems.ARROW_RIGHT.build());
            }
        }

        ItemStack x1 = UltimateMenuItems.BUILDER_X1.build();
        playerInventory.setItem(5, x1);
        playerInventory.setItem(6, x1);

        ItemStack x10 = UltimateMenuItems.BUILDER_X10.build();
        playerInventory.setItem(7, x10);
        playerInventory.setItem(8, x10);
    }

    public static class TestMenuListener implements Listener {
        @EventHandler
        public void Click(InventoryClickEvent e) {
            if (!(e.getInventory().getHolder() instanceof UltimateMenu ultimateMenu)) return;

            e.setCancelled(true);
            int slot = e.getRawSlot();
            if (slot < 0) return;

            System.out.println("Slot: " + slot);

            CrazyCrates plugin = ultimateMenu.plugin;
            Player player = ultimateMenu.getPlayer();
            Crate crate = ultimateMenu.getCrate();

            CrateManager crateManager = plugin.getCrateManager();

            int newCrateNum;

            if (slot < 3) {
                newCrateNum = 0;
            } else if (slot < 6) {
                newCrateNum = 1;
            } else if (slot < 9) {
                newCrateNum = 2;
            } else newCrateNum = -1;

            if (newCrateNum != -1) {
                PlayerBaseProfile profile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());
                CrateSettings newCrate = ultimateMenu.gachaCrates.get(ultimateMenu.currentPage).get(newCrateNum);

                Component trans = ComponentBuilder.trans(newCrate.getCrateName(), profile.getMysticTokens(), profile.getStellarShards());

                player.openInventory(new UltimateMenu(ultimateMenu, trans, newCrateNum).build().getInventory());
            }

            switch (slot) {
                case 81 -> {
                    close(player);
                    crateManager.getDatabaseManager().getHistory().sendHistory(player, player.getName(), 1, crate.getCrateSettings());
                }
                case 82 -> {
                    close(player);
                    plugin.getInventoryManager().openNewCratePreview(player, crate);
                }
                case 83 -> {
                    //TODO: Open shop
                }
                case 84 -> {
                    //TODO: handle page back
                }
                case 85 -> {
                    //TODO: handle page forward
                }
                case 86, 87 -> {
                    close(player);
                    player.setSneaking(false);
                    crateManager.openCrate(player, crate, KeyType.virtual_key, null, false, false);
                }
                case 88, 89 -> {
                    close(player);
                    player.setSneaking(true);
                    crateManager.openCrate(player, crate, KeyType.virtual_key, null, false, false);
                }
            }
        }

        private void close(Player player) {
            player.closeInventory(InventoryCloseEvent.Reason.PLAYER);
        }

        @EventHandler
        public void Close(InventoryCloseEvent e) {
            if (e.getInventory().getHolder() instanceof UltimateMenu testMenu) {
                if (e.getReason().equals(InventoryCloseEvent.Reason.PLAYER)) {
                    testMenu.plugin.getCrateManager().getDatabaseManager().getUltimateMenuManager().remove(testMenu.getPlayer());
                }
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

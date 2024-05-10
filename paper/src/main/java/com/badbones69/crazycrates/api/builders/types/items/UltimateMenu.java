package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.enums.Messages;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.ComponentBuilder;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuStuff;
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
import org.bukkit.plugin.java.JavaPlugin;
import us.crazycrew.crazycrates.api.enums.types.KeyType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UltimateMenu extends InventoryBuilder {
    private static final DatabaseManager manager = JavaPlugin.getPlugin(CrazyCrates.class).getCrateManager().getDatabaseManager();
    private final int selectedCrate;
    private final int totalPageAmount;
    private int currentPage;

    public UltimateMenu(Crate crate, Player player, Component trans) {
        super(crate, player, 54, trans);
        player.getInventory().clear();

        int page = 0;
        int selectedCrate = 0;
        List<List<CrateSettings>> crateSettingsSplit = manager.getCrateSettingsSplit();

        for (int i = 0; i < crateSettingsSplit.size(); i++) {
            for (int j = 0; j < crateSettingsSplit.get(i).size(); j++) {
                if (crateSettingsSplit.get(i).get(j).getCrate().equals(crate)) {
                    page = i;
                    selectedCrate = j;
                    break;
                }
            }
        }

        this.currentPage = page;
        this.selectedCrate = selectedCrate;
        this.totalPageAmount = crateSettingsSplit.size();
    }

    public UltimateMenu(Player player, Component trans, int page, int selectedCrate) {
        super(manager.getCrateSettingsSplit().get(page).get(selectedCrate).getCrate(), player, 54, trans);
        this.currentPage = page;
        this.selectedCrate = selectedCrate;

        this.totalPageAmount = manager.getCrateSettingsSplit().size();

        player.getInventory().clear();
    }

    public UltimateMenu(UltimateMenu ultimateMenu, Component title, int selectedCrate) {
        this(ultimateMenu.getPlayer(), title, ultimateMenu.currentPage, selectedCrate);
    }


    @Override
    public InventoryBuilder build() {
        setTopCrates();

        setTextureGlass();

        setItemsPlayerInv();

        return this;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
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
            player.playSound(UltimateMenuStuff.CLICK);
            PlayerBaseProfile profile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());
            CrateSettings newCrate = manager.getCrateSettingsSplit().get(ultimateMenu.currentPage).get(newCrateNum);

            Component trans = ComponentBuilder.trans(player.getUniqueId(), newCrate.getCrateName(), profile.getMysticTokens(), profile.getStellarShards());

            player.openInventory(new UltimateMenu(ultimateMenu, trans, newCrateNum).build().getInventory());
        }

        switch (slot) {
            case 81 -> {
                player.playSound(UltimateMenuStuff.CLICK);
                close(player);
                manager.getHistory().sendHistory(player, player.getName(), 1, crate.getCrateSettings());
            }
            case 82 -> {
                player.playSound(UltimateMenuStuff.CLICK);
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
                open(ultimateMenu, player, crate, crateManager, 1);
            }
            case 88, 89 -> {
                close(player);
                player.setSneaking(true);
                open(ultimateMenu, player, crate, crateManager, 10);
            }
        }
    }

    private void open(UltimateMenu ultimateMenu, Player player, Crate crate, CrateManager crateManager, int size) {
        if (ultimateMenu.hasKeys(player, crate, size)) {
            player.playSound(UltimateMenuStuff.CRATE);
            crateManager.openCrate(player, crate, KeyType.virtual_key, null, false, false);
        } else {
            message(crate, player);
        }
    }

    private void setTopCrates() {
        int slot = 0;
        int crate = 0;

        ItemBuilder selectedMain = UltimateMenuStuff.SELECTED;
        ItemBuilder unselectedMain = UltimateMenuStuff.UNSELECTED;
        ItemBuilder mainCrate = UltimateMenuStuff.MAIN_MENU_NAME;

        for (CrateSettings setting : manager.getCrateSettingsSplit().get(currentPage)) {
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
        playerInventory.setItem(27, UltimateMenuStuff.MAIN_MENU.build());
        playerInventory.setItem(28, UltimateMenuStuff.BANNER.build());
    }

    private void setItemsPlayerInv() {
        PlayerInventory playerInventory = getPlayer().getInventory();

        playerInventory.setItem(0, UltimateMenuStuff.BOOK.build());
        playerInventory.setItem(1, UltimateMenuStuff.PAPER.build());
        playerInventory.setItem(2, UltimateMenuStuff.SHOP.build());

        if (totalPageAmount > 1) {
            if (currentPage > 0) {
                playerInventory.setItem(3, UltimateMenuStuff.BACK_ITEM.build());
                playerInventory.setItem(30, UltimateMenuStuff.ARROW_LEFT.build());
            }

            if (currentPage < totalPageAmount - 1) {
                playerInventory.setItem(4, UltimateMenuStuff.FORWARD.build());
                playerInventory.setItem(31, UltimateMenuStuff.ARROW_RIGHT.build());
            }
        }

        ItemStack x1 = UltimateMenuStuff.BUILDER_X1.build();
        playerInventory.setItem(5, x1);
        playerInventory.setItem(6, x1);

        ItemStack x10 = UltimateMenuStuff.BUILDER_X10.build();
        playerInventory.setItem(7, x10);
        playerInventory.setItem(8, x10);
    }

    private boolean hasKeys(Player player, Crate crate, int amount) {
        return plugin.getUserManager().getVirtualKeys(player.getUniqueId(), crate.getName()) >= amount;
    }

    private void message(Crate crate, Player player) {
        player.playSound(UltimateMenuStuff.ERROR);

        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("{crate}", crate.getName());
        placeholders.put("{key}", crate.getKeyName());

        player.sendMessage(Messages.no_keys.getMessage(placeholders, player));
    }

    private void close(Player player) {
        player.closeInventory(InventoryCloseEvent.Reason.PLAYER);
    }

    public static class TestMenuListener implements Listener {
        @EventHandler
        public void Close(InventoryCloseEvent e) {
            if (e.getInventory().getHolder() instanceof UltimateMenu testMenu && e.getReason().equals(InventoryCloseEvent.Reason.PLAYER)) {
                testMenu.plugin.getCrateManager().getDatabaseManager().getUltimateMenuManager().remove(testMenu.getPlayer());
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

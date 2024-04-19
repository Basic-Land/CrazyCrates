package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import cz.basicland.blibs.spigot.utils.item.NBT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CratePickPrizeMenu extends InventoryBuilder {
    private final ItemStack item;
    private final DatabaseManager databaseManager = plugin.getCrateManager().getDatabaseManager();
    private final static ItemStack back = new ItemBuilder().setMaterial(Material.RED_STAINED_GLASS_PANE).setName("Back").build();
    private final static ItemStack save = new ItemBuilder().setMaterial(Material.GREEN_STAINED_GLASS_PANE).setName("Save").build();

    public CratePickPrizeMenu(Player player, ItemStack item, Crate crate) {
        super(crate, player, 9, "Item Menu");
        this.item = item;
    }

    @Override
    public InventoryBuilder build() {
        for (int i = 0; i < 9; i++) {
            if (i < 4) getInventory().setItem(i, back);
            else if (i == 4) getInventory().setItem(i, item);
            else getInventory().setItem(i, save);
        }
        return this;
    }

    public static class PickPrizeListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            Inventory inventory = event.getInventory();

            if (!(inventory.getHolder(false) instanceof CratePickPrizeMenu holder)) return;

            event.setCancelled(true);

            Player player = holder.getPlayer();
            Crate crate = holder.getCrate();
            CrateSettings crateSettings = crate.getCrateSettings();

            ItemStack item = event.getCurrentItem();

            if (item == null || item.getType() == Material.AIR) return;

            if (!item.hasItemMeta()) return;

            ItemStack picked = inventory.getItem(4);
            if (picked == null || picked.getType() == Material.AIR) return;

            NBT nbt = new NBT(picked);

            if (event.getSlot() < 4) {
                // Open the previous menu
                player.openInventory(crate.getTierPreview(player));
            } else if (event.getSlot() > 4) {
                // Retrieve the player's profile and save the chosen reward
                PlayerProfile playerProfile = holder.databaseManager.getPlayerProfile(player.getName(), crateSettings, false);

                String rewardName = nbt.getString("rewardName");

                System.out.println("Chosen reward: " + rewardName);

                playerProfile.setChosenReward(rewardName);
                holder.databaseManager.savePlayerProfile(player.getName(), crateSettings, playerProfile);
                player.openInventory(crate.getTierPreview(player));
            }
        }
    }
}

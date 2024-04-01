package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.CrazyCratesPaper;
import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import cz.basicland.blibs.spigot.utils.item.DBItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class ItemAddMenu extends InventoryBuilder {
    private final Crate crate;
    private final Rarity rarity;
    private final String type;

    public ItemAddMenu(Player player, int size, String title, Crate crate, Rarity rarity, String type) {
        super(player, size, title);
        this.crate = crate;
        this.rarity = rarity;
        this.type = type;
    }

    @Override
    public InventoryBuilder build() {
        Inventory inventory = getInventory();
        ItemBuilder builder = new ItemBuilder(new ItemStack(Material.CHEST));
        builder.setName("&aSave Items");
        builder.addLore("");
        builder.addLore("&7&lRight click to close.");
        builder.addLore("&7&lLeft click to save.");

        inventory.setItem(53, builder.getItemStack());
        return this;
    }

    public static class ItemsAddListener implements Listener {
        private final DatabaseManager databaseManager = CrazyCratesPaper.get().getCrateManager().getDatabaseManager();
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            Inventory inventory = event.getInventory();

            int slot = event.getSlot();
            ClickType click = event.getClick();

            if (!(inventory.getHolder(false) instanceof ItemAddMenu holder)) return;

            if (slot == 53) {
                event.setCancelled(true);
                if (click == ClickType.RIGHT) {
                    holder.getPlayer().closeInventory();
                } else if (click == ClickType.LEFT) {
                    inventory.clear(53);
                    saveItems(inventory.getStorageContents(), holder);
                    holder.getPlayer().sendMessage("Items saved.");
                    holder.getPlayer().closeInventory();
                }
            }
        }

        private void saveItems(ItemStack[] items, ItemAddMenu holder) {
            System.out.println("Saving items");
            Crate crate = holder.crate;
            Rarity rarity = holder.rarity;
            String type = holder.type;
            CrateSettings crateSettings = crate.getCrateSettings();

            String tableName = switch (type.toLowerCase()) {
                case "standard" -> "StandardItems";
                case "limited" -> "LimitedItems";
                case "extra_reward" -> "ExtraRewards";
                default -> null;
            };
            for (ItemStack item : items) {
                if (item == null || item.getType() == Material.AIR) continue;
                try {
                    int id = databaseManager.addItem(tableName, DBItemStack.encodeItem(item));
                    crateSettings.addItem(type, id, rarity, item);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

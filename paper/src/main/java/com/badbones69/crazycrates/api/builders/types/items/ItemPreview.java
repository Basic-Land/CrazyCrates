package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.CrazyCratesPaper;
import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.objects.gacha.ItemManager;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemPreview extends InventoryBuilder {
    private final String tableName;
    private final ItemManager itemManager;
    private int page;

    public ItemPreview(Player player, int size, String title, RewardType type) {
        super(player, size, title);
        tableName = switch (type) {
            case STANDARD -> "StandardItems";
            case LIMITED -> "LimitedItems";
            case EXTRA_REWARD -> "ExtraRewards";
        };

        this.itemManager = CrazyCratesPaper.get().getCrateManager().getDatabaseManager().getItemManager();
        this.page = 0;
    }

    @Override
    public InventoryBuilder build() {
        List<ItemStack> items = itemManager.getAllItems(tableName).values().stream().toList();
        int totalPages = (int) Math.ceil((double) items.size() / getSize());

        // Clear the inventory
        getInventory().clear();

        // Add items to the inventory
        int i = 0;
        for (ItemStack item : Lists.partition(items, 45).get(page)) {
            getInventory().setItem(i++, item);
        }

        // Add page navigation items
        if (page > 0) {
            // Add previous page item
            getInventory().setItem(getSize() - 9, new ItemStack(Material.ARROW)); // Replace with your own item
        }
        if (page < totalPages - 1) {
            // Add next page item
            getInventory().setItem(getSize() - 1, new ItemStack(Material.ARROW)); // Replace with your own item
        }

        return this;
    }

    public static class ItemPreviewListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            Inventory inventory = event.getInventory();

            if (!(inventory.getHolder(false) instanceof ItemPreview holder)) return;

            int slot = event.getSlot();
            event.setCancelled(true);

            if (slot == holder.getSize() - 9 && holder.page > 0) {
                // Previous page
                holder.page--;
                holder.build();
            } else if (slot == holder.getSize() - 1 && holder.page < (int) Math.ceil((double) holder.itemManager.getAllItems(holder.tableName).size() / holder.getSize()) - 1) {
                // Next page
                holder.page++;
                holder.build();
            } else {

            }
        }
    }
}

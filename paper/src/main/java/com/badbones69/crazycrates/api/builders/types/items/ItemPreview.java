package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.objects.gacha.ItemManager;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import com.google.common.collect.Lists;
import cz.basicland.blibs.spigot.utils.item.NBT;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ItemPreview extends InventoryBuilder {
    private final List<Pair<Integer, ItemStack>> items;
    @Getter
    private final RewardType type;
    private int page = 0;

    public ItemPreview(Player player, int size, String title, RewardType type) {
        super(player, size, title);
        this.type = type;
        ItemManager itemManager = JavaPlugin.getPlugin(CrazyCrates.class).getCrateManager().getDatabaseManager().getItemManager();
        items = itemManager.getAllItemsFromCache(type).entrySet().stream()
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public InventoryBuilder build() {
        int totalPages = (int) Math.ceil((double) items.size() / getSize());

        // Clear the inventory
        getInventory().clear();

        // Add items to the inventory
        int i = 0;
        for (Pair<Integer, ItemStack> item : Lists.partition(items, 45).get(page)) {
            ItemStack itemStack = item.second();
            NBT nbt = new NBT(itemStack);
            nbt.setInteger("itemID", item.first());
            getInventory().setItem(i++, itemStack);
        }

        // Add page navigation items
        if (page > 0) {
            // Add previous page item
            getInventory().setItem(getSize() - 9, new ItemStack(Material.ARROW));
        }
        if (page < totalPages - 1) {
            // Add next page item
            getInventory().setItem(getSize() - 1, new ItemStack(Material.ARROW));
        }

        return this;
    }

    public static class ItemPreviewListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            Inventory inventory = event.getInventory();

            if (!(inventory.getHolder(false) instanceof ItemPreview holder)) return;

            Player player = holder.getPlayer();
            int slot = event.getSlot();
            event.setCancelled(true);

            if (slot == holder.getSize() - 9 && holder.page > 0) {
                // Previous page
                holder.page--;
                holder.build();
            } else if (slot == holder.getSize() - 1 && holder.page < (int) Math.ceil((double) holder.items.size() / holder.getSize()) - 1) {
                // Next page
                holder.page++;
                holder.build();
            } else if (slot < holder.getSize() - 9 && slot >= 0) {
                player.openInventory(new ItemEdit(holder, player, 27, "Edit Item", event.getCurrentItem()).build().getInventory());
            }
        }
    }
}

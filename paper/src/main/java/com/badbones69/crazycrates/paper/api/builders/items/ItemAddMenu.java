package com.badbones69.crazycrates.paper.api.builders.items;

import com.badbones69.crazycrates.paper.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.paper.api.builders.LegacyItemBuilder;
import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.gacha.ItemManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.Table;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.util.Arrays;

public class ItemAddMenu extends InventoryBuilder {
    private final RewardType type;

    public ItemAddMenu(Player player, int size, String title, Crate crate, RewardType type) {
        super(player, title, size, crate);
        this.type = type;
    }

    @Override
    public InventoryBuilder build() {
        Inventory inventory = getInventory();
        LegacyItemBuilder builder = new LegacyItemBuilder(plugin, ItemType.CHEST);
        builder.setDisplayName("<green>Save Items");
        builder.addDisplayLore("");
        builder.addDisplayLore("<gray><b>Right click to close.");
        builder.addDisplayLore("<gray><b>Left click to save.");

        inventory.setItem(53, builder.asItemStack());
        return this;
    }

    @Override
    public void run(InventoryClickEvent event) {
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
        RewardType type = holder.type;
        ItemManager itemManager = holder.plugin.getCrateManager().getDatabaseManager().getItemManager();
        if (type.equals(RewardType.SHOP)) {
            Arrays.stream(items).filter(item -> item != null && item.getType() != Material.AIR).forEach(item -> itemManager.addItem(item, Table.SHOP_ITEMS));
            return;
        }
        Arrays.stream(items).filter(item -> item != null && item.getType() != Material.AIR).forEach(item -> itemManager.addItem(item, Table.ALL_ITEMS));
    }
}

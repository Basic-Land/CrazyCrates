package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.ryderbelserion.vital.util.builders.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemAddMenu extends InventoryBuilder {
    private final Rarity rarity;
    private final RewardType type;

    public ItemAddMenu(Player player, int size, String title, Crate crate, Rarity rarity, RewardType type) {
        super(player, title, size, crate);
        this.rarity = rarity;
        this.type = type;
    }

    @Override
    public InventoryBuilder build() {
        Inventory inventory = getInventory();
        ItemBuilder builder = new ItemBuilder(new ItemStack(Material.CHEST));
        builder.setDisplayName("&aSave Items");
        builder.addDisplayLore("");
        builder.addDisplayLore("&7&lRight click to close.");
        builder.addDisplayLore("&7&lLeft click to save.");

        inventory.setItem(53, builder.getStack());
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
        Crate crate = holder.getCrate();
        Rarity rarity = holder.rarity;
        RewardType type = holder.type;
        CrateSettings crateSettings = crate.getCrateSettings();

        String path;
        if (type.equals(RewardType.EXTRA_REWARD)) {
            path = "Crate.Gacha.extra-reward.items";
        } else {
            path = "Crate.Gacha." + type.name().toLowerCase() + "." + rarity.name().toLowerCase() + ".list";
        }

        List<Integer> ids = crate.getFile().getIntegerList(path);

        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;

            int id = holder.plugin.getCrateManager().getDatabaseManager().getItemManager().addItem(item);
            if (id == -1) continue;

            crateSettings.addItem(type, id, rarity, item, crate);

            ids.add(id);
        }

        crate.getFile().set(path, ids);
        crate.saveFile();
    }
}

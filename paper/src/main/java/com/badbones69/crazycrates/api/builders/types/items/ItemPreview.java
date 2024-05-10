package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.ItemManager;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import com.google.common.collect.Lists;
import cz.basicland.blibs.spigot.utils.item.NBT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.stream.Collectors;

public class ItemPreview extends InventoryBuilder {
    private final List<Pair<Integer, ItemStack>> items;
    private final RaritiesMenu raritiesMenu;
    private final RewardType type;
    private final boolean leftClick;
    private final boolean editing;
    private final Rarity rarity;
    private int page = 0;

    public ItemPreview(Player player, int size, String title, RewardType type) {
        super(player, size, title);
        this.type = type;
        this.editing = false;
        this.rarity = null;
        this.raritiesMenu = null;
        this.leftClick = true;
        ItemManager itemManager = JavaPlugin.getPlugin(CrazyCrates.class).getCrateManager().getDatabaseManager().getItemManager();
        items = itemManager.getAllItemsFromCache().entrySet().stream()
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue().clone()))
                .collect(Collectors.toList());
    }

    public ItemPreview(RaritiesMenu raritiesMenu, int size, String title, RewardType type, Rarity rarity, List<Integer> ids, boolean leftClick) {
        super(raritiesMenu.getCrate(), raritiesMenu.getPlayer(), size, title);
        this.raritiesMenu = raritiesMenu;
        this.type = type;
        this.leftClick = leftClick;
        this.editing = true;
        this.rarity = rarity;
        ItemManager itemManager = JavaPlugin.getPlugin(CrazyCrates.class).getCrateManager().getDatabaseManager().getItemManager();
        items = itemManager.getAllItemsFromCache().entrySet().stream()
                .filter(entry -> leftClick != ids.contains(entry.getKey()))
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue().clone()))
                .collect(Collectors.toList());
    }

    @Override
    public InventoryBuilder build() {
        if (items.isEmpty()) {
            add();
            return this;
        }

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
            ItemBuilder previousPage = new ItemBuilder().setMaterial(Material.ARROW).setName("&aPrevious Page");
            // Add previous page item
            getInventory().setItem(getSize() - 9, previousPage.build());
        }

        if (page < totalPages - 1) {
            ItemBuilder nextPage = new ItemBuilder().setMaterial(Material.ARROW).setName("&aNext Page");
            // Add next page item
            getInventory().setItem(getSize() - 1, nextPage.build());
        }

        add();

        return this;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof ItemPreview holder)) return;

        Player player = holder.getPlayer();
        int slot = event.getSlot();
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (slot == holder.getSize() - 9 && holder.page > 0) {
            // Previous page
            holder.page--;
            holder.build();
        } else if (slot == holder.getSize() - 1 && holder.page < (int) Math.ceil((double) holder.items.size() / holder.getSize()) - 1) {
            // Next page
            holder.page++;
            holder.build();
        } else if (slot < holder.getSize() - 9 && slot >= 0) {
            if (holder.editing && holder.raritiesMenu != null && holder.rarity != null) {
                Crate crate = holder.getCrate();
                RewardType type = holder.type;
                Rarity rarity = holder.rarity;
                int id = new NBT(item).getInteger("itemID");

                crate.getCrateSettings().addItem(type, id, rarity, item, crate);

                boolean extra = type.equals(RewardType.EXTRA_REWARD);
                String base = "Crate.Gacha.";
                String path = extra ? base + "extra-reward.items" : base + type.name().toLowerCase() + "." + rarity.name().toLowerCase() + ".list";

                List<Integer> ids = crate.getFile().getIntegerList(path);

                if (holder.leftClick) {
                    ids.add(id);
                } else if (ids.contains(id)) {
                    ids.remove((Integer) id);
                } else if (!extra) {
                    crate.getFile().set(base + type.name().toLowerCase() + "." + rarity.name().toLowerCase() + "." + id, null);
                } else {
                    throw new IllegalStateException("Detected type EXTRA_REWARD this shouldn't happen.");
                }

                if (!extra) crate.getFile().set(path, ids);

                crate.saveFile();
                player.openInventory(holder.raritiesMenu.getInventory());
            } else {
                player.openInventory(new ItemEdit(holder, player, 27, "Edit Item", item).build().getInventory());
            }
        } else if (slot == holder.getSize() - 5 && holder.editing && holder.raritiesMenu != null && holder.rarity != null) {
            player.openInventory(holder.raritiesMenu.getInventory());
        }
    }

    private void add() {
        if (editing) {
            ItemBuilder back = new ItemBuilder().setMaterial(Material.CHEST).setName("&aGo back");
            getInventory().setItem(getSize() - 5, back.build());
        }
    }
}

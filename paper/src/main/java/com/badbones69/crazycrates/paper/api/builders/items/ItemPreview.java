package com.badbones69.crazycrates.paper.api.builders.items;

import com.badbones69.crazycrates.paper.CrazyCrates;
import com.badbones69.crazycrates.paper.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.paper.api.builders.LegacyItemBuilder;
import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.gacha.ItemManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.Table;
import com.badbones69.crazycrates.paper.api.objects.gacha.util.Pair;
import com.google.common.collect.Lists;
import cz.basicland.blibs.spigot.utils.item.NBT;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ItemPreview extends InventoryBuilder {
    private final List<Pair<Integer, ItemStack>> items;
    private final RaritiesMenu raritiesMenu;
    private final RewardType type;
    private final boolean leftClick;
    private final boolean editing;
    private final Rarity rarity;
    private final Table table;
    private int page = 0;

    public ItemPreview(Player player, int size, String title, Table type) {
        super(player, title, size);
        this.type = null;
        this.editing = false;
        this.rarity = null;
        this.raritiesMenu = null;
        this.leftClick = true;
        this.table = type;
        ItemManager itemManager = CrazyCrates.getPlugin().getCrateManager().getDatabaseManager().getItemManager();
        items = itemManager.getAllItemsFromCache(type).entrySet().stream()
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue().clone()))
                .collect(Collectors.toList());
    }

    public ItemPreview(RaritiesMenu raritiesMenu, int size, String title, RewardType type, Rarity rarity, List<Integer> ids, boolean leftClick) {
        super(raritiesMenu.getPlayer(), title, size, raritiesMenu.getCrate());
        this.raritiesMenu = raritiesMenu;
        this.type = type;
        this.leftClick = leftClick;
        this.editing = true;
        this.rarity = rarity;
        this.table = Table.ALL_ITEMS;
        ItemManager itemManager = CrazyCrates.getPlugin().getCrateManager().getDatabaseManager().getItemManager();
        items = itemManager.getAllItemsFromCache(Table.ALL_ITEMS).entrySet().stream()
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

        int totalPages = Math.ceilDiv(items.size(), 45);

        // Clear the inventory
        getInventory().clear();

        // Add items to the inventory
        int i = 0;
        for (Pair<Integer, ItemStack> item : Lists.partition(items, 45).get(page)) {
            ItemStack itemStack = item.second();
            NBT nbt = new NBT(itemStack);
            nbt.setInteger("itemID", item.first());
            ItemMeta meta = itemStack.getItemMeta();
            List<Component> lore = meta.lore();
            if (lore == null) {
                lore = Lists.newArrayList();
            }

            boolean b = lore.stream().map(PlainTextComponentSerializer.plainText()::serialize).noneMatch(component -> component.contains("id: " + item.first()));
            if (b) {
                lore.add(Component.text("id: " + item.first()));
            }

            meta.lore(lore);
            itemStack.setItemMeta(meta);

            getInventory().setItem(i++, itemStack);
        }

        // Add page navigation items
        if (page > 0) {
            LegacyItemBuilder previousPage = new LegacyItemBuilder(ItemType.ARROW).setDisplayName("<green>Previous Page");
            // Add previous page item
            getInventory().setItem(getSize() - 9, previousPage.asItemStack());
        }

        if (page < totalPages - 1) {
            LegacyItemBuilder nextPage = new LegacyItemBuilder(ItemType.ARROW).setDisplayName("<green>Next Page");
            // Add next page item
            getInventory().setItem(getSize() - 1, nextPage.asItemStack());
        }

        add();

        return this;
    }

    @Override
    public void run(InventoryClickEvent event) {
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
        } else if (slot == holder.getSize() - 1 && holder.page < Math.ceilDiv(holder.items.size(), 45) - 1) {
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
                player.openInventory(new ItemEdit(holder, player, 27, "Edit Item", item, holder.table).build().getInventory());
            }
        } else if (slot == holder.getSize() - 5 && holder.editing && holder.raritiesMenu != null && holder.rarity != null) {
            player.openInventory(holder.raritiesMenu.getInventory());
        }
    }

    private void add() {
        if (editing) {
            LegacyItemBuilder back = new LegacyItemBuilder(ItemType.CHEST).setDisplayName("<green>Go back");
            getInventory().setItem(getSize() - 5, back.asItemStack());
        }
    }
}

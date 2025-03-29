package com.badbones69.crazycrates.paper.api.builders.items;

import com.badbones69.crazycrates.paper.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.paper.api.builders.LegacyItemBuilder;
import com.badbones69.crazycrates.paper.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.Table;
import cz.basicland.blibs.spigot.utils.item.DBItemStack;
import cz.basicland.blibs.spigot.utils.item.DBItemStackNew;
import cz.basicland.blibs.spigot.utils.item.NBT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.PlayerInventory;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

public class ItemEdit extends InventoryBuilder {
    private final ItemPreview preview;
    private final ItemStack itemStack;
    private final Table table;
    private final List<Integer> slots = List.of(10, 11, 15, 18, 26);

    public ItemEdit(ItemPreview preview, Player player, int size, String title, ItemStack itemStack, Table table) {
        super(player, title, size, preview.getCrate());
        this.preview = preview;
        this.itemStack = itemStack;
        this.table = table;
    }

    @Override
    public InventoryBuilder build() {
        ItemStack head = new LegacyItemBuilder(ItemType.PLAYER_HEAD).setPlayer("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMyY2E2NjA1NmI3Mjg2M2U5OGY3ZjMyYmQ3ZDk0YzdhMGQ3OTZhZjY5MWM5YWMzYTkxMzYzMzEzNTIyODhmOSJ9fX0=").setDisplayName("Current Item").asItemStack();
        ItemStack back = new LegacyItemBuilder(ItemType.RED_STAINED_GLASS_PANE).setDisplayName("Back").asItemStack();
        ItemStack save = new LegacyItemBuilder(ItemType.GREEN_STAINED_GLASS_PANE).setDisplayName("Save").asItemStack();
        ItemStack glass = new LegacyItemBuilder(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE).setDisplayName("<gray>").asItemStack();

        getInventory().setItem(10, head);
        getInventory().setItem(11, itemStack);
        getInventory().setItem(18, back);
        getInventory().setItem(26, save);
        IntStream.range(0, getSize()).filter(i -> !slots.contains(i)).forEach(i -> getInventory().setItem(i, glass));

        return this;
    }

    @Override
    public void run(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof ItemEdit holder)) return;

        Player player = holder.getPlayer();

        int slot = event.getSlot();

        if (event.getClickedInventory() instanceof PlayerInventory) return;

        if (slot != 15) {
            event.setCancelled(true);
            switch (slot) {
                case 11 -> {
                    ItemStack clone = holder.itemStack.clone();
                    NBT nbt = new NBT(clone);
                    nbt.remove("rewardName");
                    player.getInventory().addItem(clone);
                }
                case 18 -> player.openInventory(holder.preview.build().getInventory());
                case 26 -> {
                    ItemStack stack = inventory.getItem(15);
                    if (stack == null || stack.getType() == Material.AIR) return;
                    NBT nbt = new NBT(holder.itemStack);
                    Integer id = nbt.getInteger("itemID");
                    if (id == null || id == 0) return;
                    try {
                        int version = DatabaseManager.getVersion();
                        String stackString;
                        if (version == 1) {
                            stackString = DBItemStack.encodeItem(stack);
                        } else if (version == 2) {
                            stackString = DBItemStackNew.encodeItem(stack);
                        } else {
                            throw new RuntimeException("Unsupported database version: " + version);
                        }

                        holder.plugin.getCrateManager().getDatabaseManager().getItemManager().updateItem(id, stackString, holder.table);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    player.openInventory(holder.preview.build().getInventory());
                }
            }
        }
    }
}

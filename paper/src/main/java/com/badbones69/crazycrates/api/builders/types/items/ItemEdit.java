package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import cz.basicland.blibs.spigot.utils.item.CustomItemStack;
import cz.basicland.blibs.spigot.utils.item.DBItemStack;
import cz.basicland.blibs.spigot.utils.item.ItemUtils;
import cz.basicland.blibs.spigot.utils.item.NBT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;

public class ItemEdit extends InventoryBuilder {
    private final ItemPreview preview;
    private final ItemStack itemStack;
    private final RewardType type;
    private final List<Integer> slots = List.of(10, 11, 15, 18, 26);

    public ItemEdit(ItemPreview preview, Player player, int size, String title, ItemStack itemStack) {
        super(preview.getCrate(), player, size, title);
        this.preview = preview;
        this.itemStack = itemStack;
        this.type = preview.getType();
    }

    @Override
    public InventoryBuilder build() {
        getInventory().setItem(10, new CustomItemStack(Material.PLAYER_HEAD).base64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMyY2E2NjA1NmI3Mjg2M2U5OGY3ZjMyYmQ3ZDk0YzdhMGQ3OTZhZjY5MWM5YWMzYTkxMzYzMzEzNTIyODhmOSJ9fX0=").title("Current Item").getStack());
        getInventory().setItem(11, itemStack);
        getInventory().setItem(18, new CustomItemStack(Material.RED_STAINED_GLASS_PANE).title("Back").getStack());
        getInventory().setItem(26, new CustomItemStack(Material.GREEN_STAINED_GLASS_PANE).title("Save").getStack());
        ItemStack glass = ItemUtils.blankItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE).getStack();
        for (int i = 0; i < getSize(); i++) {
            if (slots.contains(i)) continue;
            getInventory().setItem(i, glass);
        }

        return this;
    }

    public static class ItemEditListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            Inventory inventory = event.getInventory();

            if (!(inventory.getHolder(false) instanceof ItemEdit holder)) return;

            Player player = holder.getPlayer();

            int slot = event.getSlot();

            if (event.getClickedInventory() instanceof PlayerInventory) return;

            if (slot != 15) {
                event.setCancelled(true);
                switch (slot) {
                    case 11 -> player.getInventory().addItem(holder.itemStack);
                    case 18 -> player.openInventory(holder.preview.build().getInventory());
                    case 26 -> {
                        ItemStack stack = inventory.getItem(15);
                        if (stack == null || stack.getType() == Material.AIR) return;
                        NBT nbt = new NBT(holder.itemStack);
                        Integer id = nbt.getInteger("itemID");
                        if (id == null || id == 0) return;
                        try {
                            JavaPlugin.getPlugin(CrazyCrates.class).getCrateManager().getDatabaseManager().getItemManager().updateItem(holder.type.getTableName(), id, DBItemStack.encodeItem(stack));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        player.openInventory(holder.preview.build().getInventory());
                    }
                }
            }
        }
    }
}

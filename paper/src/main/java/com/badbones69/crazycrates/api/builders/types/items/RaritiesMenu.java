package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.RaritySettings;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class RaritiesMenu extends InventoryBuilder {

    public RaritiesMenu(Crate crate, Player player, int size, String title) {
        super(crate, player, size, title);
    }

    @Override
    public InventoryBuilder build() {
        // Get all rarities in the crate
        for (Map.Entry<Rarity, RaritySettings> entry : getCrate().getCrateSettings().getRarityMap().entrySet()) {
            // Add the rarity to the inventory
            Rarity rarity = entry.getKey();
            RaritySettings raritySettings = entry.getValue();

            ItemStack item;
            if (raritySettings.is5050Enabled()) {
                item = new ItemBuilder().setMaterial(Material.CHEST).setName(rarity.name())
                        .addLore("&fSTANDARD")
                        .addLore("- &fKliknuti &c&lLEVYM &ftlacitkem pro &c&lPRIDANI &fitemu do crate")
                        .addLore("- &fKliknuti &c&lPRAVYM &ftlacitkem pro &c&lODEBRANI &fitemu z crate")
                        .build();

                getInventory().addItem(item);
            }

            item = new ItemBuilder().setMaterial(Material.CHEST).setName(rarity.name())
                    .addLore("&fLIMITED")
                    .addLore("- &fKliknuti &c&lLEVYM &ftlacitkem pro &c&lPRIDANI &fitemu do crate")
                    .addLore("- &fKliknuti &c&lPRAVYM &ftlacitkem pro &c&lODEBRANI &fitemu z crate")
                    .build();
            getInventory().addItem(item);
        }
        return this;
    }

    public static class RaritiesMenuListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            Inventory inventory = event.getInventory();

            if (!(inventory.getHolder(false) instanceof RaritiesMenu holder)) return;

            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;
            List<String> lore = item.getItemMeta().getLore();
            if (lore == null || lore.isEmpty()) return;
            String type = lore.get(0);
            type = type.substring(2);

            CrateSettings crateSettings = holder.getCrate().getCrateSettings();
            if (crateSettings == null) return;
            RewardType rewardType = RewardType.valueOf(type);
            Rarity rarity = Rarity.valueOf(item.getItemMeta().getDisplayName());

            boolean leftClick = event.getClick().isLeftClick();

            List<Integer> ids = leftClick ? crateSettings.getAllIDs() : crateSettings.getIDsFromRarityType(rarity, rewardType);

            holder.getPlayer().openInventory(new ItemPreview(holder, 54, "Add Items", rewardType, rarity, ids, leftClick).build().getInventory());
        }
    }
}
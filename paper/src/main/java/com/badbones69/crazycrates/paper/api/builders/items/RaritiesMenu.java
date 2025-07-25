package com.badbones69.crazycrates.paper.api.builders.items;

import com.badbones69.crazycrates.paper.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.paper.api.builders.LegacyItemBuilder;
import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.RaritySettings;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.RewardType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.util.List;
import java.util.Map;

public class RaritiesMenu extends InventoryBuilder {

    public RaritiesMenu(Crate crate, Player player, int size, String title) {
        super(player, title, size, crate);
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
                item = new LegacyItemBuilder(plugin, ItemType.CHEST).setDisplayName(rarity.name())
                        .addDisplayLore("<white>STANDARD")
                        .addDisplayLore("- <white>Kliknuti <red><b>LEVYM</red> tlacitkem pro <red><b>PRIDANI</red> itemu do crate</white>")
                        .addDisplayLore("- <white>Kliknuti <red><b>PRAVYM</red> tlacitkem pro <red><b>ODEBRANI</red> itemu z crate</white>")
                        .asItemStack();

                getInventory().addItem(item);
            }

            item = new LegacyItemBuilder(plugin, ItemType.CHEST).setDisplayName(rarity.name())
                    .addDisplayLore("<white>LIMITED")
                    .addDisplayLore("- <white>Kliknuti <red><b>LEVYM</red> tlacitkem pro <red><b>PRIDANI</red> itemu do crate</white>")
                    .addDisplayLore("- <white>Kliknuti <red><b>PRAVYM</red> tlacitkem pro <red><b>ODEBRANI</red> itemu z crate</white>")
                    .asItemStack();
            getInventory().addItem(item);
        }
        return this;
    }

    @Override
    public void run(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof RaritiesMenu holder)) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) return;
        String type = lore.getFirst();
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

package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Tier;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuStuff;
import cz.basicland.blibs.spigot.utils.item.NBT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CratePickPrizeMenu extends InventoryBuilder {
    private final ItemStack item;
    private final Tier tier;
    private final DatabaseManager databaseManager = plugin.getCrateManager().getDatabaseManager();
    private final static ItemStack back = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("Back").asItemStack();
    private final static ItemStack save = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setDisplayName("Save").asItemStack();

    public CratePickPrizeMenu(Player player, ItemStack item, Crate crate, Tier tier) {
        super(player, "Item menu", 9, crate);
        this.item = item;
        this.tier = tier;
    }

    @Override
    public InventoryBuilder build() {
        for (int i = 0; i < 9; i++) {
            if (i < 4) getInventory().setItem(i, back);
            else if (i == 4) getInventory().setItem(i, item);
            else getInventory().setItem(i, save);
        }
        return this;
    }

    @Override
    public void run(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof CratePickPrizeMenu holder)) return;

        event.setCancelled(true);

        Player player = holder.getPlayer();
        Crate crate = holder.getCrate();
        CrateSettings crateSettings = crate.getCrateSettings();

        ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) return;

        if (!item.hasItemMeta()) return;

        ItemStack picked = inventory.getItem(4);
        if (picked == null || picked.getType() == Material.AIR) return;

        NBT nbt = new NBT(picked);

        if (event.getSlot() < 4) {
            player.playSound(UltimateMenuStuff.BACK);
            // Open the previous menu
            crate.getPreview(player, tier).open();
        } else if (event.getSlot() > 4) {
            player.playSound(UltimateMenuStuff.CLICK);
            // Retrieve the player's profile and save the chosen reward
            PlayerProfile playerProfile = holder.databaseManager.getPlayerProfile(player.getName(), crateSettings, false);

            String rewardName = nbt.getString("rewardName");

            plugin.getLogger().info("Chosen reward: " + rewardName);

            playerProfile.setChosenReward(rewardName);
            holder.databaseManager.savePlayerProfile(player.getName(), crateSettings, playerProfile);
            crate.getPreview(player, tier).open();
        }
    }
}

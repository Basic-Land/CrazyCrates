package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.builders.types.CrateTierMenu;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BonusPityMenu extends InventoryBuilder {
    private final DatabaseManager databaseManager = plugin.getCrateManager().getDatabaseManager();
    private int currentPage = 0;
    private int totalPages;
    private int lastItemSlot = 10;
    private final List<ItemStack> extraRewards;
    private final CrateTierMenu crateTierMenu;
    private final static ItemStack orangeGlassPane = glass(Material.ORANGE_STAINED_GLASS_PANE);
    private final static ItemStack redGlassPane = glass(Material.RED_STAINED_GLASS_PANE);
    private final static ItemStack greenGlassPane = glass(Material.GREEN_STAINED_GLASS_PANE);

    public BonusPityMenu(Crate crate, Player player, int size, String title, CrateTierMenu crateTierMenu) {
        super(crate, player, size, title);
        this.extraRewards = crate.getCrateSettings().getExtraRewards();
        this.crateTierMenu = crateTierMenu;
    }

    public InventoryBuilder build() {
        PlayerProfile playerProfile = databaseManager.getPlayerProfile(getPlayer().getName(), getCrate().getCrateSettings(), false);

        int totalPity = playerProfile.getTotalPity();
        int extraRewardPity = playerProfile.getExtraRewardPity();

        // Calculate the total number of pages
        totalPages = (int) Math.ceil((double) extraRewards.size() / 7);
        if (totalPages == 0) totalPages = 1;

        // Set the orange glass pane at the border positions
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, orangeGlassPane); // Top border
            getInventory().setItem(i + 27, orangeGlassPane); // Bottom border
        }

        getInventory().setItem(9, orangeGlassPane); // Left border
        getInventory().setItem(18, orangeGlassPane);
        getInventory().setItem(17, orangeGlassPane); // Right border
        getInventory().setItem(26, orangeGlassPane);

        // Set the red glass pane at the specified positions
        for (int i = 10; i < 17; i++) {
            getInventory().setItem(i, redGlassPane);
        }

        // Set the displayed item at the specified positions
        for (int i = 0; i < 7; i++) {
            int index = currentPage * 7 + i;
            if (index < extraRewards.size()) {
                ItemStack displayedItem = extraRewards.get(index);
                getInventory().setItem(19 + i, displayedItem);
            } else {
                getInventory().setItem(19 + i, null);
            }
        }

        // Set the page back and forward items
        ItemStack pageBackItem = new ItemStack(Material.PLAYER_HEAD);
        ItemStack pageForwardItem = new ItemStack(Material.PLAYER_HEAD);
        if (currentPage == 0) { // If it's the first page, set the page back item to an orange glass pane
            pageBackItem = orangeGlassPane;
        }

        if (currentPage == totalPages - 1) { // If it's the last page, set the page forward item to an orange glass pane
            pageForwardItem = orangeGlassPane;
        }

        getInventory().setItem(27, pageBackItem); // Page back
        getInventory().setItem(35, pageForwardItem); // Page forward

        ItemBuilder backItem = new ItemBuilder().setMaterial(Material.ARROW).setCustomModelData(1000002).setHasCustomModelData(true);
        getInventory().setItem(29, backItem.build());

        // Set the paper at the specified position
        ItemStack paper = new ItemBuilder()
                .setMaterial(Material.PAPER)
                .setCustomModelData(10)
                .setHasCustomModelData(true)
                .setName("#f0af37Progress " + totalPity + "/" + extraRewardPity)
                .build();
        getInventory().setItem(31, paper);

        // Set the barrier or player head at the specified position based on a condition
        boolean extraPity = playerProfile.reachedExtraRewardPity();

        ItemBuilder confirm = new ItemBuilder().setMaterial(extraPity ? Material.ARROW : Material.BARRIER)
                .setCustomModelData(1000001)
                .setHasCustomModelData(true);

        if (extraPity) {
            confirm.setName("#f0af37Click to confirm");
            confirm.addLore("");
        } else {
            confirm.setName("#f0af37You have not reached the required pity");
        }


        getInventory().setItem(33, confirm.build());

        return this;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof BonusPityMenu holder)) return;

        event.setCancelled(true);

        Player player = holder.getPlayer();

        ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) return;

        int clickedSlot = event.getSlot();

        // If a displayed item is clicked
        if (clickedSlot >= 19 && clickedSlot <= 25) {
            holder.getInventory().setItem(holder.lastItemSlot, redGlassPane);

            // Change the red glass pane above it to a green glass pane
            holder.getInventory().setItem(clickedSlot - 9, greenGlassPane);
            holder.lastItemSlot = clickedSlot - 9;
        }

        // If the page back item is clicked
        if (clickedSlot == 27 && holder.currentPage > 0) {
            holder.previousPage();
        }

        // If the page forward item is clicked
        if (clickedSlot == 35 && holder.currentPage < holder.totalPages - 1) {
            holder.nextPage();
        }

        if (clickedSlot == 29) {
            player.openInventory(holder.crateTierMenu.getInventory());
        }

        if (clickedSlot == 33 && item.getType() == Material.ARROW) {
            PlayerProfile playerProfile = holder.databaseManager.getPlayerProfile(player.getName(), holder.getCrate().getCrateSettings(), false);

            if (playerProfile.isClaimedExtraReward()) {
                player.sendMessage("You have already claimed the extra reward.");
                return;
            }

            if (playerProfile.reachedExtraRewardPity()) {
                playerProfile.setClaimedExtraReward(true);
                holder.databaseManager.savePlayerProfile(player.getName(), holder.getCrate().getCrateSettings(), playerProfile);
                player.sendMessage("You have claimed the extra reward.");
                player.openInventory(holder.crateTierMenu.getInventory());
            } else {
                player.sendMessage("You have not reached the required pity.");
            }
        }
    }

    private void nextPage() {
        currentPage++;
        build();
    }

    private void previousPage() {
        currentPage--;
        build();
    }

    private static ItemStack glass(Material item) {
        return new ItemBuilder().setMaterial(item).setCustomModelData(1000001).setHasCustomModelData(true).setName("&7").build();
    }
}

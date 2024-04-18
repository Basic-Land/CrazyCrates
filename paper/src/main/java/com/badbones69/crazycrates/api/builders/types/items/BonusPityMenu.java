package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.api.objects.gacha.util.ItemData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BonusPityMenu extends InventoryBuilder {
    private final DatabaseManager databaseManager = plugin.getCrateManager().getDatabaseManager();
    private int currentPage = 0; // Add this line
    private int totalPages;
    private int lastItemSlot = 10;
    private final List<ItemData> extraRewards;

    public BonusPityMenu(Crate crate, Player player, int size, String title) {
        super(crate, player, size, title);
        this.extraRewards = crate.getCrateSettings().getExtraRewards();
    }

    public InventoryBuilder build() {
        PlayerProfile playerProfile = databaseManager.getPlayerProfile(getPlayer().getName(), getCrate().getCrateSettings(), false);

        int totalPity = playerProfile.getTotalPity();
        int extraRewardPity = playerProfile.getExtraRewardPity();

        // Calculate the total number of pages
        totalPages = (int) Math.ceil((double) extraRewards.size() / 7);
        if (totalPages == 0) totalPages = 1;

        // Set the orange glass pane at the border positions
        ItemStack orangeGlassPane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, orangeGlassPane); // Top border
            getInventory().setItem(i + 27, orangeGlassPane); // Bottom border
        }
        getInventory().setItem(9, orangeGlassPane); // Left border
        getInventory().setItem(18, orangeGlassPane);
        getInventory().setItem(17, orangeGlassPane); // Right border
        getInventory().setItem(26, orangeGlassPane);

        // Set the red glass pane at the specified positions
        ItemStack redGlassPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        for (int i = 10; i < 17; i++) {
            getInventory().setItem(i, redGlassPane);
        }

        // Set the displayed item at the specified positions
        for (int i = 0; i < 7; i++) {
            int index = currentPage * 7 + i;
            if (index < extraRewards.size()) {
                ItemStack displayedItem = extraRewards.get(index).itemStack();
                getInventory().setItem(19 + i, displayedItem);
            } else {
                getInventory().setItem(19 + i, null);
            }
        }

        // Set the page back and forward items
        ItemStack pageBackItem = new ItemStack(Material.PLAYER_HEAD);
        ItemStack pageForwardItem = new ItemStack(Material.PLAYER_HEAD);
        if (currentPage == 0) { // If it's the first page, set the page back item to an orange glass pane
            pageBackItem = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        }

        if (currentPage == totalPages - 1) { // If it's the last page, set the page forward item to an orange glass pane
            pageForwardItem = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        }

        getInventory().setItem(27, pageBackItem); // Page back
        getInventory().setItem(35, pageForwardItem); // Page forward

        // Set the paper at the specified position
        ItemStack paper = new ItemStack(Material.PAPER);
        getInventory().setItem(31, paper);

        // Set the barrier or player head at the specified position based on a condition
        ItemStack lastItem;
        if (playerProfile.reachedExtraRewardPity()) { // Replace 'extraRewardPity' with the actual condition
            lastItem = new ItemStack(Material.BARRIER);
        } else {
            lastItem = new ItemStack(Material.PLAYER_HEAD);
        }
        getInventory().setItem(33, lastItem);

        return this;
    }

    public void nextPage() {
        currentPage++;
        build();
    }

    public void previousPage() {
        currentPage--;
        build();
    }

    public static class BonusPityListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            Inventory inventory = event.getInventory();

            if (!(inventory.getHolder(false) instanceof BonusPityMenu holder)) return;

            event.setCancelled(true);

            Player player = holder.getPlayer();

            ItemStack item = event.getCurrentItem();

            if (item == null || item.getType() == Material.AIR) return;

            int clickedSlot = event.getSlot();

            // If a displayed item is clicked
            if (clickedSlot >= 19 && clickedSlot <= 25) {
                ItemStack redGlassPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                holder.getInventory().setItem(holder.lastItemSlot, redGlassPane);

                // Change the red glass pane above it to a green glass pane
                ItemStack greenGlassPane = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
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
        }
    }
}

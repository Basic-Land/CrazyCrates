package com.badbones69.crazycrates.paper.api.builders.items;

import com.badbones69.crazycrates.paper.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.paper.api.builders.LegacyItemBuilder;
import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.ItemRepo;
import com.badbones69.crazycrates.paper.tasks.menus.CrateTierMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.util.List;

public class BonusPityMenu extends InventoryBuilder {
    private final DatabaseManager databaseManager = plugin.getCrateManager().getDatabaseManager();
    private int currentPage = 0;
    private int totalPages;
    private int lastItemSlot = 10;
    private final List<ItemStack> extraRewards;
    private final CrateTierMenu crateTierMenu;
    private final static ItemStack orangeGlassPane = glass(ItemType.ORANGE_STAINED_GLASS_PANE);
    private final static ItemStack redGlassPane = glass(ItemType.RED_STAINED_GLASS_PANE);
    private final static ItemStack greenGlassPane = glass(ItemType.GREEN_STAINED_GLASS_PANE);

    public BonusPityMenu(Crate crate, Player player, int size, String title, CrateTierMenu crateTierMenu) {
        super(player, title, size, crate);
        this.extraRewards = crate.getCrateSettings().getExtraRewards();
        this.crateTierMenu = crateTierMenu;
    }

    public InventoryBuilder build() {
        PlayerProfile playerProfile = databaseManager.getPlayerProfile(getPlayer().getName(), getCrate().getCrateSettings(), false);

        int totalPity = playerProfile.getTotalPity();
        int extraRewardPity = playerProfile.getExtraRewardPity();

        // Calculate the total number of pages
        totalPages = Math.ceilDiv(extraRewards.size(), 7);
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

        LegacyItemBuilder backItem = new LegacyItemBuilder(ItemType.ARROW).setCustomModelData(1000002);
        getInventory().setItem(29, backItem.asItemStack());

        // Set the paper at the specified position
        ItemStack paper = new LegacyItemBuilder(ItemType.PAPER)
                .setCustomModelData(10)
                .setDisplayName("#f0af37Progress " + totalPity + "/" + extraRewardPity)
                .asItemStack();
        getInventory().setItem(31, paper);

        // Set the barrier or player head at the specified position based on a condition
        boolean extraPity = playerProfile.reachedExtraRewardPity();

        LegacyItemBuilder confirm = new LegacyItemBuilder(extraPity ? ItemType.ARROW : ItemType.BARRIER)
                .setCustomModelData(1000001);

        if (extraPity) {
            confirm.setDisplayName("#f0af37Click to confirm");
            confirm.addDisplayLore("");
        } else {
            confirm.setDisplayName("#f0af37You have not reached the required pity");
        }

        getInventory().setItem(33, confirm.asItemStack());

        return this;
    }

    @Override
    public void run(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof BonusPityMenu holder)) return;

        event.setCancelled(true);

        Player player = holder.getPlayer();

        ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) return;

        int clickedSlot = event.getSlot();

        // If a displayed item is clicked
        if (clickedSlot >= 19 && clickedSlot <= 25) {
            player.playSound(ItemRepo.CLICK);
            holder.getInventory().setItem(holder.lastItemSlot, redGlassPane);

            // Change the red glass pane above it to a green glass pane
            holder.getInventory().setItem(clickedSlot - 9, greenGlassPane);
            holder.lastItemSlot = clickedSlot - 9;
        }

        // If the page back item is clicked
        if (clickedSlot == 27 && holder.currentPage > 0) {
            player.playSound(ItemRepo.CLICK);
            holder.previousPage();
        }

        // If the page forward item is clicked
        if (clickedSlot == 35 && holder.currentPage < holder.totalPages - 1) {
            player.playSound(ItemRepo.CLICK);
            holder.nextPage();
        }

        if (clickedSlot == 29) {
            player.playSound(ItemRepo.BACK);
            player.openInventory(holder.crateTierMenu.getGui().getInventory());
        }

        if (clickedSlot == 33) {
            if (item.getType() == Material.ARROW) {
                PlayerProfile playerProfile = holder.databaseManager.getPlayerProfile(player.getName(), holder.getCrate().getCrateSettings(), false);

                if (playerProfile.isClaimedExtraReward()) {
                    player.sendMessage("You have already claimed the extra reward.");
                    player.playSound(ItemRepo.ERROR);
                    return;
                }

                if (playerProfile.reachedExtraRewardPity()) {
                    player.playSound(ItemRepo.CLICK);
                    playerProfile.setClaimedExtraReward(true);
                    holder.databaseManager.savePlayerProfile(player.getName(), holder.getCrate().getCrateSettings(), playerProfile);
                    player.sendMessage("You have claimed the extra reward.");
                    player.openInventory(holder.crateTierMenu.getGui().getInventory());
                } else {
                    player.playSound(ItemRepo.ERROR);
                    player.sendMessage("You have not reached the required pity.");
                }
            } else if (item.getType() == Material.BARRIER) {
                player.playSound(ItemRepo.ERROR);
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

    private static ItemStack glass(ItemType item) {
        return new LegacyItemBuilder(item).setCustomModelData(1000001).setDisplayName("<gray>").asItemStack();
    }
}

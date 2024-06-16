package com.badbones69.crazycrates.listeners.crates;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.builders.types.CratePrizeMenu;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.data.Result;
import com.badbones69.crazycrates.tasks.BukkitUserManager;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import com.badbones69.crazycrates.tasks.crates.other.GachaCrateManager;
import com.badbones69.crazycrates.tasks.crates.types.roulette.RouletteStandard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazycrates.api.enums.types.CrateType;

import java.util.List;
import java.util.UUID;

public class GachaCrateListener implements Listener {

    private @NotNull final CrazyCrates plugin = JavaPlugin.getPlugin(CrazyCrates.class);

    private @NotNull final CrateManager crateManager = this.plugin.getCrateManager();
    private static final List<Integer> slots = List.of(10, 12, 14, 16, 20, 22, 24, 28, 31, 34);

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof CratePrizeMenu holder)) return;

        final Player player = holder.getPlayer();
        final UUID uuid = player.getUniqueId();

        // Cancel event.
        event.setCancelled(true);

        // Get opening crate.
        final Crate crate = this.crateManager.getOpeningCrate(player);

        if (crate == null) return;

        // Check if player is in the opening list.
        if (!this.crateManager.isInOpeningList(player) || crate.getCrateType() != CrateType.gacha) return;

        GachaCrateManager manager = (GachaCrateManager) crate.getManager();
        RouletteStandard rouletteStandard = manager.getGachaRunnables().get(uuid);

        if (rouletteStandard == null) return;

        List<Result> prize = rouletteStandard.getPrize();
        int count = rouletteStandard.getCount();
        long time = rouletteStandard.getTime();

        if (System.currentTimeMillis() - time < 400) return;

        rouletteStandard.updateTimer();

        if (count == 11) {
            manager.getGachaRunnables().remove(uuid);
            return;
        }

        if (count == 10) {
            int i = 0;
            for (Integer slot : slots) {
                inventory.setItem(slot, prize.get(i++).getPrize().getDisplayItem());
            }
            rouletteStandard.incrementCount();
            return;
        }

        Inventory inv = event.getView().getTopInventory();
        inv.setItem(22, prize.get(count).getPrize().getDisplayItem());
        rouletteStandard.incrementCount();
    }
}
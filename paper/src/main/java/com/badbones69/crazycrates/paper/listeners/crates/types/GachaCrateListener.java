package com.badbones69.crazycrates.paper.listeners.crates.types;

import com.badbones69.crazycrates.paper.CrazyCrates;
import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.Result;
import com.badbones69.crazycrates.paper.tasks.crates.CrateManager;
import com.badbones69.crazycrates.paper.tasks.crates.other.GachaCrateManager;
import com.badbones69.crazycrates.paper.tasks.crates.types.roulette.RouletteStandard;
import com.badbones69.crazycrates.paper.tasks.menus.CratePrizeMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazycrates.api.enums.types.CrateType;

import java.util.List;
import java.util.UUID;

public class GachaCrateListener implements Listener {

    private @NotNull final CrazyCrates plugin = CrazyCrates.getPlugin();

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

        if (event.getRawSlot() == 8) {
            rouletteStandard.skip();
        }

        if (!rouletteStandard.isSkip()) return;

        List<Result> prize = rouletteStandard.getPrize();
        int count = rouletteStandard.getCount();
        Inventory inv = event.getView().getTopInventory();
        long time = rouletteStandard.getTime();
        long diff = System.currentTimeMillis() - time;

        if (diff < 400) return;

        rouletteStandard.updateTimer();

        if (count == 11) {
            manager.getGachaRunnables().remove(uuid);
            return;
        }

        if (rouletteStandard.isSneak()) {
            if (count == 10) {
                int i = 0;
                for (Integer slot : slots) {
                    inventory.setItem(slot, prize.get(i++).getPrize().getDisplayItem());
                }
                rouletteStandard.incrementCount();
                return;
            }

            inv.setItem(22, prize.get(count).getPrize().getDisplayItem());
            rouletteStandard.incrementCount();
        } else {
            rouletteStandard.incrementCount();
            if (count == 1) manager.getGachaRunnables().remove(uuid);
        }
    }
}
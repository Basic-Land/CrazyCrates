package com.badbones69.crazycrates.paper.tasks.crates.types.roulette;

import com.badbones69.crazycrates.paper.CrazyCrates;
import com.badbones69.crazycrates.paper.api.PrizeManager;
import com.badbones69.crazycrates.paper.api.builders.CrateBuilder;
import com.badbones69.crazycrates.paper.api.builders.LegacyItemBuilder;
import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.Result;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.ItemRepo;
import com.badbones69.crazycrates.paper.tasks.crates.CrateManager;
import com.badbones69.crazycrates.paper.tasks.crates.other.GachaCrateManager;
import com.ryderbelserion.fusion.paper.api.scheduler.FoliaScheduler;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class RouletteStandard extends FoliaScheduler {
    private final CrateBuilder builder;
    private final Crate crate;
    private final Player player;
    private final UUID uuid;
    private final Inventory inventory;
    @Getter
    private final List<Result> prize;
    private final CrazyCrates plugin = CrazyCrates.getPlugin();
    private final CrateManager crateManager = plugin.getCrateManager();
    private final Rarity highestRarity;
    @Getter
    private final boolean sneak;
    @Getter
    private int count = 1;
    @Getter
    private long time = System.currentTimeMillis();

    public RouletteStandard(CrateBuilder builder, List<Result> prize, boolean sneak) {
        super(CrazyCrates.getPlugin(), null, builder.getPlayer());
        this.builder = builder;
        this.crate = builder.getCrate();
        this.player = builder.getPlayer();
        this.uuid = player.getUniqueId();
        this.inventory = builder.getInventory();
        this.prize = prize;
        this.highestRarity = prize.stream().map(Result::getRarity).max(Rarity::compareTo).orElse(Rarity.COMMON);
        this.sneak = sneak;
    }

    private int modelData = 1;
    private boolean first = false;
    private boolean lock = false;
    private boolean put = false;
    @Getter
    private boolean skip = false;

    @Override
    public void run() {
        if (!player.isOnline()) {
            endCrate();
            return;
        }

        if (!put) {
            putGachaRunnable();
        }

        if (modelData == 8) {
            player.playSound(ItemRepo.OPEN);
        }

        if (!first && !skip) {
            builder.setItem(36, new LegacyItemBuilder(plugin, ItemType.GRAY_STAINED_GLASS_PANE).setCustomModelData(modelData).asItemStack());
            player.updateInventory();
        }

        if (!player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.openInventory(inventory);
        }

        if (modelData == 38) {
            modelData += getPortalData();
        }

        if (modelData % 100 == 52) {
            modelData -= getPortalData();
        }

        if (modelData == 64) {
            handleModelData64();
        }

        if (!lock) modelData++;
    }

    private void endCrate() {
        ((GachaCrateManager) crate.getManager()).getGachaRunnables().remove(uuid);
        crateManager.endCrate(player);
        crateManager.removePlayerFromOpeningList(player);
        cancel();
    }

    private void putGachaRunnable() {
        GachaCrateManager gachaCrateManager = (GachaCrateManager) crate.getManager();
        gachaCrateManager.getGachaRunnables().putIfAbsent(uuid, this);
        put = true;
    }

    private void handleModelData64() {
        if (!first) {
            first = true;
            lock = true;
            builder.setItem(36, new LegacyItemBuilder(plugin, ItemType.GRAY_STAINED_GLASS_PANE).setCustomModelData(600).asItemStack());
            if (!sneak || !skip) builder.setItem(22, prize.getFirst().getPrize().getDisplayItem());
            skip = true;
            return;
        }

        if (check()) return;

        endTask();
        cancel();
    }

    private int getPortalData() {
        return switch (highestRarity) {
            case COMMON -> 100;
            case UNCOMMON -> 200;
            case RARE -> 300;
            case EPIC -> 400;
            case LEGENDARY -> 500;
            case EXTRA_REWARD -> 0;
        };
    }

    private boolean check() {
        return ((GachaCrateManager) crate.getManager()).getGachaRunnables().get(uuid) != null;
    }

    private void endTask() {
        builder.setItem(36, new LegacyItemBuilder(plugin, ItemType.GRAY_STAINED_GLASS_PANE).setCustomModelData(600).asItemStack());
        if (!sneak) {
            builder.setItem(22, prize.getFirst().getPrize().getDisplayItem());
        }

        builder.playSound("stop-sound", Sound.Source.PLAYER, "entity.player.levelup");
        crateManager.endCrate(player);

        prize.forEach(itemData ->
                crate.getPrizes()
                        .stream()
                        .filter(prize -> prize.getSectionName().equals(itemData.getPrize().getSectionName()))
                        .findFirst()
                        .ifPresent(prize -> PrizeManager.givePrize(player, crate, prize)));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getOpenInventory().getTopInventory().equals(inventory))
                    player.closeInventory(InventoryCloseEvent.Reason.PLAYER);
                crateManager.removePlayerFromOpeningList(player);
                plugin.getCrateManager().getDatabaseManager().getUltimateMenuManager().open(player, crate);
            }
        }.runTaskLater(plugin, 20);
    }

    public void incrementCount() {
        count++;
    }

    public void skip() {
        modelData = 64;
        count = 10;
        skip = true;
        player.stopAllSounds();
    }

    public void updateTimer() {
        time = System.currentTimeMillis();
    }
}
package com.badbones69.crazycrates.tasks.crates.types.roulette;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.PrizeManager;
import com.badbones69.crazycrates.api.builders.CrateBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.gacha.data.Result;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuStuff;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import com.ryderbelserion.vital.paper.builders.items.ItemBuilder;
import com.ryderbelserion.vital.paper.util.scheduler.FoliaRunnable;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class RouletteStandard extends FoliaRunnable {
    private final CrateBuilder builder;
    private final Crate crate;
    private final Player player;
    private final Inventory inventory;
    private final List<Result> prize;
    private final CrazyCrates plugin = JavaPlugin.getPlugin(CrazyCrates.class);
    private final CrateManager crateManager = plugin.getCrateManager();
    private final Rarity highestRarity;
    private final boolean sneak;
    private final ItemBuilder glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE);

    public RouletteStandard(EntityScheduler entityScheduler, CrateBuilder builder, List<Result> prize, boolean sneak) {
        super(entityScheduler, null);
        this.builder = builder;
        this.crate = builder.getCrate();
        this.player = builder.getPlayer();
        this.inventory = builder.getInventory();
        this.prize = prize;
        this.highestRarity = prize.stream().map(Result::getRarity).max(Rarity::compareTo).orElse(Rarity.COMMON);
        this.sneak = sneak;
    }

    private int modelData = 1;

    @Override
    public void run() {
        if (modelData == 8) {
            player.playSound(UltimateMenuStuff.OPEN);
        }

        builder.setItem(36, glass.setCustomModelData(modelData).getStack());

        modelData++;
        player.openInventory(inventory);

        if (modelData == 38) {
            modelData += getPortalData();
        }

        if (modelData % 100 == 52) {
            modelData -= getPortalData();
        }

        if (modelData == 65) {
            endTask();
            cancel();
        }
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

    private void endTask() {
        builder.setItem(36, glass.setCustomModelData(600).getStack());
        if (!sneak) {
            builder.setItem(22, prize.getFirst().getPrize().getDisplayItem());
        }

        builder.playSound("stop-sound", Sound.Source.PLAYER, "entity.player.levelup");
        crateManager.endCrate(player);

        for (Result itemData : prize) {
            for (Prize prize : crate.getPrizes()) {
                if (prize.getSectionName().equals(itemData.getPrize().getSectionName())) {
                    PrizeManager.givePrize(player, prize, crate);
                    break;
                }
            }
        }

        player.sendMessage(plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName()).toString());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getOpenInventory().getTopInventory().equals(inventory)) player.closeInventory(InventoryCloseEvent.Reason.PLAYER);
                crateManager.removePlayerFromOpeningList(player);
                plugin.getCrateManager().getDatabaseManager().getUltimateMenuManager().open(player, crate);
            }
        }.runTaskLater(plugin, 60);
    }
}

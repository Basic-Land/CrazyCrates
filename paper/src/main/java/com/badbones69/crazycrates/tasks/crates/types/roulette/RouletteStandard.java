package com.badbones69.crazycrates.tasks.crates.types.roulette;

import com.badbones69.crazycrates.CrazyCratesPaper;
import com.badbones69.crazycrates.api.PrizeManager;
import com.badbones69.crazycrates.api.builders.CrateBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.utils.MiscUtils;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import cz.basicland.blibs.spigot.utils.item.ItemUtils;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class RouletteStandard extends BukkitRunnable {
    private final CrateBuilder builder;
    private final Crate crate;
    private final Player player;
    private final Inventory inventory;
    private final List<ItemStack> prize;
    private final boolean multi;
    private final int[] slots;

    public RouletteStandard(CrateBuilder builder, List<ItemStack> prize, boolean multi) {
        this.builder = builder;
        this.crate = builder.getCrate();
        this.player = builder.getPlayer();
        this.inventory = builder.getInventory();
        this.prize = prize;
        this.multi = multi;
        if (multi) {
            slots = new int[]{6,16,25,34,42,38,28,19,10,2,22};
        } else slots = new int[]{22};
    }
    private int full = 0;
    private int time = 1;
    private int even = 0;
    private int open = 0;
    private int longSpin = 0;
    private final CrazyCratesPaper plugin = CrazyCratesPaper.get();
    private final CrateManager crateManager = plugin.getCrateManager();
    private final List<Integer> slowSpin = MiscUtils.slowSpin(55, 10);

    @Override
    public void run() {
        if (this.full <= 15) {
            builder.setItem(22, pickPrize().getDisplayItem(player));
            setGlass();

            builder.playSound("cycle-sound", SoundCategory.PLAYERS, "BLOCK_NOTE_BLOCK_XYLOPHONE");
            this.even++;

            if (this.even >= 4) {
                this.even = 0;
                builder.setItem(22, pickPrize().getDisplayItem(player));
            }
        }

        this.open++;

        if (this.open >= 5) {
            player.openInventory(inventory);
            this.open = 0;
        }

        this.full++;

        if (this.full > 16) {
            if (multi) {
                if (longSpin != 10) {
                    ItemStack item = prize.get(longSpin);

                    if (slowSpin.contains(time + 1)) {
                        builder.setItem(22, item);
                    }

                    if (slowSpin.contains(this.time)) {
                        setGlass();
                        builder.setItem(slots[longSpin], item);

                        builder.playSound("cycle-sound", SoundCategory.PLAYERS, "BLOCK_NOTE_BLOCK_XYLOPHONE");
                        longSpin++;
                    }
                }

                this.time++;

                if (this.time >= 65) {
                    endTask();
                }
            } else {
                if (MiscUtils.slowSpin(46, 9).contains(this.time)) {
                    setGlass();
                    builder.setItem(22, pickPrize().getDisplayItem(player));

                    builder.playSound("cycle-sound", SoundCategory.PLAYERS, "BLOCK_NOTE_BLOCK_XYLOPHONE");
                }

                if (time == 22 && prize != null) {
                    builder.setItem(22, prize.get(0));
                }

                this.time++;

                if (this.time >= 23) {
                    endTask();
                }
            }
        }
    }

    private void setGlass() {
        for (int slot = 0; slot < builder.getSize(); slot++) {
            int finalSlot = slot;
            if (Arrays.stream(slots).noneMatch(i -> i == finalSlot)) {
                builder.setCustomGlassPane(slot);
            }
        }
    }

    private Prize pickPrize() {
        if (prize == null) {
            return crate.pickPrize(player);
        } else {
            return crateManager.getGachaSystem().pickPrize(crate);
        }
    }

    private void endTask() {
        builder.playSound("stop-sound", SoundCategory.PLAYERS, "ENTITY_PLAYER_LEVELUP");
        crateManager.endCrate(player);

        if (prize != null) {
            ItemUtils.giveOrDrop(player, prize.toArray(new ItemStack[0]));
        } else {
            ItemStack item = inventory.getItem(22);

            if (item != null) {
                Prize prize = crate.getPrize(item);
                PrizeManager.givePrize(player, crate, prize);
            }
        }

        crateManager.removePlayerFromOpeningList(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getOpenInventory().getTopInventory().equals(inventory)) player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);
            }
        }.runTaskLater(plugin, 40);
    }
}

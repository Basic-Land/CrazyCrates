package com.badbones69.crazycrates.tasks.crates.types;

import com.badbones69.crazycrates.api.builders.CrateBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.PlayerDataManager;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.api.objects.gacha.data.Result;
import com.badbones69.crazycrates.api.objects.gacha.gacha.GachaSystem;
import com.badbones69.crazycrates.api.utils.MiscUtils;
import com.badbones69.crazycrates.tasks.BukkitUserManager;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import com.badbones69.crazycrates.tasks.crates.types.roulette.RouletteStandard;
import cz.basicland.blibs.spigot.utils.item.CustomItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazycrates.api.enums.types.KeyType;

import java.util.ArrayList;
import java.util.List;

public class GachaCrate extends CrateBuilder {
    @NotNull
    private final CrateManager crateManager = this.plugin.getCrateManager();
    @NotNull
    private final BukkitUserManager userManager = this.plugin.getUserManager();
    private final PlayerDataManager playerDataManager = crateManager.getPlayerDataManager();
    private final GachaSystem gachaSystem = crateManager.getGachaSystem();

    public GachaCrate(Crate crate, Player player, int size) {
        super(crate, player, size);
    }

    @Override
    public void open(KeyType type, boolean checkHand) {
        // Crate event failed so we return.
        if (isCrateEventValid(type, checkHand)) {
            return;
        }

        String playerName = getPlayer().getName();
        CrateSettings crateSettings = playerDataManager.getCrateSettings(getCrate().getName());

        if (!playerDataManager.hasPlayerData(playerName)) {
            playerDataManager.addBlankPlayerData(playerName);
        }

        PlayerProfile playerProfile = playerDataManager.getPlayerProfile(playerName, crateSettings);
        String chosenReward = playerProfile.getChosenReward();

        if (chosenReward == null || chosenReward.isEmpty()) {
            getPlayer().sendMessage("§cYou have not chosen a reward yet. Please choose a reward using menu");
            crateManager.removePlayerFromOpeningList(getPlayer());
            return;
        }

        int amount = getPlayer().isSneaking() ? 10 : 1;

        boolean keyCheck = this.userManager.takeKeys(amount, getPlayer().getUniqueId(), getCrate().getName(), type, checkHand);

        if (!keyCheck) {
            // Send the message about failing to take the key.
            MiscUtils.failedToTakeKey(getPlayer(), getCrate().getName());

            // Remove from opening list.
            this.crateManager.removePlayerFromOpeningList(getPlayer());

            return;
        }

        setItem(22, getCrate().pickPrize(getPlayer()).getDisplayItem(getPlayer()));

        List<ItemStack> items = new ArrayList<>();

        while (amount-- > 0) {
            Result result = switch (crateSettings.getGachaType()) {
                case NORMAL -> gachaSystem.roll(playerProfile, crateSettings);
                case FATE_POINT -> {
                    CustomItemStack stack = crateSettings.find(true, false, chosenReward);
                    if (stack == null) {
                        throw new IllegalStateException("Chosen reward not found");
                    }
                    yield gachaSystem.rollWithFatePoint(playerProfile, crateSettings, stack);
                }
                case OVERRIDE -> {
                    CustomItemStack stack = crateSettings.find(false, true, chosenReward);
                    if (stack == null) {
                        throw new IllegalStateException("Chosen reward not found");
                    }
                    yield gachaSystem.rollOverrideSet(playerProfile, crateSettings, stack);
                }
            };

            System.out.println(result);
            items.add(result.getItem().getStack());
        }

        addCrateTask(new RouletteStandard(this, items, getPlayer().isSneaking()).runTaskTimer(this.plugin, 2, 2));

        playerProfile.cutHistory(300);

        playerDataManager.savePlayerProfile(playerName, crateSettings, playerProfile);
    }

    @Override
    public void run() {

    }
}

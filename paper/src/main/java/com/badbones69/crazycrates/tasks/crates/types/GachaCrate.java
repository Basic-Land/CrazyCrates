package com.badbones69.crazycrates.tasks.crates.types;

import com.badbones69.crazycrates.api.builders.CrateBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.data.*;
import com.badbones69.crazycrates.api.objects.gacha.enums.GachaType;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.gacha.GachaSystem;
import com.badbones69.crazycrates.api.utils.MiscUtils;
import com.badbones69.crazycrates.tasks.BukkitUserManager;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import com.badbones69.crazycrates.tasks.crates.types.roulette.RouletteStandard;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazycrates.api.enums.types.KeyType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GachaCrate extends CrateBuilder {
    @NotNull
    private final CrateManager crateManager = this.plugin.getCrateManager();
    @NotNull
    private final BukkitUserManager userManager = this.plugin.getUserManager();
    private final DatabaseManager playerDataManager = crateManager.getDatabaseManager();
    private final GachaSystem gachaSystem = crateManager.getGachaSystem();

    public GachaCrate(Crate crate, Player player, int size) {
        super(crate, player, size);
    }

    @Override
    public void open(@NotNull KeyType type, boolean checkHand) {
        // Crate event failed so we return.
        if (isCrateEventValid(type, checkHand)) {
            return;
        }

        String playerName = getPlayer().getName();
        CrateSettings crateSettings = playerDataManager.getCrateSettings(getCrate().getName());

        PlayerProfile playerProfile = playerDataManager.getPlayerProfile(playerName, crateSettings, false);
        PlayerBaseProfile baseProfile = this.plugin.getBaseProfileManager().getPlayerBaseProfile(playerName);

        String chosenReward = playerProfile.getChosenReward();
        GachaType gachaType = crateSettings.getGachaType();

        if (!gachaType.equals(GachaType.NORMAL) && (chosenReward == null || chosenReward.isEmpty())) {
            getPlayer().sendMessage("<red>You have not chosen a reward yet. Please choose a reward using menu");
            crateManager.removePlayerFromOpeningList(getPlayer());
            return;
        }

        System.out.println("Chosen reward: " + chosenReward);

        boolean sneak = getPlayer().isSneaking();
        int amount = sneak ? 10 : 1;

        boolean keyCheck = this.userManager.takeKeys(getPlayer().getUniqueId(), getCrate().getName(), type, amount, checkHand);

        if (!keyCheck) {
            // Send the message about failing to take the key.
            MiscUtils.failedToTakeKey(getPlayer(), getCrate().getName());
            // Remove from opening list.
            this.crateManager.removePlayerFromOpeningList(getPlayer());
            return;
        }

        List<Result> items = new ArrayList<>();

        Prize prize = crateSettings.findLegendary(chosenReward);

        if (prize == null && !gachaType.equals(GachaType.NORMAL)) {
            throw new IllegalStateException("Chosen reward not found");
        }

        Map<Rarity, RaritySettings> rarityMap = crateSettings.getRarityMap();

        int stellarShards = 0;
        int mysticTokens = 0;

        while (amount-- > 0) {
            Result result = switch (gachaType) {
                case NORMAL -> gachaSystem.roll(playerProfile, crateSettings);
                case FATE_POINT -> gachaSystem.rollWithFatePoint(playerProfile, crateSettings, prize);
                case OVERRIDE -> gachaSystem.rollOverrideSet(playerProfile, crateSettings, prize);
            };

            System.out.println(result);

            Rarity rarity = result.getRarity();
            stellarShards += rarityMap.get(rarity).stellarShards();
            mysticTokens += rarityMap.get(rarity).mysticTokens();

            items.add(result);
        }

        baseProfile.addMysticTokens(mysticTokens);
        baseProfile.addStellarShards(stellarShards);

        //setItem(8, new ItemBuilder(Material.PLAYER_HEAD).setDisplayName("<green>Skip").getStack());

        addCrateTask(new RouletteStandard(this, items, sneak).runAtFixedRate(this.plugin, 1, 2));

        playerDataManager.savePlayerProfile(playerName, crateSettings, playerProfile);
    }

    @Override
    public void run() {

    }
}

package com.badbones69.crazycrates.tasks.crates.types;

import com.badbones69.crazycrates.api.builders.CrateBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.data.*;
import com.badbones69.crazycrates.api.objects.gacha.enums.GachaType;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.gacha.GachaSystem;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.ItemRepo;
import com.badbones69.crazycrates.managers.BukkitUserManager;
import com.badbones69.crazycrates.managers.events.enums.EventType;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import com.badbones69.crazycrates.tasks.crates.types.roulette.RouletteStandard;
import com.badbones69.crazycrates.utils.MiscUtils;
import org.bukkit.Material;
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
    public void open(@NotNull KeyType type, boolean checkHand, boolean isSilent, EventType eventType) {
        // Crate event failed so we return.
        if (isCrateEventValid(type, checkHand, isSilent, eventType)) {
            return;
        }

        String playerName = getPlayer().getName();
        CrateSettings crateSettings = playerDataManager.getCrateSettings(getCrate().getFileName());

        PlayerProfile playerProfile = playerDataManager.getPlayerProfile(playerName, crateSettings, false);
        PlayerBaseProfile baseProfile = this.plugin.getBaseProfileManager().getPlayerBaseProfile(playerName);

        String chosenReward = playerProfile.getChosenReward();
        GachaType gachaType = crateSettings.getGachaType();

        if (!gachaType.equals(GachaType.NORMAL) && (chosenReward == null || chosenReward.isEmpty())) {
            getPlayer().sendMessage("<red>You have not chosen a reward yet. Please choose a reward using menu");
            crateManager.removePlayerFromOpeningList(getPlayer());
            return;
        }

        boolean sneak = getPlayer().isSneaking();
        int amount = sneak ? 10 : 1;

        boolean keyCheck = this.userManager.takeKeys(getPlayer().getUniqueId(), getCrate().getFileName(), type, amount, checkHand);

        if (!keyCheck) {
            MiscUtils.failedToTakeKey(getPlayer(), getCrate().getFileName());
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

            Rarity rarity = result.getRarity();
            stellarShards += rarityMap.get(rarity).stellarShards();
            mysticTokens += rarityMap.get(rarity).mysticTokens();

            items.add(result);
        }

        baseProfile.addMysticTokens(mysticTokens);
        baseProfile.addStellarShards(stellarShards);

        setItem(37, ItemRepo.BORDER.asItemStack());
        setItem(8, new ItemBuilder(Material.PLAYER_HEAD).setDisplayName("<green>Skip").setCustomModelData(1000002).asItemStack());

        addCrateTask(new RouletteStandard(this, items, sneak).runAtFixedRate(this.plugin, 1, 2));

        playerDataManager.savePlayerProfile(playerName, crateSettings, playerProfile);
    }

    @Override
    public void run() {

    }
}

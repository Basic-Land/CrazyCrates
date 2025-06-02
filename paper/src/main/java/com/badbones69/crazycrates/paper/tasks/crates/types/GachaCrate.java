package com.badbones69.crazycrates.paper.tasks.crates.types;

import com.badbones69.crazycrates.paper.api.builders.CrateBuilder;
import com.badbones69.crazycrates.paper.api.builders.LegacyItemBuilder;
import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.Prize;
import com.badbones69.crazycrates.paper.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.Result;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.GachaType;
import com.badbones69.crazycrates.paper.api.objects.gacha.gacha.GachaSystem;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.ItemRepo;
import com.badbones69.crazycrates.paper.managers.BukkitUserManager;
import com.badbones69.crazycrates.paper.managers.events.enums.EventType;
import com.badbones69.crazycrates.paper.tasks.crates.CrateManager;
import com.badbones69.crazycrates.paper.tasks.crates.types.roulette.RouletteStandard;
import com.badbones69.crazycrates.paper.utils.MiscUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazycrates.api.enums.types.KeyType;

import java.util.ArrayList;
import java.util.List;

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

        int stellarShards = 0;
        int mysticTokens = 0;

        while (amount-- > 0) {
            Result result = switch (gachaType) {
                case NORMAL -> gachaSystem.roll(playerProfile, crateSettings);
                case FATE_POINT -> gachaSystem.rollWithFatePoint(playerProfile, crateSettings, prize);
                case OVERRIDE -> gachaSystem.rollOverrideSet(playerProfile, crateSettings, prize);
            };

            stellarShards += result.getStellar();
            mysticTokens += result.getMystic();

            items.add(result);
        }

        baseProfile.addMysticTokens(mysticTokens);
        baseProfile.addStellarShards(stellarShards);

        setItem(37, ItemRepo.BORDER.asItemStack());
        setItem(8, new LegacyItemBuilder(ItemType.PLAYER_HEAD).setDisplayName("<green>Skip").setCustomModelData(1000002).asItemStack());

        addCrateTask(new RouletteStandard(this, items, sneak).runAtFixedRate(1, 2));

        playerDataManager.savePlayerProfile(playerName, crateSettings, playerProfile);
    }

    @Override
    public void run() {

    }
}

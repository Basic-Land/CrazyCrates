package com.badbones69.crazycrates.api.objects.gacha.gacha;

import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.api.objects.gacha.data.RaritySettings;
import com.badbones69.crazycrates.api.objects.gacha.data.Result;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.ResultType;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.badbones69.crazycrates.CrazyCrates.LOGGER;

public class GachaSystem {
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    public GachaSystem() {
    }

    public Result getResult(PlayerProfile playerProfile, CrateSettings itemSet) {
        if (itemSet == null) throw new IllegalArgumentException("ItemSet cannot be null");

        playerProfile.incrementTotalPity();
        double roll = random.nextDouble(100);
        LOGGER.info("Roll: " + roll);
        double chance5050 = random.nextDouble(100);
        Rarity finalRarity = Rarity.COMMON;
        ResultType final5050 = ResultType.WON;

        Map<Rarity, RaritySettings> rarityMap = itemSet.getRarityMap();

        for (Map.Entry<Rarity, RaritySettings> entry : rarityMap.entrySet()) {
            Rarity rarity = entry.getKey();
            RaritySettings raritySettings = entry.getValue();

            Pair<Integer, ResultType> pair = playerProfile.getPity(rarity);

            int currentRarityPity = pair.first() + 1;
            ResultType last5050 = pair.second();

            boolean softPityUse = raritySettings.softPityGen(currentRarityPity);
            double softPity = raritySettings.calculateSoftPityChance(currentRarityPity);

            if (roll <= raritySettings.baseChance() || (softPityUse && softPity >= roll) || currentRarityPity >= raritySettings.pity()) {
                finalRarity = rarity;
                if (raritySettings.is5050Enabled()) {
                    if (last5050.equals(ResultType.LOST)) final5050 = ResultType.GUARANTEED;
                    else final5050 = chance5050 <= raritySettings.get5050Chance() ? ResultType.WON : ResultType.LOST;
                }
            }

            playerProfile.setPity(rarity, currentRarityPity, last5050);
        }

        Pair<Integer, ResultType> pair = playerProfile.getPity(finalRarity);
        playerProfile.setPity(finalRarity, 0, final5050);

        return new Result(finalRarity, final5050, pair.first());
    }

    public Result roll(PlayerProfile playerProfile, CrateSettings itemSet) {
        Result result = getResult(playerProfile, itemSet);
        result.setItemData(pickRandomPrice(result, itemSet, null));
        playerProfile.addHistory(result);
        return result;
    }

    public Result rollOverrideSet(PlayerProfile playerProfile, CrateSettings itemSet, Prize wantedItem) {
        Result result = getResult(playerProfile, itemSet);

        if (result.isLegendary()) {
            if (result.isWon5050()) {
                result.setItemData(wantedItem);
            } else {
                Set<Prize> set = itemSet.getBoth(Rarity.LEGENDARY);
                set.remove(wantedItem);
                result.setItemData(pickRandomPrice(result, null, set));
            }
        } else {
            result.setItemData(pickRandomPrice(result, itemSet, null));
        }

        playerProfile.addHistory(result);

        return result;
    }

    public Result rollWithFatePoint(PlayerProfile playerProfile, CrateSettings crateSettings, Prize wantedItem) {
        Result result = getResult(playerProfile, crateSettings);

        if (result.isLegendary()) {
            if (playerProfile.getFatePoint() >= crateSettings.getFatePointAmount()) {
                result.setItemData(wantedItem);
                result.setWon5050(ResultType.GUARANTEED);

                playerProfile.resetFatePoint();
                playerProfile.resetNextLegendaryLimited();

                playerProfile.addHistory(result);
                return result;
            }

            double chanceLimited = random.nextDouble(10000);

            if (chanceLimited <= 7500 || playerProfile.isNextLegendaryLimited()) {
                double chanceWanted = random.nextDouble(10000);

                if (chanceWanted <= 5000) {
                    result.setItemData(wantedItem);
                    result.setWon5050(ResultType.WON);
                    playerProfile.resetFatePoint();
                } else {
                    Set<Prize> limited = new HashSet<>(crateSettings.getLegendaryLimited());
                    limited.remove(wantedItem);

                    result.setItemData(pickRandomPrice(result, null, limited));
                    result.setWon5050(ResultType.WON_OF_RATE_UP);

                    playerProfile.incrementFatePoint();
                }
                playerProfile.resetNextLegendaryLimited();
            } else {
                result.setItemData(pickRandomPrice(result, null, crateSettings.getLegendaryStandard()));
                result.setWon5050(ResultType.LOST);

                playerProfile.setNextLegendaryLimited();
                playerProfile.incrementFatePoint();
            }
        } else {
            result.setItemData(pickRandomPrice(result, crateSettings, null));
        }

        playerProfile.addHistory(result);
        return result;
    }

    private Prize pickRandomPrice(Result result, CrateSettings itemSet, Set<Prize> overrideSet) {
        if (overrideSet != null)
            return overrideSet.stream().skip(random.nextInt(overrideSet.size())).findFirst().orElse(null);

        if (itemSet == null) return null;

        Set<Prize> itemData = result.isWon5050() ? itemSet.getLimited() : itemSet.getStandard();
        Set<Prize> set = itemData.stream().filter(item -> item.getRarity() == result.getRarity()).collect(Collectors.toSet());
        return set.stream().skip(random.nextInt(set.size())).findFirst().orElse(null);
    }
}


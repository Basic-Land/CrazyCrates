package com.badbones69.crazycrates.api.objects.gacha.gacha;

import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.api.objects.gacha.data.RaritySettings;
import com.badbones69.crazycrates.api.objects.gacha.data.Result;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import com.badbones69.crazycrates.api.objects.gacha.util.Rarity;
import cz.basicland.blibs.spigot.utils.item.CustomItemStack;

import java.util.*;

public class GachaSystem {
    private final Map<Rarity, RaritySettings> rarityMap = new LinkedHashMap<>();
    private final Random random = new Random(System.nanoTime() * new Random(System.nanoTime()).nextLong());

    public GachaSystem() {
        rarityMap.put(Rarity.COMMON, new RaritySettings(1, false, -1, 100, 1, -1, true, -1));     //70% - incl. guarantee - 53,2%
        //rarityMap.put("uncommon", new Rarity    (5, false, -1, 30,  5,  -1, true, -1));     //16% - incl. guarantee - 25,3%
        //rarityMap.put("rare", new Rarity        (10, true, 50, 10.6,  9,  56, true, -1));   //8,9% - incl. guarantee - 14,7%
        //rarityMap.put("epic", new Rarity        (40, true, 60, 5.1, 35, 12, false, 80));    //4,5% - incl. guarantee - 5,3%
        rarityMap.put(Rarity.EPIC, new RaritySettings(10, true, 50, 5.1, 9, 56.1, true, -1));    //4,5% - incl. guarantee - 5,3%
        rarityMap.put(Rarity.LEGENDARY, new RaritySettings(90, true, 50, 0.6, 74, 6, false, 100));    //0,6% - incl. guarantee - 1.6%
    }

    public Result getResult(PlayerProfile playerProfile, CrateSettings itemSet) {
        playerProfile.incrementTotalPity();
        double roll = random.nextDouble(100);
        double chance5050 = random.nextDouble(100);
        Rarity finalRarity = Rarity.COMMON;
        boolean final5050 = true;

        Map<Rarity, RaritySettings> rarityMap;

        if (itemSet == null) {
            rarityMap = this.rarityMap;
        } else {
            rarityMap = itemSet.getRarityMap();
        }

        for (Map.Entry<Rarity, RaritySettings> entry : rarityMap.entrySet()) {

            Rarity rarity = entry.getKey();
            RaritySettings raritySettings = entry.getValue();

            Pair<Integer, Boolean> pair = playerProfile.getPity(rarity);

            int currentRarityPity = pair.first() + 1;
            boolean last5050 = pair.second();

            boolean softPityUse = raritySettings.softPityGen(currentRarityPity);
            double softPity = raritySettings.calculateSoftPityChance(currentRarityPity);

            if (roll <= raritySettings.baseChance() || (softPityUse && softPity >= roll) || currentRarityPity >= raritySettings.pity()) {
                finalRarity = rarity;
                if (raritySettings.is5050Enabled()) {
                    if (!last5050) final5050 = true;
                    else final5050 = chance5050 <= raritySettings.get5050Chance();
                }
            }

            playerProfile.setPity(rarity, currentRarityPity, last5050);
        }

        Pair<Integer, Boolean> pair = playerProfile.getPity(finalRarity);
        playerProfile.setPity(finalRarity, 0, final5050);

        return new Result(finalRarity, final5050, pair.first());
    }

    public Result roll(PlayerProfile playerProfile, CrateSettings itemSet) {
        Result result = getResult(playerProfile, itemSet);
        result.setItem(pickRandomPrice(result, itemSet, null));
        playerProfile.addHistory(result);
        return result;
    }

    public Result rollOverrideSet(PlayerProfile playerProfile, CrateSettings itemSet, CustomItemStack wantedItem) {
        Result result = getResult(playerProfile, itemSet);

        if (result.isLegendary() && !result.isWon5050()) {
            Set<CustomItemStack> set = itemSet.getStandard().get(Rarity.LEGENDARY);
            set.addAll(itemSet.getLimited().get(Rarity.LEGENDARY));
            set.remove(wantedItem);
            result.setItem(set.stream().skip(random.nextInt(set.size())).findFirst().orElse(null));
            return result;
        } else {
            result.setItem(pickRandomPrice(result, itemSet, null));
        }
        playerProfile.addHistory(result);
        return result;
    }

    public Prize pickPrize(Crate crate) {
        List<Prize> prizes = new ArrayList<>(crate.getPrizes());
        Collections.shuffle(prizes);
        return prizes.stream().skip(random.nextInt(prizes.size())).findFirst().orElse(null);
    }

    public Result rollWithFatePoint(PlayerProfile playerProfile, CrateSettings itemSet, CustomItemStack wantedItem) {
        System.out.println("chosen reward: " + playerProfile.getChosenReward());
        System.out.println("fate point: " + playerProfile.getFatePoint());
        System.out.println("next legendary limited: " + playerProfile.isNextLegendaryLimited());

        Result result = getResult(playerProfile, itemSet);

        if (result.isLegendary()) {
            if (playerProfile.getFatePoint() >= itemSet.getFatePointAmount()) {
                result.setItem(wantedItem);
                playerProfile.resetFatePoint();
                playerProfile.resetNextLegendaryLimited();
                playerProfile.addHistory(result);

                return result;
            }

            double chanceLimited = random.nextDouble(10000);
            System.out.println("chance limited: " + chanceLimited / 10000 * 100 + "%");

            if (chanceLimited <= 7500 || playerProfile.isNextLegendaryLimited()) {
                double chanceWanted = random.nextDouble(10000);
                System.out.println("chance wanted: " + chanceWanted / 10000 * 100 + "%");

                if (chanceWanted <= 5000) {
                    result.setItem(wantedItem);
                    playerProfile.resetFatePoint();
                } else {
                    result.setItem(pickRandomPrice(result, null, itemSet.getLimited().get(result.getRarity())));
                    if (result.getItem().equals(wantedItem)) {
                        playerProfile.resetFatePoint();
                        playerProfile.resetNextLegendaryLimited();
                        playerProfile.addHistory(result);

                        return result;
                    }
                    playerProfile.incrementFatePoint();
                }
                playerProfile.resetNextLegendaryLimited();
            } else {
                result.setItem(pickRandomPrice(result, null, itemSet.getStandard().get(result.getRarity())));
                playerProfile.setNextLegendaryLimited();
                playerProfile.incrementFatePoint();
            }
        } else {
            result.setItem(pickRandomPrice(result, itemSet, null));
        }

        playerProfile.addHistory(result);
        return result;
    }

    private CustomItemStack pickRandomPrice(Result result, CrateSettings itemSet, Set<CustomItemStack> overrideSet) {
        random.setSeed(System.nanoTime() * new Random(System.nanoTime()).nextLong());
        if (overrideSet != null)
            return overrideSet.stream().skip(random.nextInt(overrideSet.size())).findFirst().orElse(null);

        if (itemSet == null) return null;

        Map<Rarity, Set<CustomItemStack>> map = result.isWon5050() ? itemSet.getLimited() : itemSet.getStandard();
        Set<CustomItemStack> set = map.get(result.getRarity());
        return set.stream().skip(random.nextInt(set.size())).findFirst().orElse(null);
    }
}


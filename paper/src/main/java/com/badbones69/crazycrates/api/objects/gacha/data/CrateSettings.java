package com.badbones69.crazycrates.api.objects.gacha.data;

import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.Tier;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.enums.GachaType;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.api.objects.gacha.util.ItemData;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import cz.basicland.blibs.spigot.utils.item.NBT;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@ToString
public class CrateSettings {
    private final String crateName;
    private final boolean fatePointEnabled, overrideEnabled, extraRewardEnabled;
    private final int fatePointAmount, bonusPity;

    private final List<ItemData> extraRewards = new ArrayList<>();

    private final Map<Rarity, RaritySettings> rarityMap = new LinkedHashMap<>();
    private final GachaType gachaType;
    private final Crate crate;

    public CrateSettings(FileConfiguration config, String crateName, Crate crate) {
        String path = "Crate.Gacha.settings";

        this.crateName = crateName;
        this.crate = crate;
        this.fatePointEnabled = config.getBoolean(path + ".fate-point.enabled");
        this.fatePointAmount = config.getInt(path + ".fate-point.amount");
        this.overrideEnabled = config.getBoolean(path + ".override");
        this.gachaType = GachaType.getType(fatePointEnabled, overrideEnabled);

        this.extraRewardEnabled = config.getBoolean(path + ".extra-reward.enabled");
        this.bonusPity = config.getInt(path + ".extra-reward.pity");

        for (Rarity rarity : Rarity.values()) {
            String rarityName = rarity.name().toLowerCase();
            path = "Crate.Gacha.rarity." + rarityName;
            if (!config.isConfigurationSection(path)) continue;

            RaritySettings raritySettings = new RaritySettings(
                    config.getInt(path + ".pity"),
                    config.getBoolean(path + ".5050.enabled"),
                    config.getInt(path + ".5050.chance"),
                    config.getDouble(path + ".base-chance"),
                    config.getInt(path + ".soft-pity.from"),
                    config.getDouble(path + ".soft-pity.formula"),
                    config.getBoolean(path + ".soft-pity.static"),
                    config.getInt(path + ".soft-pity.limit"),
                    config.getInt(path + ".mysticTokens"),
                    config.getInt(path + ".stellarShards")
            );

            rarityMap.put(rarity, raritySettings);
        }
    }

    public void loadItems(Crate crate, List<Prize> prizes, DatabaseManager databaseManager) {
        FileConfiguration config = crate.getFile();
        List<Tier> tiers = crate.getTiers();
        boolean emptyTiers = tiers.isEmpty();

        String path = "Crate.Gacha";
        RewardType type = RewardType.EXTRA_REWARD;
        ConfigurationSection section = config.getConfigurationSection(path + ".extra-reward");

        if (section != null) {
            for (int key : section.getIntegerList("items")) {
                Pair<Integer, ItemStack> pair = databaseManager.getItemManager().getItemFromCache(key);
                extraRewards.add(new ItemData(String.valueOf(key), Rarity.EXTRA_REWARD, type, pair.second()));
            }
        }

        int slot = 20;

        for (Map.Entry<Rarity, RaritySettings> entry : rarityMap.entrySet()) {
            Rarity rarity = entry.getKey();
            String rarityName = rarity.name().toLowerCase();
            RaritySettings raritySettings = entry.getValue();

            ItemBuilder tierStack = getTierItem(rarity, raritySettings);

            Tier tier;
            if (emptyTiers) {
                tier = new Tier(rarityName, raritySettings.baseChance(), slot, tierStack);
                tiers.add(tier);
            } else {
                tier = tiers.stream().filter(t -> t.getName().equals(rarityName)).findFirst().orElseThrow();
            }

            path = "Crate.Gacha.standard." + rarityName;
            type = RewardType.STANDARD;

            addItems(prizes, databaseManager, config, path, type, rarity, tier);

            path = "Crate.Gacha.limited." + rarityName;
            type = RewardType.LIMITED;

            addItems(prizes, databaseManager, config, path, type, rarity, tier);

            slot += 2;
        }
    }

    private void addItems(List<Prize> prizes, DatabaseManager databaseManager, FileConfiguration config, String path, RewardType type, Rarity rarity, Tier tier) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            if (key.equals("list")) {
                for (String id : config.getStringList(path + ".list")) {
                    item(id, databaseManager, type, rarity, tier, prizes, config, path);
                }
                continue;
            }
            item(key, databaseManager, type, rarity, tier, prizes, config, path);
        }
    }

    private void item(String key, DatabaseManager databaseManager, RewardType type, Rarity rarity, Tier tier, List<Prize> prizes, FileConfiguration config, String path) {
        String rewardName = key + "_" + type.name();
        int id = Integer.parseInt(key);

        ItemStack item = databaseManager.getItemManager().getItemFromCache(id).second();
        if (item == null) {
            return;
        }

        NBT nbt = new NBT(item);
        nbt.setString("rewardName", rewardName);

        List<String> commands = config.getStringList(path + "." + key + ".commands");
        List<String> messages = config.getStringList(path + "." + key + ".messages");
        boolean give = config.getBoolean(path + "." + key + ".give", true);

        prizes.add(new Prize(rewardName, crateName, tier, item, give, commands, messages, rarity, type));
    }

    public void addItem(RewardType type, int id, Rarity rarity, ItemStack stack, Crate crate) {
        String rewardName = id + "_" + type.name();

        NBT nbt = new NBT(stack);
        nbt.setString("rewardName", rewardName);

        ItemData itemData = new ItemData(rewardName, rarity, type, stack);

        if (type == RewardType.EXTRA_REWARD) {
            extraRewards.add(itemData);
            return;
        }

        crate.getPrizes().add(new Prize(rewardName, crateName, crate.getTier(rarity.name().toLowerCase()), stack, true, Collections.emptyList(), Collections.emptyList(), rarity, type));
    }

    @NotNull
    private ItemBuilder getTierItem(Rarity rarity, RaritySettings raritySettings) {
        ItemBuilder tierStack = new ItemBuilder().setMaterial(Material.CHEST);
        tierStack.setName(rarity.name());

        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.add("");
        lore.add("&8│ &fPity: &e" + raritySettings.pity());
        lore.add("&8│ &fZákladní šance: &e" + raritySettings.baseChance() + "&f%");

        lore.add("&8│ &f50/50 je: &e" + (raritySettings.is5050Enabled() ? "Zapnutá" : "Vypnutá"));
        if (raritySettings.is5050Enabled()) {
            lore.add("&8│ &f50/50 Šance: &e" + raritySettings.get5050Chance() + "&f% na výhru");
        }

        if (raritySettings.softPityFrom() != 1) {
            lore.add("&8│ &fSoft pity začíná od: &e" + raritySettings.softPityFrom() + " &fotevření");
            if (raritySettings.staticFormula()) {
                lore.add("&8│ &fa od &e" + raritySettings.softPityFrom() + "&f a výše je &e" + raritySettings.softPityFormula() + "&f%");
            } else {
                lore.add("&8│ &fSoft pity od &e" + raritySettings.softPityFrom() + "&f se zvyšuje o &e" + raritySettings.softPityFormula() + "&f% pokaždém otevření");
            }
            if (raritySettings.softPityLimit() != -1) {
                lore.add("&8│ &fMaximální šance pro soft pity je &e" + raritySettings.softPityLimit() + "&f%");
            }
        }
        lore.add("");

        int maxSize = 0;
        for (String s : lore) {
            if (s.length() > maxSize) {
                maxSize = s.length();
            }
        }

        lore.set(1, "&8┌" + "─".repeat(maxSize / 2));
        lore.set(lore.size() - 1, "&8└" + "─".repeat(maxSize / 2));


        tierStack.setLore(lore);
        return tierStack;
    }

    public List<Integer> getAllIDs() {
        return crate.getPrizes().stream().map(prize -> prize.getPrizeNumber().split("_")[0]).map(Integer::parseInt).toList();
    }

    public List<Integer> getIDsFromRarityType(Rarity rarity, RewardType type) {
        return crate.getPrizes().stream().filter(prize -> prize.getRarity() == rarity && prize.getType() == type).map(prize -> prize.getPrizeNumber().split("_")[0]).map(Integer::parseInt).toList();
    }

    public Prize findLegendary(String chosenReward) {
        return crate.getPrizes().stream().filter(prize -> prize.getPrizeNumber().equals(chosenReward)).findFirst().orElse(null);
    }

    public Set<Prize> getBoth(Rarity rarity) {
        HashSet<Prize> temp = new HashSet<>(crate.getPrizes());
        temp.removeIf(item -> item.getRarity() != rarity);
        return temp;
    }

    public Set<Prize> getLegendaryStandard() {
        RewardType type = RewardType.STANDARD;
        Rarity rarity = Rarity.LEGENDARY;
        return crate.getPrizes().stream().filter(item -> item.getRarity() == rarity && item.getType().equals(type)).collect(Collectors.toSet());
    }

    public Set<Prize> getLegendaryLimited() {
        RewardType type = RewardType.LIMITED;
        Rarity rarity = Rarity.LEGENDARY;
        return crate.getPrizes().stream().filter(item -> item.getRarity() == rarity && item.getType().equals(type)).collect(Collectors.toSet());
    }

    public Set<Prize> getLimited() {
        RewardType type = RewardType.LIMITED;
        return crate.getPrizes().stream().filter(item -> item.getType() == type).collect(Collectors.toSet());
    }

    public Set<Prize> getStandard() {
        RewardType type = RewardType.STANDARD;
        return crate.getPrizes().stream().filter(item -> item.getType() == type).collect(Collectors.toSet());
    }
}

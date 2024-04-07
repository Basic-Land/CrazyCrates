package com.badbones69.crazycrates.api.objects.gacha.data;

import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.Tier;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.enums.GachaType;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.api.objects.gacha.util.ItemData;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import cz.basicland.blibs.spigot.utils.item.CustomItemStack;
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

    private final Set<ItemData> standard = new HashSet<>();
    private final Set<ItemData> limited = new HashSet<>();
    private final Set<ItemData> extraRewards = new HashSet<>();

    private final Map<Rarity, RaritySettings> rarityMap = new LinkedHashMap<>();
    private final GachaType gachaType;

    public CrateSettings(FileConfiguration config, String crateName) {
        String path = "Crate.Gacha.settings";

        this.crateName = crateName;
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

        String path = "Crate.Gacha.settings";
        RewardType type = RewardType.EXTRA_REWARD;
        ConfigurationSection section = config.getConfigurationSection(path + ".extra-reward.items");

        if (section != null) {
            for (String key : section.getKeys(false)) {
                Pair<Integer, ItemStack> pair = databaseManager.getItemManager().getItemFromCache(type, Integer.parseInt(key));
                extraRewards.add(new ItemData(pair.first(), Rarity.EXTRA_REWARD, type, new CustomItemStack(pair.second()), true, Collections.emptyList(), Collections.emptyList()));
            }
        }

        int slot = 20;

        for (Map.Entry<Rarity, RaritySettings> entry : rarityMap.entrySet()) {
            Rarity rarity = entry.getKey();
            String rarityName = rarity.name().toLowerCase();
            RaritySettings raritySettings = entry.getValue();

            CustomItemStack tierStack = getCustomItemStack(rarity, raritySettings);

            Tier tier;
            if (emptyTiers) {
                tier = new Tier(rarityName, raritySettings.baseChance(), slot, tierStack);
                tiers.add(tier);
            } else {
                tier = tiers.stream().filter(t -> t.getName().equals(rarityName)).findFirst().orElseThrow();
            }

            path = "Crate.Gacha.standard." + rarityName;
            type = RewardType.STANDARD;

            addItems(prizes, databaseManager, config, path, type, rarity, tier, standard);

            path = "Crate.Gacha.limited." + rarityName;
            type = RewardType.LIMITED;

            addItems(prizes, databaseManager, config, path, type, rarity, tier, limited);

            slot += 2;
        }
    }

    private void addItems(List<Prize> prizes, DatabaseManager databaseManager, FileConfiguration config, String path, RewardType type, Rarity rarity, Tier tier, Set<ItemData> itemSet) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            if (key.equals("list")) {
                for (String id : config.getStringList(path + ".list")) {
                    item(id, databaseManager, type, rarity, tier, itemSet, prizes, config, path);
                }
                continue;
            }
            item(key, databaseManager, type, rarity, tier, itemSet, prizes, config, path);
        }
    }

    private void item(String key, DatabaseManager databaseManager, RewardType type, Rarity rarity, Tier tier, Set<ItemData> itemSet, List<Prize> prizes, FileConfiguration config, String path) {
        int id = Integer.parseInt(key);
        CustomItemStack stack = new CustomItemStack(databaseManager.getItemManager().getItemFromCache(type, id).second());
        stack.setString("type", type.name());
        stack.setInteger("itemID", id);
        List<String> commands = config.getStringList(path + "." + key + ".commands");
        List<String> messages = config.getStringList(path + "." + key + ".messages");
        boolean give = config.getBoolean(path + "." + key + ".give", true);
        ItemData data = new ItemData(id, rarity, type, stack, give, commands, messages);
        itemSet.add(data);
        prizes.add(new Prize(key, crateName, tier, stack, give, commands, messages));
    }

    public void addItem(RewardType type, int id, Rarity rarity, ItemStack stack, Crate crate) {
        CustomItemStack customItemStack = new CustomItemStack(stack);

        customItemStack.setString("type", type.name());
        customItemStack.setInteger("itemID", id);

        ItemData itemData = new ItemData(id, rarity, type, customItemStack, true, Collections.emptyList(), Collections.emptyList());

        switch (type) {
            case STANDARD -> standard.add(itemData);
            case LIMITED -> limited.add(itemData);
            case EXTRA_REWARD -> extraRewards.add(itemData);
        }

        crate.getPrizes().add(new Prize(String.valueOf(id), crateName, crate.getTier(rarity.name().toLowerCase()), customItemStack, true, Collections.emptyList(), Collections.emptyList()));
    }

    @NotNull
    private static CustomItemStack getCustomItemStack(Rarity rarity, RaritySettings raritySettings) {
        CustomItemStack tierStack = new CustomItemStack(Material.CHEST);
        tierStack.title(rarity.name());

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


        tierStack.lore(lore);
        return tierStack;
    }

    public ItemData findLegendary(boolean isLimited, boolean all, Pair<Integer, String> itemValues) {
        Rarity rarity = Rarity.LEGENDARY;
        if (all) {
            Set<ItemData> temp = getBoth(rarity);
            return temp.stream().filter(item -> Objects.equals(item.id(), itemValues.first())).findFirst().orElse(null);
        }

        if (isLimited) {
            return getLegendaryLimited().stream().filter(item -> Objects.equals(item.id(), itemValues.first())).findFirst().orElse(null);
        } else {
            return getLegendaryStandard().stream().filter(item -> Objects.equals(item.id(), itemValues.first())).findFirst().orElse(null);
        }
    }

    public Set<ItemData> getBoth(Rarity rarity) {
        HashSet<ItemData> temp = new HashSet<>(standard);
        temp.addAll(limited);
        temp.removeIf(item -> item.rarity() != rarity);
        return temp;
    }

    public Set<ItemData> getLegendaryStandard() {
        RewardType type = RewardType.STANDARD;
        return standard.stream().filter(item -> item.rarity() == Rarity.LEGENDARY && item.type().equals(type)).collect(Collectors.toSet());
    }

    public Set<ItemData> getLegendaryLimited() {
        RewardType type = RewardType.LIMITED;
        return limited.stream().filter(item -> item.rarity() == Rarity.LEGENDARY && item.type().equals(type)).collect(Collectors.toSet());
    }
}

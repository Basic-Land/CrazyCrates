package com.badbones69.crazycrates.api.objects.gacha.data;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.Tier;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.banners.BannerData;
import com.badbones69.crazycrates.api.objects.gacha.banners.BannerItem;
import com.badbones69.crazycrates.api.objects.gacha.banners.BannerPackage;
import com.badbones69.crazycrates.api.objects.gacha.enums.GachaType;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.api.objects.gacha.enums.Table;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.ComponentBuilder;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import com.badbones69.crazycrates.api.objects.gacha.util.TierInfo;
import com.ryderbelserion.vital.paper.files.config.CustomFile;
import com.ryderbelserion.vital.paper.files.config.FileManager;
import cz.basicland.blibs.spigot.utils.item.NBT;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@ToString
public class CrateSettings {
    private final CrazyCrates plugin = JavaPlugin.getPlugin(CrazyCrates.class);
    private final FileManager yamlManager = plugin.getFileManager();
    private final String crateName, bannerFile;
    private final boolean fatePointEnabled, overrideEnabled, extraRewardEnabled;
    private final int fatePointAmount, bonusPity, modelDataPreviewName, modelDataMainMenu;

    private final List<ItemStack> extraRewards = new ArrayList<>();

    private final Map<Rarity, RaritySettings> rarityMap = new LinkedHashMap<>();
    private final GachaType gachaType;
    private final Crate crate;
    private final BannerPackage bannerPackage;

    public CrateSettings(FileConfiguration config, String crateName, Crate crate) {
        String path = "Crate.Gacha.settings";

        this.crateName = crateName;
        this.crate = crate;
        this.bannerFile = config.getString(path + ".banner-file", "");
        this.fatePointEnabled = config.getBoolean(path + ".fate-point.enabled");
        this.fatePointAmount = config.getInt(path + ".fate-point.amount");
        this.overrideEnabled = config.getBoolean(path + ".override");
        this.modelDataPreviewName = config.getInt(path + ".model-data-preview-name");
        this.modelDataMainMenu = config.getInt(path + ".model-data-main-menu");

        this.bonusPity = config.getInt(path + ".bonus-pity");

        this.gachaType = GachaType.getType(fatePointEnabled, overrideEnabled);

        this.extraRewardEnabled = config.getBoolean(path + ".extra-reward.enabled");

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

        CustomFile customFile = yamlManager.getCustomFile(bannerFile);

        if (customFile == null) {
            bannerPackage = new BannerPackage(null, null, false);
            return;
        }

        YamlConfiguration yaml = customFile.getConfiguration();

        BannerData currentBanner = getBanner(yaml, "currentBanner");
        BannerData nextBanner = getBanner(yaml, "nextBanner");

        bannerPackage = new BannerPackage(currentBanner, nextBanner, true);
        updateItems();
    }

    private void updateItems() {
        BannerData currentBanner = bannerPackage.getBanner();
        if (currentBanner == null) return;

        FileConfiguration crateFile = crate.getFile();
        String path = "Crate.Gacha";
        crateFile.set(path + ".standard", null);
        crateFile.set(path + ".limited", null);

        List<BannerItem> items = currentBanner.items();

        for (BannerItem item : items) {
            int id = item.number();
            Rarity rarity = item.rarity();
            RewardType type = item.rewardType();
            List<String> commands = item.commands();
            List<String> messages = item.messages();
            boolean give = item.give();
            String rarityPath = path + "." + type.name().toLowerCase() + "." + rarity.name().toLowerCase();

            if (give && commands.isEmpty() && messages.isEmpty()) {
                String listPath = rarityPath + ".list";
                List<String> list = crateFile.getStringList(listPath);
                list.add(String.valueOf(id));
                crateFile.set(listPath, list);
                continue;
            }

            crateFile.set(rarityPath + "." + id + ".commands", commands);
            crateFile.set(rarityPath + "." + id + ".messages", messages);
            crateFile.set(rarityPath + "." + id + ".give", give);
        }

        crate.saveFile();

    }

    private BannerData getBanner(YamlConfiguration file, String banner) {
        String bannerName = file.getString("Banner.Name");

        String path = banner + ".duration.start";
        LocalDateTime start = getTime(file, path);

        path = banner + ".duration.end";
        LocalDateTime end = getTime(file, path);

        return new BannerData(bannerName, start, end, getItems(file, banner));
    }

    private List<BannerItem> getItems(YamlConfiguration file, String banner) {
        List<BannerItem> items = new ArrayList<>();
        ConfigurationSection section = file.getConfigurationSection(banner + ".items");
        if (section == null) return items;

        addItems(file, banner, RewardType.STANDARD, items);
        addItems(file, banner, RewardType.LIMITED, items);

        return items;
    }

    private void addItems(YamlConfiguration file, String banner, RewardType type, List<BannerItem> items) {
        ConfigurationSection section = file.getConfigurationSection(banner + ".items." + type.name().toLowerCase());

        if (section != null) {
            for (String rarity : section.getKeys(false)) {
                ConfigurationSection raritySection = section.getConfigurationSection(rarity.toLowerCase());

                if (raritySection == null) continue;

                Rarity rarityEnum = Rarity.valueOf(rarity.toUpperCase());

                for (String key : raritySection.getKeys(false)) {
                    if (key.equals("list")) {
                        for (String id : raritySection.getStringList("list")) {
                            items.add(new BannerItem(rarityEnum, type, Integer.parseInt(id), Collections.emptyList(), Collections.emptyList(), true));
                        }
                        continue;
                    }

                    List<String> commands = raritySection.getStringList(key + ".commands");
                    List<String> messages = raritySection.getStringList(key + ".messages");
                    boolean give = raritySection.getBoolean(key + ".give", true);
                    items.add(new BannerItem(rarityEnum, type, Integer.parseInt(key), commands, messages, give));
                }
            }
        }
    }

    private LocalDateTime getTime(YamlConfiguration file, String path) {
        int year, month, day, hour, minute;
        year = file.getInt(path + ".year");
        month = file.getInt(path + ".month");
        day = file.getInt(path + ".day");
        hour = file.getInt(path + ".hour");
        minute = file.getInt(path + ".minute");
        return LocalDateTime.of(year, month, day, hour, minute);
    }

    public void loadItems(Crate crate, List<Prize> prizes, DatabaseManager databaseManager) {
        FileConfiguration config = crate.getFile();
        List<Tier> tiers = crate.getTiers();
        boolean emptyTiers = tiers.isEmpty();

        String path = "Crate.Gacha";
        ConfigurationSection section = config.getConfigurationSection(path + ".extra-reward");

        if (section != null) {
            for (int key : section.getIntegerList("items")) {
                ItemStack item = databaseManager.getItemManager().getItemFromCache(key, Table.ALL_ITEMS);
                extraRewards.add(item);
            }
        }

        int slot = 23 - rarityMap.size();

        for (Map.Entry<Rarity, RaritySettings> entry : rarityMap.entrySet()) {
            Rarity rarity = entry.getKey();
            String rarityName = rarity.name().toLowerCase();
            RaritySettings raritySettings = entry.getValue();

            TierInfo tierInfo = getTierInfo(rarity, raritySettings);

            Tier tier;
            if (emptyTiers) {
                tier = new Tier(rarityName, raritySettings.baseChance(), slot, tierInfo);
                tiers.add(tier);
            } else {
                tier = tiers.stream().filter(t -> t.getName().equals(rarityName)).findFirst().orElseThrow();
            }

            path = "Crate.Gacha.standard." + rarityName;
            RewardType type = RewardType.STANDARD;

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

        ItemStack item = databaseManager.getItemManager().getItemFromCache(id, Table.ALL_ITEMS);
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

        if (type.equals(RewardType.EXTRA_REWARD)) {
            extraRewards.add(stack);
            return;
        }

        crate.getPrizes().add(new Prize(rewardName, crateName, crate.getTier(rarity.name().toLowerCase()), stack, true, Collections.emptyList(), Collections.emptyList(), rarity, type));
    }

    @NotNull
    private TierInfo getTierInfo(Rarity rarity, RaritySettings raritySettings) {
        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.add("<dark_gray>│ <white>Pity: <yellow>" + raritySettings.pity());
        lore.add("<dark_gray>│ <white>Základní šance: <yellow>" + raritySettings.baseChance() + "<white>%");

        lore.add("<dark_gray>│ <white>50/50 je: <yellow>" + (raritySettings.is5050Enabled() ? "Zapnutá" : "Vypnutá"));
        if (raritySettings.is5050Enabled()) {
            lore.add("<dark_gray>│ <white>50/50 Šance: <yellow>" + raritySettings.get5050Chance() + "<white>% na výhru");
        }

        if (raritySettings.softPityFrom() != 1) {
            lore.add("<dark_gray>│ <white>Soft pity začíná od: <yellow>" + raritySettings.softPityFrom() + " <white>otevření");
            if (raritySettings.staticFormula()) {
                lore.add("<dark_gray>│ <white>a od <yellow>" + raritySettings.softPityFrom() + "<white> a výše je <yellow>" + raritySettings.softPityFormula() + "<white>%");
            } else {
                lore.add("<dark_gray>│ <white>Soft pity od <yellow>" + raritySettings.softPityFrom() + "<white> se zvyšuje o <yellow>" + raritySettings.softPityFormula() + "<white>% pokaždém otevření");
            }
            if (raritySettings.softPityLimit() != -1) {
                lore.add("<dark_gray>│ <white>Maximální šance pro soft pity je <yellow>" + raritySettings.softPityLimit() + "<white>%");
            }
        }

        int maxSize = 0;
        int size;
        for (String s : lore) {
            size = ComponentBuilder.getSize(s);
            if (size > maxSize) {
                maxSize = size;
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<dark_gray>┌");

        int i = 6;
        while (i < maxSize) {
            sb.append("─");
            i += 9;
        }

        lore.add(1, sb.toString());

        sb.setCharAt(11, '└');

        lore.add(sb.toString());

        return new TierInfo(Material.CHEST, rarity.name(), lore, rarity.getModelData());
    }

    public List<Integer> getAllIDs() {
        return crate.getPrizes().stream().map(prize -> prize.getSectionName().split("_")[0]).map(Integer::parseInt).toList();
    }

    public List<Integer> getIDsFromRarityType(Rarity rarity, RewardType type) {
        return crate.getPrizes().stream().filter(prize -> prize.getRarity().equals(rarity) && prize.getType().equals(type)).map(prize -> prize.getSectionName().split("_")[0]).map(Integer::parseInt).toList();
    }

    public Prize findLegendary(String chosenReward) {
        return crate.getPrizes().stream().filter(prize -> prize.getSectionName().equals(chosenReward)).findFirst().orElse(null);
    }

    public Set<Prize> getBoth(Rarity rarity) {
        HashSet<Prize> temp = new HashSet<>(crate.getPrizes());
        temp.removeIf(item -> item.getRarity() != rarity);
        return temp;
    }

    public Set<Prize> getLegendaryStandard() {
        RewardType type = RewardType.STANDARD;
        Rarity rarity = Rarity.LEGENDARY;
        return crate.getPrizes().stream().filter(item -> item.getRarity().equals(rarity) && item.getType().equals(type)).collect(Collectors.toSet());
    }

    public Set<Prize> getLegendaryLimited() {
        RewardType type = RewardType.LIMITED;
        Rarity rarity = Rarity.LEGENDARY;
        return crate.getPrizes().stream().filter(item -> item.getRarity().equals(rarity) && item.getType().equals(type)).collect(Collectors.toSet());
    }

    public Set<Prize> getLimited() {
        RewardType type = RewardType.LIMITED;
        return crate.getPrizes().stream().filter(item -> item.getType().equals(type)).collect(Collectors.toSet());
    }

    public Set<Prize> getStandard() {
        RewardType type = RewardType.STANDARD;
        return crate.getPrizes().stream().filter(item -> item.getType().equals(type)).collect(Collectors.toSet());
    }
}

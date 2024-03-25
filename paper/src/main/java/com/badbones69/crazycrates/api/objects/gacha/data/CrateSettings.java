package com.badbones69.crazycrates.api.objects.gacha.data;

import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.Tier;
import com.badbones69.crazycrates.api.objects.gacha.gacha.GachaType;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import com.badbones69.crazycrates.api.objects.gacha.util.Rarity;
import cz.basicland.blibs.shared.dataholder.Config;
import cz.basicland.blibs.spigot.utils.item.CustomItemStack;
import cz.basicland.blibs.spigot.utils.item.ItemUtils;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

import java.awt.*;
import java.util.*;
import java.util.List;

@Getter
@ToString
public class CrateSettings {
    private final String name;
    private final boolean fatePointEnabled, overrideEnabled, extraRewardEnabled;
    private final int fatePointAmount, bonusPity;
    private final Map<Rarity, Set<CustomItemStack>> standard = new HashMap<>();
    private final Map<Rarity, Set<CustomItemStack>> limited = new HashMap<>();
    private final Map<Rarity, RaritySettings> rarityMap = new LinkedHashMap<>();
    private final Map<String, CustomItemStack> extraRewards = new HashMap<>();
    private final GachaType gachaType;

    public CrateSettings(Config config, String name, List<Prize> prizes, List<Tier> tiers) {
        String path = "Crate.Gacha.settings";

        this.name = name;
        this.fatePointEnabled = config.getBoolean(path + ".fate-point.enabled");
        this.fatePointAmount = config.getInt(path + ".fate-point.amount");
        this.overrideEnabled = config.getBoolean(path + ".override");
        this.gachaType = GachaType.getType(fatePointEnabled, overrideEnabled);

        this.extraRewardEnabled = config.getBoolean(path + ".extra-reward.enabled");
        this.bonusPity = config.getInt(path + ".extra-reward.pity");

        for (String itemPath : config.getKeys(path + ".extra-reward.items")) {
            CustomItemStack customItemStack = ItemUtils.get(config, path + ".extra-reward.items." + itemPath);
            extraRewards.put(itemPath, customItemStack);
        }

        int slot = 20;

        for (Rarity rarity : Rarity.values()) {
            String rarityName = rarity.name().toLowerCase();
            path = "Crate.Gacha.rarity." + rarityName;
            if (!config.exists(path)) continue;

            RaritySettings raritySettings = new RaritySettings(
                    config.getInt(path + ".pity"),
                    config.getBoolean(path + ".5050.enabled"),
                    config.getInt(path + ".5050.chance"),
                    config.getDouble(path + ".base-chance"),
                    config.getInt(path + ".soft-pity.from"),
                    config.getDouble(path + ".soft-pity.formula"),
                    config.getBoolean(path + ".soft-pity.static"),
                    config.getInt(path + ".soft-pity.limit")
            );

            rarityMap.put(rarity, raritySettings);

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

            Tier tier = new Tier(rarityName, raritySettings.baseChance(), slot, tierStack);

            tiers.add(tier);

            path = "Crate.Gacha.standard." + rarityName;

            for (String itemPath : config.getKeys(path)) {
                CustomItemStack customItemStack = ItemUtils.get(config, path + "." + itemPath);
                customItemStack.setString("type", "standard");
                customItemStack.setString("itemName", itemPath);
                standard.computeIfAbsent(rarity, k -> new HashSet<>()).add(customItemStack);
                prizes.add(new Prize(customItemStack.getTitle(), itemPath, name, tier, customItemStack));
            }

            path = "Crate.Gacha.limited." + rarityName;

            for (String itemPath : config.getKeys(path)) {
                CustomItemStack customItemStack = ItemUtils.get(config, path + "." + itemPath);
                customItemStack.setString("type", "limited");
                customItemStack.setString("itemName", itemPath);
                limited.computeIfAbsent(rarity, k -> new HashSet<>()).add(customItemStack);
                prizes.add(new Prize(customItemStack.getTitle(), itemPath, name, tier, customItemStack));
            }
            slot += 2;
        }
    }

    public CustomItemStack find(boolean isLimited, boolean all, Pair<String, String> itemValues) {
        Rarity rarity = Rarity.LEGENDARY;
        if (all) {
            Set<CustomItemStack> temp = getBoth(rarity);
            return temp.stream().filter(item -> {
                String itemName = item.getString("itemName");
                String type = item.getString("type");
                return itemValues.first().equals(type) && itemValues.second().equals(itemName);
            }).findFirst().orElse(null);
        }

        if (isLimited) {
            return limited.get(rarity).stream().filter(item -> item.getString("itemName").equals(itemValues.second())).findFirst().orElse(null);
        } else {
            return standard.get(rarity).stream().filter(item -> item.getString("itemName").equals(itemValues.second())).findFirst().orElse(null);
        }
    }

    public Set<CustomItemStack> getBoth(Rarity rarity) {
        HashSet<CustomItemStack> customItemStacks = new HashSet<>(standard.get(rarity));
        customItemStacks.addAll(limited.get(rarity));
        return customItemStacks;
    }

    private TextColor[] generateColors(Color start, Color end, int colorCount) {
        int rStep = (end.getRed() - start.getRed()) / (colorCount - 1);
        int gStep = (end.getGreen() - start.getGreen()) / (colorCount - 1);
        int bStep = (end.getBlue() - start.getBlue()) / (colorCount - 1);

        TextColor[] colors = new TextColor[colorCount];
        for (int i = 0; i < colorCount; i++) {
            int r = start.getRed() + (i * rStep);
            int g = start.getGreen() + (i * gStep);
            int b = start.getBlue() + (i * bStep);
            colors[i] = TextColor.color(r, g, b);
        }

        return colors;
    }
}

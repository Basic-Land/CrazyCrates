package com.badbones69.crazycrates.api.objects;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.enums.PersistentKeys;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.api.utils.ItemUtils;
import com.badbones69.crazycrates.api.utils.MiscUtils;
import com.ryderbelserion.vital.paper.builders.items.ItemBuilder;
import com.ryderbelserion.vital.paper.util.ItemUtil;
import cz.basicland.blibs.spigot.utils.item.NBT;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Prize {

    private final CrazyCrates plugin = JavaPlugin.getPlugin(CrazyCrates.class);

    private final ConfigurationSection section;
    private final List<ItemBuilder> builders;
    private final List<String> commands;
    private final List<String> messages;
    private final String sectionName;
    private final String prizeName;

    private List<String> permissions = new ArrayList<>();
    private ItemBuilder displayItem = new ItemBuilder();
    private ItemBuilder prizeItem = new ItemBuilder();
    private boolean firework = false;
    private String crateName = "";
    private int maxRange = 100;
    private double chance = 0;

    private List<Tier> tiers = new ArrayList<>();
    private Prize alternativePrize;
    @Getter
    private Rarity rarity;
    @Getter
    private RewardType type;

    public Prize(@NotNull final ConfigurationSection section, @NotNull final List<Tier> tierPrizes, @NotNull final String crateName, @Nullable final Prize alternativePrize) {
        this.section = section;

        this.sectionName = section.getName();

        this.crateName = crateName;

        this.builders = ItemUtils.convertStringList(this.section.getStringList("Items"), this.sectionName);

        this.tiers = tierPrizes;

        this.alternativePrize = alternativePrize;

        Material material = null;

        if (section.contains("DisplayItem")) {
            material = ItemUtil.getMaterial(section.getString("DisplayItem", "stone"));
        }

        // Only run this if DisplayItem isn't found.
        if (section.contains("DisplayData") && !section.contains("DisplayItem")) {
            material = ItemUtil.fromBase64(section.getString("DisplayData", "")).getType();
        }

        String key = "";

        if (material != null) {
            key = material.isBlock() ? "<lang:" + material.getBlockTranslationKey() + ">" : "<lang:" + material.getItemTranslationKey() + ">";
        }

        this.prizeName = section.getString("DisplayName", key.isBlank() ? "<red>No valid display name found." : key);
        this.maxRange = section.getInt("MaxRange", 100);
        this.chance = section.getDouble("Chance", 50);
        this.firework = section.getBoolean("Firework", false);

        this.messages = section.getStringList("Messages"); // this returns an empty list if not found anyway.
        this.commands = section.getStringList("Commands"); // this returns an empty list if not found anyway.

        this.permissions = section.getStringList("BlackListed-Permissions"); // this returns an empty list if not found anyway.

        if (!this.permissions.isEmpty()) {
            this.permissions.replaceAll(String::toLowerCase);
        }

        this.prizeItem = display();
        this.displayItem = new ItemBuilder(this.prizeItem, true);
    }

    public Prize(String prizeNumber, String crateName, Tier tier, ItemStack stack, boolean give, List<String> messages, List<String> commands, Rarity rarity, RewardType type) {
        this.section = null;
        this.rarity = rarity;
        this.type = type;
        this.sectionName = prizeNumber;

        this.crateName = crateName;

        ItemStack itemStack = stack.clone();
        NBT nbt = new NBT(itemStack);
        nbt.remove("rewardName");

        this.builders = give ? Collections.singletonList(new ItemBuilder(itemStack).setAmount(itemStack.getAmount())) : Collections.emptyList();

        this.tiers = Collections.singletonList(tier);

        this.alternativePrize = null;

        this.prizeName = stack.getItemMeta().getDisplayName();
        this.maxRange = 100;
        this.chance = 100;
        this.firework = false;

        this.messages = messages;
        this.commands = commands;

        this.permissions = Collections.emptyList();

        ItemBuilder display = new ItemBuilder(stack.clone());
        List<String> updatedLore = display.getStack().getLore();
        if (updatedLore == null) updatedLore = new ArrayList<>();
        updatedLore.addFirst("");
        updatedLore.addFirst(type.name());
        display.setDisplayLore(updatedLore.stream().map(s -> s.replace("§", "&")).toList());

        this.displayItem = display;
        this.prizeItem = display;
    }

    /**
     * Create a new prize.
     * This option is used only for Alternative Prizes.
     *
     * @param section the configuration section.
     */
    public Prize(@NotNull final String prizeName, @NotNull final String sectionName, @NotNull final ConfigurationSection section) {
        this.prizeName = prizeName;

        this.messages = section.getStringList("Messages"); // this returns an empty list if not found anyway.
        this.commands = section.getStringList("Commands"); // this returns an empty list if not found anyway.

        this.sectionName = sectionName;

        this.section = section;

        this.builders = ItemUtils.convertStringList(this.section.getStringList("Items"), this.sectionName);
    }

    /**
     * @return the name of the prize.
     */
    public @NotNull final String getPrizeName() {
        return this.prizeName;
    }

    /**
     * @return the section name.
     */
    public @NotNull final String getSectionName() {
        return this.sectionName;
    }

    /**
     * @return the display item that is shown for the preview and the winning prize.
     */
    public @NotNull final ItemStack getDisplayItem() {
        return this.displayItem.setPersistentString(PersistentKeys.crate_prize.getNamespacedKey(), this.sectionName).getStack();
    }

    /**
     * @return the display item that is shown for the preview and the winning prize.
     */
    public @NotNull final ItemStack getDisplayItem(@NotNull final Player player) {
        return this.displayItem.setPlayer(player).setPersistentString(PersistentKeys.crate_prize.getNamespacedKey(), this.sectionName).getStack();
    }

    /**
     * @return the ItemBuilder of the display item.
     */
    public @NotNull final ItemBuilder getPrizeItem() {
        return this.prizeItem;
    }
    
    /**
     * @return the list of tiers the prize is in.
     */
    public @NotNull final List<Tier> getTiers() {
        return this.tiers;
    }
    
    /**
     * @return the messages sent to the player.
     */
    public @NotNull final List<String> getMessages() {
        return this.messages;
    }
    
    /**
     * @return the commands that are run when the player wins.
     */
    public @NotNull final List<String> getCommands() {
        return this.commands;
    }
    
    /**
     * @return the ItemBuilders for all the custom items made from the Items: option.
     */
    public @NotNull final List<ItemBuilder> getItemBuilders() {
        return this.builders;
    }
    
    /**
     * @return the name of the crate the prize is in.
     */
    public @NotNull final String getCrateName() {
        return this.crateName;
    }
    
    /**
     * @return the chance the prize has of being picked.
     */
    public final double getChance() {
        return this.chance;
    }
    
    /**
     * @return the max range of the prize.
     */
    public final int getMaxRange() {
        return this.maxRange;
    }
    
    /**
     * @return true if a firework explosion is played and false if not.
     */
    public final boolean useFireworks() {
        return this.firework;
    }
    
    /**
     * @return the alternative prize the player wins if they have a blacklist permission.
     */
    public @NotNull final Prize getAlternativePrize() {
        return this.alternativePrize;
    }
    
    /**
     * @return true if the prize doesn't have an alternative prize and false if it does.
     */
    public final boolean hasAlternativePrize() {
        return this.alternativePrize == null;
    }
    
    /**
     * @return true if they prize has blacklist permissions and false if not.
     */
    public final boolean hasPermission(@NotNull final Player player) {
        if (player.isOp()) return false;

        for (String permission : this.permissions) {
            if (player.hasPermission(permission)) return true;
        }

        return false;
    }

    private @NotNull ItemBuilder display() {
        ItemBuilder builder = new ItemBuilder();

        try {
            if (this.section.contains("DisplayData")) {
                builder = builder.fromBase64(this.section.getString("DisplayData"));
            }

            if (this.section.contains("DisplayItem")) {
                builder.withType(this.section.getString("DisplayItem", "red_terracotta"));
            }

            if (this.section.contains("DisplayAmount")) {
                builder.setAmount(this.section.getInt("DisplayAmount", 1));
            }

            builder.setDisplayName(this.prizeName);

            if (this.section.contains("DisplayLore") && !this.section.contains("Lore")) {
                builder.setDisplayLore(this.section.getStringList("DisplayLore"));
            }

            if (this.section.contains("Lore")) {
                if (MiscUtils.isLogging()) {
                    List.of(
                            "Detected deprecated usage of Lore in " + this.sectionName + ", please change Lore to DisplayLore",
                            "Lore will be removed in the next major version of Minecraft in favor of DisplayLore."
                    ).forEach(this.plugin.getLogger()::warning);
                }

                builder.setDisplayLore(this.section.getStringList("Lore"));
            }

            builder.setGlowing(this.section.contains("Glowing") ? section.getBoolean("Glowing") : null);

            builder.setDamage(this.section.getInt("DisplayDamage", 0));

            builder.addPatterns(this.section.getStringList("Patterns"));

            builder.setItemFlags(this.section.getStringList("Flags"));

            builder.setHidingItemFlags(this.section.getBoolean("HideItemFlags", false));

            builder.setUnbreakable(section.getBoolean("Unbreakable", false));

            if (this.section.contains("Skull") && this.plugin.getApi() != null) {
                builder.setSkull(section.getString("Skull", ""), this.plugin.getApi());
            }

            if (this.section.contains("Player")) {
                builder.setPlayer(this.section.getString("Player", ""));
            }

            if (this.section.contains("DisplayTrim.Pattern") && builder.isArmor()) {
                builder.applyTrimPattern(this.section.getString("DisplayTrim.Pattern", "sentry"));
            }

            if (this.section.contains("DisplayTrim.Material") && builder.isArmor()) {
                builder.applyTrimMaterial(this.section.getString("DisplayTrim.Material", "quartz"));
            }

            if (this.section.contains("DisplayEnchantments")) {
                for (String ench : this.section.getStringList("DisplayEnchantments")) {
                    String[] value = ench.split(":");

                    builder.addEnchantment(value[0], Integer.parseInt(value[1]), true);
                }
            }

            return builder;
        } catch (Exception exception) {
            return new ItemBuilder(Material.RED_TERRACOTTA).setDisplayName("<bold><red>ERROR</bold>").setDisplayLore(new ArrayList<>() {{
                add("<red>There was an error with one of your prizes!");
                add("<red>The reward in question is labeled: <yellow>" + section.getName() + " <red>in crate: <yellow>" + crateName);
                add("<red>Name of the reward is " + section.getString("DisplayName"));
                add("<red>If you are confused, Stop by our discord for support!");
            }});
        }
    }
}
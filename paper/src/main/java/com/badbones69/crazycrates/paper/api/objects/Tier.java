package com.badbones69.crazycrates.paper.api.objects;

import com.badbones69.crazycrates.paper.api.enums.other.keys.ItemKeys;
import com.badbones69.crazycrates.paper.api.builders.LegacyItemBuilder;
import com.badbones69.crazycrates.api.objects.gacha.util.TierInfo;
import com.ryderbelserion.fusion.core.util.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.configuration.ConfigurationSection;
import java.util.List;

public class Tier {

    private final LegacyItemBuilder item;
    private final String name;
    private final List<String> lore;
    private final String coloredName;
    private final double weight;
    private final int slot;

    public Tier(@NotNull final String tier, @NotNull final ConfigurationSection section) {
        this.name = tier;

        this.coloredName = section.getString("Name", "");

        this.lore = section.getStringList("Lore"); // this returns an empty list if not found anyway.

        this.item = new LegacyItemBuilder().withType(section.getString("Item", "chest").toLowerCase()).setHidingItemFlags(section.getBoolean("HideItemFlags", false)).setCustomModelData(section.getInt("Custom-Model-Data", -1));

        this.weight = section.getDouble("Weight", -1);

        this.slot = section.getInt("Slot");
    }

    public Tier(String tier, double chance, int slot, @NotNull TierInfo stack) {
        this.name = tier;
        this.coloredName = stack.name();
        this.lore = stack.lore();
        this.item = new ItemBuilder(stack.material()).setCustomModelData(stack.modelData());
        this.weight = chance;
        this.slot = slot;
    }

    /**
     * @return name of the tier.
     */
    public @NotNull final String getName() {
        return this.name;
    }

    /**
     * @return colored name of the tier.
     */
    public @NotNull final String getColoredName() {
        return this.coloredName;
    }

    /**
     * @return the colored glass pane.
     */
    public @NotNull final LegacyItemBuilder getItem() {
        return this.item;
    }

    /**
     * Get the total chance
     *
     * @return the total chance divided
     */
    public final double getWeight() {
        return this.weight;
    }

    /**
     * @return slot in the inventory.
     */
    public final int getSlot() {
        return this.slot;
    }

    /**
     * @return the tier item shown in the preview.
     */
    public @NotNull final ItemStack getTierItem(final @Nullable Player target, final Crate crate) {
        if (target != null) this.item.setPlayer(target);

        return this.item.setDisplayName(this.coloredName).setDisplayLore(this.lore).addLorePlaceholder("%chance%", StringUtils.format(crate.getTierChance(getWeight()))).setPersistentString(ItemKeys.crate_tier.getNamespacedKey(), this.name).asItemStack();
    }
}
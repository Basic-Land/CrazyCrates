package com.badbones69.crazycrates.api.objects;

import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.enums.PersistentKeys;
import com.ryderbelserion.cluster.utils.RegistryUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import us.crazycrew.crazycrates.platform.keys.KeyConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Key {

    private final String name;

    private final Material material;
    private final String itemName;
    private final List<String> lore;

    private final List<ItemFlag> flags;

    private final boolean isUnbreakable;

    private final boolean isGlowing;

    private final ItemBuilder itemStack;

    public Key(KeyConfig config) {
        this.name = config.getName();

        this.material = RegistryUtils.getMaterial(config.getMaterial().toLowerCase());

        this.itemName = config.getItemName();

        this.lore = config.getLore();

        if (config.getItemFlags() == null) {
            this.flags = Collections.emptyList();
        } else {
            this.flags = new ArrayList<>();

            config.getItemFlags().forEach(line -> this.flags.add(ItemFlag.valueOf(line)));
        }

        this.isUnbreakable = config.isUnbreakable();
        this.isGlowing = config.isGlowing();

        //todo() when checking for old keys, we need to check if the crate name matches still.
        this.itemStack = new ItemBuilder().setMaterial(getMaterial())
                // Bind the file name to the key item.
                .setString(PersistentKeys.crate_key.getNamespacedKey(), this.name)
                .setName(getItemName()).setLore(getLore()).setGlow(isGlowing()).setUnbreakable(isUnbreakable()).setItemFlags(getFlags());
    }

    /**
     * @return the name of the key.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the name of the key.
     */
    public Material getMaterial() {
        return this.material;
    }

    /**
     * @return the item name of the key.
     */
    public String getItemName() {
        return this.itemName;
    }

    /**
     * @return the lore of the key.
     */
    public List<String> getLore() {
        return this.lore;
    }

    /**
     * @return the flags of the key.
     */
    public List<ItemFlag> getFlags() {
        return this.flags;
    }

    /**
     * @return true or false.
     */
    public boolean isUnbreakable() {
        return this.isUnbreakable;
    }

    /**
     * @return true or false.
     */
    public boolean isGlowing() {
        return this.isGlowing;
    }

    /**
     * @return the key itemstack.
     */
    public ItemStack getKey() {
        return this.itemStack.build();
    }

    /**
     * Builds a key with placeholder api support.
     *
     * @param player the player associated with the key.
     * @return the key itemstack.
     */
    public ItemStack getKey(Player player) {
        return this.itemStack.setTarget(player).build();
    }

    /**
     * Builds a key with a specific amount of keys.
     *
     * @param amount the amount of keys to give.
     * @return the key itemstack.
     */
    public ItemStack getKey(int amount) {
        return this.itemStack.setAmount(amount).build();
    }

    /**
     * Builds a key with placeholder api support.
     *
     * @param amount the amount of keys to give.
     * @param player the player associated with the key.
     * @return the key itemstack.
     */
    public ItemStack getKey(int amount, Player player) {
        return this.itemStack.setAmount(amount).setTarget(player).build();
    }
}
package com.badbones69.crazycrates.paper.api.objects.gacha.constellation;

import com.badbones69.crazycrates.paper.CrazyCrates;
import cz.basicland.blibs.spigot.nms.containers.CompoundWrapper;
import cz.basicland.blibs.spigot.utils.item.NBT;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public record ItemConData(ItemStack stack, int constellation, String owner, Object2IntOpenHashMap<Enchantment> defEnchants, int defMaxDurability) {
    private static final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    public ItemConData(ItemStack itemStack) {
        this(itemStack, true);
    }

    public ItemConData(ItemStack stack, boolean increase) {
        NBT nbt = new NBT(stack);
        int constellation = nbt.getInteger("constellation");
        if (increase) nbt.setInteger("constellation", ++constellation);

        String owner = nbt.getString("owner");
        Object2IntOpenHashMap<Enchantment> defEnchants = new Object2IntOpenHashMap<>();
        defEnchants.defaultReturnValue(-1);
        nbt.getTags().values().stream().filter(tagValue -> {
            CompoundWrapper compoundWrapper = tagValue.compoundWrapper();
            return tagValue.dataType().isCompound() && compoundWrapper != null && compoundWrapper.compoundName().equals("gacha_default_enchants");
        }).findFirst().ifPresent(values -> {
            CompoundWrapper compoundWrapper = values.compoundWrapper();
            compoundWrapper.values().forEach(tagValue1 -> {
                NamespacedKey key = NamespacedKey.fromString(tagValue1.name());
                if (key == null) {
                    CrazyCrates.LOGGER.severe("Invalid enchantment key: " + tagValue1.name());
                    return;
                }
                defEnchants.put(registry.get(key), Integer.parseInt(tagValue1.value()));
            });
        });

        int defMaxDurability = nbt.getInteger("defMaxDurability");
        this(stack, constellation, owner, defEnchants, defMaxDurability);
    }

    @Override
    public @NonNull String toString() {
        return "ItemConData{" +
                "constellation=" + constellation +
                ", owner='" + owner + '\'' +
                ", defEnchants=" + defEnchants +
                ", defMaxDurability=" + defMaxDurability +
                '}';
    }
}

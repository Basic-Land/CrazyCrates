package com.badbones69.crazycrates.paper.api.objects.gacha.constellation;

import com.badbones69.crazycrates.paper.CrazyCrates;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("UnstableApiUsage")
public final class ConMethods {
    private static final NamespacedKey EFFICIENCY_KEY = NamespacedKey.fromString("effi", CrazyCrates.getPlugin());
    public static void unbreakingArmor(ItemConData itemConData) {
        ItemStack stack = itemConData.stack();
        int percentageIncrease = 15;
        int newVal = (int) (itemConData.defMaxDurability() + itemConData.constellation() * percentageIncrease * (5d + (percentageIncrease / 20d)));

        stack.setData(DataComponentTypes.MAX_DAMAGE, newVal);
        stack.setData(DataComponentTypes.DAMAGE, 0);
    }

    public static void unbreakingTools(ItemConData itemConData) {
        ItemStack stack = itemConData.stack();
        int defUnb = itemConData.defEnchants().getInt(Enchantment.UNBREAKING);
        if (defUnb == -1) return;
        stack.addUnsafeEnchantment(Enchantment.UNBREAKING, defUnb + (itemConData.constellation() / 2));
    }

    public static void efficiencyTools(ItemConData itemConData) {
        ItemStack stack = itemConData.stack();

        int levelStepping = 3;

        int constellation = itemConData.constellation();
        int defEffi = itemConData.defEnchants().getInt(Enchantment.EFFICIENCY);
        if (defEffi == -1) return;

        int addEffiLvl = constellation / levelStepping;
        int stepAdd = constellation % levelStepping;

        if (stepAdd == 0 && addEffiLvl > 0) {
            stack.addUnsafeEnchantment(Enchantment.EFFICIENCY, defEffi + addEffiLvl);
            stack.unsetData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        } else {
            int current = defEffi + addEffiLvl;
            int nextLvl = current + 1;
            double stepSize = ((nextLvl * nextLvl + 1) - (current * current + 1d)) / levelStepping;

            ItemAttributeModifiers modifiers = ItemAttributeModifiers.itemAttributes()
                    .addModifier(Attribute.MINING_EFFICIENCY, new AttributeModifier(EFFICIENCY_KEY, stepSize * stepAdd, AttributeModifier.Operation.ADD_NUMBER))
                    .build();

            stack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, modifiers);
        }
    }
}

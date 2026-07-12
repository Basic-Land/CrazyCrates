package com.badbones69.crazycrates.paper.api.objects.gacha.constellation;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.EnumSet;

public enum ConManager {
    SWORD(EnumSet.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.COPPER_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);
        }
    },
    AXE(EnumSet.of(Material.WOODEN_AXE, Material.STONE_AXE, Material.COPPER_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);
            ConMethods.efficiencyTools(itemConData);

        }
    },
    TRIDENT(EnumSet.of(Material.TRIDENT)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);

        }
    },
    MACE(EnumSet.of(Material.MACE)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);

        }
    },
    SHIELD(EnumSet.of(Material.SHIELD)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);

        }
    },
    HELMET(EnumSet.of(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.COPPER_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingArmor(itemConData);
        }
    },
    CHEST_PLATE(EnumSet.of(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.COPPER_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingArmor(itemConData);
        }
    },
    LEGGINGS(EnumSet.of(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.COPPER_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingArmor(itemConData);
        }
    },
    BOOTS(EnumSet.of(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.COPPER_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingArmor(itemConData);
        }
    },
    BOW(EnumSet.of(Material.BOW)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);

        }
    },
    CROSS_BOW(EnumSet.of(Material.CROSSBOW)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);

        }
    },
    FISHING_ROD(EnumSet.of(Material.FISHING_ROD)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);

        }
    },
    SHOVEL(EnumSet.of(Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.COPPER_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);
            ConMethods.efficiencyTools(itemConData);

        }
    },
    PICKAXE(EnumSet.of(Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.COPPER_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);
            ConMethods.efficiencyTools(itemConData);

        }
    },
    HOE(EnumSet.of(Material.WOODEN_HOE, Material.STONE_HOE, Material.COPPER_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);
            ConMethods.efficiencyTools(itemConData);

        }
    },
    FLINT_AND_STEEL(EnumSet.of(Material.FLINT_AND_STEEL)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);

        }
    },
    SHEARS(EnumSet.of(Material.SHEARS)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);
            ConMethods.efficiencyTools(itemConData);

        }
    },
    ELYTRA(EnumSet.of(Material.ELYTRA)) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);
        }
    };

    private static final ConManager[] values = values();
    protected final EnumSet<Material> materials;

    ConManager(EnumSet<Material> materials) {
        this.materials = materials;
    }

    public static ConManager getType(ItemStack stack) {
        return Arrays.stream(values).filter(conManager -> conManager.matches(stack)).findFirst().orElse(null);
    }

    private boolean matches(ItemStack stack) {
        return materials.contains(stack.getType());
    }

    public abstract void modification(ItemConData itemConData);
}

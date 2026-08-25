package com.badbones69.crazycrates.paper.api.objects.gacha.constellation;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Supplier;

public enum ConManager {
    SWORD(matchMats("_SWORD")) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);
        }
    },
    AXE(matchMats("_AXE")) {
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
    HELMET(matchMats("_HELMET")) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingArmor(itemConData);
        }
    },
    CHEST_PLATE(matchMats("_CHESTPLATE")) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingArmor(itemConData);
        }
    },
    LEGGINGS(matchMats("_LEGGINGS")) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingArmor(itemConData);
        }
    },
    BOOTS(matchMats("_BOOTS")) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingArmor(itemConData);
        }
    },
    BOW(EnumSet.of(Material.BOW, Material.CROSSBOW)) {
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
    SHOVEL(matchMats("_SHOVEL")) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);
            ConMethods.efficiencyTools(itemConData);

        }
    },
    PICKAXE(matchMats("_PICKAXE")) {
        @Override
        public void modification(ItemConData itemConData) {
            ConMethods.unbreakingTools(itemConData);
            ConMethods.efficiencyTools(itemConData);

        }
    },
    HOE(matchMats("_HOE")) {
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

    private static EnumSet<Material> matchMats(String match) {
        return Arrays.stream(Material.values())
                .filter(m -> !m.isLegacy() && m.name().endsWith(match))
                .collect(() -> EnumSet.noneOf(Material.class), EnumSet::add, EnumSet::addAll);
    }

    private static final ConManager[] values = values();
    private final EnumSet<Material> materials;

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

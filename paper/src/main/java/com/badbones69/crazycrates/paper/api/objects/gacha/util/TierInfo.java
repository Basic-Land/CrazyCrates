package com.badbones69.crazycrates.paper.api.objects.gacha.util;

import org.bukkit.Material;

import java.util.List;

public record TierInfo(Material material, String name, List<String> lore, int modelData) {
}

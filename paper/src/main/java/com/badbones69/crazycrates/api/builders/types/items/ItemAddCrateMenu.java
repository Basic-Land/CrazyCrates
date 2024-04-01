package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import org.bukkit.entity.Player;

public class ItemAddCrateMenu extends InventoryBuilder {
    public ItemAddCrateMenu(Player player, int size, String title) {
        super(player, size, title);
    }

    @Override
    public InventoryBuilder build() {
        return null;
    }
}

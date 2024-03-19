package com.badbones69.crazycrates.api.builders.types;

import com.badbones69.crazycrates.api.objects.Crate;
import org.bukkit.entity.Player;
import com.badbones69.crazycrates.api.builders.InventoryBuilder;

public class CratePrizeMenu extends InventoryBuilder {

    public CratePrizeMenu(Crate crate, Player player, int rows, String title) {
        super(crate, player, rows, title);
    }

    @Override
    public InventoryBuilder build() {
        return this;
    }
}
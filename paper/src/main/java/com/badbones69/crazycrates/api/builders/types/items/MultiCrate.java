package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class MultiCrate extends InventoryBuilder {
    public MultiCrate(Player player, int size, Component title) {
        super(player, size, title);
    }

    @Override
    public InventoryBuilder build() {
        return this;
    }
}

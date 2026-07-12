package com.badbones69.crazycrates.paper.api.builders.items;

import com.badbones69.crazycrates.paper.api.builders.InventoryBuilder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class MergeMenu extends InventoryBuilder {
    @Override
    public InventoryBuilder build() {
        return this;
    }

    @Override
    public void run(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof MergeMenu holder)) return;
    }
}

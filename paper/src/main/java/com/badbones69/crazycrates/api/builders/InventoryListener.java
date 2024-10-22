package com.badbones69.crazycrates.api.builders;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class InventoryListener implements Listener {
    private final List<Class<? extends InventoryBuilder>> menus = new ArrayList<>();

    public InventoryListener() {
    }

    public void addMenu(Class<? extends InventoryBuilder> menu) {
        menus.add(menu);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder(false);
        menus.stream().filter(menu -> menu.isInstance(holder)).map(menu -> (InventoryBuilder) holder).findFirst().ifPresent(inventoryBuilder -> inventoryBuilder.run(event));
    }
}

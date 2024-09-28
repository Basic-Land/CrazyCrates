package com.badbones69.crazycrates.api.objects.gacha.ultimatemenu;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.builders.items.UltimateMenu;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public class UltimateMenuManager {
    private final Map<String, ItemStack[]> items = new HashMap<>();
    private final CrazyCrates plugin = CrazyCrates.getPlugin(CrazyCrates.class);
    private final DatabaseManager databaseManager;

    public UltimateMenuManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void open(Player player) {
        open(player, databaseManager.getCrateSettingsSplit().getFirst().getFirst().getCrate());
    }

    public void open(Player player, Crate crate) {
        items.put(player.getName(), player.getInventory().getContents());
        databaseManager.saveInventory(player);

        UltimateMenu menu = new UltimateMenu(crate, player, ComponentBuilder.mainMenu(player, crate.getCrateSettings()));
        player.openInventory(menu.build().getInventory());
    }

    public List<ItemStack> getItems(Player player) {
        ItemStack[] itemStacks = items.get(player.getName());
        databaseManager.clearInventory(player);
        items.remove(player.getName());
        return Arrays.stream(itemStacks)
                .filter(Objects::nonNull)
                .toList();
    }

    public void remove(Player player) {
        player.getInventory().setContents(items.get(player.getName()));
        databaseManager.clearInventory(player);
        items.remove(player.getName());
    }

    public void closeAll() {
        items.forEach((name, itemStacks) -> {
            Player player = Bukkit.getPlayer(name);
            if (player != null) {
                player.getInventory().setContents(itemStacks);
                databaseManager.clearInventory(player);
            }
        });
        items.clear();
    }
}

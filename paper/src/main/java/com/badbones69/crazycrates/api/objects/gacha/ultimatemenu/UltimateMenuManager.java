package com.badbones69.crazycrates.api.objects.gacha.ultimatemenu;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.builders.items.UltimateMenu;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import lombok.Getter;
import lombok.Synchronized;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class UltimateMenuManager {
    private final static Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_TIME = 250;
    private final Map<String, ItemStack[]> items = new ConcurrentHashMap<>();
    private final CrazyCrates plugin = CrazyCrates.getPlugin(CrazyCrates.class);
    private final DatabaseManager databaseManager;

    public UltimateMenuManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void open(Player player) {
        open(player, databaseManager.getCrateSettingsSplit().getFirst().getFirst().getCrate());
    }

    @Synchronized
    public void open(Player player, Crate crate) {
        if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof UltimateMenu) {
            return;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (cooldowns.getOrDefault(playerId, 0L) + COOLDOWN_TIME > currentTime) {
            return;
        }

        cooldowns.put(playerId, currentTime);

        items.put(player.getName(), player.getInventory().getContents());
        databaseManager.saveInventory(player);

        UltimateMenu menu = new UltimateMenu(crate, player, ComponentBuilder.mainMenu(player, crate.getCrateSettings()));
        player.openInventory(menu.build().getInventory());
    }

    @Synchronized
    public ItemStack[] getItems(Player player) {
        ItemStack[] itemStacks = items.get(player.getName());
        databaseManager.clearInventory(player);
        items.remove(player.getName());
        return itemStacks;
    }

    public List<ItemStack> getItemsClean(Player player) {
        return Arrays.stream(getItems(player))
                .filter(Objects::nonNull)
                .toList();
    }

    @Synchronized
    public void remove(Player player) {
        ItemStack[] itemStacks = items.get(player.getName());
        if (itemStacks != null) {
            player.getInventory().setContents(itemStacks);
            player.updateInventory();
        }
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

    public boolean hasItems(Player player) {
        return items.containsKey(player.getName());
    }
}

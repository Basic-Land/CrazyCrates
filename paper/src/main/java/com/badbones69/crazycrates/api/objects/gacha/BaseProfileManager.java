package com.badbones69.crazycrates.api.objects.gacha;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ConcurrentHashMap;

public class BaseProfileManager implements Listener {
    private final DatabaseManager databaseManager;
    private final CrazyCrates plugin = CrazyCrates.getPlugin();
    private final ConcurrentHashMap<String, PlayerBaseProfile> profilesCache = new ConcurrentHashMap<>();

    public BaseProfileManager() {
        this.databaseManager = plugin.getCrateManager().getDatabaseManager();
        startSavingTask();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        databaseManager.getPlayerBaseProfile(player.getName()).thenAccept(profile -> {
            profilesCache.put(player.getName(), profile);
            databaseManager.restoreInv(player.getUniqueId());
        });
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerBaseProfile profile = profilesCache.remove(player.getName());
        if (profile != null) {
            databaseManager.savePlayerBaseProfile(player.getName(), profile);
        }
    }

    public PlayerBaseProfile getPlayerBaseProfile(String playerName) {
        return profilesCache.get(playerName);
    }

    public void startSavingTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> profilesCache.forEach(databaseManager::savePlayerBaseProfile), 0L, 20L * 60 * 5); // Run every 5 minutes
    }

    public void save() {
        profilesCache.forEach(databaseManager::savePlayerBaseProfile);
    }
}

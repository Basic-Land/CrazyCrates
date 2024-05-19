package com.badbones69.crazycrates.api.objects.gacha.ultimatemenu;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.builders.types.items.UltimateMenu;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

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
        PlayerBaseProfile playerBaseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());

        int mysticTokens = playerBaseProfile.getMysticTokens();
        int stellarShards = playerBaseProfile.getStellarShards();

        items.put(player.getName(), player.getInventory().getContents());
        databaseManager.saveInventory(player);

        UltimateMenu menu = new UltimateMenu(crate, player, ComponentBuilder.trans(player.getUniqueId(), crate.getName(), mysticTokens, stellarShards));
        player.openInventory(menu.build().getInventory());
    }

    public void remove(Player player) {
        player.getInventory().setContents(items.get(player.getName()));
        databaseManager.clearInventory(player);
        items.remove(player.getName());
    }

    public void closeAll() {
        for (Map.Entry<String, ItemStack[]> items : items.entrySet()) {
            Player player = Bukkit.getPlayer(items.getKey());
            if (player != null) {
                player.getInventory().setContents(items.getValue());
                databaseManager.clearInventory(player);
            }
        }
        items.clear();
    }
}

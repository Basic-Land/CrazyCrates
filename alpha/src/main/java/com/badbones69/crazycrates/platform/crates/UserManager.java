package com.badbones69.crazycrates.platform.crates;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazycrates.CrazyCratesPaper;
import com.badbones69.crazycrates.api.enums.Files;
import com.badbones69.crazycrates.platform.crates.objects.Key;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.FileConfiguration;
import us.crazycrew.crazycrates.api.enums.types.KeyType;
import us.crazycrew.crazycrates.platform.config.ConfigManager;
import us.crazycrew.crazycrates.platform.config.impl.ConfigKeys;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {

    private final @NotNull CrazyCratesPaper plugin = JavaPlugin.getPlugin(CrazyCratesPaper.class);

    private final @NotNull KeyManager keyManager = this.plugin.getKeyManager();

    private final @NotNull SettingsManager config = ConfigManager.getConfig();

    private final @NotNull FileConfiguration data = Files.data.getFile();

    public void addKeys(UUID uuid, String keyName, boolean isVirtual, int amount) {
        if (isVirtual) {
            addVirtualKeys(uuid, keyName, amount);

            return;
        }

        addPhysicalKeys(uuid, keyName, amount);
    }

    public int getVirtualKeys(UUID uuid, String keyName) {
        return this.data.getInt("Players." + uuid + "." + keyName, 0);
    }

    public void addVirtualKeys(UUID uuid, String keyName, int amount) {
        int keys = getVirtualKeys(uuid, keyName);

        this.data.set("Players." + uuid + "." + keyName, (Math.max((keys + amount), 0)));

        Files.data.save();
    }

    public boolean hasVirtualKeys(UUID uuid, String keyName) {
        return getVirtualKeys(uuid, keyName) >= 1;
    }

    public void addPhysicalKeys(UUID uuid, String keyName, int amount) {
        Player player = getUser(uuid);

        Key key = this.keyManager.getKey(keyName);

        // Add virtual keys if inventory is not empty.
        if (MiscUtil.isInventoryFull(player)) {
            if (this.config.getProperty(ConfigKeys.give_virtual_keys_when_inventory_full)) {
                addVirtualKeys(uuid, keyName, amount);

                if (config.getProperty(ConfigKeys.notify_player_when_inventory_full)) {
                    Map<String, String> placeholders = new HashMap<>();

                    placeholders.put("{amount}", String.valueOf(amount));
                    placeholders.put("{player}", player.getName());
                    placeholders.put("{keytype}", KeyType.getType(true).getFriendlyName());
                    placeholders.put("{key}", keyName);

                    player.sendMessage(Messages.cannot_give_player_keys.getMessage(placeholders, player));
                }

                player.getWorld().dropItemNaturally(player.getLocation(), key.getKey(player, amount).build());

                return;
            }

            return;
        }

        player.getInventory().addItem(key.getKey(player, amount).build());
    }

    public Player getUser(UUID uuid) {
        return this.plugin.getServer().getPlayer(uuid);
    }
}
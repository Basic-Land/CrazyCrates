package com.badbones69.crazycrates.tasks.crates;

import com.badbones69.crazycrates.CrazyCratesPaper;
import com.badbones69.crazycrates.api.FileManager.Files;
import com.badbones69.crazycrates.api.events.PlayerReceiveKeyEvent;
import com.badbones69.crazycrates.api.objects.Key;
import com.badbones69.crazycrates.api.utils.MiscUtils;
import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class UserManager extends us.crazycrew.crazycrates.api.users.UserManager {

    private final @NotNull CrazyCratesPaper plugin = JavaPlugin.getPlugin(CrazyCratesPaper.class);

    private final @NotNull KeyManager keyManager = this.plugin.getKeyManager();

    private final @NotNull CrateManager crateManager = this.plugin.getCrateManager();

    private final @NotNull FileConfiguration data = Files.DATA.getFile();

    @Override
    public Player getUser(UUID uuid) {
        return this.plugin.getServer().getPlayer(uuid);
    }

    @Override
    public boolean isUserNull(UUID uuid) {
        return getUser(uuid) == null;
    }

    @Override
    public int getVirtualKeys(UUID uuid, String keyName) {
        return this.data.getInt("Players." + uuid + "." + keyName, 0);
    }

    @Override
    public void addVirtualKeys(int amount, UUID uuid, String keyName) {
        if (isUserNull(uuid)) {
            if (MiscUtils.isLogging()) this.plugin.getLogger().warning("Player with the uuid " + uuid + " is null.");

            return;
        }

        if (isKeyInvalid(keyName)) {
            if (MiscUtils.isLogging()) this.plugin.getLogger().warning("Key " + keyName + " doesn't exist.");

            return;
        }

        Player player = getUser(uuid);

        int keys = getVirtualKeys(uuid, keyName);

        if (!this.data.contains("Players." + uuid + ".Name")) this.data.set("Players." + uuid + ".Name", player.getName());

        this.data.set("Players." + uuid + "." + keyName, (Math.max((keys + amount), 0)));

        Files.DATA.saveFile();
    }

    @Override
    public void setKeys(int amount, UUID uuid, String keyName) {
        if (isUserNull(uuid)) {
            if (MiscUtils.isLogging()) this.plugin.getLogger().warning("Player with the uuid " + uuid + " is null.");

            return;
        }

        if (isKeyInvalid(keyName)) {
            if (MiscUtils.isLogging()) this.plugin.getLogger().warning("Key " + keyName + " doesn't exist.");

            return;
        }

        Player player = getUser(uuid);

        this.data.set("Players." + uuid + ".Name", player.getName());
        this.data.set("Players." + uuid + "." + keyName, amount);

        Files.DATA.saveFile();
    }

    @Override
    public void addKeys(int amount, UUID uuid, String keyName, boolean isVirtual) {
        /*switch (keyType) {
            case physical_key -> {
                Player player = getUser(uuid);

                SettingsManager config = ConfigManager.getConfig();

                Key key = this.keyManager.getKey(keyName);

                if (!MiscUtils.isInventoryFull(player)) {
                    player.getInventory().addItem(key.getKey(player));

                    return;
                }

                if (config.getProperty(ConfigKeys.give_virtual_keys_when_inventory_full)) {
                    addVirtualKeys(amount, uuid, key.getName());

                    if (config.getProperty(ConfigKeys.notify_player_when_inventory_full)) {
                        Map<String, String> placeholders = new HashMap<>();

                        placeholders.put("{amount}", String.valueOf(amount));
                        placeholders.put("{player}", player.getName());
                        placeholders.put("{keytype}", keyType.getFriendlyName());
                        placeholders.put("{key}", key.getName());

                        player.sendMessage(Messages.cannot_give_player_keys.getMessage(placeholders, player));
                    }

                    return;
                }

                player.getWorld().dropItem(player.getLocation(), key.getKey(amount, player));
            }

            case virtual_key -> addVirtualKeys(amount, uuid, keyName);
        }*/
    }

    @Override
    public int getTotalKeys(UUID uuid, String crateName, String keyName) {
        return getVirtualKeys(uuid, keyName) + getPhysicalKeys(uuid, crateName, keyName);
    }

    @Override
    public int getPhysicalKeys(UUID uuid, String crateName, String keyName) {
        Player player = getUser(uuid);

        int keys = 0;

        for (ItemStack item : player.getOpenInventory().getBottomInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            if (!item.hasItemMeta()) continue;

            if (this.keyManager.getKey(this.crateManager.getCrate(crateName), item.getItemMeta()) != null) keys += item.getAmount();
        }

        return keys;
    }

    @Override
    public boolean takeKeys(int amount, UUID uuid, String crateName, String keyName, boolean isVirtual, boolean loopInventory) {
        Player player = getUser(uuid);

        /*switch (keyType) {
            case physical_key -> {
                return this.keyManager.takeKeys(checkHand, player, amount, crateName, keyName);
            }

            case virtual_key -> {
                int keys = getVirtualKeys(uuid, keyName);

                this.data.set("Players." + uuid + ".Name", player.getName());

                int newAmount = Math.max((keys - amount), 0);

                if (newAmount < 1) {
                    this.data.set("Players." + uuid + "." + keyName, null);
                } else {
                    this.data.set("Players." + uuid + "." + keyName, newAmount);
                }

                Files.DATA.saveFile();

                return true;
            }

            case free_key -> {
                return true;
            }
        }*/

        return false;
    }

    @Override
    public boolean hasPhysicalKey(UUID uuid, String crateName, String keyName, boolean loopInventory) {
        Preconditions.checkNotNull(uuid, "Player can't be null.");
        Preconditions.checkNotNull(crateName, "Crate can't be null.");
        Preconditions.checkNotNull(keyName, "Key can't be null.");

        return this.keyManager.hasKey(getUser(uuid), this.crateManager.getCrate(crateName), loopInventory);
    }

    @Override
    public boolean addOfflineKeys(UUID uuid, String keyName, int keys, boolean isVirtual) {
        Preconditions.checkNotNull(uuid, "Player can't be null.");
        Preconditions.checkNotNull(keyName, "Key can't be null.");

        /*switch (type) {
            /*case physical_key -> {
                if (this.data.contains("Offline-Players." + uuid + ".Physical." + keyName)) keys += this.data.getInt("Offline-Players." + uuid + ".Physical." + keyName);

                this.data.set("Offline-Players." + uuid + ".Physical." + keyName, keys);

                Files.DATA.saveFile();

                return true;
            }

            case virtual_key -> {
                if (this.data.contains("Offline-Players." + uuid + "." + keyName)) keys += this.data.getInt("Offline-Players." + uuid + "." + keyName);

                this.data.set("Offline-Players." + uuid + "." + keyName, keys);

                Files.DATA.saveFile();

                return true;
            }
        }*/

        return false;
    }

    public void loadOfflinePlayersKeys(Player player, Set<Key> keys) {
        UUID uuid = player.getUniqueId();

        if (!this.data.contains("Offline-Players." + uuid) || keys.isEmpty()) return;

        for (Key key : keys) {
            if (this.data.contains("Offline-Players." + uuid + "." + key.getName())) {
                PlayerReceiveKeyEvent event = new PlayerReceiveKeyEvent(player, key, PlayerReceiveKeyEvent.KeyReceiveReason.OFFLINE_PLAYER, 1);
                this.plugin.getServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) return;

                int keysGiven = 0;

                int amount = this.data.getInt("Offline-Players." + uuid + "." + key.getName());

                while (keysGiven < amount) {
                    if (MiscUtils.isInventoryFull(player)) {
                        player.getWorld().dropItemNaturally(player.getLocation(), key.getKey(amount, player));

                        break;
                    }

                    keysGiven++;
                }

                if (MiscUtils.isInventoryFull(player)) {
                    player.getInventory().addItem(key.getKey(amount, player));
                } else {
                    addVirtualKeys(amount, uuid, key.getName());
                }

                if (keysGiven >= amount) this.data.set("Offline-Players." + uuid + "." + key.getName(), null);
            }

            if (this.data.contains("Offline-Players." + uuid + ".Physical." + key.getName())) {
                PlayerReceiveKeyEvent event = new PlayerReceiveKeyEvent(player, key, PlayerReceiveKeyEvent.KeyReceiveReason.OFFLINE_PLAYER, 1);
                this.plugin.getServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) return;

                int keysGiven = 0;

                int amount = this.data.getInt("Offline-Players." + uuid + ".Physical." + key.getName());

                while (keysGiven < amount) {
                    // If the inventory is full, drop the remaining keys then stop.
                    if (MiscUtils.isInventoryFull(player)) {
                        player.getWorld().dropItemNaturally(player.getLocation(), key.getKey(amount - keysGiven, player));

                        break;
                    }

                    keysGiven++;
                }

                // If the inventory not full, add to inventory.
                player.getInventory().addItem(key.getKey(keysGiven, player));

                // If keys given is greater or equal than, remove data.
                if (keysGiven >= amount) this.data.set("Offline-Players." + uuid + ".Physical." + key.getName(), null);
            }
        }

        ConfigurationSection physicalSection = this.data.getConfigurationSection("Offline-Players." + uuid + ".Physical");

        if (physicalSection != null) {
            if (physicalSection.getKeys(false).isEmpty()) this.data.set("Offline-Players." + uuid + ".Physical", null);
        }

        ConfigurationSection section = this.data.getConfigurationSection("Offline-Players." + uuid);

        if (section != null) {
            if (section.getKeys(false).isEmpty()) this.data.set("Offline-Players." + uuid, null);
        }

        Files.DATA.saveFile();
    }

    public void loadOldOfflinePlayersKeys(Player player, Set<Key> keys) {
        FileConfiguration data = Files.DATA.getFile();
        String name = player.getName().toLowerCase();

        if (data.contains("Offline-Players." + name)) {
            for (Key key : keys) {
                if (data.contains("Offline-Players." + name + "." + key.getName())) {
                    PlayerReceiveKeyEvent event = new PlayerReceiveKeyEvent(player, key, PlayerReceiveKeyEvent.KeyReceiveReason.OFFLINE_PLAYER, 1);
                    this.plugin.getServer().getPluginManager().callEvent(event);

                    if (!event.isCancelled()) {
                        int amount = getVirtualKeys(player.getUniqueId(), key.getName());
                        int addedKeys = data.getInt("Offline-Players." + name + "." + key.getName());

                        data.set("Players." + player.getUniqueId() + "." + key.getName(), (Math.max((amount + addedKeys), 0)));

                        Files.DATA.saveFile();
                    }
                }
            }

            data.set("Offline-Players." + name, null);

            Files.DATA.saveFile();
        }
    }

    @Override
    public boolean takeOfflineKeys(UUID uuid, String keyName, int keys, boolean isVirtual) {
        if (isUserNull(uuid)) {
            if (MiscUtils.isLogging()) this.plugin.getLogger().warning("Player with the uuid " + uuid + " is null.");

            return false;
        }

        if (isKeyInvalid(keyName)) {
            if (MiscUtils.isLogging()) this.plugin.getLogger().warning("Key " + keyName + " doesn't exist.");

            return false;
        }

        /*switch (type) {
            case physical_key -> {
                int offlineKeys = this.data.getInt("Offline-Players." + uuid + ".Physical." + keyName);

                // If the offline keys are less than the keys the person wants to take. We will set the keys variable to how many offline keys they have.
                if (offlineKeys < keys) {
                    keys = offlineKeys;
                }

                this.data.set("Offline-Players." + uuid + ".Physical." + keyName, this.data.getInt("Offline-Players." + uuid + ".Physical." + keyName) - keys);

                // Remove the data if 0 keys remain after if checks.
                if (this.data.getInt("Offline-Players." + uuid + ".Physical." + keyName) <= 0) this.data.set("Offline-Players." + uuid + ".Physical." + keyName, null);

                Files.DATA.saveFile();
            }

            case virtual_key -> {
                this.data.set("Offline-Players." + uuid + "." + keyName, this.data.getInt("Offline-Players." + uuid + "." + keyName) - keys);

                Files.DATA.saveFile();
            }
        }*/

        return false;
    }

    @Override
    public int getTotalCratesOpened(UUID uuid) {
        return this.data.getInt("Players." + uuid + ".tracking.total-crates", 0);
    }

    @Override
    public int getCrateOpened(UUID uuid, String crateName) {
        if (isCrateInvalid(crateName)) {
            if (MiscUtils.isLogging()) this.plugin.getLogger().warning("Crate " + crateName + " doesn't exist.");

            return 0;
        }

        return this.data.getInt("Players." + uuid + ".tracking." + crateName, 0);
    }

    @Override
    public void addOpenedCrate(UUID uuid, int amount, String crateName) {
        if (isCrateInvalid(crateName)) {
            if (MiscUtils.isLogging()) this.plugin.getLogger().warning("Crate " + crateName + " doesn't exist.");

            return;
        }

        boolean hasValue = this.data.contains("Players." + uuid + ".tracking." + crateName);

        int newAmount;

        if (hasValue) {
            newAmount = this.data.getInt("Players." + uuid + ".tracking." + crateName) + amount;

            this.data.set("Players." + uuid + ".tracking." + crateName, newAmount);
            this.data.set("Players." + uuid + ".tracking.total-crates", this.data.getInt("Players." + uuid + ".tracking.total-crates") + amount);

            Files.DATA.saveFile();

            return;
        }

        this.data.set("Players." + uuid + ".tracking.total-crates", this.data.getInt("Players." + uuid + ".tracking.total-crates", 0) + amount);
        this.data.set("Players." + uuid + ".tracking." + crateName, amount);

        Files.DATA.saveFile();
    }

    @Override
    public void addOpenedCrate(UUID uuid, String crateName) {
        if (isCrateInvalid(crateName)) {
            if (MiscUtils.isLogging()) this.plugin.getLogger().warning("Crate " + crateName + " doesn't exist.");

            return;
        }

        boolean hasValue = this.data.contains("Players." + uuid + ".tracking." + crateName);

        int amount;

        if (hasValue) {
            amount = this.data.getInt("Players." + uuid + ".tracking." + crateName);

            this.data.set("Players." + uuid + ".tracking." + crateName, amount + 1);
            this.data.set("Players." + uuid + ".tracking.total-crates", this.data.getInt("Players." + uuid + ".tracking.total-crates") + 1);

            Files.DATA.saveFile();

            return;
        }

        amount = this.data.contains("Players." + uuid + ".tracking.total-crates") ? this.data.getInt("Players." + uuid + ".tracking.total-crates") + 1 : 1;

        this.data.set("Players." + uuid + ".tracking.total-crates", amount);
        this.data.set("Players." + uuid + ".tracking." + crateName, 1);

        Files.DATA.saveFile();
    }

    private boolean isKeyInvalid(String keyName) {
        return keyName.isBlank() || this.keyManager.getKey(keyName) == null;
    }

    private boolean isCrateInvalid(String crateName) {
        return crateName.isBlank() || this.crateManager.getCrate(crateName) == null;
    }
}
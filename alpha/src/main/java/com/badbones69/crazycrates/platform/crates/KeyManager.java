package com.badbones69.crazycrates.platform.crates;

import com.badbones69.crazycrates.CrazyCratesPaper;
import com.badbones69.crazycrates.platform.crates.objects.Key;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import us.crazycrew.crazycrates.platform.Server;
import us.crazycrew.crazycrates.platform.keys.KeyConfig;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyManager {

    private final @NotNull CrazyCratesPaper plugin = JavaPlugin.getPlugin(CrazyCratesPaper.class);

    private final @NotNull Logger logger = this.plugin.getLogger();

    private final @NotNull Server instance = this.plugin.getInstance();

    private final Set<Key> keys = new HashSet<>();

    public void load() {
        Preconditions.checkNotNull(this.instance.getKeyFiles(), "Could not read from the keys directory! " + this.instance.getKeyFolder().getAbsolutePath());

        for (File file : this.instance.getKeyFiles()) {
            this.logger.info("Loading key: " + file.getName());

            KeyConfig keyConfig = new KeyConfig(file);

            try {
                keyConfig.load();
            } catch (InvalidConfigurationException exception) {
                this.logger.log(Level.WARNING, file.getName() + " contains invalid YAML structure.", exception);

                continue;
            } catch (IOException exception) {
                this.logger.log(Level.WARNING, "Could not load key file: " + file.getName(), exception);

                continue;
            }

            this.keys.add(new Key(keyConfig));
        }
    }

    public Key getKey(String keyName) {
        Key key = null;

        for (Key pair : this.keys) {
            if (!pair.getKeyName().equalsIgnoreCase(keyName)) continue;

            key = pair;

            break;
        }

        return key;
    }

    public void addKey(Player player, Key key) {
        player.getInventory().addItem(key.getKey(player).build());
    }

    public void addKey(Player player, Key key, int amount) {
        player.getInventory().addItem(key.getKey(player, amount).build());
    }

    public Set<Key> getKeys() {
        return Collections.unmodifiableSet(this.keys);
    }
}
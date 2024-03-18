package com.badbones69.crazycrates;

import com.badbones69.crazycrates.api.FileManager;
import com.badbones69.crazycrates.api.builders.types.CrateAdminMenu;
import com.badbones69.crazycrates.api.builders.types.CrateMainMenu;
import com.badbones69.crazycrates.api.builders.types.CratePreviewMenu;
import com.badbones69.crazycrates.api.builders.types.CrateTierMenu;
import com.badbones69.crazycrates.api.utils.MiscUtils;
import com.badbones69.crazycrates.api.utils.MsgUtils;
import com.badbones69.crazycrates.commands.CommandManager;
import com.badbones69.crazycrates.listeners.BrokeLocationsListener;
import com.badbones69.crazycrates.listeners.CrateControlListener;
import com.badbones69.crazycrates.listeners.MiscListener;
import com.badbones69.crazycrates.listeners.crates.*;
import com.badbones69.crazycrates.listeners.other.EntityDamageListener;
import com.badbones69.crazycrates.platform.PaperServer;
import com.badbones69.crazycrates.support.PluginSupport;
import com.badbones69.crazycrates.support.holograms.HologramManager;
import com.badbones69.crazycrates.support.metrics.MetricsManager;
import com.badbones69.crazycrates.support.placeholders.PlaceholderAPISupport;
import com.badbones69.crazycrates.tasks.InventoryManager;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import com.badbones69.crazycrates.tasks.crates.KeyManager;
import com.badbones69.crazycrates.tasks.crates.UserManager;
import com.ryderbelserion.cluster.ClusterFactory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazycrates.platform.Server;
import us.crazycrew.crazycrates.platform.config.ConfigManager;
import us.crazycrew.crazycrates.platform.config.impl.ConfigKeys;
import java.util.List;
import java.util.Timer;

public class CrazyCratesPaper extends JavaPlugin {

    private InventoryManager inventoryManager;
    private KeyManager keyManager;
    private CrateManager crateManager;
    private UserManager userManager;
    private FileManager fileManager;
    private MetricsManager metrics;
    private ClusterFactory factory;
    private Server instance;
    private Timer timer;

    @Override
    public void onLoad() {
        this.factory = new ClusterFactory(this);

        ConfigManager.load(getDataFolder());

        this.fileManager = new FileManager();

        this.fileManager.registerDefaultGenerateFiles("CasinoKey.yml", "/keys", "/keys")
                .registerDefaultGenerateFiles("DiamondKey.yml", "/keys", "/keys")
                .registerDefaultGenerateFiles("CrateExample.yml", "/crates", "/crates")
                .registerDefaultGenerateFiles("QuadCrateExample.yml", "/crates", "/crates")
                .registerDefaultGenerateFiles("QuickCrateExample.yml", "/crates", "/crates")
                .registerDefaultGenerateFiles("WarCrateExample.yml", "/crates", "/crates")
                .registerDefaultGenerateFiles("classic.nbt", "/schematics", "/schematics")
                .registerDefaultGenerateFiles("nether.nbt", "/schematics", "/schematics")
                .registerDefaultGenerateFiles("outdoors.nbt", "/schematics", "/schematics")
                .registerDefaultGenerateFiles("sea.nbt", "/schematics", "/schematics")
                .registerDefaultGenerateFiles("soul.nbt", "/schematics", "/schematics")
                .registerDefaultGenerateFiles("wooden.nbt", "/schematics", "/schematics")
                .registerCustomFilesFolder("/keys")
                .registerCustomFilesFolder("/crates")
                .registerCustomFilesFolder("/schematics")
                .setup(false);

        this.instance = new PaperServer();

        this.timer = new Timer();
    }

    @Override
    public void onEnable() {
        this.inventoryManager = new InventoryManager();

        this.keyManager = new KeyManager();

        this.crateManager = new CrateManager();

        this.userManager = new UserManager();

        this.crateManager.loadHolograms();
        this.crateManager.load();

        if (ConfigManager.getConfig().getProperty(ConfigKeys.toggle_metrics)) {
            this.metrics = new MetricsManager();

            this.metrics.start();
        }

        CommandManager.load();

        List.of(
                new CratePreviewMenu.CratePreviewListener(),
                new CrateAdminMenu.CrateAdminListener(),
                new CrateMainMenu.CrateMenuListener(),
                new CrateTierMenu.CrateTierListener(),

                new BrokeLocationsListener(),
                new CrateControlListener(),
                new EntityDamageListener(),
                new MobileCrateListener(),
                new QuadCrateListener(),
                new CrateOpenListener(),
                new WarCrateListener(),
                new MiscListener()
        ).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        boolean isLogging = MiscUtils.isLogging();

        if (isLogging) {
            String prefix = ConfigManager.getConfig().getProperty(ConfigKeys.console_prefix);

            for (PluginSupport value : PluginSupport.values()) {
                if (value.isPluginEnabled()) {
                    getServer().getConsoleSender().sendMessage(MsgUtils.color(prefix + "&6&l" + value.name() + " &a&lFOUND"));
                } else {
                    getServer().getConsoleSender().sendMessage(MsgUtils.color(prefix + "&6&l" + value.name() + " &c&lNOT FOUND"));
                }
            }
        }

        if (PluginSupport.PLACEHOLDERAPI.isPluginEnabled()) {
            if (isLogging) getLogger().info("PlaceholderAPI support is enabled!");

            new PlaceholderAPISupport().register();
        }

        if (isLogging) getLogger().info("You can disable logging by going to the plugin-config.yml and setting verbose to false.");
    }

    @Override
    public void onDisable() {
        if (this.instance != null) this.instance.disable();

        if (this.factory != null) this.factory.disable();

        if (this.timer != null) this.timer.cancel();

        if (this.crateManager != null) {
            this.crateManager.purgeRewards();

            HologramManager holograms = this.crateManager.getHologramManager();

            if (holograms != null && !holograms.isMapEmpty()) {
                holograms.removeAllHolograms();
            }
        }
    }

    public @NotNull InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public KeyManager getKeyManager() {
        return this.keyManager;
    }

    public @NotNull CrateManager getCrateManager() {
        return this.crateManager;
    }

    public @NotNull UserManager getUserManager() {
        return this.userManager;
    }

    public @NotNull FileManager getFileManager() {
        return this.fileManager;
    }

    public @NotNull MetricsManager getMetrics() {
        return this.metrics;
    }

    public @NotNull Server getInstance() {
        return this.instance;
    }

    public @NotNull Timer getTimer() {
        return this.timer;
    }
}
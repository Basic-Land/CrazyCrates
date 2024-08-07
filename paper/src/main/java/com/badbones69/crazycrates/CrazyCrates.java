package com.badbones69.crazycrates;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.InventoryListener;
import com.badbones69.crazycrates.api.builders.types.items.UltimateMenu;
import com.badbones69.crazycrates.api.objects.gacha.BaseProfileManager;
import com.badbones69.crazycrates.api.utils.MiscUtils;
import com.badbones69.crazycrates.commands.CommandManager;
import com.badbones69.crazycrates.config.ConfigManager;
import com.badbones69.crazycrates.config.impl.ConfigKeys;
import com.badbones69.crazycrates.listeners.BrokeLocationsListener;
import com.badbones69.crazycrates.listeners.CrateControlListener;
import com.badbones69.crazycrates.listeners.MiscListener;
import com.badbones69.crazycrates.listeners.crates.*;
import com.badbones69.crazycrates.listeners.other.EntityDamageListener;
import com.badbones69.crazycrates.support.MetricsWrapper;
import com.badbones69.crazycrates.support.holograms.HologramManager;
import com.badbones69.crazycrates.support.placeholders.PlaceholderAPISupport;
import com.badbones69.crazycrates.tasks.BukkitUserManager;
import com.badbones69.crazycrates.tasks.InventoryManager;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import com.google.common.reflect.ClassPath;
import com.ryderbelserion.vital.paper.enums.Support;
import com.ryderbelserion.vital.paper.files.config.FileManager;
import lombok.Getter;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Timer;
import static com.badbones69.crazycrates.api.utils.MiscUtils.registerPermissions;

public class CrazyCrates extends JavaPlugin {

    private final Timer timer;

    public CrazyCrates() {
        // Create timer object.
        this.timer = new Timer();
    }

    private InventoryManager inventoryManager;
    private BukkitUserManager userManager;
    private CrateManager crateManager;
    @Getter
    private BaseProfileManager baseProfileManager;
    private FileManager fileManager;
    private HeadDatabaseAPI api;

    private Server instance;

    @Override
    public void onEnable() {
        this.instance = new Server(getDataFolder(), getLogger());
        this.instance.apply();

        this.fileManager = new FileManager();
        this.fileManager.addFile("locations.yml").addFile("data.yml")
                .addFolder("crates")
                .addFolder("schematics")
                .addFolder("banners")
                .addFolder("shops")
                .init();

        // Register permissions that we need.
        registerPermissions();

        if (Support.head_database.isEnabled()) {
            this.api = new HeadDatabaseAPI();
        }

        this.inventoryManager = new InventoryManager();
        this.crateManager = new CrateManager();
        this.userManager = new BukkitUserManager();

        this.instance.setUserManager(this.userManager);

        // Load holograms.
        this.crateManager.loadHolograms();

        // Load the buttons.
        this.inventoryManager.loadButtons();

        // Load the crates.
        this.crateManager.loadCrates();

        // Load commands.
        CommandManager.load();

        new MetricsWrapper(this, 4514).start();

        baseProfileManager = new BaseProfileManager();
        InventoryListener inventoryListener = new InventoryListener();
        try {
            ClassPath.from(getClassLoader()).getAllClasses().stream()
                    .filter(info -> info.getName().startsWith("com.badbones69.crazycrates.api.builders.types"))
                    .map(ClassPath.ClassInfo::load)
                    .filter(InventoryBuilder.class::isAssignableFrom)
                    .map(clazz -> (Class<? extends InventoryBuilder>) clazz)
                    .forEach(aClass -> {
                        inventoryListener.addMenu(aClass);
                        System.out.println("Added " + aClass.getSimpleName());
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        List.of(
                // Menu listeners.
                inventoryListener,
                new UltimateMenu.TestMenuListener(),

                // Other listeners.
                new BrokeLocationsListener(),
                new CrateControlListener(),
                new EntityDamageListener(),
                new MobileCrateListener(),
                new CosmicCrateListener(),
                new QuadCrateListener(),
                new CrateOpenListener(),
                new WarCrateListener(),
                new MiscListener(),
                new GachaCrateListener(),
                baseProfileManager
        ).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        if (MiscUtils.isLogging()) {
            final String prefix = ConfigManager.getConfig().getProperty(ConfigKeys.console_prefix);

            // Print dependency garbage
            for (final Support value : Support.values()) {
                if (value.isEnabled()) {
                    getServer().getConsoleSender().sendRichMessage(prefix + "<bold><gold>" + value.getName() + " <green>FOUND");
                } else {
                    getServer().getConsoleSender().sendRichMessage(prefix + "<bold><gold>" + value.getName() + " <red>NOT FOUND");
                }
            }
        }

        if (Support.placeholder_api.isEnabled()) {
            if (MiscUtils.isLogging()) getLogger().info("PlaceholderAPI support is enabled!");

            new PlaceholderAPISupport().register();
        }

        if (MiscUtils.isLogging()) getLogger().info("You can disable logging by going to the plugin-config.yml and setting verbose to false.");
    }

    @Override
    public void onDisable() {
        // Cancel the tasks
        getServer().getGlobalRegionScheduler().cancelTasks(this);
        getServer().getAsyncScheduler().cancelTasks(this);

        // Cancel the timer task.
        this.timer.cancel();

        if (this.baseProfileManager != null) this.baseProfileManager.save();

        // Clean up any mess we may have left behind.
        if (this.crateManager != null) {
            this.crateManager.getDatabaseManager().getUltimateMenuManager().closeAll();
            this.crateManager.purgeRewards();

            final HologramManager holograms = this.crateManager.getHolograms();

            if (holograms != null) {
                holograms.purge(true);
            }
        }

        if (this.instance != null) {
            this.instance.disable();
        }
    }

    public @NotNull final InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public @NotNull final BukkitUserManager getUserManager() {
        return this.userManager;
    }

    public @NotNull final CrateManager getCrateManager() {
        return this.crateManager;
    }

    public @NotNull final FileManager getFileManager() {
        return this.fileManager;
    }

    public @Nullable final HeadDatabaseAPI getApi() {
        if (this.api == null) {
            return null;
        }

        return this.api;
    }

    public @NotNull final Server getInstance() {
        return this.instance;
    }

    public @NotNull final Timer getTimer() {
        return this.timer;
    }
}
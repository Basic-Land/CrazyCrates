package com.badbones69.crazycrates;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.InventoryListener;
import com.badbones69.crazycrates.api.builders.items.UltimateMenu;
import com.badbones69.crazycrates.api.objects.gacha.BaseProfileManager;
import com.badbones69.crazycrates.listeners.crates.types.*;
import com.badbones69.crazycrates.common.Server;
import com.badbones69.crazycrates.utils.MiscUtils;
import com.badbones69.crazycrates.commands.CommandManager;
import com.badbones69.crazycrates.listeners.BrokeLocationsListener;
import com.badbones69.crazycrates.listeners.CrateControlListener;
import com.badbones69.crazycrates.listeners.MiscListener;
import com.badbones69.crazycrates.listeners.crates.CrateOpenListener;
import com.badbones69.crazycrates.listeners.other.EntityDamageListener;
import com.badbones69.crazycrates.support.MetricsWrapper;
import com.badbones69.crazycrates.support.holograms.HologramManager;
import com.badbones69.crazycrates.support.placeholders.PlaceholderAPISupport;
import com.badbones69.crazycrates.managers.BukkitUserManager;
import com.badbones69.crazycrates.managers.InventoryManager;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import com.google.common.reflect.ClassPath;
import com.ryderbelserion.vital.paper.Vital;
import com.ryderbelserion.vital.paper.api.enums.Support;
import com.ryderbelserion.vital.paper.util.AdvUtil;
import lombok.Getter;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.logging.Logger;

import static com.badbones69.crazycrates.utils.MiscUtils.registerPermissions;

@ApiStatus.Internal
public class CrazyCrates extends JavaPlugin {

    @ApiStatus.Internal
    public static CrazyCrates getPlugin() {
        return JavaPlugin.getPlugin(CrazyCrates.class);
    }

    private final Vital vital;
    private final Timer timer;
    private final long startTime;

    public CrazyCrates() {
        this.startTime = System.nanoTime();

        this.vital = new Vital(this);

        this.timer = new Timer();
    }

    private InventoryManager inventoryManager;
    private BukkitUserManager userManager;
    private CrateManager crateManager;
    @Getter
    private BaseProfileManager baseProfileManager;
    private HeadDatabaseAPI api;

    private Server instance;
    public static Logger LOGGER;

    @Override
    public void onEnable() {
        this.instance = new Server(getDataFolder());
        this.instance.apply();
        LOGGER = getLogger();

        this.vital.getFileManager()
                .addFile("locations.yml")
                .addFile("data.yml")
                .addFile("respin-gui.yml", "guis")
                .addFile("crates.log", "logs")
                .addFile("keys.log", "logs")
                .addFolder("crates")
                .addFolder("schematics")
                .addFolder("logs")
                .addFolder("banners")
                .addFolder("shops")
                .init();

        MiscUtils.janitor();
        MiscUtils.save();

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
                    .filter(info -> info.getName().startsWith("com.badbones69.crazycrates.api.builders.items"))
                    .map(ClassPath.ClassInfo::load)
                    .filter(InventoryBuilder.class::isAssignableFrom)
                    .map(clazz -> (Class<? extends InventoryBuilder>) clazz)
                    .forEach(aClass -> {
                        inventoryListener.addMenu(aClass);
                        getLogger().info("Added " + aClass.getSimpleName());
                    });
        } catch (Exception e) {
            getLogger().warning(e.getMessage());
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

        if (Support.placeholder_api.isEnabled()) {
            if (MiscUtils.isLogging()) getComponentLogger().info("PlaceholderAPI support is enabled!");

            new PlaceholderAPISupport().register();
        }

        if (MiscUtils.isLogging()) {
            // Print dependency garbage
            for (final Support value : Support.values()) {
                if (value.isEnabled()) {
                    getComponentLogger().info(AdvUtil.parse("<bold><gold>" + value.getName() + " <green>FOUND"));
                } else {
                    getComponentLogger().info(AdvUtil.parse("<bold><gold>" + value.getName() + " <red>NOT FOUND"));
                }
            }

            getComponentLogger().info("Done ({})!", String.format(Locale.ROOT, "%.3fs", (double) (System.nanoTime() - this.startTime) / 1.0E9D));
        }
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

        MiscUtils.janitor();
    }

    @ApiStatus.Internal
    public final InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    @ApiStatus.Internal
    public final BukkitUserManager getUserManager() {
        return this.userManager;
    }

    @ApiStatus.Internal
    public final CrateManager getCrateManager() {
        return this.crateManager;
    }

    @ApiStatus.Internal
    public @Nullable final HeadDatabaseAPI getApi() {
        if (this.api == null) {
            return null;
        }

        return this.api;
    }

    @ApiStatus.Internal
    public final Server getInstance() {
        return this.instance;
    }

    @ApiStatus.Internal
    public final Vital getVital() {
        return this.vital;
    }

    @ApiStatus.Internal
    public final Timer getTimer() {
        return this.timer;
    }
}
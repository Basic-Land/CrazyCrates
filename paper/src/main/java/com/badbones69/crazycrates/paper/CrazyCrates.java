package com.badbones69.crazycrates.paper;

import com.badbones69.crazycrates.paper.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.paper.api.builders.InventoryListener;
import com.badbones69.crazycrates.paper.api.builders.items.UltimateMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.BaseProfileManager;
import com.badbones69.crazycrates.paper.api.enums.other.Plugins;
import com.badbones69.crazycrates.core.Server;
import com.badbones69.crazycrates.core.config.ConfigManager;
import com.badbones69.crazycrates.core.config.impl.ConfigKeys;
import com.badbones69.crazycrates.paper.listeners.crates.CrateInteractListener;
import com.badbones69.crazycrates.paper.listeners.crates.types.*;
import com.badbones69.crazycrates.paper.listeners.items.PaperInteractListener;
import com.badbones69.crazycrates.paper.support.MetricsWrapper;
import com.badbones69.crazycrates.paper.utils.MiscUtils;
import com.badbones69.crazycrates.paper.commands.CommandManager;
import com.badbones69.crazycrates.paper.listeners.BrokeLocationsListener;
import com.badbones69.crazycrates.paper.listeners.CrateControlListener;
import com.badbones69.crazycrates.paper.listeners.MiscListener;
import com.badbones69.crazycrates.paper.listeners.crates.CrateOpenListener;
import com.badbones69.crazycrates.paper.listeners.other.EntityDamageListener;
import com.badbones69.crazycrates.paper.support.holograms.HologramManager;
import com.badbones69.crazycrates.paper.support.placeholders.PlaceholderAPISupport;
import com.badbones69.crazycrates.paper.managers.BukkitUserManager;
import com.badbones69.crazycrates.paper.managers.InventoryManager;
import com.badbones69.crazycrates.paper.tasks.crates.CrateManager;
import com.google.common.reflect.ClassPath;
import com.ryderbelserion.fusion.api.enums.FileType;
import com.ryderbelserion.fusion.core.utils.AdvUtils;
import com.ryderbelserion.fusion.paper.FusionPaper;
import com.ryderbelserion.fusion.paper.files.LegacyFileManager;
import lombok.Getter;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.logging.Logger;

import static com.badbones69.crazycrates.paper.utils.MiscUtils.registerPermissions;

public class CrazyCrates extends JavaPlugin {

    public static CrazyCrates getPlugin() {
        return JavaPlugin.getPlugin(CrazyCrates.class);
    }

    private final Timer timer;
    private final long startTime;

    public CrazyCrates() {
        this.startTime = System.nanoTime();

        this.timer = new Timer();
    }

    private InventoryManager inventoryManager;
    private BukkitUserManager userManager;
    private CrateManager crateManager;

    private FusionPaper api;

    private Server instance;

    private MetricsWrapper metrics;
    public static Logger LOGGER;

    @Getter
    private BaseProfileManager baseProfileManager;

    private LegacyFileManager fileManager;

    @Override
    public void onEnable() {
        this.api = new FusionPaper(getComponentLogger(), getDataPath());
        this.api.enable(this);
        LOGGER = getLogger();

        this.fileManager = this.api.getLegacyFileManager();

        this.instance = new Server(getDataFolder());
        this.instance.apply();

        this.fileManager.addFile("locations.yml", FileType.YAML)
                .addFile("data.yml", FileType.YAML)
                .addFile("respin-gui.yml", "guis", false, FileType.YAML)
                .addFile("crates.log", "logs", false, FileType.NONE)
                .addFile("keys.log", "logs", false, FileType.NONE)
                .addFolder("crates", FileType.YAML)
                .addFolder("schematics", FileType.NONE)
                .addFolder("banners", FileType.YAML)
                .addFolder("shops", FileType.YAML);

        MiscUtils.janitor();
        MiscUtils.save();

        registerPermissions();

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

        if (ConfigManager.getConfig().getProperty(ConfigKeys.toggle_metrics)) {
            this.metrics = new MetricsWrapper(4514);
            this.metrics.start();
        }

        // Load commands.
        CommandManager.load();

        baseProfileManager = new BaseProfileManager();
        InventoryListener inventoryListener = new InventoryListener();
        try {
            ClassPath.from(getClassLoader()).getAllClasses().stream()
                    .filter(info -> info.getName().startsWith("com.badbones69.crazycrates.paper.api.builders.items"))
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

        final PluginManager manager = getServer().getPluginManager();

        List.of(
                // Other listeners.
                new BrokeLocationsListener(),
                new EntityDamageListener(),
                new MobileCrateListener(),
                new CosmicCrateListener(),
                new QuadCrateListener(),
                new WarCrateListener(),
                new MiscListener(),

                new CrateInteractListener(),
                new CrateControlListener(),
                new CrateOpenListener(),

                new PaperInteractListener(),
                new MiscListener(),
                new GachaCrateListener(),
                baseProfileManager,
                inventoryListener,
                new UltimateMenu.TestMenuListener()
        ).forEach(listener -> manager.registerEvents(listener, this));

        this.crateManager.loadCustomItems();

        final ComponentLogger logger = getComponentLogger();

        if (Plugins.placeholder_api.isEnabled()) {
            if (MiscUtils.isLogging()) logger.info("PlaceholderAPI support is enabled!");

            new PlaceholderAPISupport().register();
        }

        if (MiscUtils.isLogging()) {
            // Print dependency garbage
            for (final Plugins value : Plugins.values()) {
                if (value.isEnabled()) {
                    logger.info(AdvUtils.parse("<bold><gold>" + value.getName() + " <green>FOUND"));
                } else {
                    logger.info(AdvUtils.parse("<bold><gold>" + value.getName() + " <red>NOT FOUND"));
                }
            }

            logger.info("Done ({})!", String.format(Locale.ROOT, "%.3fs", (double) (System.nanoTime() - this.startTime) / 1.0E9D));
        }
    }

    @Override
    public void onDisable() {
        // Cancel the tasks
        getServer().getGlobalRegionScheduler().cancelTasks(this);
        getServer().getAsyncScheduler().cancelTasks(this);

        // Cancel the timer task.
        this.timer.cancel();

        // Clean up any mess we may have left behind.
        if (this.crateManager != null) {
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

        this.api.save();
    }

    public final InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public final BukkitUserManager getUserManager() {
        return this.userManager;
    }

    public final CrateManager getCrateManager() {
        return this.crateManager;
    }

    public final Server getInstance() {
        return this.instance;
    }

    public final MetricsWrapper getMetrics() {
        return this.metrics;
    }

    public final LegacyFileManager getFileManager() {
        return this.fileManager;
    }

    public final Timer getTimer() {
        return this.timer;
    }

    public final FusionPaper getFusion() {
        return this.api;
    }
}
package com.badbones69.crazycrates;

import com.badbones69.crazycrates.api.builders.types.CrateAdminMenu;
import com.badbones69.crazycrates.api.builders.types.CrateMainMenu;
import com.badbones69.crazycrates.api.builders.types.CratePreviewMenu;
import com.badbones69.crazycrates.api.builders.types.CrateTierMenu;
import com.badbones69.crazycrates.api.utils.FileUtils;
import com.badbones69.crazycrates.api.utils.MiscUtils;
import com.badbones69.crazycrates.api.utils.MsgUtils;
import com.badbones69.crazycrates.commands.CommandManager;
import com.badbones69.crazycrates.listeners.BrokeLocationsListener;
import com.badbones69.crazycrates.listeners.CrateControlListener;
import com.badbones69.crazycrates.listeners.MiscListener;
import com.badbones69.crazycrates.listeners.crates.CosmicCrateListener;
import com.badbones69.crazycrates.listeners.crates.CrateOpenListener;
import com.badbones69.crazycrates.listeners.crates.MobileCrateListener;
import com.badbones69.crazycrates.listeners.crates.QuadCrateListener;
import com.badbones69.crazycrates.listeners.crates.WarCrateListener;
import com.badbones69.crazycrates.listeners.other.EntityDamageListener;
import com.badbones69.crazycrates.support.holograms.HologramManager;
import com.badbones69.crazycrates.support.metrics.MetricsManager;
import com.badbones69.crazycrates.support.placeholders.PlaceholderAPISupport;
import com.badbones69.crazycrates.tasks.BukkitUserManager;
import com.badbones69.crazycrates.tasks.InventoryManager;
import com.badbones69.crazycrates.tasks.MigrationManager;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import com.ryderbelserion.vital.VitalPaper;
import com.ryderbelserion.vital.enums.Support;
import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.plugin.java.JavaPlugin;
import com.badbones69.crazycrates.api.FileManager;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazycrates.platform.Server;
import us.crazycrew.crazycrates.platform.config.ConfigManager;
import us.crazycrew.crazycrates.platform.config.impl.ConfigKeys;
import java.util.List;
import java.util.Timer;
import static com.badbones69.crazycrates.api.utils.MiscUtils.isLogging;
import static com.badbones69.crazycrates.api.utils.MiscUtils.registerPermissions;

public class CrazyCrates extends JavaPlugin {

    private Server instance;

    private final Timer timer;

    public CrazyCrates() {
        // Create timer object.
        this.timer = new Timer();
    }

    private InventoryManager inventoryManager;
    private BukkitUserManager userManager;
    private CrateManager crateManager;
    private FileManager fileManager;
    @Getter
    private BaseProfileManager baseProfileManager;
    private MetricsManager metrics;

    @Override
    public void onLoad() {
        this.instance = new Server(this);
        this.instance.enable();
    }

    @Override
    public void onEnable() {
        new VitalPaper(this);

        // Register permissions that we need.
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
                baseProfileManager
        ).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        if (MiscUtils.isLogging()) {
            String prefix = ConfigManager.getConfig().getProperty(ConfigKeys.console_prefix);

            // Print dependency garbage
            for (Support value : Support.values()) {
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

            HologramManager holograms = this.crateManager.getHolograms();

            if (holograms != null && !holograms.isEmpty()) {
                holograms.removeAllHolograms(true);
            }
        }

        if (this.instance != null) {
            this.instance.disable();
        }
    }

    public @NotNull InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public @NotNull BukkitUserManager getUserManager() {
        return this.userManager;
    }

    public @NotNull CrateManager getCrateManager() {
        return this.crateManager;
    }

    public @NotNull FileManager getFileManager() {
        return this.instance.getFileManager();
    }

    public @NotNull Server getInstance() {
        return this.instance;
    }

    public @NotNull Timer getTimer() {
        return this.timer;
    }
}
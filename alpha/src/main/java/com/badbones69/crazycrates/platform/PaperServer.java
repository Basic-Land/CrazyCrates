package com.badbones69.crazycrates.platform;

import com.badbones69.crazycrates.CrazyCratesPaper;
import us.crazycrew.crazycrates.platform.Server;
import us.crazycrew.crazycrates.platform.config.ConfigManager;
import us.crazycrew.crazycrates.platform.config.impl.ConfigKeys;
import java.io.File;
import java.util.logging.Logger;

public class PaperServer extends Server {

    private final CrazyCratesPaper plugin;
    private final File crateFolder;
    private final File keyFolder;
    private final Logger logger;

    public PaperServer(CrazyCratesPaper plugin) {
        super(plugin.getDataFolder());

        this.plugin = plugin;

        this.logger = this.plugin.getLogger();

        this.crateFolder = new File(this.plugin.getDataFolder(), "crates");
        this.keyFolder = new File(this.plugin.getDataFolder(), "keys");
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public boolean isLogging() {
        return ConfigManager.getConfig().getProperty(ConfigKeys.verbose_logging);
    }

    @Override
    public File getFolder() {
        return this.plugin.getDataFolder();
    }

    @Override
    public File getKeyFolder() {
        return this.keyFolder;
    }

    @Override
    public File getCrateFolder() {
        return this.crateFolder;
    }

    @Override
    public File[] getKeyFiles() {
        return getKeyFolder().listFiles();
    }

    @Override
    public File[] getCrateFiles() {
        return getCrateFolder().listFiles();
    }
}
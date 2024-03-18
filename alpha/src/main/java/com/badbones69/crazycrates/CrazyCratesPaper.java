package com.badbones69.crazycrates;

import com.badbones69.crazycrates.commands.CommandManager;
import com.badbones69.crazycrates.platform.PaperServer;
import com.badbones69.crazycrates.platform.crates.KeyManager;
import com.badbones69.crazycrates.platform.crates.UserManager;
import com.badbones69.crazycrates.platform.utils.MiscUtil;
import com.ryderbelserion.cluster.ClusterFactory;
import com.ryderbelserion.cluster.api.files.FileManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CrazyCratesPaper extends JavaPlugin {

    private FileManager fileManager;
    private PaperServer instance;
    private KeyManager keyManager;
    private UserManager userManager;

    @Override
    public void onLoad() {
        this.instance = new PaperServer(this);

        new ClusterFactory(this).setLogging(MiscUtil.isLogging());
    }

    @Override
    public void onEnable() {
        this.fileManager = new FileManager();

        this.fileManager.addStaticFile("locations.yml").addStaticFile("data.yml")
                .addDynamicFile("keys", "CasinoKey.yml")
                .addDynamicFile("keys", "DiamondKey.yml")
                .addFolder("keys")
                .create();

        this.keyManager = new KeyManager();
        this.keyManager.load();

        this.userManager = new UserManager();

        CommandManager.load();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public FileManager getFileManager() {
        return this.fileManager;
    }

    public PaperServer getInstance() {
        return this.instance;
    }

    public KeyManager getKeyManager() {
        return this.keyManager;
    }

    public UserManager getUserManager() {
        return this.userManager;
    }
}
package com.badbones69.crazycrates;

import com.badbones69.crazycrates.platform.PaperServer;
import com.badbones69.crazycrates.platform.crates.CrateManager;
import com.badbones69.crazycrates.platform.crates.KeyManager;
import com.badbones69.crazycrates.platform.crates.UserManager;
import com.badbones69.crazycrates.platform.utils.MiscUtils;
import com.ryderbelserion.cluster.ClusterFactory;
import com.ryderbelserion.cluster.api.files.FileManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class CrazyCratesPaper extends JavaPlugin {

    private FileManager fileManager;
    private PaperServer instance;
    private ClusterFactory factory;
    private KeyManager keyManager;
    private CrateManager crateManager;
    private UserManager userManager;

    @Override
    public void onLoad() {
        this.instance = new PaperServer(getDataFolder());

        this.factory = new ClusterFactory(this);
        this.factory.setLogging(MiscUtils.isLogging());

        this.fileManager = new FileManager();

        this.fileManager.addStaticFile("locations.yml").addStaticFile("data.yml").create();

        List.of(
                "CrateExample.yml",
                "WarCrateExample.yml",
                "QuadCrateExample.yml",
                "QuickCrateExample.yml"
        ).forEach(file -> this.factory.copyFile(new File(getDataFolder(), "crates").toPath(), file));

        Path directory = new File(getDataFolder(), "keys").toPath();

        this.factory.copyFile(directory, "CasinoKey.yml");
        this.factory.copyFile(directory, "DiamondKey.yml");
    }

    @Override
    public void onEnable() {
        //this.crateManager = new CrateManager();
        //this.crateManager.load();

        //this.keyManager = new KeyManager();
        //this.keyManager.load();

        //this.userManager = new UserManager();

        //CommandManager.load();
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

    public CrateManager getCrateManager() {
        return this.crateManager;
    }

    public UserManager getUserManager() {
        return this.userManager;
    }
}
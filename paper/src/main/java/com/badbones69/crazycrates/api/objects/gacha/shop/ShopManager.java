package com.badbones69.crazycrates.api.objects.gacha.shop;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.data.ShopInfo;
import com.ryderbelserion.vital.paper.files.config.FileManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ShopManager {
    private final CrazyCrates plugin = JavaPlugin.getPlugin(CrazyCrates.class);
    private final FileManager yamlManager = plugin.getFileManager();
    private final List<ShopInfo> names = new ArrayList<>();
    private final DatabaseManager databaseManager;

    public ShopManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        yamlManager.getCustomFiles().stream().filter(customFile -> customFile.getFile().getParent().contains("banners")).forEach(customFile -> {
            System.out.println("Reading file: " + customFile.getFile().getName());
        });
    }
}

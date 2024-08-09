package com.badbones69.crazycrates.api.objects.gacha.shop;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.enums.CurrencyType;
import com.badbones69.crazycrates.api.objects.gacha.enums.ShopID;
import com.badbones69.crazycrates.api.objects.gacha.enums.Table;
import com.ryderbelserion.vital.paper.files.config.CustomFile;
import com.ryderbelserion.vital.paper.files.config.FileManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShopManager {
    private final CrazyCrates plugin = JavaPlugin.getPlugin(CrazyCrates.class);
    private final FileManager yamlManager = plugin.getFileManager();
    private final DatabaseManager databaseManager;
    private final List<ShopData> shops = new ArrayList<>();

    public ShopManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        yamlManager.getCustomFiles()
                .stream()
                .filter(customFile -> customFile.getFile().getParent().contains("shops"))
                .map(CustomFile::getConfiguration)
                .map(cfg -> {
                    ShopID shopID = ShopID.getShopID(cfg.getString("id"));
                    CurrencyType currencyType = CurrencyType.getCurrencyType(cfg.getString("currency"));
                    String name = cfg.getString("name");

                    if (shopID == null || currencyType == null || name == null) {
                        return null;
                    }

                    ConfigurationSection items = cfg.getConfigurationSection("items");
                    if (items == null) {
                        return null;
                    }

                    List<ShopItem> shopItems = items
                            .getKeys(false)
                            .stream()
                            .map(items::getConfigurationSection)
                            .filter(Objects::nonNull)
                            .map(item -> {
                                ItemStack itemFromCache = databaseManager
                                        .getItemManager()
                                        .getItemFromCache(item.getInt("id"), Table.SHOP_ITEMS);

                                if (itemFromCache == null) {
                                    return null;
                                }

                                return new ShopItem(
                                        itemFromCache,
                                        item.getInt("price"),
                                        item.getInt("limit"),
                                        item.getInt("place"));
                            })
                            .filter(Objects::nonNull)
                            .toList();

                    return new ShopData(shopID, currencyType, name, shopItems);
                })
                .filter(Objects::nonNull)
                .forEach(shops::add);
    }
}

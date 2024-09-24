package com.badbones69.crazycrates.api.objects.gacha.shop;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.builders.items.ShopMenu;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.enums.CurrencyType;
import com.badbones69.crazycrates.api.objects.gacha.enums.ShopID;
import com.badbones69.crazycrates.api.objects.gacha.enums.Table;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.ComponentBuilder;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuStuff;
import com.ryderbelserion.vital.paper.api.files.CustomFile;
import com.ryderbelserion.vital.paper.api.files.FileManager;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class ShopManager {
    private final List<ShopData> shops = new ArrayList<>();
    private final LimitManager limitManager;

    public ShopManager(DatabaseManager databaseManager) {
        CrazyCrates plugin = JavaPlugin.getPlugin(CrazyCrates.class);
        FileManager yamlManager = plugin.getFileManager();
        limitManager = new LimitManager();

        yamlManager.getCustomFiles()
                .values()
                .stream()
                .filter(customFile -> customFile.getFile().getParent().contains("shops"))
                .map(CustomFile::getConfiguration)
                .filter(Objects::nonNull)
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
                                int id = item.getInt("id");
                                ItemStack itemFromCache = databaseManager
                                        .getItemManager()
                                        .getItemFromCache(id, Table.SHOP_ITEMS);

                                if (itemFromCache == null) {
                                    return null;
                                }

                                return new ShopItem(
                                        itemFromCache,
                                        item.getInt("price"),
                                        item.getInt("limit"),
                                        item.getInt("place"),
                                        id,
                                        item.getString("crate")
                                );
                            })
                            .filter(Objects::nonNull)
                            .toList();

                    return new ShopData(shopID, currencyType, name, shopItems);
                })
                .filter(Objects::nonNull)
                .sorted()
                .forEach(shops::add);
    }

    public void openFirst(Crate crate, Player player) {
        if (shops.isEmpty()) return;
        ShopData shopData = shops.getFirst();

        shop(crate, player, shopData);
    }

    public void openID(Crate crate, Player player, int id) {
        if (shops.isEmpty()) return;
        if (id < 0 || id >= shops.size()) return;
        ShopData shopData = shops.get(id);

        shop(crate, player, shopData);
    }

    public void openShop(Crate crate, Player player, ShopID shopID) {
        if (shops.isEmpty()) return;
        ShopData shopData = shops.stream()
                .filter(shop -> shop.shopID() == shopID)
                .findFirst()
                .orElse(null);

        if (shopData == null) return;

        shop(crate, player, shopData);
    }

    private void shop(Crate crate, Player player, ShopData shopData) {
        Component shop = ComponentBuilder.shop(player, shopData.shopName());
        ShopMenu shopMenu = new ShopMenu(crate, player, shop, this, shopData);

        Inventory inventory = shopMenu.getInventory();
        inventory.setItem(45, UltimateMenuStuff.SHOP_BANNER.asItemStack());
        inventory.setItem(49, UltimateMenuStuff.MAIN_MENU_SHOP.asItemStack());

        buildGUI(shopMenu, shopData.shopID());
        buildItems(player, inventory, shopData);

        player.openInventory(shopMenu.getInventory());
    }

    private void buildItems(Player player, Inventory inventory, ShopData shopData) {
        AtomicInteger slot = new AtomicInteger(27);
        shopData.sorted()
                .limit(18)
                .map(shopItem -> apply(player, shopItem, shopData))
                .filter(Objects::nonNull)
                .forEach(itemStack -> inventory.setItem(slot.getAndIncrement(), itemStack));
    }

    private ItemStack apply(Player player, ShopItem shopItem, ShopData shopData) {
        if (shopItem == null) return null;
        ShopPurchase shopPurchase = limitManager.getData(player, shopData.shopID(), shopItem, false);
        ItemStack stack = shopItem.stack().clone();
        ItemBuilder itemBuilder = new ItemBuilder(stack);
        itemBuilder.addDisplayLore("<green><b>Price: <white>" + shopItem.price());

        switch (shopPurchase.limitType()) {
            case SUCCESS -> itemBuilder.addDisplayLore("<green><b>Bought: <white>" + shopPurchase.bought() + "/" +  shopItem.limit());
            case LIMIT_REACHED -> itemBuilder.addDisplayLore("<red><b>Limit Reached");
            case UNLIMITED -> itemBuilder.addDisplayLore("<green><b>Limit: <white>Unlimited");
        }

        return itemBuilder.asItemStack();
    }

    public void buildGUI(InventoryBuilder builder, ShopID selected) {
        if (shops.isEmpty()) return;

        int slot = 0;

        ItemBuilder selectedMain = UltimateMenuStuff.SHOP_SELECTED;
        ItemBuilder unselectedMain = UltimateMenuStuff.SHOP_UNSELECTED;
        Inventory inv = builder.getInventory();

        for (ShopData shopData : shops) {
            selectedMain.setDisplayName("<green><b>" + shopData.shopName());
            unselectedMain.setDisplayName("<red><b>" + shopData.shopName());

            ShopID shopID = shopData.shopID();
            int ordinal = shopID.ordinal();

            if (selected == shopID) {
                selectedMain.setCustomModelData(1000007 - ordinal);
                inv.setItem(slot, selectedMain.asItemStack());

                selectedMain.setCustomModelData(1000003);
                inv.setItem(slot + 1, selectedMain.asItemStack());
                inv.setItem(slot + 2, selectedMain.asItemStack());
            } else {
                unselectedMain.setCustomModelData(1000007 - ordinal);
                inv.setItem(slot, unselectedMain.asItemStack());

                unselectedMain.setCustomModelData(1000001);
                inv.setItem(slot + 1, unselectedMain.asItemStack());
                inv.setItem(slot + 2, unselectedMain.asItemStack());
            }

            slot += 3;
        }
    }
}

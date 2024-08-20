package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.api.objects.gacha.shop.ShopData;
import com.badbones69.crazycrates.api.objects.gacha.shop.ShopItem;
import com.badbones69.crazycrates.api.objects.gacha.shop.ShopManager;
import com.badbones69.crazycrates.api.objects.gacha.shop.ShopPurchase;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuStuff;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopMenu extends InventoryBuilder {

    private final ShopManager shopManager;
    private final ShopData shopData;

    public ShopMenu(Crate crate, Player player, Component title, ShopManager shopManager, ShopData shopData) {
        super(crate, player, 54, title);
        this.shopManager = shopManager;
        this.shopData = shopData;
    }

    @Override
    public InventoryBuilder build() {
        return this;
    }

    @Override
    public void run(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof ShopMenu shopMenu)) return;

        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0) return;

        Player player = shopMenu.getPlayer();

        int shopID = switch (slot) {
            case 0, 1, 2 -> 0;
            case 3, 4, 5 -> 1;
            case 6, 7, 8 -> 2;
            default -> -1;
        };

        if (shopID != -1) {
            player.playSound(UltimateMenuStuff.CLICK);
            shopManager.openID(getCrate(), getPlayer(), shopID);
            return;
        }

        ItemStack item = e.getCurrentItem();

        if (slot >= 27 && slot < 45 && item != null) {
            ShopItem itemByPlace = shopData.getItemByPlace(slot - 27);

            PlayerBaseProfile playerBaseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());
            if (playerBaseProfile.has(itemByPlace.price(), shopData.currencyType())) {
                ShopPurchase data = shopManager.getLimitManager().getData(player, shopData.shopID(), itemByPlace, true);

                if (data.isSuccess()) {
                    playerBaseProfile.remove(itemByPlace.price(), shopData.currencyType());
                    player.getInventory().addItem(itemByPlace.stack());
                    shopManager.openShop(getCrate(), getPlayer(), shopData.shopID());
                } else {
                    player.sendMessage("You have reached the limit for this item.");
                }
            } else {
                player.sendMessage("You don't have enough money to buy this item.");
            }

            return;
        }

        if (slot == 49) {
            plugin.getCrateManager().getDatabaseManager().getUltimateMenuManager().open(player, shopMenu.getCrate());
        }
    }
}

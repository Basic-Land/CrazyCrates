package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.enums.ShopID;
import com.badbones69.crazycrates.api.objects.gacha.shop.ShopManager;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuStuff;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopMenu extends InventoryBuilder {

    private final ShopManager shopManager;

    public ShopMenu(Crate crate, Player player, Component title, ShopManager shopManager) {
        super(crate, player, 54, title);
        this.shopManager = shopManager;
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

        int shopID;

        if (slot < 3) {
            shopID = 0;
        } else if (slot < 6) {
            shopID = 1;
        } else if (slot < 9) {
            shopID = 2;
        } else shopID = -1;

        if (shopID != -1) {
            player.playSound(UltimateMenuStuff.CLICK);
            System.out.println("slot: " + slot);
            shopManager.openID(getCrate(), getPlayer(), shopID);
        }

        if (slot == 49) {
            plugin.getCrateManager().getDatabaseManager().getUltimateMenuManager().open(player, shopMenu.getCrate());
        }
    }
}

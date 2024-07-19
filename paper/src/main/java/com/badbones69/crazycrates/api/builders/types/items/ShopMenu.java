package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.ComponentBuilder;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuStuff;
import com.ryderbelserion.vital.paper.builders.items.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;

public class ShopMenu extends InventoryBuilder {
    private static final Map<Integer, String> names = new HashMap<>();
    private final int selectedCrate;
    static {
        names.put(0, "rare shop");
        names.put(1, "less rare shop");
        names.put(2, "vote shop");
    }

    public ShopMenu(Crate crate, Player player, Component title, int selectedCrate) {
        super(crate, player, 54, title);
        this.selectedCrate = selectedCrate;
    }

    public ShopMenu(ShopMenu menu, int page) {
        this(menu.getCrate(), menu.getPlayer(), ComponentBuilder.shop(menu.getPlayer(), names.get(page)), page);
    }

    public ShopMenu(Crate crate, Player player) {
        this(crate, player, ComponentBuilder.shop(player, names.get(0)), 0);
    }

    @Override
    public InventoryBuilder build() {
        setTopCrates();
        setTextureGlass();
        return this;
    }

    @Override
    public void run(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof ShopMenu shopMenu)) return;

        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0) return;

        Player player = shopMenu.getPlayer();

        int newCrateNum;

        if (slot < 3) {
            newCrateNum = 0;
        } else if (slot < 6) {
            newCrateNum = 1;
        } else if (slot < 9) {
            newCrateNum = 2;
        } else newCrateNum = -1;

        if (newCrateNum != -1) {
            player.playSound(UltimateMenuStuff.CLICK);
            player.openInventory(new ShopMenu(shopMenu, newCrateNum).build().getInventory());
        }
    }

    private void setTopCrates() {
        int slot = 0;

        ItemBuilder selectedMain = UltimateMenuStuff.SHOP_SELECTED;
        ItemBuilder unselectedMain = UltimateMenuStuff.SHOP_UNSELECTED;

        for (int crate = 0; crate < 3; crate++) {
            selectedMain.setDisplayName("<green><b>" + names.get(crate));
            unselectedMain.setDisplayName("<red><b>" + names.get(crate));

            if (crate == selectedCrate) {
                selectedMain.setCustomModelData(1000005 + crate);
                getInventory().setItem(slot, selectedMain.getStack());

                selectedMain.setCustomModelData(1000003);
                getInventory().setItem(slot + 1, selectedMain.getStack());
                getInventory().setItem(slot + 2, selectedMain.getStack());
            } else {
                unselectedMain.setCustomModelData(1000005 + crate);
                getInventory().setItem(slot, unselectedMain.getStack());

                unselectedMain.setCustomModelData(1000001);
                getInventory().setItem(slot + 1, unselectedMain.getStack());
                getInventory().setItem(slot + 2, unselectedMain.getStack());
            }

            slot += 3;
        }
    }

    private void setTextureGlass() {
        getInventory().setItem(45, UltimateMenuStuff.SHOP_BANNER.getStack());
    }
}

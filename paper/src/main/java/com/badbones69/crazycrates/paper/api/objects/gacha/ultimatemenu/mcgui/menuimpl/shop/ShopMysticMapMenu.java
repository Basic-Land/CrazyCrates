package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl.shop;

import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MenuType;
import org.bukkit.entity.Player;

import java.util.List;

public class ShopMysticMapMenu extends ShopBase {
    @Override
    public List<String> getItems() {
        return List.of();
    }

    @Override
    public void build(MapMenu menu) {
        super.build(menu);
    }

    @Override
    public void populate(MapMenu menu, Player player) {
        super.populate(menu, player);

    }

    @Override
    public MenuType menuType() {
        return MenuType.MYSTIC_SHOP;
    }
}

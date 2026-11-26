package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl;

import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MenuType;
import org.bukkit.entity.Player;

public class EmptyMapMenu extends BaseMenu {
    @Override
    public void build(MapMenu menu) {

    }

    @Override
    public void populate(MapMenu menu, Player player) {

    }

    @Override
    public MenuType menuType() {
        return MenuType.EMPTY;
    }
}

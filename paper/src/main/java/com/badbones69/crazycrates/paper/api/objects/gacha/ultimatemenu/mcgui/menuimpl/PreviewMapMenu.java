package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl;

import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MenuType;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MenuUtils;
import org.bukkit.entity.Player;

import java.util.Map;

public class PreviewMapMenu extends BaseMenu {
    public PreviewMapMenu() {

    }


    @Override
    public void build(MapMenu menu) {
        addScaledImageBy(menu, "preview/preview-menu", 248, 147, 3);
        addScaledImageBy(menu, "preview/common", 380, 309, 3);
        addScaledImageBy(menu, "preview/epic", 488, 309, 3);
        addScaledImageBy(menu, "preview/legendary", 596, 309, 3);
        addScaledImageBy(menu, "mainmenu", 275, 416, 3);

        menu.addField("preview", "Preview Menu", 248, 101, 529, 48);

        menu.addButton(371, 300, 66, 66);
        menu.addButton(479, 300, 66, 66);
        menu.addButton(587, 300, 66, 66);
        menu.addButton(700, 408, 56, 56);
        menu.addButton(480, 413, 64, 51);
        menu.addButton("main menu", 273, 413, 52, 52, b -> {
            openMenu(b, MenuType.GACHA);
        });
    }

    @Override
    public void populate(MapMenu menu, Player player) {

    }

    @Override
    public MenuType menuType() {
        return MenuType.PREVIEW;
    }
}

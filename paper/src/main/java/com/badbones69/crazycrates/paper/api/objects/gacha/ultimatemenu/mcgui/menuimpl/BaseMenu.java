package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl;

import com.badbones69.crazycrates.paper.CrazyCrates;
import com.badbones69.crazycrates.paper.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.*;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseMenu implements MCGUIContent {
    private static final CanvasSettings CANVAS_SETTINGS = new CanvasSettings(1024, 640, true, 0);
    protected static final CrazyCrates plugin = CrazyCrates.getPlugin();
    protected static final DatabaseManager databaseManager = plugin.getCrateManager().getDatabaseManager();

    @Override
    public final CanvasSettings canvasSettings() {
        return CANVAS_SETTINGS;
    }

    protected final void addImage(MapMenu menu, String name, int x, int y) {
        BufferedImage image = MenuUtils.loadImage(name);
        if (image != null) menu.addImage(name, image, x, y);
    }

    protected final void addScaledImage(MapMenu menu, String name, int x, int y) {
        BufferedImage image = MenuUtils.loadImage(name);
        if (image != null) menu.addImage(name, MenuUtils.scaleBy2(image), x, y);
    }

    protected final void addScaledImageBy(MapMenu menu, String name, int x, int y, int scale) {
        BufferedImage image = MenuUtils.loadImage(name);
        if (image != null) menu.addImage(name, MenuUtils.scaleBy(image, scale), x, y);
    }

    protected final void openMenu(ButtonClick b, MenuType type) {
        b.close();
        Player player = b.player();
        MenuUtils.OPEN_MENUS.computeIfAbsent(player.getUniqueId(), _ -> new ConcurrentHashMap<>()).computeIfAbsent(type, a -> new MapMenu(a.getMenu())).open(player);
    }
}

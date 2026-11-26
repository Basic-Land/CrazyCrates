package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl;

import com.badbones69.crazycrates.paper.CrazyCrates;
import com.badbones69.crazycrates.paper.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.CanvasSettings;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MCGUIContent;

public abstract class BaseMenu implements MCGUIContent {
    private static final CanvasSettings CANVAS_SETTINGS = new CanvasSettings(1024, 640, true, 0);
    protected static final CrazyCrates plugin = CrazyCrates.getPlugin();
    protected static final DatabaseManager databaseManager = plugin.getCrateManager().getDatabaseManager();

    @Override
    public final CanvasSettings canvasSettings() {
        return CANVAS_SETTINGS;
    }
}

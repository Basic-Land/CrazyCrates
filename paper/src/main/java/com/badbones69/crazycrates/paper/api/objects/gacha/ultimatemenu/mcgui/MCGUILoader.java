package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

import com.badbones69.crazycrates.paper.CrazyCrates;

import java.awt.*;
import java.io.InputStream;
import java.util.logging.Level;

public class MCGUILoader {
    private Font font;

    public MCGUILoader(CrazyCrates plugin) {
        try (InputStream in = plugin.getDataFolder().toPath().resolve("mcgui/font/Monocraft.ttf").toFile().toURI().toURL().openStream()) {
            font = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Could not load the bundled font; using a plain one", e);
            font = new Font(Font.MONOSPACED, Font.PLAIN, 16);
        }
    }
}


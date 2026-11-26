package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

import com.badbones69.crazycrates.paper.CrazyCrates;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.InputStream;

/**
 * Supplies the initial layout and live values for a {@link MapMenu}.
 * Implementations keep GUI-specific content out of the editor.
 */
public interface MCGUIContent {
    void build(MapMenu menu);

    void populate(MapMenu menu, Player player);

    CanvasSettings canvasSettings();

    MenuType menuType();

    default Font font() {
        Font font;
        CrazyCrates plugin = CrazyCrates.getPlugin();
        try (InputStream in = plugin.getDataFolder().toPath().resolve("mcgui/font/Monocraft.ttf").toFile().toURI().toURL().openStream()) {
            font = Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(12f);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not load font, using monospaced: " + e);
            font = new Font(Font.MONOSPACED, Font.PLAIN, 16);
        }
        return font;
    }
}

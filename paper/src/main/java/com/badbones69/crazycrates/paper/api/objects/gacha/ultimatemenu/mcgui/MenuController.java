package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

import ai.resourcepack.ui.framework.api.Key;
import org.bukkit.entity.Player;

import java.awt.*;

/**
 * Optional hook that takes over a {@link MapMenu}'s input and draws on top of it.
 * Without a controller the menu only dispatches button actions. The editor is a controller.
 */
public interface MenuController {
    void click(Player player, int x, int y, String clickName);

    /**
     * @return true if the menu should be redrawn
     */
    boolean key(Player player, Key key, boolean down);

    void chat(Player player, String text);

    void close(Player player);
    /**
     * Painted after the images and fields, on the same graphics.
     */
    default void paintOverlay(Graphics2D g) {
    }
}

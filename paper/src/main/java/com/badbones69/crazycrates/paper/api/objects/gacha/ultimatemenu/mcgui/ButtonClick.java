package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

import org.bukkit.entity.Player;

/**
 * Passed to a button's action when a player clicks it.
 *
 * @param player    the player who clicked
 * @param menu      the menu the button belongs to
 * @param button    the clicked button
 * @param x         canvas x of the click
 * @param y         canvas y of the click
 * @param clickName name of the framework's click type (e.g. LEFT / RIGHT), never null
 */
public record ButtonClick(Player player, MapMenu menu, Element button, int x, int y, String clickName) {
    public String name() {
        return button.name();
    }

    public boolean isRight() {
        return clickName.toUpperCase().contains("RIGHT");
    }

    public boolean isLeft() {
        return !isRight();
    }
}

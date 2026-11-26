package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

import ai.resourcepack.ui.framework.api.*;
import com.badbones69.crazycrates.paper.api.objects.gacha.util.TickedCache;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Forwards view events of one player to the {@link MapMenu}.
 */
public record ClickListener(MapMenu menu, Player player) implements ViewContent {
    public static final TickedCache<UUID, Boolean> CACHE = new TickedCache<>(10);

    @Override
    public Canvas canvas() {
        return menu.getCanvas();
    }

    @Override
    public void pointer(View view, int x, int y) {
        menu.handleHover(player, x, y);
    }

    @Override
    public void click(View view, int x, int y, Click click) {
        menu.handleClick(player, x, y, click == null ? "" : click.name());
    }

    @Override
    public void key(View view, Key key, boolean down) {
        menu.handleKey(player, key, down);
    }

    @Override
    public void chat(View view, String text) {
        menu.handleChat(player, text);
    }

    @Override
    public void closed(View view, CloseReason reason) {
        menu.handleClose(player);
        CACHE.put(player.getUniqueId(), true);
    }
}
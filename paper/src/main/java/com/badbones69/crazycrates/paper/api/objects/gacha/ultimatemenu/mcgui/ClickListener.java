package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

import ai.resourcepack.ui.framework.api.*;

public record ClickListener(MapMenu menu) implements ViewContent {

    @Override
    public Canvas canvas() {
        return menu.getCanvas();
    }

    @Override
    public void pointer(View view, int x, int y) {
        // intentionally empty (no console spam)
    }

    @Override
    public void click(View view, int x, int y, Click click) {
        try {
            menu.handleClick(x, y, click == null ? "" : click.name());
        } catch (Exception e) {
            System.out.println("[ClickListener] click error: " + e);
        }
    }

    @Override
    public void key(View view, Key key, boolean down) {
        System.out.println("Key event: " + key + ", down: " + down);
        try {
            menu.handleKey(key, down);
        } catch (Exception e) {
            System.out.println("[ClickListener] key error: " + e);
        }
    }

    @Override
    public void chat(View view, String text) {
        try {
            menu.handleChat(text);
        } catch (Exception e) {
            System.out.println("[ClickListener] chat error: " + e);
        }
    }

    @Override
    public void closed(View view, CloseReason reason) {
        System.out.println("View closed with reason: " + reason);
    }
}
package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

public record CanvasSettings(int width, int height, boolean transparency, int tintColor) {
    public CanvasSettings {
        if (width % 128 != 0 || height % 128 != 0 || width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive integers that are multiples of 128.");
        }
    }
}

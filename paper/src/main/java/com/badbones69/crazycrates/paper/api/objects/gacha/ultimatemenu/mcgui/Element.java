package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * One image, text field or button placed on a {@link MapMenu}.
 */
@Getter
@Accessors(fluent = true)
public final class Element {
    private final Kind kind;
    private final String name;
    private final BufferedImage image;   // images only
    private int x, y, width, height;
    private String text = "";            // fields only
    private Color background, textColor; // fields only
    @Setter
    private Consumer<ButtonClick> action; // buttons only
    @Setter
    private boolean hoverOutline = true;  // buttons only: outline when hovered
    Element(Kind kind, String name, BufferedImage image, int x, int y, int width, int height) {
        this.kind = kind;
        this.name = name;
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setBounds(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    void setColors(Color background, Color textColor) {
        this.background = background;
        this.textColor = textColor;
    }

    /**
     * Whether the canvas point is inside this element. Transparent image pixels do not count.
     */
    public boolean contains(int cx, int cy) {
        int px = cx - x;
        int py = cy - y;
        if (px < 0 || py < 0 || px >= width || py >= height) return false;
        if (kind == Kind.IMAGE) return (image.getRGB(px, py) >>> 24) != 0;
        return true;
    }

    public enum Kind {IMAGE, FIELD, BUTTON}
}

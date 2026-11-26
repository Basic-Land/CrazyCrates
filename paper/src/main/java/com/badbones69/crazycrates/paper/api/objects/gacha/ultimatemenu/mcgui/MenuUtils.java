package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

import com.badbones69.crazycrates.paper.CrazyCrates;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class MenuUtils {
    public static BufferedImage scaleBy2(BufferedImage source) {
        if (source == null) return null;
        BufferedImage scaled = new BufferedImage(source.getWidth() * 2, source.getHeight() * 2, source.getType());
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(source, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
        g.dispose();
        return scaled;
    }

    public static String stripPng(String name) {
        return name.toLowerCase().endsWith(".png") ? name.substring(0, name.length() - 4) : name;
    }

    /**
     * Loads mcgui/textures/<n>.png (name may be given with or without .png). Returns null on failure.
     */
    public static BufferedImage loadImage(String name) {
        try {
            name = stripPng(name);
            File file = CrazyCrates.getPlugin().getDataFolder().toPath().resolve("mcgui/textures/" + name + ".png").toFile();
            BufferedImage img = ImageIO.read(file);
            if (img == null) log("Not a readable image: " + file);
            return img;
        } catch (Exception e) {
            log("Could not load image '" + name + "': " + e);
            return null;
        }
    }

    public static void log(String message) {
        CrazyCrates.getPlugin().getLogger().info("[MapMenu] " + message);
    }
}

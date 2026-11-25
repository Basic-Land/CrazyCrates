package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

import ai.resourcepack.ui.framework.api.Canvas;
import ai.resourcepack.ui.framework.api.Key;
import ai.resourcepack.ui.framework.api.UIFramework;
import ai.resourcepack.ui.framework.api.ViewOptions;
import com.badbones69.crazycrates.paper.CrazyCrates;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * A map-canvas GUI made of images, text fields and buttons.
 * <p>
 * Used on its own it is the final menu: build it from a {@link MCGUIContent}, bind button actions and open it.
 * <pre>{@code
 * MapMenu menu = new MapMenu(plugin, new GachaMapMenuContent(plugin));
 * menu.onClick("1x pull", click -> pull(click.player(), 1));
 * menu.open(player);
 * }</pre>
 * To lay it out, attach the editor first: {@code MenuEditor.attach(menu)} (see the editor package).
 * <p>
 * Layers: {@link MenuRenderer} paints it, {@link MenuController} (optional, e.g. the editor) takes over input.
 * Button actions run on whichever thread the UI framework calls from; schedule Bukkit work yourself.
 */
public class MapMenu {
    private static final Color FIELD_BG = new Color(30, 30, 30);
    private static final Color FIELD_TEXT = Color.WHITE;

    private final CrazyCrates plugin;
    private final MCGUIContent content;
    private final UIFramework engine = UIFramework.get();
    private final BufferedImage image;
    @Getter
    private final Canvas canvas;
    @Getter
    private final Font font;
    private final MenuRenderer renderer;
    private final List<Element> elements = new ArrayList<>();
    @Setter
    @Getter
    private volatile MenuController controller;
    private Element hovered; // button currently under the cursor; guarded by this

    public MapMenu(CrazyCrates plugin, MCGUIContent content) {
        this.plugin = plugin;
        this.content = content;
        CanvasSettings settings = content.canvasSettings();
        canvas = engine.canvas(settings.width() / 128, settings.height() / 128);
        canvas.transparency(settings.transparency());
        canvas.tint(settings.tintColor());
        font = content.font();
        image = new BufferedImage(settings.width(), settings.height(), BufferedImage.TYPE_INT_ARGB);
        renderer = new MenuRenderer(image, font);
        content.build(this);
    }

    public static void log(String message) {
        CrazyCrates.getPlugin().getLogger().info("[MapMenu] " + message);
    }

    public Logger logger() {
        return plugin.getLogger();
    }

    public int canvasWidth() {
        return image.getWidth();
    }

    public int canvasHeight() {
        return image.getHeight();
    }

    // ------------------------------------------------------------------ images

    public BufferedImage scaleBy2(BufferedImage source) {
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
    public BufferedImage loadImage(String name) {
        try {
            name = stripPng(name);
            File file = plugin.getDataFolder().toPath().resolve("mcgui/textures/" + name + ".png").toFile();
            BufferedImage img = ImageIO.read(file);
            if (img == null) log("Not a readable image: " + file);
            return img;
        } catch (Exception e) {
            log("Could not load image '" + name + "': " + e);
            return null;
        }
    }

    // ------------------------------------------------------------------ adding elements

    public synchronized Element addImage(String name, BufferedImage img, int x, int y) {
        if (img == null) return null;
        Element el = new Element(Element.Kind.IMAGE, name, img, x, y, img.getWidth(), img.getHeight());
        elements.add(el);
        return el;
    }

    public Element addField(int x, int y, int w, int h) {
        return addField(null, null, null, null, x, y, w, h);
    }

    public Element addField(String name, String value, int x, int y, int w, int h) {
        return addField(name, value, null, null, x, y, w, h);
    }

    public synchronized Element addField(String name, String value, Color background, Color text,
                                         int x, int y, int w, int h) {
        Element el = new Element(Element.Kind.FIELD, name, null, x, y, w, h);
        el.setText(value);
        el.setColors(background == null ? FIELD_BG : background, text == null ? FIELD_TEXT : text);
        elements.add(el);
        return el;
    }

    public Element addButton(int x, int y, int w, int h) {
        return addButton(null, x, y, w, h, null);
    }

    public Element addButton(String name, int x, int y, int w, int h) {
        return addButton(name, x, y, w, h, null);
    }

    public synchronized Element addButton(String name, int x, int y, int w, int h, Consumer<ButtonClick> action) {
        Element el = new Element(Element.Kind.BUTTON, name, null, x, y, w, h);
        el.action(action);
        elements.add(el);
        return el;
    }

    // ------------------------------------------------------------------ button actions

    /**
     * Sets the action for every button with this name (case-insensitive).
     *
     * @return false if no button has that name
     */
    public synchronized boolean onClick(String buttonName, Consumer<ButtonClick> action) {
        boolean found = false;
        for (Element el : elements) {
            if (el.kind() == Element.Kind.BUTTON && buttonName.equalsIgnoreCase(el.name())) {
                el.action(action);
                found = true;
            }
        }
        if (!found) log("No button named '" + buttonName + "'");
        return found;
    }

    // ------------------------------------------------------------------ hover

    /**
     * Color of the outline drawn around a hovered button (null = default white).
     */
    public void setHoverColor(Color color) {
        renderer.setHoverColor(color);
    }

    /**
     * Call when the cursor moves over the canvas. Outlines the button under the cursor and only redraws when the
     * hovered button changes. Every button is outlined by default; see {@link Element#hoverOutline()} (boolean).
     */
    public synchronized void handleHover(Player player, int x, int y) {
        Element button = hit(Element.Kind.BUTTON, x, y);
        if (button != null && !button.hoverOutline()) button = null;
        if (button == hovered) return;
        hovered = button;
        redraw();
    }

    /**
     * Removes the hover outline (e.g. when the cursor leaves or the menu is opened).
     */
    public synchronized void clearHover() {
        if (hovered == null) return;
        hovered = null;
        redraw();
    }

    // ------------------------------------------------------------------ element access

    public synchronized List<Element> elements() {
        return Collections.unmodifiableList(elements);
    }

    public synchronized void remove(Element el) {
        elements.remove(el);
        if (el == hovered) hovered = null;
    }

    /**
     * The n-th (1-based) element of a kind, in creation order.
     */
    public synchronized Element element(Element.Kind kind, int n) {
        int count = 0;
        for (Element el : elements) {
            if (el.kind() == kind && ++count == n) return el;
        }
        return null;
    }

    /**
     * 1-based number of the element among elements of its kind, or -1.
     */
    public synchronized int number(Element target) {
        int n = 0;
        for (Element el : elements) {
            if (el.kind() == target.kind()) n++;
            if (el == target) return n;
        }
        return -1;
    }

    /**
     * The topmost (last added) element of a kind at the canvas point, or null.
     */
    public synchronized Element hit(Element.Kind kind, int x, int y) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            Element el = elements.get(i);
            if (el.kind() == kind && el.contains(x, y)) return el;
        }
        return null;
    }

    // ------------------------------------------------------------------ live values

    /**
     * Sets the text of a field (1-based field number) and redraws.
     */
    public void setFieldValue(int fieldNumber, int number) {
        setFieldValue(fieldNumber, String.valueOf(number));
    }

    public synchronized void setFieldValue(int fieldNumber, String text) {
        Element field = element(Element.Kind.FIELD, fieldNumber);
        if (field == null) {
            log("No text field #" + fieldNumber);
            return;
        }
        field.setText(text);
        redraw();
    }

    // ------------------------------------------------------------------ drawing / lifecycle

    /**
     * Repaints the backing image and sends it to the canvas.
     */
    public synchronized void redraw() {
        try {
            renderer.render(elements, hovered, controller);
            canvas.draw(image, 0, 0);
        } catch (Exception e) {
            log("Draw error: " + e);
        }
    }

    public void open(Player player) {
        try {
            hovered = null;
            redraw();
            player.getInventory().setHeldItemSlot(8);
            content.populate(this, player);
            engine.openView(player, new ClickListener(this, player), new ViewOptions(0, true, true));
        } catch (Exception e) {
            log("Open error: " + e);
        }
    }

    // ------------------------------------------------------------------ input (called by ClickListener)

    public void handleClick(Player player, int x, int y, String clickName) {
        String click = clickName == null ? "" : clickName;
        MenuController c = controller;
        try {
            if (c != null) {
                synchronized (this) {
                    c.click(player, x, y, click);
                    redraw();
                }
                return;
            }
            Element button;
            synchronized (this) {
                button = hit(Element.Kind.BUTTON, x, y);
            }
            // run outside the lock so a slow action cannot block drawing
            if (button != null && button.action() != null) {
                button.action().accept(new ButtonClick(player, this, button, x, y, click));
            }
        } catch (Exception e) {
            log("Click error: " + e);
        }
    }

    public synchronized void handleKey(Player player, Key key, boolean down) {
        MenuController c = controller;
        if (c == null) return;
        try {
            if (!c.key(player, key, down)) return;
        } catch (Exception e) {
            log("Key error: " + e);
        }
        redraw();
    }

    public synchronized void handleChat(Player player, String text) {
        MenuController c = controller;
        if (c == null) return;
        try {
            c.chat(player, text);
        } catch (Exception e) {
            log("Chat error: " + e);
        }
        redraw();
    }
}
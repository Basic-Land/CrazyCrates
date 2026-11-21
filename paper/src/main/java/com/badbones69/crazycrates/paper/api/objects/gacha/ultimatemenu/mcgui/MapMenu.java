package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

import ai.resourcepack.ui.framework.api.Canvas;
import ai.resourcepack.ui.framework.api.Key;
import ai.resourcepack.ui.framework.api.UIFramework;
import ai.resourcepack.ui.framework.api.ViewOptions;
import com.badbones69.crazycrates.paper.CrazyCrates;
import lombok.Getter;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock-up editor with one unified control scheme. Everything is wrapped in try/catch: errors only go to the console.
 * <p>
 * Elements: images, text fields and buttons. One element can be selected at a time.
 * <p>
 * TOOLS (hotbar)
 *   HOTBAR_1 Select   left click selects the element under the cursor (buttons > fields > images),
 *                     left click on nothing deselects, right click moves the selection to the cursor
 *   HOTBAR_2 Text     click two corners -> new text field, already selected with the SIZE handle
 *   HOTBAR_3 Button   click two corners -> new button, already selected with the SIZE handle
 *   HOTBAR_4 Preview  edit overlays hidden, clicking a button prints to the console
 * HANDLES (what the arrow keys change on the selection)
 *   HOTBAR_5 Move     HOTBAR_6 Size (bottom-right corner)     HOTBAR_7 Origin (top-left corner, bottom-right stays)
 *   images can only be moved
 * KEYS
 *   UP/DOWN/LEFT/RIGHT (W A S D)  nudge the selection with the current handle
 *   SPRINT                        toggle coarse (MOVE_STEP px) / fine (1 px) step, applies to everything
 *   JUMP (space)                  confirm and deselect
 *   SWAP_HANDS (F)                cancel: discard a new element, or revert the selection to where it was selected
 *   DROP (Q)                      delete the selection
 * CHAT
 *   load <image> | select <image> | select field <n> | select button <n> | center | delete | hud | save
 */
public class MapMenu {
    private final CrazyCrates plugin;
    private final BufferedImage image;
    @Getter
    private final Canvas canvas;
    private final UIFramework engine = UIFramework.get();
    private Font font;

    // 1024 x 640
    private static final int IMAGE_WIDTH = 8;
    private static final int IMAGE_HEIGHT = 5;
    private static final int MOVE_STEP = 4;
    private static final float FONT_SIZE = 16f;
    private static final int MIN_FONT_SIZE = 6;
    private static final int MAX_FONT_SIZE = 64;
    private static final Color FIELD_BG = new Color(30, 30, 30);
    private static final Color FIELD_TEXT = Color.WHITE;
    private static final Color BUTTON_FILL = new Color(0, 120, 255, 90);
    private static final Color BUTTON_LINE = new Color(0, 120, 255, 220);

    private enum Kind { IMAGE, FIELD, BUTTON }

    private enum Tool { SELECT, TEXT, BUTTON, PREVIEW }

    private enum Handle { MOVE, SIZE, ORIGIN }

    private static final class Element {
        final Kind kind;
        final String name;      // images only
        final BufferedImage img; // images only
        int x, y, w, h;

        Element(Kind kind, String name, BufferedImage img, int x, int y, int w, int h) {
            this.kind = kind;
            this.name = name;
            this.img = img;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    /** Draw order is by kind (images, then fields, then buttons) and then by list order. */
    private final List<Element> elements = new ArrayList<>();

    private Tool tool = Tool.SELECT;
    private Handle handle = Handle.MOVE;
    private Element selected;
    /** The selected element was just created (cancel discards it instead of reverting it). */
    private boolean selectedIsNew;
    /** {x, y, w, h} of the selection when it was selected, used by cancel. */
    private int[] backup;
    /** First corner while a create tool waits for the second click. */
    private int[] firstCorner;
    /** false = coarse step (MOVE_STEP px), true = fine step (1 px). Toggled with SPRINT. */
    private boolean fineAdjust;
    private boolean hud = true;

    public MapMenu(CrazyCrates plugin) {
        this.plugin = plugin;
        canvas = engine.canvas(IMAGE_WIDTH, IMAGE_HEIGHT);
        canvas.transparency(true);
        canvas.tint(0);

        try (InputStream in = plugin.getDataFolder().toPath().resolve("mcgui/font/Monocraft.ttf").toFile().toURI().toURL().openStream()) {
            font = Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(FONT_SIZE);
        } catch (Exception e) {
            log("Could not load font, using monospaced: " + e);
            font = new Font(Font.MONOSPACED, Font.PLAIN, 16);
        }

        image = new BufferedImage(IMAGE_WIDTH * 128, IMAGE_HEIGHT * 128, BufferedImage.TYPE_INT_ARGB);

        // starting layout
        try {
            addImage("art", scaleBy(loadImage("art")), 160, 164);
            addImage("hlmenu", scaleBy(loadImage("hlmenu")), 148, 60);
            addImage("gacha", scaleBy(loadImage("gacha")), 456, 77);
            addImage("selected", scaleBy(loadImage("selected")), 333, 122);
            addImage("notselected", scaleBy(loadImage("notselected")), 451, 122);
            addImage("notselected", scaleBy(loadImage("notselected")), 570, 122);
            addImage("pravosipka", loadImage("pravosipka"), 488, 485);
            addImage("levosipka", loadImage("levosipka"), 453, 485);

            // Text fields
            addRect(Kind.FIELD, 174, 296, 60, 22);
            addRect(Kind.FIELD, 174, 322, 60, 22);
            addRect(Kind.FIELD, 236, 322, 32, 22);
            addRect(Kind.FIELD, 236, 296, 32, 22);
            addRect(Kind.FIELD, 286, 296, 32, 22);
            addRect(Kind.FIELD, 286, 322, 32, 22);
            addRect(Kind.FIELD, 178, 436, 134, 18);
            addRect(Kind.FIELD, 222, 126, 90, 18);
            addRect(Kind.FIELD, 222, 80, 90, 18);
            addRect(Kind.FIELD, 758, 126, 90, 18);

            // Buttons
            addRect(Kind.BUTTON, 345, 485, 32, 32);
            addRect(Kind.BUTTON, 381, 485, 32, 32);
            addRect(Kind.BUTTON, 418, 485, 32, 32);
            addRect(Kind.BUTTON, 523, 482, 72, 36);
            addRect(Kind.BUTTON, 598, 482, 72, 36);
            addRect(Kind.BUTTON, 334, 123, 108, 40);
            addRect(Kind.BUTTON, 451, 123, 108, 40);
            addRect(Kind.BUTTON, 570, 123, 108, 40);
            addRect(Kind.BUTTON, 453, 485, 32, 32);
            addRect(Kind.BUTTON, 488, 485, 32, 32);
        } catch (Exception e) {
            log("Init error: " + e);
        }
    }

    private static void log(String message) {
        System.out.println("[MapMenu] " + message);
    }

    private BufferedImage scaleBy(BufferedImage source) {
        BufferedImage scaled = new BufferedImage(
                source.getWidth() * 2,
                source.getHeight() * 2,
                source.getType()
        );

        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(source, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
        g.dispose();
        return scaled;
    }

    private static String stripPng(String name) {
        return name.toLowerCase().endsWith(".png") ? name.substring(0, name.length() - 4) : name;
    }

    /** Loads mcgui/textures/<n>.png (name may be given with or without .png). Returns null on failure. */
    private BufferedImage loadImage(String name) {
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

    // ------------------------------------------------------------------ elements

    private Element addImage(String name, BufferedImage img, int x, int y) {
        Element el = new Element(Kind.IMAGE, name, img, x, y, img.getWidth(), img.getHeight());
        elements.add(el);
        return el;
    }

    private Element addRect(Kind kind, int x, int y, int w, int h) {
        Element el = new Element(kind, null, null, x, y, w, h);
        elements.add(el);
        return el;
    }

    private Element findImage(String name) {
        for (Element el : elements) {
            if (el.kind == Kind.IMAGE && el.name.equalsIgnoreCase(name)) return el;
        }
        return null;
    }

    /** 1-based number of the element among elements of its kind. */
    private int number(Element target) {
        int n = 0;
        for (Element el : elements) {
            if (el.kind == target.kind) n++;
            if (el == target) return n;
        }
        return -1;
    }

    private Element nth(Kind kind, int n) {
        int count = 0;
        for (Element el : elements) {
            if (el.kind == kind && ++count == n) return el;
        }
        return null;
    }

    private String label(Element el) {
        return switch (el.kind) {
            case IMAGE -> "image '" + el.name + "'";
            case FIELD -> "field #" + number(el);
            case BUTTON -> "button #" + number(el);
        };
    }

    private String describe(Element el) {
        return label(el) + " at " + el.x + ", " + el.y + " size " + el.w + "x" + el.h;
    }

    private boolean contains(Element el, int x, int y) {
        int px = x - el.x;
        int py = y - el.y;
        if (px < 0 || py < 0 || px >= el.w || py >= el.h) return false;
        if (el.kind == Kind.IMAGE) return (el.img.getRGB(px, py) >>> 24) != 0; // ignore transparent pixels
        return true;
    }

    private Element topmost(Kind kind, int x, int y) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            Element el = elements.get(i);
            if (el.kind == kind && contains(el, x, y)) return el;
        }
        return null;
    }

    /** Hit test order: buttons, then fields, then images. */
    private Element hitAny(int x, int y) {
        Element el = topmost(Kind.BUTTON, x, y);
        if (el != null) return el;
        el = topmost(Kind.FIELD, x, y);
        if (el != null) return el;
        return topmost(Kind.IMAGE, x, y);
    }

    // ------------------------------------------------------------------ selection

    private void select(Element el) {
        selected = el;
        selectedIsNew = false;
        backup = new int[]{el.x, el.y, el.w, el.h};
        handle = Handle.MOVE;
        log("Selected " + describe(el));
    }

    private void deselect() {
        selected = null;
        selectedIsNew = false;
        backup = null;
        handle = Handle.MOVE;
    }

    private void setTool(Tool newTool) {
        tool = newTool;
        firstCorner = null;
        log("Tool: " + switch (newTool) {
            case SELECT -> "SELECT (click to select, right click moves the selection)";
            case TEXT -> "TEXT (click two corners)";
            case BUTTON -> "BUTTON (click two corners)";
            case PREVIEW -> "PREVIEW (buttons print when clicked)";
        });
    }

    private void setHandle(Handle newHandle) {
        if (selected == null) {
            log("Nothing selected.");
            return;
        }
        if (selected.kind == Kind.IMAGE && newHandle != Handle.MOVE) {
            log("Images can only be moved.");
            return;
        }
        handle = newHandle;
        log("Handle: " + switch (newHandle) {
            case MOVE -> "MOVE";
            case SIZE -> "SIZE (bottom-right corner, top-left stays fixed)";
            case ORIGIN -> "ORIGIN (top-left corner, bottom-right stays fixed)";
        });
    }

    private void confirm() {
        if (selected == null) return;
        log("Confirmed " + describe(selected));
        deselect();
    }

    private void cancel() {
        if (firstCorner != null) {
            firstCorner = null;
            log("First corner cancelled.");
            return;
        }
        if (selected == null) return;
        if (selectedIsNew) {
            log("Discarded new " + label(selected));
            elements.remove(selected);
        } else if (backup != null) {
            selected.x = backup[0];
            selected.y = backup[1];
            selected.w = backup[2];
            selected.h = backup[3];
            log("Reverted " + describe(selected));
        }
        deselect();
    }

    private void deleteSelected() {
        if (selected == null) {
            log("Nothing selected to delete.");
            return;
        }
        log("Deleted " + describe(selected));
        elements.remove(selected);
        deselect();
    }

    private void nudge(int dx, int dy) {
        if (tool == Tool.PREVIEW) return;
        if (selected == null) {
            log("Nothing selected.");
            return;
        }
        int step = fineAdjust ? 1 : MOVE_STEP;
        int sx = dx * step;
        int sy = dy * step;
        Handle active = selected.kind == Kind.IMAGE ? Handle.MOVE : handle;
        switch (active) {
            case MOVE -> {
                selected.x += sx;
                selected.y += sy;
            }
            case SIZE -> {
                selected.w = Math.max(1, selected.w + sx);
                selected.h = Math.max(1, selected.h + sy);
            }
            case ORIGIN -> {
                // move the top-left corner; the bottom-right corner stays where it is
                if (selected.w - sx >= 1) {
                    selected.x += sx;
                    selected.w -= sx;
                }
                if (selected.h - sy >= 1) {
                    selected.y += sy;
                    selected.h -= sy;
                }
            }
        }
    }

    // ------------------------------------------------------------------ rendering

    public synchronized void draw() {
        try {
            Graphics2D g = image.createGraphics();
            try {
                // clear previous frame
                g.setComposite(AlphaComposite.Clear);
                g.fillRect(0, 0, image.getWidth(), image.getHeight());
                g.setComposite(AlphaComposite.SrcOver);

                for (Element el : elements) {
                    if (el.kind == Kind.IMAGE) g.drawImage(el.img, el.x, el.y, null);
                }

                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                g.setFont(font);
                for (Element el : elements) {
                    if (el.kind == Kind.FIELD) drawField(g, el);
                }

                // everything below is an editing overlay; Preview shows the plain mock-up
                if (tool != Tool.PREVIEW) {
                    for (Element el : elements) {
                        if (el.kind != Kind.BUTTON) continue;
                        g.setColor(BUTTON_FILL);
                        g.fillRect(el.x, el.y, el.w, el.h);
                        g.setColor(BUTTON_LINE);
                        g.drawRect(el.x, el.y, el.w - 1, el.h - 1);
                    }

                    if (selected != null) {
                        // yellow = move, green = size (bottom-right), orange = origin (top-left)
                        Color color = switch (handle) {
                            case MOVE -> new Color(255, 255, 0, 220);
                            case SIZE -> Color.GREEN;
                            case ORIGIN -> Color.ORANGE;
                        };
                        g.setColor(color);
                        g.drawRect(selected.x, selected.y, selected.w - 1, selected.h - 1);
                        if (handle == Handle.SIZE) g.fillRect(selected.x + selected.w - 4, selected.y + selected.h - 4, 5, 5);
                        if (handle == Handle.ORIGIN) g.fillRect(selected.x - 1, selected.y - 1, 5, 5);
                    }

                    if (firstCorner != null) {
                        g.setColor(Color.RED);
                        g.fillRect(firstCorner[0] - 2, firstCorner[1] - 2, 5, 5);
                    }

                    if (hud) drawHud(g);
                }
            } finally {
                g.dispose();
            }
        } catch (Exception e) {
            log("Draw error: " + e);
        }
    }

    /** One line at the bottom of the canvas showing the current tool, handle, step and selection. */
    private void drawHud(Graphics2D g) {
        String text = "TOOL " + tool + " | HANDLE " + handle + " | STEP " + (fineAdjust ? 1 : MOVE_STEP) + "px | SEL "
                + (selected == null ? "-" : label(selected));
        Font hudFont = font.deriveFont(12f);
        g.setFont(hudFont);
        FontMetrics fm = g.getFontMetrics(hudFont);
        int height = fm.getHeight() + 4;
        int top = image.getHeight() - height;
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, top, image.getWidth(), height);
        g.setColor(Color.WHITE);
        g.drawString(text, 4, top + 2 + fm.getAscent());
        g.setFont(font);
    }

    /**
     * Draws a solid background, then fills the rectangle with digits.
     * The font is scaled to the largest size where at least one line and one digit fit the field,
     * and the digits are centered inside it.
     */
    private void drawField(Graphics2D g, Element f) {
        java.awt.Shape oldClip = g.getClip();
        Font oldFont = g.getFont();
        try {
            // solid background
            g.setColor(FIELD_BG);
            g.fillRect(f.x, f.y, f.w, f.h);

            // scale the font to the field
            Font fitted = font.deriveFont((float) MIN_FONT_SIZE);
            FontMetrics fm = g.getFontMetrics(fitted);
            int start = Math.min(MAX_FONT_SIZE, Math.max(MIN_FONT_SIZE, f.h));
            for (int size = start; size >= MIN_FONT_SIZE; size--) {
                Font candidate = font.deriveFont((float) size);
                FontMetrics m = g.getFontMetrics(candidate);
                if (m.getHeight() <= f.h && m.charWidth('0') <= f.w) {
                    fitted = candidate;
                    fm = m;
                    break;
                }
            }

            g.setFont(fitted);
            g.setClip(f.x, f.y, f.w, f.h);
            g.setColor(FIELD_TEXT);

            int lineHeight = Math.max(1, fm.getHeight());
            int digitWidth = Math.max(1, fm.charWidth('0'));
            int perLine = Math.max(1, f.w / digitWidth);
            int lines = Math.max(1, f.h / lineHeight);

            int top = f.y + (f.h - lines * lineHeight) / 2;
            int left = f.x + (f.w - perLine * digitWidth) / 2;

            for (int row = 0; row < lines; row++) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < perLine; i++) sb.append((row * perLine + i) % 10);
                g.drawString(sb.toString(), left, top + row * lineHeight + fm.getAscent());
            }
        } catch (Exception e) {
            log("Text field error: " + e);
        } finally {
            g.setClip(oldClip);
            g.setFont(oldFont);
        }
    }

    private void refresh() {
        try {
            draw();
            canvas.draw(image, 0, 0);
        } catch (Exception e) {
            log("Refresh error: " + e);
        }
    }

    public void open(Player player) {
        try {
            refresh();
            player.getInventory().setHeldItemSlot(8);
            engine.openView(player, new ClickListener(this), new ViewOptions(0, true, true));
        } catch (Exception e) {
            log("Open error: " + e);
        }
    }

    // ------------------------------------------------------------------ input

    public synchronized void handleClick(int x, int y, String clickName) {
        try {
            boolean right = clickName != null && clickName.toUpperCase().contains("RIGHT");
            switch (tool) {
                case PREVIEW -> {
                    Element button = topmost(Kind.BUTTON, x, y);
                    if (button != null) {
                        log("Button #" + number(button) + " clicked at " + x + ", " + y + " (" + clickName + ")");
                    }
                }
                case TEXT, BUTTON -> createClick(x, y, right);
                case SELECT -> selectClick(x, y, right);
            }
        } catch (Exception e) {
            log("Click error: " + e);
        }
        refresh();
    }

    private void createClick(int x, int y, boolean right) {
        if (right) {
            if (firstCorner != null) {
                firstCorner = null;
                log("First corner cancelled.");
            }
            return;
        }

        if (firstCorner == null) {
            firstCorner = new int[]{x, y};
            log("First corner: " + x + ", " + y + " (click again for the second corner)");
            return;
        }

        int x1 = Math.min(firstCorner[0], x);
        int y1 = Math.min(firstCorner[1], y);
        int x2 = Math.max(firstCorner[0], x);
        int y2 = Math.max(firstCorner[1], y);
        firstCorner = null;
        if (x2 - x1 < 1 || y2 - y1 < 1) {
            log("No area, ignored.");
            return;
        }

        Element created = addRect(tool == Tool.BUTTON ? Kind.BUTTON : Kind.FIELD, x1, y1, x2 - x1, y2 - y1);
        selected = created;
        selectedIsNew = true;
        backup = null;
        handle = Handle.SIZE;
        log("Created " + describe(created) + ". Arrows resize (HOTBAR_5/6/7 change handle), SPACE confirms, F discards.");
    }

    private void selectClick(int x, int y, boolean right) {
        if (right) {
            if (selected == null) {
                log("Nothing selected to move.");
                return;
            }
            selected.x = x - selected.w / 2;
            selected.y = y - selected.h / 2;
            return;
        }

        Element hit = hitAny(x, y);
        if (hit == null) {
            if (selected != null) {
                log("Deselected " + label(selected));
                deselect();
            }
        } else if (hit != selected) {
            select(hit);
        }
    }

    public synchronized void handleKey(Key keyName, boolean down) {
        try {
            if (!down || keyName == null) return;
            switch (keyName) {
                case UP -> nudge(0, -1);
                case DOWN -> nudge(0, 1);
                case RIGHT -> nudge(1, 0);
                case LEFT -> nudge(-1, 0);
                case JUMP -> confirm();
                case SWAP_HANDS -> cancel();
                case DROP -> deleteSelected();
                case SPRINT -> {
                    fineAdjust = !fineAdjust;
                    log("Step: " + (fineAdjust ? "FINE (1px)" : "COARSE (" + MOVE_STEP + "px)"));
                }
                case HOTBAR_1 -> setTool(Tool.SELECT);
                case HOTBAR_2 -> setTool(Tool.TEXT);
                case HOTBAR_3 -> setTool(Tool.BUTTON);
                case HOTBAR_4 -> setTool(Tool.PREVIEW);
                case HOTBAR_5 -> setHandle(Handle.MOVE);
                case HOTBAR_6 -> setHandle(Handle.SIZE);
                case HOTBAR_7 -> setHandle(Handle.ORIGIN);
                default -> { return; }
            }
        } catch (Exception e) {
            log("Key error: " + e);
        }
        refresh();
    }

    public synchronized void handleChat(String text) {
        try {
            if (text == null) return;
            String[] parts = text.trim().split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            String arg = parts.length > 1 ? parts[1].trim() : "";

            switch (cmd) {
                case "load" -> {
                    if (arg.isEmpty()) {
                        log("Usage: load <image>");
                        break;
                    }
                    BufferedImage img = loadImage(arg);
                    if (img == null) break;
                    String name = stripPng(arg);
//                    Element old = findImage(name);
//                    if (old != null) elements.remove(old);
                    Element loaded = addImage(name, img, (image.getWidth() - img.getWidth()) / 2, (image.getHeight() - img.getHeight()) / 2);
                    select(loaded);
                    log("Loaded '" + name + "'");
                }
                case "load-scaled" -> {
                    if (arg.isEmpty()) {
                        log("Usage: load-scaled <image>");
                        break;
                    }
                    BufferedImage img = loadImage(arg);
                    if (img == null) break;
                    String name = stripPng(arg);
                    Element loaded = addImage(name, scaleBy(img), (image.getWidth() - img.getWidth() * 2) / 2, (image.getHeight() - img.getHeight() * 2) / 2);
                    select(loaded);
                    log("Loaded scaled '" + name + "'");
                }
                case "select" -> {
                    String[] t = arg.split("\\s+");
                    Element target = null;
                    if (t.length == 2 && (t[0].equalsIgnoreCase("field") || t[0].equalsIgnoreCase("button"))) {
                        Kind kind = t[0].equalsIgnoreCase("field") ? Kind.FIELD : Kind.BUTTON;
                        try {
                            target = nth(kind, Integer.parseInt(t[1]));
                        } catch (NumberFormatException ex) {
                            target = null;
                        }
                    } else if (!arg.isEmpty()) {
                        target = findImage(stripPng(arg));
                    }
                    if (target == null) {
                        log("Nothing matches '" + arg + "'. Use: select <image> | select field <n> | select button <n>");
                    } else {
                        select(target);
                    }
                }
                case "center" -> {
                    if (selected == null) {
                        log("Nothing selected.");
                        break;
                    }
                    selected.x = (image.getWidth() - selected.w) / 2;
                    selected.y = (image.getHeight() - selected.h) / 2;
                    log("Centered " + describe(selected));
                }
                case "delete" -> deleteSelected();
                case "hud" -> {
                    hud = !hud;
                    log("HUD " + (hud ? "ON" : "OFF"));
                }
                case "save" -> save();
                default -> log("Unknown command. Use: load <image>, select <image|field n|button n>, center, delete, hud, save");
            }
        } catch (Exception e) {
            log("Chat error: " + e);
        }
        refresh();
    }

    // ------------------------------------------------------------------ output

    private void save() {
        System.out.println("[MapMenu] ===== SAVE =====");
        System.out.println("Images (x, y = top-left):");
        for (Element el : elements) {
            if (el.kind == Kind.IMAGE) {
                System.out.println("  " + el.name + ": x=" + el.x + ", y=" + el.y + " (" + el.w + "x" + el.h + ")");
            }
        }
        System.out.println("Text fields (x, y = top-left):");
        int i = 1;
        for (Element el : elements) {
            if (el.kind == Kind.FIELD) {
                System.out.println("  #" + i++ + ": x=" + el.x + ", y=" + el.y + ", width=" + el.w + ", height=" + el.h);
            }
        }
        System.out.println("Buttons (x, y = top-left):");
        int b = 1;
        for (Element el : elements) {
            if (el.kind == Kind.BUTTON) {
                System.out.println("  #" + b++ + ": x=" + el.x + ", y=" + el.y + ", width=" + el.w + ", height=" + el.h);
            }
        }
        System.out.println("[MapMenu] ================");
    }
}
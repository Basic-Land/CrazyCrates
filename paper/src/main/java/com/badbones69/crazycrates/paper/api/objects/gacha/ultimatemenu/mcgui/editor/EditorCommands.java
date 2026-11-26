package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.editor;

import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.Element;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MenuUtils;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.logging.Logger;

import static com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MenuUtils.log;

/**
 * The editor's chat commands:
 * load &lt;image&gt; | load-scaled &lt;image&gt; | select &lt;image&gt; | select field &lt;n&gt; | select button &lt;n&gt; |
 * text &lt;n&gt; &lt;text&gt; | center | delete | hud | save
 */
final class EditorCommands {
    private final MenuEditor editor;
    private final MapMenu menu;

    EditorCommands(MenuEditor editor) {
        this.editor = editor;
        this.menu = editor.menu;
    }

    void handle(String text) {
        if (text == null) return;
        String[] parts = text.trim().split("\\s+");
        String cmd = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1].trim() : "";
        String[] multiparts = Arrays.copyOfRange(parts, 1, parts.length);

        switch (cmd) {
            case "load" -> load(arg, 1);
            case "load-scaled" -> load(arg, 2);
            case "load-scaled-by" -> load(arg, parts.length > 2 ? Integer.parseInt(parts[2]) : -1);
            case "select" -> select(multiparts);
            case "center" -> editor.centerSelected();
            case "text", "set-text", "settext" -> setText(multiparts);
            case "delete" -> editor.deleteSelected();
            case "hud" -> editor.toggleHud();
            case "save" -> save();
            default ->
                    log("Unknown command. Use: load <image>, select <image|field n|button n>, text <n> <text>, center, delete, hud, save");
        }
    }

    private void load(String arg, int scale) {
        if (arg.isEmpty() || (scale == -1)) {
            log("Usage: " + (scale == 1 ? "load" : scale == 2 ? "load-scaled" : "load-scaled-by") + " <image>");
            return;
        }
        BufferedImage img = MenuUtils.loadImage(arg);
        if (img == null) return;
        if (scale == 2) img = MenuUtils.scaleBy2(img);
        if (scale >= 3) img = MenuUtils.scaleBy(img, 3);

        String name = MenuUtils.stripPng(arg);
        Element loaded = menu.addImage(name, img,
                (menu.canvasWidth() - img.getWidth()) / 2, (menu.canvasHeight() - img.getHeight()) / 2);
        editor.select(loaded);
        log((scale == 2 ? "Loaded scaled '" : scale == 3 ? "Loaded scaled by 3 '" : "Loaded '") + name + "'");
    }

    private void select(String[] args) {
        Element target = null;
        if (args.length == 2 && (args[0].equalsIgnoreCase("field") || args[0].equalsIgnoreCase("button"))) {
            Element.Kind kind = args[0].equalsIgnoreCase("field") ? Element.Kind.FIELD : Element.Kind.BUTTON;
            try {
                target = menu.element(kind, Integer.parseInt(args[1]));
            } catch (NumberFormatException _) {
            }
        } else if (!args[0].isEmpty()) {
            target = editor.findImage(MenuUtils.stripPng(args[0]));
        }
        if (target == null) {
            log("Nothing matches '" + args[0] + "'. Use: select <image> | select field <n> | select button <n>");
        } else {
            editor.select(target);
        }
    }

    private void setText(String[] args) {
        if (args.length != 2) {
            log("Usage: text <field-number> <text>");
            return;
        }
        try {
            menu.setFieldValue(Integer.parseInt(args[0]), args[1].trim());
        } catch (NumberFormatException ex) {
            log("Usage: text <field-number> <text>");
        }
    }

    /**
     * Prints the layout to the console so it can be copied into a MCGUIContent.
     */
    void save() {
        Logger out = menu.logger();
        out.info("[MapMenu] ===== SAVE =====");
        out.info("Images (x, y = top-left):");
        for (Element el : menu.elements()) {
            if (el.kind() == Element.Kind.IMAGE) {
                out.info("  " + el.name() + ": x=" + el.x() + ", y=" + el.y() + " (" + el.width() + "x" + el.height() + ")");
            }
        }
        out.info("Text fields (x, y = top-left):");
        for (Element el : menu.elements()) {
            if (el.kind() == Element.Kind.FIELD) {
                out.info("  #" + menu.number(el) + ": x=" + el.x() + ", y=" + el.y() + ", width=" + el.width()
                        + ", height=" + el.height() + ", value=" + el.text());
            }
        }
        out.info("Buttons (x, y = top-left):");
        for (Element el : menu.elements()) {
            if (el.kind() == Element.Kind.BUTTON) {
                out.info("  #" + menu.number(el) + ": x=" + el.x() + ", y=" + el.y() + ", width=" + el.width()
                        + ", height=" + el.height());
            }
        }
        out.info("[MapMenu] ================");
    }
}

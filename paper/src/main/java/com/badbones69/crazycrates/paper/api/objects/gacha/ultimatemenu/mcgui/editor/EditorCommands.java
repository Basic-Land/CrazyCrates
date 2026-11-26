package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.editor;

import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.Element;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MenuUtils;

import java.awt.image.BufferedImage;
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
        String[] parts = text.trim().split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1].trim() : "";

        switch (cmd) {
            case "load" -> load(arg, false);
            case "load-scaled" -> load(arg, true);
            case "select" -> select(arg);
            case "center" -> editor.centerSelected();
            case "text", "set-text", "settext" -> setText(arg);
            case "delete" -> editor.deleteSelected();
            case "hud" -> editor.toggleHud();
            case "save" -> save();
            default ->
                    log("Unknown command. Use: load <image>, select <image|field n|button n>, text <n> <text>, center, delete, hud, save");
        }
    }

    private void load(String arg, boolean scaled) {
        if (arg.isEmpty()) {
            log("Usage: " + (scaled ? "load-scaled" : "load") + " <image>");
            return;
        }
        BufferedImage img = MenuUtils.loadImage(arg);
        if (img == null) return;
        if (scaled) img = MenuUtils.scaleBy2(img);

        String name = MenuUtils.stripPng(arg);
        Element loaded = menu.addImage(name, img,
                (menu.canvasWidth() - img.getWidth()) / 2, (menu.canvasHeight() - img.getHeight()) / 2);
        editor.select(loaded);
        log((scaled ? "Loaded scaled '" : "Loaded '") + name + "'");
    }

    private void select(String arg) {
        String[] t = arg.split("\\s+");
        Element target = null;
        if (t.length == 2 && (t[0].equalsIgnoreCase("field") || t[0].equalsIgnoreCase("button"))) {
            Element.Kind kind = t[0].equalsIgnoreCase("field") ? Element.Kind.FIELD : Element.Kind.BUTTON;
            try {
                target = menu.element(kind, Integer.parseInt(t[1]));
            } catch (NumberFormatException _) {
            }
        } else if (!arg.isEmpty()) {
            target = editor.findImage(MenuUtils.stripPng(arg));
        }
        if (target == null) {
            log("Nothing matches '" + arg + "'. Use: select <image> | select field <n> | select button <n>");
        } else {
            editor.select(target);
        }
    }

    private void setText(String arg) {
        String[] value = arg.split("\\s+", 2);
        if (value.length != 2) {
            log("Usage: text <field-number> <text>");
            return;
        }
        try {
            menu.setFieldValue(Integer.parseInt(value[0]), value[1].trim());
        } catch (NumberFormatException ex) {
            log("Usage: text <field-number> <text>");
        }
    }

    /**
     * Prints the layout to the console so it can be copied into a MCGUIContent.
     */
    private void save() {
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

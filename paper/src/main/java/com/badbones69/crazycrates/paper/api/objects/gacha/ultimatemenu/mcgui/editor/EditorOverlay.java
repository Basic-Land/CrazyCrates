package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.editor;

import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.Element;

import java.awt.*;

/**
 * Paints the editing overlay (button boxes, selection, corner marker, HUD) on top of the menu.
 */
final class EditorOverlay {
    private static final Color BUTTON_FILL = new Color(0, 120, 255, 90);
    private static final Color BUTTON_LINE = new Color(0, 120, 255, 220);

    private final MenuEditor editor;

    EditorOverlay(MenuEditor editor) {
        this.editor = editor;
    }

    void paint(Graphics2D g) {
        if (editor.tool == MenuEditor.Tool.PREVIEW) return; // preview shows the plain mock-up
        drawButtons(g);
        drawSelection(g);
        drawFirstCorner(g);
        if (editor.hud) drawHud(g);
    }

    private void drawButtons(Graphics2D g) {
        for (Element el : editor.menu.elements()) {
            if (el.kind() != Element.Kind.BUTTON) continue;
            g.setColor(BUTTON_FILL);
            g.fillRect(el.x(), el.y(), el.width(), el.height());
            g.setColor(BUTTON_LINE);
            g.drawRect(el.x(), el.y(), el.width() - 1, el.height() - 1);
        }
    }

    private void drawSelection(Graphics2D g) {
        Element sel = editor.selected;
        if (sel == null) return;
        // yellow = move, green = size (bottom-right), orange = origin (top-left)
        g.setColor(switch (editor.handle) {
            case MOVE -> new Color(255, 255, 0, 220);
            case SIZE -> Color.GREEN;
            case ORIGIN -> Color.ORANGE;
        });
        g.drawRect(sel.x(), sel.y(), sel.width() - 1, sel.height() - 1);
        if (editor.handle == MenuEditor.Handle.SIZE)
            g.fillRect(sel.x() + sel.width() - 4, sel.y() + sel.height() - 4, 5, 5);
        if (editor.handle == MenuEditor.Handle.ORIGIN) g.fillRect(sel.x() - 1, sel.y() - 1, 5, 5);
    }

    private void drawFirstCorner(Graphics2D g) {
        if (editor.firstCorner == null) return;
        g.setColor(Color.RED);
        g.fillRect(editor.firstCorner[0] - 2, editor.firstCorner[1] - 2, 5, 5);
    }

    /**
     * One line at the bottom of the canvas showing the current tool, handle, step and selection.
     */
    private void drawHud(Graphics2D g) {
        String text = "TOOL " + editor.tool + " | HANDLE " + editor.handle + " | STEP " + editor.step() + "px | SEL "
                + (editor.selected == null ? "-" : editor.label(editor.selected));
        Font previous = g.getFont();
        Font hudFont = editor.menu.getFont().deriveFont(12f);
        g.setFont(hudFont);
        FontMetrics fm = g.getFontMetrics(hudFont);
        int height = fm.getHeight() + 4;
        int top = editor.menu.canvasHeight() - height;
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, top, editor.menu.canvasWidth(), height);
        g.setColor(Color.WHITE);
        g.drawString(text, 4, top + 2 + fm.getAscent());
        g.setFont(previous);
    }
}

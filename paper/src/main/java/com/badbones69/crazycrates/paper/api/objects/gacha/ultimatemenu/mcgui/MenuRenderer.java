package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MenuUtils.log;

/**
 * Paints the final look of a menu: images, then text fields. Buttons are invisible.
 * Knows nothing about editing; an optional {@link MenuController} can paint an overlay on top.
 */
final class MenuRenderer {
    private static final int MIN_FONT_SIZE = 6;
    private static final int MAX_FONT_SIZE = 64;

    private static final Color DEFAULT_HOVER_COLOR = new Color(255, 255, 255, 230);
    private static final int HOVER_THICKNESS = 2;

    private final BufferedImage image;
    private final Font font;
    private volatile Color hoverColor = DEFAULT_HOVER_COLOR;

    MenuRenderer(BufferedImage image, Font font) {
        this.image = image;
        this.font = font;
    }

    void setHoverColor(Color color) {
        this.hoverColor = color == null ? DEFAULT_HOVER_COLOR : color;
    }

    /**
     * @param hovered the button under the cursor (gets an outline), or null
     * @param overlay optional controller painting on top (e.g. the editor)
     */
    void render(List<Element> elements, Element hovered, MenuController overlay) {
        Graphics2D g = image.createGraphics();
        try {
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setComposite(AlphaComposite.SrcOver);

            for (Element el : elements) {
                if (el.kind() == Element.Kind.IMAGE) g.drawImage(el.image(), el.x(), el.y(), null);
            }

            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setFont(font);
            for (Element el : elements) {
                if (el.kind() == Element.Kind.FIELD) drawField(g, el);
            }

            if (hovered != null) drawHoverOutline(g, hovered);

            if (overlay != null) overlay.paintOverlay(g);
        } finally {
            g.dispose();
        }
    }

    /**
     * A thin outline drawn just inside the button's bounds.
     */
    private void drawHoverOutline(Graphics2D g, Element button) {
        g.setColor(hoverColor);
        for (int i = 0; i < HOVER_THICKNESS; i++) {
            g.drawRect(button.x() + i, button.y() + i, button.width() - 1 - 2 * i, button.height() - 1 - 2 * i);
        }
    }

    /**
     * Draws a solid background and renders the field's text, scaled and wrapped to fit.
     */
    private void drawField(Graphics2D g, Element f) {
        Shape oldClip = g.getClip();
        Font oldFont = g.getFont();
        try {
            g.setColor(f.background());
            g.fillRect(f.x(), f.y(), f.width(), f.height());

            String value = f.text();
            if (value.isEmpty()) return;

            Font fitted = font.deriveFont((float) MIN_FONT_SIZE);
            FontMetrics fm = g.getFontMetrics(fitted);
            List<String> lines = new ArrayList<>();
            int start = Math.clamp(f.height(), MIN_FONT_SIZE, MAX_FONT_SIZE);
            for (int size = start; size >= MIN_FONT_SIZE; size--) {
                Font candidate = font.deriveFont((float) size);
                FontMetrics m = g.getFontMetrics(candidate);
                List<String> wrapped = wrapText(value, f.width(), m);
                if (!wrapped.isEmpty() && wrapped.size() * m.getHeight() <= f.height()) {
                    fitted = candidate;
                    fm = m;
                    lines = wrapped;
                    break;
                }
            }

            g.setFont(fitted);
            g.setClip(f.x(), f.y(), f.width(), f.height());
            g.setColor(f.textColor());

            int lineHeight = Math.max(1, fm.getHeight());
            int top = f.y() + (f.height() - lines.size() * lineHeight) / 2;
            for (int row = 0; row < lines.size(); row++) {
                String line = lines.get(row);
                int left = f.x() + (f.width() - fm.stringWidth(line)) / 2;
                g.drawString(line, left, top + row * lineHeight + fm.getAscent());
            }
        } catch (Exception e) {
            log("Text field error: " + e);
        } finally {
            g.setClip(oldClip);
            g.setFont(oldFont);
        }
    }

    private List<String> wrapText(String text, int width, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
        String remaining = text;
        while (!remaining.isEmpty()) {
            int end = remaining.length();
            while (end > 0 && fm.stringWidth(remaining.substring(0, end)) > width) end--;
            if (end == 0) {
                end = 1;
            } else if (end < remaining.length() && end > 1) {
                int space = remaining.lastIndexOf(' ', end);
                if (space > 0) end = space;
            }
            lines.add(remaining.substring(0, end));
            remaining = remaining.substring(end).trim();
        }
        return lines;
    }
}
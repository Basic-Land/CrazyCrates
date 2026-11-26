package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.editor;

import ai.resourcepack.ui.framework.api.Key;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.Element;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MenuController;
import org.bukkit.entity.Player;

import java.awt.*;

import static com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MenuUtils.log;

/**
 * Optional layout editor for a {@link MapMenu}. Attach it while designing, leave it out for final use:
 * <pre>{@code
 * MenuEditor.attach(menu);   // editing on
 * menu.open(player);
 * }</pre>
 * While attached the editor owns all input (button actions do not run) and draws an overlay on top of the menu.
 * <p>
 * TOOLS (hotbar)
 * HOTBAR_1 Select   left click selects the element under the cursor (buttons > fields > images),
 * left click on nothing deselects, right click moves the selection to the cursor
 * HOTBAR_2 Text     click two corners -> new text field, already selected with the SIZE handle
 * HOTBAR_3 Button   click two corners -> new button, already selected with the SIZE handle
 * HOTBAR_4 Preview  overlays hidden, clicking a button only logs it
 * HANDLES (what the arrow keys change on the selection; images can only be moved)
 * HOTBAR_5 Move     HOTBAR_6 Size (bottom-right corner)     HOTBAR_7 Origin (top-left corner, bottom-right stays)
 * KEYS
 * UP/DOWN/LEFT/RIGHT (W A S D)  nudge the selection with the current handle
 * SPRINT                        toggle coarse (4 px) / fine (1 px) step
 * JUMP (space)                  confirm and deselect
 * SWAP_HANDS (F)                cancel: discard a new element, or revert the selection
 * DROP (Q)                      delete the selection
 * CHAT: see {@link EditorCommands}
 */
public final class MenuEditor implements MenuController {
    private static final int COARSE_STEP = 4;
    final MapMenu menu;
    private final EditorOverlay overlay = new EditorOverlay(this);
    private final EditorCommands commands = new EditorCommands(this);
    // editor state (read by the overlay)
    Tool tool = Tool.SELECT;
    Handle handle = Handle.MOVE;
    Element selected;
    int[] firstCorner;   // first corner while a create tool waits for the second click
    boolean fine;        // false = coarse step, true = 1 px
    boolean hud = true;
    private boolean selectedIsNew;
    private int[] backup; // {x, y, w, h} when selected, for cancel
    private MenuEditor(MapMenu menu) {
        this.menu = menu;
    }

    public static MenuEditor attach(MapMenu menu) {
        MenuEditor editor = new MenuEditor(menu);
        menu.setController(editor);
        return editor;
    }

    public void detach() {
        if (menu.getController() == this) menu.setController(null);
    }

    @Override
    public void paintOverlay(Graphics2D g) {
        overlay.paint(g);
    }

    @Override
    public void chat(Player player, String text) {
        commands.handle(text);
    }

    // ---------------------------------------------------------------- MenuController

    @Override
    public void click(Player player, int x, int y, String clickName) {
        boolean right = clickName.toUpperCase().contains("RIGHT");
        switch (tool) {
            case PREVIEW -> {
                Element button = menu.hit(Element.Kind.BUTTON, x, y);
                if (button != null) {
                    log("Button #" + menu.number(button) + " clicked at " + x + ", " + y + " (" + clickName + ")");
                }
            }
            case TEXT, BUTTON -> createClick(x, y, right);
            case SELECT -> selectClick(x, y, right);
        }
    }

    @Override
    public boolean key(Player player, Key key, boolean down) {
        if (!down || key == null) return false;
        switch (key) {
            case UP -> nudge(0, -1);
            case DOWN -> nudge(0, 1);
            case RIGHT -> nudge(1, 0);
            case LEFT -> nudge(-1, 0);
            case JUMP -> confirm();
            case SWAP_HANDS -> cancel();
            case DROP -> deleteSelected();
            case SPRINT -> toggleStep();
            case HOTBAR_1 -> setTool(Tool.SELECT);
            case HOTBAR_2 -> setTool(Tool.TEXT);
            case HOTBAR_3 -> setTool(Tool.BUTTON);
            case HOTBAR_4 -> setTool(Tool.PREVIEW);
            case HOTBAR_5 -> setHandle(Handle.MOVE);
            case HOTBAR_6 -> setHandle(Handle.SIZE);
            case HOTBAR_7 -> setHandle(Handle.ORIGIN);
            default -> {
                return false;
            }
        }
        return true;
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

        Element created = tool == Tool.BUTTON
                ? menu.addButton(null, x1, y1, x2 - x1, y2 - y1)
                : menu.addField(null, null, x1, y1, x2 - x1, y2 - y1);
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
            selected.setBounds(x - selected.width() / 2, y - selected.height() / 2, selected.width(), selected.height());
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

    // ---------------------------------------------------------------- clicks

    void select(Element el) {
        selected = el;
        selectedIsNew = false;
        backup = new int[]{el.x(), el.y(), el.width(), el.height()};
        handle = Handle.MOVE;
        log("Selected " + describe(el));
    }

    private void deselect() {
        selected = null;
        selectedIsNew = false;
        backup = null;
        handle = Handle.MOVE;
    }

    // ---------------------------------------------------------------- selection

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
            menu.remove(selected);
        } else if (backup != null) {
            selected.setBounds(backup[0], backup[1], backup[2], backup[3]);
            log("Reverted " + describe(selected));
        }
        deselect();
    }

    void deleteSelected() {
        if (selected == null) {
            log("Nothing selected to delete.");
            return;
        }
        log("Deleted " + describe(selected));
        menu.remove(selected);
        deselect();
    }

    void centerSelected() {
        if (selected == null) {
            log("Nothing selected.");
            return;
        }
        selected.setBounds((menu.canvasWidth() - selected.width()) / 2, (menu.canvasHeight() - selected.height()) / 2,
                selected.width(), selected.height());
        log("Centered " + describe(selected));
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
        if (selected.kind() == Element.Kind.IMAGE && newHandle != Handle.MOVE) {
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

    // ---------------------------------------------------------------- tool / handle / toggles

    private void toggleStep() {
        fine = !fine;
        log("Step: " + (fine ? "FINE (1px)" : "COARSE (" + COARSE_STEP + "px)"));
    }

    void toggleHud() {
        hud = !hud;
        log("HUD " + (hud ? "ON" : "OFF"));
    }

    int step() {
        return fine ? 1 : COARSE_STEP;
    }

    private void nudge(int dx, int dy) {
        if (tool == Tool.PREVIEW) return;
        if (selected == null) {
            log("Nothing selected.");
            return;
        }
        int sx = dx * step();
        int sy = dy * step();
        int x = selected.x(), y = selected.y(), w = selected.width(), h = selected.height();
        Handle active = selected.kind() == Element.Kind.IMAGE ? Handle.MOVE : handle;
        switch (active) {
            case MOVE -> selected.setBounds(x + sx, y + sy, w, h);
            case SIZE -> selected.setBounds(x, y, Math.max(1, w + sx), Math.max(1, h + sy));
            case ORIGIN -> {
                // move the top-left corner; the bottom-right corner stays where it is
                boolean okX = w - sx >= 1;
                boolean okY = h - sy >= 1;
                selected.setBounds(okX ? x + sx : x, okY ? y + sy : y, okX ? w - sx : w, okY ? h - sy : h);
            }
        }
    }

    /**
     * Hit test order: buttons, then fields, then images.
     */
    Element hitAny(int x, int y) {
        Element el = menu.hit(Element.Kind.BUTTON, x, y);
        if (el == null) el = menu.hit(Element.Kind.FIELD, x, y);
        if (el == null) el = menu.hit(Element.Kind.IMAGE, x, y);
        return el;
    }

    Element findImage(String name) {
        for (Element el : menu.elements()) {
            if (el.kind() == Element.Kind.IMAGE && el.name().equalsIgnoreCase(name)) return el;
        }
        return null;
    }

    // ---------------------------------------------------------------- lookup helpers

    String label(Element el) {
        return switch (el.kind()) {
            case IMAGE -> "image '" + el.name() + "'";
            case FIELD -> "field #" + menu.number(el);
            case BUTTON -> "button #" + menu.number(el);
        };
    }

    String describe(Element el) {
        return label(el) + " at " + el.x() + ", " + el.y() + " size " + el.width() + "x" + el.height();
    }

    enum Tool {SELECT, TEXT, BUTTON, PREVIEW}

    enum Handle {MOVE, SIZE, ORIGIN}
}

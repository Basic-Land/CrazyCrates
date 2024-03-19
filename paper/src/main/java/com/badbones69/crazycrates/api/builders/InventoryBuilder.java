package com.badbones69.crazycrates.api.builders;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazycrates.CrazyCratesPaper;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Tier;
import com.badbones69.crazycrates.platform.utils.MiscUtils;
import com.ryderbelserion.cluster.utils.AdvUtils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import us.crazycrew.crazycrates.platform.config.ConfigManager;
import us.crazycrew.crazycrates.platform.config.impl.ConfigKeys;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import java.util.List;

@SuppressWarnings("ALL")
public abstract class InventoryBuilder {

    protected final @NotNull CrazyCratesPaper plugin = JavaPlugin.getPlugin(CrazyCratesPaper.class);

    private @NotNull Gui gui;
    private Player player;
    private String title;
    private Component newTitle;
    private Crate crate;
    private int rows;
    private int page;
    private List<Tier> tiers;

    public InventoryBuilder(Player player, int rows, String title) {
        this.title = title;
        this.player = player;
        this.rows = rows;

        Component inventoryTitle = MiscUtils.isPapiActive() ? AdvUtils.parse(PlaceholderAPI.setPlaceholders(getPlayer(), this.title)) : AdvUtils.parse(this.title);

        this.gui = Gui.gui().title(inventoryTitle).rows(this.rows).apply(consumer -> consumer.setDefaultTopClickAction(event -> event.setCancelled(true))).create();
    }

    public InventoryBuilder(Crate crate, Player player, int rows, String title) {
        this.title = title;
        this.player = player;
        this.rows = rows;

        this.crate = crate;

        Component inventoryTitle = MiscUtils.isPapiActive() ? AdvUtils.parse(PlaceholderAPI.setPlaceholders(getPlayer(), this.title)) : AdvUtils.parse(this.title);

        this.gui = Gui.gui().title(inventoryTitle).rows(this.rows).apply(consumer -> consumer.setDefaultTopClickAction(event -> event.setCancelled(true))).create();
    }

    public InventoryBuilder(Crate crate, Player player, int rows, int page, String title) {
        this.title = title;
        this.player = player;
        this.rows = rows;
        this.page = page;

        this.crate = crate;

        Component inventoryTitle = MiscUtils.isPapiActive() ? AdvUtils.parse(PlaceholderAPI.setPlaceholders(getPlayer(), this.title)) : AdvUtils.parse(this.title);

        this.gui = Gui.gui().title(inventoryTitle).rows(this.rows).apply(consumer -> consumer.setDefaultTopClickAction(event -> event.setCancelled(true))).create();
    }

    public InventoryBuilder(List<Tier> tiers, Crate crate, Player player, int rows, String title) {
        this.title = title;
        this.player = player;
        this.rows = rows;

        this.crate = crate;

        this.tiers = tiers;

        Component inventoryTitle = MiscUtils.isPapiActive() ? AdvUtils.parse(PlaceholderAPI.setPlaceholders(getPlayer(), this.title)) : AdvUtils.parse(this.title);

        this.gui = Gui.gui().title(inventoryTitle).rows(this.rows).apply(consumer -> consumer.setDefaultTopClickAction(event -> event.setCancelled(true))).create();
    }

    public boolean overrideMenu() {
        SettingsManager config = ConfigManager.getConfig();

        if (config.getProperty(ConfigKeys.menu_button_override)) {
            List<String> commands = config.getProperty(ConfigKeys.menu_button_command_list);

            if (!commands.isEmpty()) {
                commands.forEach(value -> {
                    //String command = value.replaceAll("%player%", quoteReplacement(player.getName())).replaceAll("%crate%", quoteReplacement(crate.getName()));

                    //MiscUtils.sendCommand(command);
                });

                return true;
            }

            if (MiscUtils.isLogging()) this.plugin.getLogger().warning("The property " + ConfigKeys.menu_button_command_list.getPath() + " is empty so no commands were run.");

            return true;
        }

        return false;
    }

    public abstract InventoryBuilder build();

    public InventoryBuilder setPlayer(Player player) {
        this.player = player;

        return this;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getRows() {
        return this.rows;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPage() {
        return this.page;
    }

    public Crate getCrate() {
        return this.crate;
    }

    public void title(String title) {
        this.title = title;
    }

    public boolean contains(String message) {
        return this.title.contains(message);
    }

    public Player getPlayer() {
        return this.player;
    }

    public List<Tier> getTiers() {
        return this.tiers;
    }

    public InventoryView getView() {
        return getPlayer().getOpenInventory();
    }

    public @NotNull GuiItem getItem(ItemStack itemStack, @NotNull final GuiAction<InventoryClickEvent> action) {
        return ItemBuilder.from(itemStack).asGuiItem(event -> {

        });
    }

    public @NotNull GuiItem getItem(ItemStack itemStack) {
        return ItemBuilder.from(itemStack).asGuiItem();
    }

    public @NotNull Gui getGui() {
        return this.gui;
    }
}
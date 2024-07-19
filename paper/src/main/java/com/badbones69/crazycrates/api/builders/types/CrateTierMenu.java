package com.badbones69.crazycrates.api.builders.types;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.types.items.BonusPityMenu;
import com.badbones69.crazycrates.api.enums.PersistentKeys;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Tier;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuStuff;
import com.badbones69.crazycrates.config.ConfigManager;
import com.badbones69.crazycrates.config.impl.ConfigKeys;
import com.ryderbelserion.vital.paper.builders.items.ItemBuilder;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazycrates.api.enums.types.CrateType;

import java.util.Arrays;
import java.util.List;

public class CrateTierMenu extends InventoryBuilder {

    private @NotNull final SettingsManager config = ConfigManager.getConfig();
    private final boolean gacha = getCrate().getCrateType().equals(CrateType.gacha);

    public CrateTierMenu(@NotNull final Player player, @NotNull final String title, final int size, @NotNull final Crate crate, @NotNull final List<Tier> tiers) {
        super(player, title, size, crate, tiers);
    }

    @Override
    public InventoryBuilder build() {
        setDefaultItems();

        return this;
    }

    @Override
    public void run(InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof CrateTierMenu holder)) return;

        event.setCancelled(true);

        final Player player = holder.getPlayer();

        final ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) return;

        if (!item.hasItemMeta()) return;

        final Crate crate = this.inventoryManager.getCratePreview(player);

        if (crate == null) return;

        final ItemMeta itemMeta = item.getItemMeta();

        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        if (this.config.getProperty(ConfigKeys.enable_crate_menu) && container.has(PersistentKeys.main_menu_button.getNamespacedKey())) {
            if (this.inventoryManager.inCratePreview(player)) {
                if (holder.overrideMenu()) return;

                crate.playSound(player, player.getLocation(), "click-sound", "ui.button.click", Sound.Source.PLAYER);

                this.inventoryManager.removeViewer(player);
                this.inventoryManager.closeCratePreview(player);

                final CrateMainMenu crateMainMenu = new CrateMainMenu(player, this.config.getProperty(ConfigKeys.inventory_name), this.config.getProperty(ConfigKeys.inventory_size));

                player.openInventory(crateMainMenu.build().getInventory());
            }

            return;
        }

        if (container.has(PersistentKeys.crate_tier.getNamespacedKey())) {
            if (!gacha) crate.playSound(player, player.getLocation(), "click-sound", "ui.button.click", Sound.Source.PLAYER);
            else player.playSound(UltimateMenuStuff.CLICK);

            final String tierName = container.get(PersistentKeys.crate_tier.getNamespacedKey(), PersistentDataType.STRING);

            final Tier tier = crate.getTier(tierName);

            final Inventory cratePreviewMenu = crate.getPreview(player, this.inventoryManager.getPage(player), true, tier);

            player.openInventory(cratePreviewMenu);
        }

        if (!gacha) return;

        if (event.getSlot() == holder.getCrate().getAbsolutePreviewItemPosition(8)) {
            player.playSound(UltimateMenuStuff.CLICK);
            player.openInventory(new BonusPityMenu(crate, player, 36, "<green><b>Bonus pity prize", holder).build().getInventory());
        }

        if (event.getSlot() == holder.getCrate().getAbsolutePreviewItemPosition(0)) {
            player.playSound(UltimateMenuStuff.BACK);
            plugin.getCrateManager().getDatabaseManager().getUltimateMenuManager().open(player, crate);
        }
    }

    private void setDefaultItems() {
        final Inventory inventory = getInventory();
        final Player player = getPlayer();
        final Crate crate = getCrate();

        getTiers().forEach(tier -> inventory.setItem(tier.getSlot(), tier.getTierItem(player)));

        if (crate.isPreviewTierBorderToggle()) {
            final List<Integer> borderItems = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);

            final ItemBuilder glass = crate.getPreviewTierBorderItem().setPlayer(player);
            if (gacha) {
                glass.setCustomModelData(1000001);
            }
            ItemStack itemStack = glass.getStack();

            for (int item : borderItems) { // Top border slots
                inventory.setItem(item, itemStack);
            }

            borderItems.replaceAll(crate::getAbsolutePreviewItemPosition);

            for (int item : borderItems) { // Bottom border slots
                inventory.setItem(item, itemStack);
            }
        }

        if (this.inventoryManager.inCratePreview(getPlayer()) && this.config.getProperty(ConfigKeys.enable_crate_menu)) {
            if (gacha) {
                setItemsGacha();
                return;
            }
            getInventory().setItem(getCrate().getAbsolutePreviewItemPosition(4), this.inventoryManager.getMenuButton(getPlayer()));
        }
    }

    private void setItemsGacha() {
        ItemBuilder item = new ItemBuilder(Material.PLAYER_HEAD).setDisplayName("<green><b>Bonus pity prize").addDisplayLore("<gray>Click to preview/pick a prize");
        item.setCustomModelData(1000001);
        getInventory().setItem(getCrate().getAbsolutePreviewItemPosition(8), item.getStack());

        ItemBuilder paper = new ItemBuilder(Material.PAPER).setCustomModelData(11).setDisplayName("Info");
        getInventory().setItem(getCrate().getAbsolutePreviewItemPosition(4), paper.getStack());

        ItemBuilder mainMenu = new ItemBuilder(Material.CHEST).setDisplayName("<green><b>Main menu");
        mainMenu.setCustomModelData(1000001);
        getInventory().setItem(getCrate().getAbsolutePreviewItemPosition(0), mainMenu.getStack());
    }
}
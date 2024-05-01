package com.badbones69.crazycrates.api.builders.types;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.builders.types.items.BonusPityMenu;
import com.badbones69.crazycrates.api.enums.PersistentKeys;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Tier;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import org.bukkit.plugin.java.JavaPlugin;
import us.crazycrew.crazycrates.api.enums.types.CrateType;
import us.crazycrew.crazycrates.platform.config.ConfigManager;
import us.crazycrew.crazycrates.platform.config.impl.ConfigKeys;
import com.badbones69.crazycrates.tasks.InventoryManager;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;

public class CrateTierMenu extends InventoryBuilder {

    private final @NotNull InventoryManager inventoryManager = this.plugin.getInventoryManager();

    private final @NotNull SettingsManager config = ConfigManager.getConfig();

    public CrateTierMenu(List<Tier> tiers, Crate crate, Player player, int size, String title) {
        super(tiers, crate, player, size, title);
    }

    @Override
    public InventoryBuilder build() {
        setDefaultItems();

        return this;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof CrateTierMenu holder)) return;

        event.setCancelled(true);

        Player player = holder.getPlayer();

        ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) return;

        if (!item.hasItemMeta()) return;

        Crate crate = this.inventoryManager.getCratePreview(player);

        if (crate == null) return;

        ItemMeta itemMeta = item.getItemMeta();

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        if (container.has(PersistentKeys.main_menu_button.getNamespacedKey()) && this.config.getProperty(ConfigKeys.enable_crate_menu)) {
            if (this.inventoryManager.inCratePreview(player)) {
                if (holder.overrideMenu()) return;

                crate.playSound(player, player.getLocation(), "click-sound", "UI_BUTTON_CLICK", SoundCategory.PLAYERS);

                this.inventoryManager.removeViewer(player);
                this.inventoryManager.closeCratePreview(player);

                CrateMainMenu crateMainMenu = new CrateMainMenu(player, this.config.getProperty(ConfigKeys.inventory_size), this.config.getProperty(ConfigKeys.inventory_name));

                player.openInventory(crateMainMenu.build().getInventory());
            }

            return;
        }

        if (container.has(PersistentKeys.preview_tier_button.getNamespacedKey())) {
            crate.playSound(player, player.getLocation(), "click-sound", "UI_BUTTON_CLICK", SoundCategory.PLAYERS);

            String tierName = container.get(PersistentKeys.preview_tier_button.getNamespacedKey(), PersistentDataType.STRING);

            Tier tier = crate.getTier(tierName);

            Inventory cratePreviewMenu = crate.getPreview(player, this.inventoryManager.getPage(player), true, tier);

            player.openInventory(cratePreviewMenu);
        }

        if (!crate.getCrateType().equals(CrateType.gacha)) return;

        if (event.getSlot() == holder.getCrate().getAbsolutePreviewItemPosition(8)) {
            crate.playSound(player, player.getLocation(), "click-sound", "UI_BUTTON_CLICK", SoundCategory.PLAYERS);
            player.openInventory(new BonusPityMenu(crate, player, 36, "&a&lBonus pity prize", holder).build().getInventory());
        }

        if (event.getSlot() == holder.getCrate().getAbsolutePreviewItemPosition(0)) {
            crate.playSound(player, player.getLocation(), "click-sound", "UI_BUTTON_CLICK", SoundCategory.PLAYERS);
            plugin.getCrateManager().getDatabaseManager().getUltimateMenuManager().open(player, crate);
        }
    }

    private void setDefaultItems() {
        getTiers().forEach(tier -> getInventory().setItem(tier.getSlot(), tier.getTierItem(getPlayer())));

        boolean gacha = getCrate().getCrateType() == CrateType.gacha;

        if (getCrate().isPreviewTierBorderToggle()) {
            List<Integer> borderItems = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);

            ItemBuilder glass = getCrate().getPreviewTierBorderItem().setTarget(getPlayer());
            if (gacha) {
                glass.setCustomModelData(1000001).setHasCustomModelData(true);
            }
            ItemStack glassItem = glass.build();

            for (int item : borderItems) { // Top border slots
                getInventory().setItem(item, glassItem);
            }

            borderItems.replaceAll(getCrate()::getAbsolutePreviewItemPosition);

            for (int item : borderItems) { // Bottom border slots
                getInventory().setItem(item, glassItem);
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
        CrateSettings settings = getCrate().getCrateSettings();

        ItemBuilder item = new ItemBuilder().setMaterial(Material.PLAYER_HEAD).setName("&a&lBonus pity prize").addLore("&7Click to preview/pick a prize");
        item.setCustomModelData(settings.getModelDataPreviewName()).setHasCustomModelData(true);
        getInventory().setItem(getCrate().getAbsolutePreviewItemPosition(8), item.build());

        ItemBuilder paper = new ItemBuilder().setMaterial(Material.PAPER).setCustomModelData(11).setHasCustomModelData(true).setName("Info");
        getInventory().setItem(getCrate().getAbsolutePreviewItemPosition(4), paper.build());

        ItemBuilder mainMenu = new ItemBuilder().setMaterial(Material.CHEST).setName("&a&lMain menu");
        mainMenu.setCustomModelData(1000001).setHasCustomModelData(true);
        getInventory().setItem(getCrate().getAbsolutePreviewItemPosition(0), mainMenu.build());
    }
}
package com.badbones69.crazycrates.tasks.menus;

import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.builders.gui.StaticInventoryBuilder;
import com.badbones69.crazycrates.api.builders.types.items.BonusPityMenu;
import com.badbones69.crazycrates.api.enums.misc.Keys;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Tier;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuStuff;
import com.ryderbelserion.vital.paper.api.builders.gui.interfaces.Gui;
import com.ryderbelserion.vital.paper.api.builders.gui.interfaces.GuiFiller;
import com.ryderbelserion.vital.paper.api.builders.gui.interfaces.GuiItem;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.crazycrew.crazycrates.api.enums.types.CrateType;
import java.util.List;

public class CrateTierMenu extends StaticInventoryBuilder {

    public CrateTierMenu(final Player player, final Crate crate) {
        super(player, crate);
    }

    private final Player player = getPlayer();
    private final Crate crate = getCrate();
    private final Gui gui = getGui();
    private final boolean gacha = crate.getCrateType().isGacha();

    @Override
    public void open() {
        if (this.crate == null) return;

        final boolean isPreviewBorderEnabled = this.crate.isPreviewTierBorderToggle();

        if (isPreviewBorderEnabled) {
            final GuiItem guiItem = this.crate.getPreviewTierBorderItem().setPlayer(this.player).setCustomModelData(gacha ? 1000001 : -1).asGuiItem();

            final GuiFiller guiFiller = this.gui.getFiller();

            guiFiller.fillTop(guiItem);
            guiFiller.fillBottom(guiItem);
        }

        final List<Tier> tiers = this.crate.getTiers();

        tiers.forEach(tier -> {
            final ItemStack item = tier.getTierItem(this.player);
            final int slot = tier.getSlot();

            this.gui.setItem(slot, new GuiItem(item, action -> {
                final ItemStack itemStack = action.getCurrentItem();

                if (itemStack == null || itemStack.getType().isAir()) return;

                final PersistentDataContainerView tags = itemStack.getPersistentDataContainer();

                if (tags.has(Keys.crate_tier.getNamespacedKey())) {
                    if (!gacha) crate.playSound(player, player.getLocation(), "click-sound", "ui.button.click", Sound.Source.PLAYER);
                    else player.playSound(UltimateMenuStuff.CLICK);

                    this.crate.getPreview(this.player, tier).open();
                }
            }));
        });

        if (gacha) setItemsGacha();
        else addMenuButton(this.player, this.crate, this.gui, this.gui.getRows(), 5);

        this.gui.setOpenGuiAction(event -> this.inventoryManager.addPreviewViewer(event.getPlayer().getUniqueId()));

        this.gui.setCloseGuiAction(event -> this.inventoryManager.removePreviewViewer(event.getPlayer().getUniqueId()));

        this.gui.open(this.player);
    }

    private void setItemsGacha() {
        ItemBuilder item = new ItemBuilder(Material.PLAYER_HEAD).setDisplayName("<green><b>Bonus pity cena")
                .addDisplayLore("<gray>Klikni pro otevření")
                .addDisplayLore("<gray>bonusového výběru ceny")
                .addDisplayLore("<gray>po dostatku otevření");
        item.setCustomModelData(1000001);

        gui.setItem(crate.getMaxSlots() - 1, item.asGuiItem(action -> {
            player.playSound(UltimateMenuStuff.CLICK);
            player.openInventory(new BonusPityMenu(crate, player, 36, "<green><b>Bonus pity prize", this).build().getInventory());
        }));

        ItemBuilder paper = new ItemBuilder(Material.PAPER).setCustomModelData(11).setDisplayName("Info");
        paper.addDisplayLore("<gray>Zde najdeš informace o");
        paper.addDisplayLore("<gray>itemech a jejich šancích");
        gui.setItem(crate.getMaxSlots() - 5, paper.asGuiItem());

        ItemBuilder mainMenu = new ItemBuilder(Material.CHEST).setDisplayName("<green><b>Hlavní Menu");
        mainMenu.setCustomModelData(1000001);
        gui.setItem(crate.getMaxSlots() - 8, mainMenu.asGuiItem(action -> {
            player.playSound(UltimateMenuStuff.BACK);
            plugin.getCrateManager().getDatabaseManager().getUltimateMenuManager().open(player, crate);
        }));
    }
}
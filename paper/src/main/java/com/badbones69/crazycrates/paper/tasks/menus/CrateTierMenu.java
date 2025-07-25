package com.badbones69.crazycrates.paper.tasks.menus;

import com.badbones69.crazycrates.paper.api.builders.gui.StaticInventoryBuilder;
import com.badbones69.crazycrates.paper.api.builders.items.BonusPityMenu;
import com.badbones69.crazycrates.paper.api.enums.other.keys.ItemKeys;
import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.Tier;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.ItemRepo;
import com.ryderbelserion.fusion.paper.api.builders.gui.interfaces.Gui;
import com.ryderbelserion.fusion.paper.api.builders.gui.interfaces.GuiFiller;
import com.ryderbelserion.fusion.paper.api.builders.gui.interfaces.GuiItem;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazycrates.api.enums.types.CrateType;
import java.util.List;
import java.util.UUID;

public class CrateTierMenu extends StaticInventoryBuilder {

    public CrateTierMenu(@NotNull final Player player, @NotNull final Crate crate) {
        super(player, crate);
    }

    private final Player player = getPlayer();
    private final Crate crate = getCrate();
    private final Gui gui = getGui();

    @Override
    public void open() {
        if (this.crate == null) return;

        final CrateType crateType = this.crate.getCrateType();
        boolean gacha = crate.getCrateType().isGacha();

        if (crateType != CrateType.casino && crateType != CrateType.cosmic && !gacha) return;

        final boolean isPreviewBorderEnabled = this.crate.isPreviewTierBorderToggle();

        if (isPreviewBorderEnabled) {
            final GuiItem guiItem = this.crate.getPreviewTierBorderItem().setPlayer(this.player).setCustomModelData(gacha ? 1000001 : -1).asGuiItem();

            final GuiFiller guiFiller = this.gui.getFiller();

            guiFiller.fillTop(guiItem);
            guiFiller.fillBottom(guiItem);
        }

        final UUID uuid = this.player.getUniqueId();

        final List<Tier> tiers = this.crate.getTiers();

        tiers.forEach(tier -> {
            final ItemStack item = tier.getTierItem(this.player, this.crate);
            final int slot = tier.getSlot();

            this.gui.setItem(slot, new GuiItem(item, action -> {
                final ItemStack itemStack = action.getCurrentItem();

                if (itemStack == null || itemStack.getType().isAir()) return;

                final PersistentDataContainerView tags = itemStack.getPersistentDataContainer();

                if (tags.has(ItemKeys.crate_tier.getNamespacedKey())) {
                    if (!gacha) crate.playSound(player, player.getLocation(), "click-sound", "ui.button.click", Sound.Source.MASTER);
                    else player.playSound(ItemRepo.CLICK);

                    this.crate.getPreview(this.player, tier).open();
                }
            }));
        });

        if (gacha) setItemsGacha();
        else addMenuButton(this.player, this.crate, this.gui, this.gui.getRows(), 5);

        this.gui.setOpenGuiAction(event -> this.inventoryManager.addPreviewViewer(uuid));

        this.gui.setCloseGuiAction(event -> this.inventoryManager.removePreviewViewer(uuid));

        this.gui.open(this.player);
    }

    private void setItemsGacha() {
        if (player.hasPermission("cc.bonuspity")) {
            gui.setItem(gui.getSize() - 1, ItemRepo.PREVIEW_HEAD.asGuiItem(action -> {
                player.playSound(ItemRepo.CLICK);
                player.openInventory(new BonusPityMenu(crate, player, 36, "<green><b>Bonus pity prize", this).build().getInventory());
            }));
        }

        gui.setItem(gui.getSize() - 5, ItemRepo.PREVIEW_INFO.asGuiItem());

        gui.setItem(gui.getSize() - 9, ItemRepo.MAIN_MENU_BACK.asGuiItem(action -> {
            player.playSound(ItemRepo.BACK);
            plugin.getCrateManager().getDatabaseManager().getUltimateMenuManager().open(player, crate);
        }));
    }
}
package com.badbones69.crazycrates.paper.tasks.menus;

import com.badbones69.crazycrates.paper.api.builders.LegacyItemBuilder;
import com.badbones69.crazycrates.paper.api.builders.gui.DynamicInventoryBuilder;
import com.badbones69.crazycrates.paper.api.builders.items.CratePickPrizeMenu;
import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.Prize;
import com.badbones69.crazycrates.paper.api.objects.Tier;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.GachaType;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.ItemRepo;
import com.ryderbelserion.fusion.paper.builder.gui.interfaces.GuiFiller;
import com.ryderbelserion.fusion.paper.builder.gui.interfaces.GuiItem;
import com.ryderbelserion.fusion.paper.builder.gui.types.PaginatedGui;
import cz.basicland.blibs.spigot.utils.item.NBT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.util.UUID;

public class CratePreviewMenu extends DynamicInventoryBuilder {

    private final Tier tier;

    public CratePreviewMenu(final Player player, final Crate crate, final Tier tier) {
        super(player, crate, crate.getPreviewName(), crate.getPreviewChestLines());

        this.tier = tier;
    }

    private final Player player = getPlayer();
    private final PaginatedGui gui = getGui();

    //TODO: FIX THIS

    private final boolean gacha = getCrate().getCrateType().isGacha();

    @Override
    public void open() {
        final Crate crate = getCrate();

        if (crate == null) return;

        if (crate.isBorderToggle()) {
            final GuiFiller guiFiller = this.gui.getFiller();

            final GuiItem guiItem = new GuiItem(crate.getBorderItem().setCustomModelData(gacha ? 1000001 : -1).asItemStack());

            guiFiller.fillTop(guiItem);
            guiFiller.fillBottom(guiItem);
        }

        final UUID uuid = this.player.getUniqueId();

        crate.getPreviewItems(this.player, this.tier).forEach(itemStack -> this.gui.addItem(new GuiItem(itemStack, this::legendary)));

        this.gui.setOpenGuiAction(event -> this.inventoryManager.addPreviewViewer(uuid));

        this.gui.setCloseGuiAction(event -> this.inventoryManager.removePreviewViewer(uuid));

        this.gui.open(this.player, gui -> {
            final int rows = gui.getRows();

            setBackButton(rows, 4, true);
            setNextButton(rows, 6, true);

            if (gacha) {
                gui.setItem(gui.getSize() - 5, new LegacyItemBuilder(ItemType.COMPASS).setCustomModelData(1000001).asGuiItem(action -> crate.getTierPreview(player).open()));
            } else {
                addMenuButton(this.player, crate, this.gui, rows, 5);
            }
        });
    }

    private void legendary(InventoryClickEvent action) {
        ItemStack item = action.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        if (gacha && tier.getName().equals("legendary")) {
            GachaType gachaType = getCrate().getCrateSettings().getGachaType();

            NBT nbt = new NBT(item);
            String rewardName = nbt.getString("rewardName");
            if (rewardName == null || rewardName.isEmpty()) return;

            boolean standard = rewardName.split("_")[1].equals(RewardType.STANDARD.name());

            switch (gachaType) {
                case NORMAL -> {
                    return;
                }

                case FATE_POINT -> {
                    if (getCrate().getCrateSettings().getLegendaryStandard()
                            .stream()
                            .map(Prize::getSectionName)
                            .anyMatch(rewardName::equals) && standard) {
                        return;
                    }
                }

                case OVERRIDE -> {
                }
            }

            player.playSound(ItemRepo.CLICK);

            player.openInventory(new CratePickPrizeMenu(player, item, getCrate(), tier).build().getInventory());
        }
    }
}
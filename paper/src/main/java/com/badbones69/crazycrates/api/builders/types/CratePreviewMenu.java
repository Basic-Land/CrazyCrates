package com.badbones69.crazycrates.api.builders.types;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.builders.types.items.CratePickPrizeMenu;
import com.badbones69.crazycrates.api.enums.PersistentKeys;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.Tier;
import com.badbones69.crazycrates.api.objects.gacha.enums.GachaType;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuStuff;
import com.badbones69.crazycrates.tasks.InventoryManager;
import cz.basicland.blibs.spigot.utils.item.NBT;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazycrates.api.enums.types.CrateType;
import us.crazycrew.crazycrates.platform.config.ConfigManager;
import us.crazycrew.crazycrates.platform.config.impl.ConfigKeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CratePreviewMenu extends InventoryBuilder {

    private final @NotNull CrazyCrates plugin = JavaPlugin.getPlugin(CrazyCrates.class);

    private final @NotNull InventoryManager inventoryManager = this.plugin.getInventoryManager();

    private final @NotNull SettingsManager config = ConfigManager.getConfig();
    private final boolean gacha = getCrate().getCrateType() == CrateType.gacha;

    private final boolean isTier;
    private final Tier tier;

    public CratePreviewMenu(Crate crate, Player player, int size, int page, String title, boolean isTier, Tier tier) {
        super(crate, player, size, page, title);

        this.isTier = isTier;
        this.tier = tier;
    }

    @Override
    public InventoryBuilder build() {
        Inventory inventory = getInventory();

        setDefaultItems(inventory);

        for (ItemStack item : getPageItems(getPage())) {
            int nextSlot = inventory.firstEmpty();

            if (nextSlot >= 0) {
                inventory.setItem(nextSlot, item);
            } else {
                break;
            }
        }

        return this;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof CratePreviewMenu holder)) return;

        event.setCancelled(true);

        Player player = holder.getPlayer();

        ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) return;

        if (!item.hasItemMeta()) return;

        Crate crate = this.inventoryManager.getCratePreview(player);

        if (crate == null) return;

        ItemMeta itemMeta = item.getItemMeta();

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        if (container.has(PersistentKeys.main_menu_button.getNamespacedKey()) && this.config.getProperty(ConfigKeys.enable_crate_menu)) { // Clicked the menu button.
            if (this.inventoryManager.inCratePreview(player)) {
                if (holder.overrideMenu()) return;

                if (!gacha) crate.playSound(player, player.getLocation(), "click-sound","UI_BUTTON_CLICK", SoundCategory.PLAYERS);
                else player.playSound(UltimateMenuStuff.BACK);

                if (crate.isPreviewTierToggle()) {
                    player.openInventory(crate.getTierPreview(player));

                    return;
                }

                this.inventoryManager.removeViewer(player);
                this.inventoryManager.closeCratePreview(player);

                CrateMainMenu crateMainMenu = new CrateMainMenu(player, this.config.getProperty(ConfigKeys.inventory_size), this.config.getProperty(ConfigKeys.inventory_name));

                player.openInventory(crateMainMenu.build().getInventory());
            }

            return;
        }

        if (container.has(PersistentKeys.next_button.getNamespacedKey())) {  // Clicked the next button.
            if (this.inventoryManager.getPage(player) < crate.getMaxPage()) {
                crate.playSound(player, player.getLocation(), "click-sound","UI_BUTTON_CLICK", SoundCategory.PLAYERS);

                this.inventoryManager.nextPage(player);

                this.inventoryManager.openCratePreview(player, crate);
            }

            return;
        }

        if (container.has(PersistentKeys.back_button.getNamespacedKey())) {  // Clicked the back button.
            if (this.inventoryManager.getPage(player) > 1 && this.inventoryManager.getPage(player) <= crate.getMaxPage()) {
                crate.playSound(player, player.getLocation(), "click-sound","UI_BUTTON_CLICK", SoundCategory.PLAYERS);

                this.inventoryManager.backPage(player);

                this.inventoryManager.openCratePreview(player, crate);
            }
        }

        if (crate.getCrateType() == CrateType.gacha && holder.tier.getName().equals("legendary")) {
            GachaType gachaType = crate.getCrateSettings().getGachaType();

            NBT nbt = new NBT(item);
            String rewardName = nbt.getString("rewardName");
            if (rewardName.isEmpty()) return;

            System.out.println("Item type: " + rewardName);
            boolean standard = rewardName.split("_")[1].equals(RewardType.STANDARD.name());

            switch (gachaType) {
                case NORMAL:
                    return;
                case FATE_POINT:
                    if (crate.getCrateSettings().getLegendaryStandard().stream().map(Prize::getPrizeNumber).anyMatch(rewardName::equals) && standard) {
                        return;
                    }
                    break;
                case OVERRIDE:
                    break;
            }

            player.playSound(UltimateMenuStuff.CLICK);

            player.openInventory(new CratePickPrizeMenu(player, item, crate).build().getInventory());
        }
    }

    private void setDefaultItems(Inventory inventory) {
        if (getCrate().isBorderToggle()) {
            List<Integer> borderItems = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);

            ItemBuilder borderItem = getCrate().getBorderItem().setTarget(getPlayer());

            if (getCrate().getCrateType() == CrateType.gacha) {
                borderItem.setCustomModelData(1000001).setHasCustomModelData(true);
            }

            ItemStack borderItemStack = borderItem.build();

            for (int i : borderItems) { // Top Border slots
                inventory.setItem(i, borderItemStack);
            }

            borderItems.replaceAll(getCrate()::getAbsoluteItemPosition);

            boolean first = false;
            boolean second = false;
            ItemBuilder textures = new ItemBuilder(borderItemStack.clone());
            textures.setHasCustomModelData(true);

            for (int i : borderItems) { // Bottom Border slots
                if (gacha) {
                    if (!first) {
                        first = true;
                        textures.setCustomModelData(1000003);
                        inventory.setItem(i, textures.build());
                        continue;
                    }

                    if (!second) {
                        second = true;
                        textures.setCustomModelData(1000004);
                        inventory.setItem(i, textures.build());
                        continue;
                    }
                }

                inventory.setItem(i, borderItemStack);
            }
        }

        int page = this.inventoryManager.getPage(getPlayer());

        if (this.inventoryManager.inCratePreview(getPlayer()) && ConfigManager.getConfig().getProperty(ConfigKeys.enable_crate_menu)) {
            inventory.setItem(getCrate().getAbsoluteItemPosition(4), this.inventoryManager.getMenuButton(getPlayer()));
        }

        if (page == 1) {
            if (getCrate().isBorderToggle()) {
                inventory.setItem(getCrate().getAbsoluteItemPosition(3), getCrate().getBorderItem().setTarget(getPlayer()).build());
            }
        } else {
            inventory.setItem(getCrate().getAbsoluteItemPosition(3), this.inventoryManager.getBackButton(getPlayer()));
        }

        if (page == getCrate().getMaxPage()) {
            if (getCrate().isBorderToggle()) {
                inventory.setItem(getCrate().getAbsoluteItemPosition(5), getCrate().getBorderItem().setTarget(getPlayer()).build());
            }
        } else {
            inventory.setItem(getCrate().getAbsoluteItemPosition(5), this.inventoryManager.getNextButton(getPlayer()));
        }
    }

    private List<ItemStack> getPageItems(int page) {
        List<ItemStack> list = !this.isTier ? getCrate().getPreviewItems(getPlayer()) : getCrate().getPreviewItems(this.tier, getPlayer());
        List<ItemStack> items = new ArrayList<>();

        if (page <= 0) page = 1;

        int max = getCrate().getMaxSlots() - (getCrate().isBorderToggle() ? 18 : getCrate().getMaxSlots() >= list.size() ? 0 : getCrate().getMaxSlots() != 9 ? 9 : 0);
        int index = page * max - max;
        int endIndex = index >= list.size() ? list.size() - 1 : index + max;

        for (; index < endIndex; index++) {
            if (index < list.size()) items.add(list.get(index));
        }

        for (; items.isEmpty(); page--) {
            if (page <= 0) break;
            index = page * max - max;
            endIndex = index >= list.size() ? list.size() - 1 : index + max;

            for (; index < endIndex; index++) {
                if (index < list.size()) items.add(list.get(index));
            }
        }

        return items;
    }
}
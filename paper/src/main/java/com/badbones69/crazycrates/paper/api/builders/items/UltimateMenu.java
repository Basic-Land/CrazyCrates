package com.badbones69.crazycrates.paper.api.builders.items;

import com.badbones69.crazycrates.paper.CrazyCrates;
import com.badbones69.crazycrates.paper.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.paper.api.builders.LegacyItemBuilder;
import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.banners.BannerData;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.OpenData;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.CurrencyType;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.ComponentBuilder;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.ItemRepo;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.UltimateMenuManager;
import com.badbones69.crazycrates.paper.managers.events.enums.EventType;
import com.badbones69.crazycrates.paper.tasks.crates.CrateManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import us.crazycrew.crazycrates.api.enums.types.KeyType;

import java.util.List;
import java.util.stream.IntStream;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public class UltimateMenu extends InventoryBuilder {
    private final DatabaseManager manager;
    private final int selectedCrate, totalPageAmount, currentPage;
    private boolean premiumShop, store = false;
    private OpenData openData = null;

    public UltimateMenu(Crate crate, Player player, Component trans) {
        super(crate, player, 54, trans);
        manager = CrazyCrates.getPlugin().getCrateManager().getDatabaseManager();
        player.getInventory().clear();

        int page = 0;
        int selectedCrate = 0;
        List<List<CrateSettings>> crateSettingsSplit = manager.getCrateSettingsSplit();

        for (int i = 0; i < crateSettingsSplit.size(); i++) {
            for (int j = 0; j < crateSettingsSplit.get(i).size(); j++) {
                if (crateSettingsSplit.get(i).get(j).getCrate().equals(crate)) {
                    page = i;
                    selectedCrate = j;
                    break;
                }
            }
        }

        this.currentPage = page;
        this.selectedCrate = selectedCrate;
        this.totalPageAmount = crateSettingsSplit.size();
    }

    private UltimateMenu(Player player, Component trans, int page, int selectedCrate) {
        super(CrazyCrates.getPlugin().getCrateManager().getDatabaseManager().getCrateSettingsSplit().get(page).get(selectedCrate).getCrate(), player, 54, trans);
        manager = CrazyCrates.getPlugin().getCrateManager().getDatabaseManager();
        this.currentPage = page;
        this.selectedCrate = selectedCrate;

        this.totalPageAmount = manager.getCrateSettingsSplit().size();

        player.getInventory().clear();
    }

    private UltimateMenu(UltimateMenu ultimateMenu, Component title, int selectedCrate) {
        this(ultimateMenu.getPlayer(), title, ultimateMenu.currentPage, selectedCrate);
    }

    private UltimateMenu(CrateSettings settings, Player player) {
        this(settings.getCrate(), player, ComponentBuilder.mainMenu(player, settings));
    }

    @Override
    public InventoryBuilder build() {
        setTopCrates();
        setTextureGlass();
        setItemsPlayerInv();
        return this;
    }

    @Override
    public void run(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder(false) instanceof UltimateMenu ultimateMenu)) return;

        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0) return;

        CrazyCrates plugin = ultimateMenu.plugin;
        Player player = ultimateMenu.getPlayer();
        Crate crate = ultimateMenu.getCrate();

        int newCrateNum = switch (slot) {
            case 0, 1, 2 -> 0;
            case 3, 4, 5 -> 1;
            case 6, 7, 8 -> 2;
            default -> -1;
        };

        if (newCrateNum != -1) {
            List<CrateSettings> settings = manager.getCrateSettingsSplit().get(ultimateMenu.currentPage);
            if (settings.size() <= newCrateNum) return;
            if (selectedCrate == newCrateNum) return;

            CrateSettings newCrate = settings.get(newCrateNum);

            player.playSound(ItemRepo.CLICK);
            Component component = ComponentBuilder.mainMenu(player, newCrate);

            player.openInventory(new UltimateMenu(ultimateMenu, component, newCrateNum).build().getInventory());
            return;
        }

        switch (slot) {
            case 65, 66 -> {
                if (premiumShop || store) {
                    setTextureGlass();
                    premiumShop = false;
                }

                PlayerInventory playerInventory = player.getInventory();
                IntStream.range(20, 25).forEach(i -> playerInventory.setItem(i, null));
            }

            case 68, 69 -> {
                if (premiumShop) {
                    if (openFrom(player, crate)) return;
                    plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName()).removePremiumCurrency(openData.currencyTake());
                } else if (store) {
                    player.sendMessage(empty()
                            .append(miniMessage().deserialize("<red><b>Server <dark_gray>» </b><gray>Klikni zde (<yellow>store.basicland.cz</yellow>) pro otevření stránky obchodu."))
                            .hoverEvent(text("https://store.basicland.cz/category/tokeny"))
                            .clickEvent(ClickEvent.openUrl("https://store.basicland.cz/category/tokeny")));
                    close(player);
                }
            }

            case 81 -> {
                player.playSound(ItemRepo.CLICK);
                close(player);
                manager.getHistory().sendHistory(player, player.getName(), 1, crate.getCrateSettings());
            }

            case 82 -> {
                player.playSound(ItemRepo.CLICK);
                close(player);
                plugin.getInventoryManager().openNewCratePreview(player, crate);
            }

            case 83 -> {
                player.playSound(ItemRepo.CLICK);
                close(player);
                manager.getShopManager().openFirst(crate, player);
            }

            case 84 -> {
                if (totalPageAmount == 1 || currentPage == 0) return;
                if (currentPage > 0) {
                    CrateSettings first = manager.getCrateSettingsSplit().get(currentPage - 1).getFirst();
                    player.openInventory(new UltimateMenu(first, player).build().getInventory());
                }
            }

            case 85 -> {
                if (totalPageAmount == 1 || currentPage == totalPageAmount - 1) return;
                if (currentPage < totalPageAmount - 1) {
                    CrateSettings first = manager.getCrateSettingsSplit().get(currentPage + 1).getFirst();
                    player.openInventory(new UltimateMenu(first, player).build().getInventory());
                }
            }
            case 86, 87 -> open1(player, crate);
            case 88, 89 -> open10(player, crate);
        }
    }

    private boolean openFrom(Player player, Crate crate) {
        if (openData.isZero()) return true;
        addKeys(player, crate, openData.neededKeys());
        int selectAmount = openData.selectAmount();

        if (selectAmount == 1) {
            open1(player, crate);
        } else if (selectAmount == 10) {
            open10(player, crate);
        }
        return false;
    }

    private void open10(Player player, Crate crate) {
        open(player, crate, crateManager, 10, () -> {
            close(player);
            player.setSneaking(true);
        });
        player.setSneaking(false);
    }

    private void open1(Player player, Crate crate) {
        open(player, crate, crateManager, 1, () -> {
            close(player);
            player.setSneaking(false);
        });
    }

    private void open(Player player, Crate crate, CrateManager crateManager, int size, Runnable runnable) {
        int keys = getKeys(player, crate);
        if (keys >= size) {
            runnable.run();
            player.playSound(ItemRepo.CRATE);
            crateManager.openCrate(player, crate, KeyType.virtual_key, player.getLocation(), false, false, EventType.event_crate_opened);
        } else {
            PlayerBaseProfile playerBaseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());
            int keysNeeded = size - keys;
            int premium = keysNeeded * 160;

            if (playerBaseProfile.hasPremiumCurrency(premium)) {
                setPremiumMenu(player, keysNeeded, premium);
                openData = new OpenData(size, keysNeeded, premium);
            } else {
                openStoreMenu(player);
            }
        }
    }

    private void setPremiumMenu(Player player, int keysNeeded, int premiumNeeded) {
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.setItem(28, ItemRepo.PREMIUM_SHOP.asItemStack());

        ItemStack no = ItemRepo.SHOP_BACK_MENU.asItemStack();
        playerInventory.setItem(20, no);
        playerInventory.setItem(21, no);

        LegacyItemBuilder shopVotePremiumYes = ItemRepo.SHOP_VOTE_PREMIUM_YES;

        shopVotePremiumYes.addLorePlaceholder("{keys}", keysNeeded + "");
        shopVotePremiumYes.addLorePlaceholder("{premium}", premiumNeeded + "");
        shopVotePremiumYes.addLorePlaceholder("{currency}", CurrencyType.PREMIUM_CURRENCY.translateMM());

        ItemStack yes = shopVotePremiumYes.asItemStack();

        playerInventory.setItem(23, yes);
        playerInventory.setItem(24, yes);
        premiumShop = true;
    }

    private void openStoreMenu(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.setItem(28, ItemRepo.STORE_MENU.asItemStack());

        ItemStack no = ItemRepo.SHOP_BACK_MENU.asItemStack();
        playerInventory.setItem(20, no);
        playerInventory.setItem(21, no);

        LegacyItemBuilder shopVoteTokensYes = ItemRepo.OPEN_STORE;

        playerInventory.setItem(23, shopVoteTokensYes.asItemStack());
        playerInventory.setItem(24, shopVoteTokensYes.asItemStack());
        store = true;
    }

    private void setTopCrates() {
        int slot = 0;
        int crate = 0;

        LegacyItemBuilder selectedMain = ItemRepo.SELECTED;
        LegacyItemBuilder unselectedMain = ItemRepo.UNSELECTED;
        LegacyItemBuilder mainCrate = ItemRepo.MAIN_MENU_NAME;

        for (CrateSettings setting : manager.getCrateSettingsSplit().get(currentPage)) {
            if (setting == null) continue;

            selectedMain.setDisplayName("<green><b>" + setting.getCrateName());
            unselectedMain.setDisplayName("<red><b>" + setting.getCrateName());

            if (crate == selectedCrate) {
                getInventory().setItem(slot, new LegacyItemBuilder(selectedMain, true).setCustomModelData(1000002).asItemStack());

                selectedMain = new LegacyItemBuilder(selectedMain, true).setCustomModelData(1000003);
                getInventory().setItem(slot + 1, selectedMain.asItemStack());
                getInventory().setItem(slot + 2, selectedMain.asItemStack());

                int model = setting.getModelDataMainMenu();
                mainCrate.setCustomModelData(model);
                getPlayer().getInventory().setItem(29, mainCrate.asItemStack());
            } else {
                getInventory().setItem(slot, new LegacyItemBuilder(unselectedMain, true).setCustomModelData(1000002).asItemStack());

                unselectedMain = new LegacyItemBuilder(unselectedMain, true).setCustomModelData(1000001);

                getInventory().setItem(slot + 1, unselectedMain.asItemStack());
                getInventory().setItem(slot + 2, unselectedMain.asItemStack());
            }

            crate++;
            slot += 3;
        }
    }

    private void setTextureGlass() {
        PlayerInventory playerInventory = getPlayer().getInventory();
        playerInventory.setItem(27, ItemRepo.MAIN_MENU.asItemStack());
        BannerData banner = getCrate().getCrateSettings().getBannerPackage().getBanner();
        playerInventory.setItem(28, ItemRepo.BANNER.setCustomModelData(banner == null ? -1 : banner.modelData()).asItemStack());
    }

    private void setItemsPlayerInv() {
        PlayerInventory playerInventory = getPlayer().getInventory();

        playerInventory.setItem(0, ItemRepo.BOOK.asItemStack());
        playerInventory.setItem(1, ItemRepo.PAPER.asItemStack());
        playerInventory.setItem(2, ItemRepo.SHOP.asItemStack());

        if (totalPageAmount > 1) {
            if (currentPage == 0) {
                playerInventory.setItem(4, ItemRepo.FORWARD.asItemStack());
            } else if (currentPage < totalPageAmount - 1) {
                playerInventory.setItem(3, ItemRepo.BACK_ITEM.asItemStack());
                playerInventory.setItem(4, ItemRepo.FORWARD.asItemStack());
            } else if (currentPage == totalPageAmount - 1) {
                playerInventory.setItem(3, ItemRepo.BACK_ITEM.asItemStack());
            }
        }

        ItemStack x1 = ItemRepo.BUILDER_X1.asItemStack();
        playerInventory.setItem(5, x1);
        playerInventory.setItem(6, x1);

        ItemStack x10 = ItemRepo.BUILDER_X10.asItemStack();
        playerInventory.setItem(7, x10);
        playerInventory.setItem(8, x10);
    }

    private int getKeys(Player player, Crate crate) {
        return plugin.getUserManager().getVirtualKeys(player.getUniqueId(), crate.getFileName());
    }

    private void addKeys(Player player, Crate crate, int amount) {
        plugin.getUserManager().addVirtualKeys(player.getUniqueId(), crate.getFileName(), amount);
    }

    private void close(Player player) {
        player.closeInventory(InventoryCloseEvent.Reason.PLAYER);
    }

    public static class TestMenuListener implements Listener {
        @EventHandler
        public void Close(InventoryCloseEvent e) {
            InventoryCloseEvent.Reason reason = e.getReason();
            switch (e.getInventory().getHolder(false)) {
                case UltimateMenu ultimateMenu -> {
                    Player player = ultimateMenu.getPlayer();
                    if (reason.equals(InventoryCloseEvent.Reason.PLAYER) || reason.equals(InventoryCloseEvent.Reason.PLUGIN)) {
                        ultimateMenu.manager.getUltimateMenuManager().remove(player);
                    }
                    print(player, reason, ultimateMenu);
                }
                case InventoryBuilder builder -> print(builder.getPlayer(), reason, builder);
                case null, default -> {
                }
            }
        }

        private void print(Player player, InventoryCloseEvent.Reason reason, InventoryBuilder builder) {
            UltimateMenuManager ultimateMenuManager = CrazyCrates.getPlugin().getCrateManager().getDatabaseManager().getUltimateMenuManager();
            CrazyCrates.LOGGER.info(player.getName() +
                    " closed " + (builder instanceof UltimateMenu ? "Ultimatemenu" : builder.getClass().getName()) +
                    " reason: " + reason +
                    " hasItems: " + ultimateMenuManager.hasItems(player));
        }

        @EventHandler
        public void pickUpItem(EntityPickupItemEvent e) {
            if (e.getEntity() instanceof Player player && player.getOpenInventory().getTopInventory().getHolder(false) instanceof UltimateMenu) {
                e.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void kokotUmrel(PlayerDeathEvent e) {
            Inventory inv = e.getPlayer().getOpenInventory().getTopInventory();
            if (inv.getHolder(false) instanceof UltimateMenu ultimateMenu) {
                List<ItemStack> items = ultimateMenu.manager.getUltimateMenuManager().getItemsClean(ultimateMenu.getPlayer());
                e.getDrops().clear();
                e.getDrops().addAll(items);
                e.getPlayer().getInventory().clear();
            }
        }
    }
}

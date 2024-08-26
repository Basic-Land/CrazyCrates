package com.badbones69.crazycrates.api.builders.types.items;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.builders.InventoryBuilder;
import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.OpenData;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.ComponentBuilder;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuStuff;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import us.crazycrew.crazycrates.api.enums.types.KeyType;

import java.util.List;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public class UltimateMenu extends InventoryBuilder {
    private static final DatabaseManager manager = JavaPlugin.getPlugin(CrazyCrates.class).getCrateManager().getDatabaseManager();
    private final int selectedCrate;
    private final int totalPageAmount;
    private final int currentPage;
    private boolean voteShop, premiumShop, store = false;
    private OpenData openData = null;

    public UltimateMenu(Crate crate, Player player, Component trans) {
        super(crate, player, 54, trans);
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
        super(manager.getCrateSettingsSplit().get(page).get(selectedCrate).getCrate(), player, 54, trans);
        this.currentPage = page;
        this.selectedCrate = selectedCrate;

        this.totalPageAmount = manager.getCrateSettingsSplit().size();

        player.getInventory().clear();
    }

    private UltimateMenu(UltimateMenu ultimateMenu, Component title, int selectedCrate) {
        this(ultimateMenu.getPlayer(), title, ultimateMenu.currentPage, selectedCrate);
    }

    private UltimateMenu(UltimateMenu ultimateMenu) {
        this(ultimateMenu.getPlayer(),
                ComponentBuilder.mainMenu(
                        ultimateMenu.getPlayer(),
                        ultimateMenu.getCrate().getCrateSettings()),
                ultimateMenu.currentPage,
                ultimateMenu.selectedCrate
        );
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
        if (!(e.getInventory().getHolder() instanceof UltimateMenu ultimateMenu)) return;

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

            CrateSettings newCrate = settings.get(newCrateNum);

            player.playSound(UltimateMenuStuff.CLICK);
            Component component = ComponentBuilder.mainMenu(player, newCrate);

            player.openInventory(new UltimateMenu(ultimateMenu, component, newCrateNum).build().getInventory());
            return;
        }

        switch (slot) {
            case 65, 66 -> {
                if (voteShop || premiumShop || store) {
                    setTextureGlass();
                    voteShop = false;
                    premiumShop = false;
                }

                PlayerInventory playerInventory = player.getInventory();
                for (int i = 20; i < 25; i++) {
                    playerInventory.setItem(i, null);
                }
            }

            case 68, 69 -> {
                if (voteShop && !premiumShop) {
                    if (openFrom(player, crate)) return;
                    plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName()).removeVoteTokens(openData.currencyTake());
                } else if (premiumShop && !voteShop) {
                    plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName()).convertPremiumToVote(openData.currencyTake());
                    voteShop = false;
                    premiumShop = false;
                    player.openInventory(new UltimateMenu(this).build().getInventory());
                } else if (store) {
                    player.sendMessage(empty()
                            .append(miniMessage().deserialize("<red><b>Server <dark_gray>» </b><gray>Klikni pro otevření stránky obchodu"))
                            .hoverEvent(text("https://store.basicland.cz/"))
                            .clickEvent(ClickEvent.openUrl("https://store.basicland.cz/")));
                    close(player);
                }
            }

            case 81 -> {
                player.playSound(UltimateMenuStuff.CLICK);
                close(player);
                manager.getHistory().sendHistory(player, player.getName(), 1, crate.getCrateSettings());
            }
            case 82 -> {
                player.playSound(UltimateMenuStuff.CLICK);
                close(player);
                plugin.getInventoryManager().openNewCratePreview(player, crate);
            }
            case 83 -> {
                player.playSound(UltimateMenuStuff.CLICK);
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
            player.playSound(UltimateMenuStuff.CRATE);
            crateManager.openCrate(player, crate, KeyType.virtual_key, player.getLocation(), false, false);
        } else {
            PlayerBaseProfile playerBaseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());
            int cost = size * 160;
            int keysNeeded = size - keys;
            int vote = keysNeeded * 160;
            int premium = cost - (cost - vote) - playerBaseProfile.getVoteTokens();

            if (playerBaseProfile.hasVoteTokens(vote)) {
                setVoteMenu(player, keysNeeded, vote);
                openData = new OpenData(size, keysNeeded, vote);
            } else if (playerBaseProfile.hasPremiumCurrency(premium)) {
                setPremiumMenu(player, premium);
                openData = new OpenData(size, keysNeeded, premium);
            } else {
                openStoreMenu(player);
            }
        }
    }

    private void setVoteMenu(Player player, int keysNeeded, int voteTokensNeeded) {
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.setItem(28, UltimateMenuStuff.TOKEN_SHOP.getStack());

        ItemStack no = UltimateMenuStuff.SHOP_BACK_MENU.getStack();
        playerInventory.setItem(20, no);
        playerInventory.setItem(21, no);

        ItemBuilder shopVoteTokensYes = UltimateMenuStuff.SHOP_VOTE_TOKENS_YES;

        shopVoteTokensYes.addLorePlaceholder("{keys}", keysNeeded + "");
        shopVoteTokensYes.addLorePlaceholder("{vote}", voteTokensNeeded + "");

        playerInventory.setItem(23, shopVoteTokensYes.getStack());
        playerInventory.setItem(24, shopVoteTokensYes.getStack());
        voteShop = true;
        premiumShop = false;
    }

    private void setPremiumMenu(Player player, int premiumNeeded) {
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.setItem(28, UltimateMenuStuff.PREMIUM_SHOP.getStack());

        ItemStack no = UltimateMenuStuff.SHOP_BACK_MENU.getStack();
        playerInventory.setItem(20, no);
        playerInventory.setItem(21, no);

        ItemBuilder shopVoteTokensYes = UltimateMenuStuff.SHOP_VOTE_PREMIUM_YES;

        shopVoteTokensYes.addLorePlaceholder("{premium}", premiumNeeded + "");

        playerInventory.setItem(23, shopVoteTokensYes.getStack());
        playerInventory.setItem(24, shopVoteTokensYes.getStack());
        premiumShop = true;
        voteShop = false;
    }

    private void openStoreMenu(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.setItem(28, UltimateMenuStuff.STORE_MENU.getStack());

        ItemStack no = UltimateMenuStuff.SHOP_BACK_MENU.getStack();
        playerInventory.setItem(20, no);
        playerInventory.setItem(21, no);

        ItemBuilder shopVoteTokensYes = UltimateMenuStuff.OPEN_STORE;

        playerInventory.setItem(23, shopVoteTokensYes.getStack());
        playerInventory.setItem(24, shopVoteTokensYes.getStack());
        store = true;
    }

    private void setTopCrates() {
        int slot = 0;
        int crate = 0;

        ItemBuilder selectedMain = UltimateMenuStuff.SELECTED;
        ItemBuilder unselectedMain = UltimateMenuStuff.UNSELECTED;
        ItemBuilder mainCrate = UltimateMenuStuff.MAIN_MENU_NAME;

        for (CrateSettings setting : manager.getCrateSettingsSplit().get(currentPage)) {
            if (setting == null) continue;

            selectedMain.setDisplayName("<green><b>" + setting.getCrateName());
            unselectedMain.setDisplayName("<red><b>" + setting.getCrateName());

            if (crate == selectedCrate) {
                selectedMain.setCustomModelData(1000002);
                getInventory().setItem(slot, selectedMain.getStack());

                selectedMain.setCustomModelData(1000003);
                getInventory().setItem(slot + 1, selectedMain.getStack());
                getInventory().setItem(slot + 2, selectedMain.getStack());

                int model = setting.getModelDataMainMenu();
                mainCrate.setCustomModelData(model);
                ItemStack item = mainCrate.getStack();
                getPlayer().getInventory().setItem(29, item);
            } else {
                unselectedMain.setCustomModelData(1000002);
                getInventory().setItem(slot, unselectedMain.getStack());

                unselectedMain.setCustomModelData(1000001);
                getInventory().setItem(slot + 1, unselectedMain.getStack());
                getInventory().setItem(slot + 2, unselectedMain.getStack());
            }

            crate++;
            slot += 3;
        }
    }

    private void setTextureGlass() {
        PlayerInventory playerInventory = getPlayer().getInventory();
        playerInventory.setItem(27, UltimateMenuStuff.MAIN_MENU.getStack());
        playerInventory.setItem(28, UltimateMenuStuff.BANNER.getStack());
    }

    private void setItemsPlayerInv() {
        PlayerInventory playerInventory = getPlayer().getInventory();

        playerInventory.setItem(0, UltimateMenuStuff.BOOK.getStack());
        playerInventory.setItem(1, UltimateMenuStuff.PAPER.getStack());
        playerInventory.setItem(2, UltimateMenuStuff.SHOP.getStack());

        if (totalPageAmount > 1) {
            if (currentPage == 0) {
                playerInventory.setItem(4, UltimateMenuStuff.FORWARD.getStack());
            } else if (currentPage < totalPageAmount - 1) {
                playerInventory.setItem(3, UltimateMenuStuff.BACK_ITEM.getStack());
                playerInventory.setItem(4, UltimateMenuStuff.FORWARD.getStack());
            } else if (currentPage == totalPageAmount - 1) {
                playerInventory.setItem(3, UltimateMenuStuff.BACK_ITEM.getStack());
            }
        }

        ItemStack x1 = UltimateMenuStuff.BUILDER_X1.getStack();
        playerInventory.setItem(5, x1);
        playerInventory.setItem(6, x1);

        ItemStack x10 = UltimateMenuStuff.BUILDER_X10.getStack();
        playerInventory.setItem(7, x10);
        playerInventory.setItem(8, x10);
    }

    private int getKeys(Player player, Crate crate) {
        return plugin.getUserManager().getVirtualKeys(player.getUniqueId(), crate.getCrateName());
    }

    private void addKeys(Player player, Crate crate, int amount) {
        plugin.getUserManager().addVirtualKeys(player.getUniqueId(), crate.getCrateName(), amount);
    }

    private void close(Player player) {
        player.closeInventory(InventoryCloseEvent.Reason.PLAYER);
    }

    public static class TestMenuListener implements Listener {
        @EventHandler
        public void Close(InventoryCloseEvent e) {
            if (e.getInventory().getHolder(false) instanceof UltimateMenu ultimateMenu && e.getReason().equals(InventoryCloseEvent.Reason.PLAYER)) {
                ultimateMenu.plugin.getCrateManager().getDatabaseManager().getUltimateMenuManager().remove(ultimateMenu.getPlayer());
            }
        }

        @EventHandler
        public void pickUpItem(EntityPickupItemEvent e) {
            if (e.getEntity() instanceof Player player && player.getOpenInventory().getTopInventory().getHolder(false) instanceof UltimateMenu) {
                e.setCancelled(true);
            }
        }
    }
}

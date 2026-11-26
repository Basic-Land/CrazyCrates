package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl;

import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.ItemRepo;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MenuUtils;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * The implemented gacha GUI layout used by {@link MapMenu}.
 */
public class GachaMapMenuContent extends BaseMenu {
    private final int selectedCrate, totalPageAmount, currentPage;
    private final CrateSettings crateSettings;

    public GachaMapMenuContent(Crate crate) {
        crateSettings = crate.getCrateSettings();
        int page = 0;
        int selectedCrate = 0;
        List<List<CrateSettings>> crateSettingsSplit = databaseManager.getCrateSettingsSplit();

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

    private void setTopCrates(MapMenu menu) {
        int crate = 0;
        int[][] positions = new int[][]{{333, 122}, {451, 122}, {570, 122}};

        for (CrateSettings setting : databaseManager.getCrateSettingsSplit().get(currentPage)) {
            if (setting == null) continue;
            addScaledImage(menu, crate == selectedCrate ? "selected" : "notselected", positions[crate][0], positions[crate][1]);
            int finalCrate = crate;
            menu.addButton("crate " + crate, positions[crate][0], positions[crate][1], 108, 40, b -> {
                Player player = b.player();
                player.playSound(ItemRepo.CLICK);
                System.out.println("Clicked crate " + finalCrate);
            });
            crate++;
        }
    }

    private void setArrows(MapMenu menu) {
        if (currentPage > 0) {
            addImage(menu, "levosipka", 453, 485);
            menu.addButton("left arrow", 453, 485, 32, 32, b -> {
                Player player = b.player();
                player.playSound(ItemRepo.CLICK);
                System.out.println("Clicked left arrow");
            });
        }

        if (currentPage < totalPageAmount - 1) {
            addImage(menu, "pravosipka", 488, 485);
            menu.addButton("right arrow", 488, 485, 32, 32, b -> {
                Player player = b.player();
                player.playSound(ItemRepo.CLICK);
                System.out.println("Clicked right arrow");
            });
        }
    }

    @Override
    public void build(MapMenu menu) {
        addScaledImage(menu, "art", 160, 164);
        addScaledImage(menu, "hlmenu", 148, 60);
        addScaledImage(menu, "gacha", 456, 77);

        setTopCrates(menu);
        setArrows(menu);

        Color background = new Color(0xd5cdbd);
        Color text = new Color(0);

        menu.addField("legendary display", "LEG", background, text, 174, 296, 60, 22);
        menu.addField("epic display", "EPIC", background, text, 174, 322, 60, 22);
        menu.addField("epic current pity", null, background, text, 236, 322, 32, 22);
        menu.addField("legendary current pity", null, background, text, 236, 296, 32, 22);
        menu.addField("legendary max pity", null, background, text, 286, 296, 32, 22);
        menu.addField("epic max pity", null, background, text, 286, 322, 32, 22);
        menu.addField("remaining time", null, background, text, 178, 436, 134, 18);
        menu.addField("premium tokeny", null, 222, 126, 90, 18);
        menu.addField("vote tokeny", null, 222, 80, 90, 18);
        menu.addField("keys", null, 758, 126, 90, 18);

        menu.addButton("history", 345, 485, 32, 32, b -> {
            Player player = b.player();
            player.playSound(ItemRepo.CLICK);
            databaseManager.getHistory().sendHistory(player, player.getName(), 1, crateSettings, null);
            b.close();
        });
        menu.addButton("preview menu", 381, 485, 32, 32);
        menu.addButton("shop", 418, 485, 32, 32);
        menu.addButton("1x pull", 523, 482, 72, 36);
        menu.addButton("10x pull", 598, 482, 72, 36);
    }

    private void addImage(MapMenu menu, String name, int x, int y) {
        BufferedImage image = MenuUtils.loadImage(name);
        if (image != null) menu.addImage(name, image, x, y);
    }

    private void addScaledImage(MapMenu menu, String name, int x, int y) {
        BufferedImage image = MenuUtils.loadImage(name);
        if (image != null) menu.addImage(name, MenuUtils.scaleBy2(image), x, y);
    }

    @Override
    public void populate(MapMenu menu, Player player) {
        int virtualKeys = plugin.getUserManager().getVirtualKeys(player.getUniqueId(), crateSettings.getCrateName());
        PlayerProfile playerProfile = databaseManager.getPlayerProfile(player.getName(), crateSettings, false);
        PlayerBaseProfile baseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());

        int epic = playerProfile.getPity(Rarity.EPIC).first();
        int legendary = playerProfile.getPity(Rarity.LEGENDARY).first();

        menu.setFieldValue(3, epic);
        menu.setFieldValue(4, legendary);
        menu.setFieldValue(5, crateSettings.getRarityMap().get(Rarity.LEGENDARY).pity());
        menu.setFieldValue(6, crateSettings.getRarityMap().get(Rarity.EPIC).pity());
        menu.setFieldValue(7, crateSettings.getBannerPackage().getRemainingDuration());
        menu.setFieldValue(8, baseProfile.getPremiumCurrency());
        menu.setFieldValue(9, baseProfile.getVoteTokens());
        menu.setFieldValue(10, virtualKeys);
    }
}

package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

import com.badbones69.crazycrates.paper.CrazyCrates;
import com.badbones69.crazycrates.paper.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.Rarity;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The implemented gacha GUI layout used by {@link MapMenu}.
 */
public class GachaMapMenuContent implements MCGUIContent {
    private final CrazyCrates plugin;

    public GachaMapMenuContent(CrazyCrates plugin) {
        this.plugin = plugin;
    }

    @Override
    public void build(MapMenu menu) {
        addScaledImage(menu, "art", 160, 164);
        addScaledImage(menu, "hlmenu", 148, 60);
        addScaledImage(menu, "gacha", 456, 77);
        addScaledImage(menu, "selected", 333, 122);
        addScaledImage(menu, "notselected", 451, 122);
        addScaledImage(menu, "notselected", 570, 122);
        addImage(menu, "pravosipka", 488, 485);
        addImage(menu, "levosipka", 453, 485);

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

        menu.addButton("history", 345, 485, 32, 32);
        menu.addButton("preview menu", 381, 485, 32, 32);
        menu.addButton("shop", 418, 485, 32, 32);
        menu.addButton("1x pull", 523, 482, 72, 36);
        menu.addButton("10x pull", 598, 482, 72, 36);
        menu.addButton("chest 1", 334, 123, 108, 40);
        menu.addButton("chest 2", 451, 123, 108, 40);
        menu.addButton("chest 3", 570, 123, 108, 40);
        menu.addButton("left arrow", 453, 485, 32, 32);
        menu.addButton("right arrow", 488, 485, 32, 32);
    }

    private void addImage(MapMenu menu, String name, int x, int y) {
        BufferedImage image = menu.loadImage(name);
        if (image != null) menu.addImage(name, image, x, y);
    }

    private void addScaledImage(MapMenu menu, String name, int x, int y) {
        BufferedImage image = menu.loadImage(name);
        if (image != null) menu.addImage(name, menu.scaleBy2(image), x, y);
    }

    @Override
    public void populate(MapMenu menu, Player player) {
        DatabaseManager databaseManager = plugin.getCrateManager().getDatabaseManager();
        CrateSettings crateSettings = databaseManager.getCrateSettingsSplit().get(0).get(0);
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

    @Override
    public CanvasSettings canvasSettings() {
        return new CanvasSettings(1024, 640, true, 0);
    }
}

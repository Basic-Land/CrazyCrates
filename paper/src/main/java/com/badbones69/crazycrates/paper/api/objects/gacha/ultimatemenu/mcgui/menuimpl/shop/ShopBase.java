package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl.shop;

import com.badbones69.crazycrates.paper.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.ItemRepo;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MenuType;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MenuUtils;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl.BaseMenu;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;
import java.util.List;

public abstract class ShopBase extends BaseMenu {
    protected static final int[][][] positions = new int[2][9][2];
    private static final BufferedImage blank = MenuUtils.loadImage("shop/blank");
    static {
        int x = 345, y = 342;
        for (int row = 0; row < positions.length; row++) {
            for (int column = 0; column < positions[row].length; column++) {
                positions[row][column][0] = x;
                positions[row][column][1] = y;
                x += 36;
            }
            x = 345;
            y += 36;
        }
    }

    public abstract List<String> getItems();

    @Override
    public void build(MapMenu menu) {
        addScaledImage(menu, "shop/store", 149, 168);
        addScaledImage(menu, "shop/vote2", 343, 231);
        addScaledImage(menu, "shop/mystic2", 451, 231);
        addScaledImage(menu, "shop/stellar2", 558, 231);

        menu.addField("icon1", "1", 221, 188, 90, 18);
        menu.addField("icon2", "2", 221, 234, 90, 18);
        menu.addField("icon3", "3", 755, 188, 90, 18);
        menu.addField("icon4", "4", 757, 234, 90, 18);

        menu.addButton("vote", 344, 231, 107, 40, b -> {
            Player player = b.player();
            player.playSound(ItemRepo.CLICK);
            openMenu(b, MenuType.VOTE_SHOP);
        });
        menu.addButton("mystic", 451, 231, 107, 40, b -> {
            Player player = b.player();
            player.playSound(ItemRepo.CLICK);
            openMenu(b, MenuType.MYSTIC_SHOP);
        });
        menu.addButton("stellar", 558, 231, 107, 40, b -> {
            Player player = b.player();
            player.playSound(ItemRepo.CLICK);
            openMenu(b, MenuType.STELLAR_SHOP);
        });


        menu.addButton("menu", 479, 412, 49, 32, b -> {
            Player player = b.player();
            player.playSound(ItemRepo.CLICK);
            openMenu(b, MenuType.GACHA);
        });

        setItems(menu, getItems());
    }

    protected final void setItems(MapMenu menu, List<String> names) {
        int index = 0;
        for (int[][] position : positions) {
            for (int[] ints : position) {
                int x = ints[0];
                int y = ints[1];

                if (index < names.size()) {
                    String name = names.get(index);
                    addImage(menu, name, x, y);
                } else {
                    menu.addImage("blank", blank, x, y);
                }
                index++;
            }
        }
    }

    @Override
    public void populate(MapMenu menu, Player player) {
        PlayerBaseProfile baseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());
        menu.setFieldValue(1, baseProfile.getMysticTokens());
        menu.setFieldValue(2, baseProfile.getStellarShards());
        menu.setFieldValue(3, baseProfile.getVoteTokens());
        menu.setFieldValue(4, baseProfile.getPremiumCurrency());
    }
}

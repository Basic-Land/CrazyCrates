package com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui;

import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl.EmptyMapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl.GachaMapMenuContent;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl.PreviewMapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl.shop.ShopVoteMapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl.shop.ShopMysticMapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl.shop.ShopStellarMapMenu;
import lombok.Getter;

public enum MenuType {
    GACHA(new GachaMapMenuContent()),
    PREVIEW(new PreviewMapMenu()),
    VOTE_SHOP(new ShopVoteMapMenu()),
    MYSTIC_SHOP(new ShopMysticMapMenu()),
    STELLAR_SHOP(new ShopStellarMapMenu()),
    EMPTY(new EmptyMapMenu());

    @Getter
    final MCGUIContent menu;

    MenuType(MCGUIContent supplier) {
        this.menu = supplier;
    }
}

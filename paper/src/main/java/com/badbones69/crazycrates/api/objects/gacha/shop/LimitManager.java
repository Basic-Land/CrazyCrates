package com.badbones69.crazycrates.api.objects.gacha.shop;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.api.objects.gacha.enums.LimitType;
import com.badbones69.crazycrates.api.objects.gacha.enums.ShopID;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class LimitManager {
    private final CrazyCrates plugin = CrazyCrates.getPlugin();

    public ShopPurchase getData(Player player, ShopID shopID, ShopItem item, boolean increment) {
        PlayerBaseProfile playerBaseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());
        int bought = playerBaseProfile.getShops().computeIfAbsent(shopID, k -> new HashMap<>()).computeIfAbsent(item.id(), k -> 0);

        LimitType limitType;

        if (item.limit() == -1) {
            limitType = LimitType.UNLIMITED;
        } else if (bought >= item.limit()) {
            limitType = LimitType.LIMIT_REACHED;
        } else {
            limitType = LimitType.SUCCESS;
        }

        if (increment && limitType == LimitType.SUCCESS) {
            playerBaseProfile.getShops().get(shopID).put(item.id(), bought + 1);
        }

        return new ShopPurchase(limitType, bought);
    }
}

package com.badbones69.crazycrates.paper.api.objects.gacha.shop;

import com.badbones69.crazycrates.paper.CrazyCrates;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.LimitType;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.ShopID;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class LimitManager {
    private final CrazyCrates plugin = CrazyCrates.getPlugin();

    public ShopPurchase getData(Player player, ShopID shopID, ShopItem item, int amount) {
        PlayerBaseProfile playerBaseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());
        var shops = playerBaseProfile.getShops();
        int bought = shops.computeIfAbsent(shopID, _ -> new HashMap<>()).computeIfAbsent(item.id(), _ -> 0);

        LimitType limitType;

        if (item.isUnlimited()) {
            limitType = LimitType.UNLIMITED;
        } else if (bought >= item.limit()) {
            limitType = LimitType.LIMIT_REACHED;
        } else {
            limitType = LimitType.SUCCESS;
        }

        if (amount > 0 && limitType == LimitType.SUCCESS) {
            shops.get(shopID).put(item.id(), bought + amount);
        }

        return new ShopPurchase(limitType, bought);
    }
}

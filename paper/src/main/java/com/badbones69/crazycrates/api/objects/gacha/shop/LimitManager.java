package com.badbones69.crazycrates.api.objects.gacha.shop;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.objects.gacha.BaseProfileManager;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.api.objects.gacha.enums.LimitType;
import com.badbones69.crazycrates.api.objects.gacha.enums.ShopID;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class LimitManager {
    private final BaseProfileManager baseProfileManager;

    public LimitManager() {
        baseProfileManager = JavaPlugin.getPlugin(CrazyCrates.class).getBaseProfileManager();
    }

    public LimitType canPurchase(Player player, ShopID shopID, ShopItem item) {
        PlayerBaseProfile playerBaseProfile = baseProfileManager.getPlayerBaseProfile(player.getName());
        int bought = playerBaseProfile.getShops().computeIfAbsent(shopID, k -> new HashMap<>()).computeIfAbsent(item.id(), k -> 0);

        if (item.limit() == -1) {
            return LimitType.UNLIMITED;
        }

        return bought < item.limit() ? LimitType.SUCCESS : LimitType.LIMIT_REACHED;
    }

    public void purchaseItem(Player player, ShopID shopID, ShopItem item) {
        PlayerBaseProfile playerBaseProfile = baseProfileManager.getPlayerBaseProfile(player.getName());
        playerBaseProfile.getShops().computeIfAbsent(shopID, k -> new HashMap<>()).compute(item.id(), (id, bought) -> bought == null ? 1 : bought + 1);
        baseProfileManager.save();
    }
}

package com.badbones69.crazycrates.api.objects.gacha.shop;

import com.badbones69.crazycrates.api.objects.gacha.enums.CurrencyType;
import com.badbones69.crazycrates.api.objects.gacha.enums.ShopID;

import java.util.List;

public record ShopData(ShopID shopID,
                       CurrencyType currencyType,
                       String shopName,
                       List<ShopItem> items) {

}

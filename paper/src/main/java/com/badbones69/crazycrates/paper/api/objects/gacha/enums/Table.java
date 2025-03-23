package com.badbones69.crazycrates.paper.api.objects.gacha.enums;

import lombok.Getter;

@Getter
public enum Table {
    ALL_ITEMS("AllItems"),
    SHOP_ITEMS("ShopItems");

    private final String table;

    Table(String table) {
        this.table = table;
    }
}

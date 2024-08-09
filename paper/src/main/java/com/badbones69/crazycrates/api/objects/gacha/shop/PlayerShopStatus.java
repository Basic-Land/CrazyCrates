package com.badbones69.crazycrates.api.objects.gacha.shop;

import com.badbones69.crazycrates.api.objects.gacha.enums.ShopID;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PlayerShopStatus implements Serializable {
    @Serial
    private static final long serialVersionUID = 9021068169306546545L;

    private final Map<ShopID, Object> map = new HashMap<>();

}

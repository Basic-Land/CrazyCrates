package com.badbones69.crazycrates.api.objects.gacha.shop;

import com.badbones69.crazycrates.api.objects.gacha.enums.LimitType;

public record ShopPurchase(LimitType limitType, int bought) {
    public boolean isSuccess() {
        return limitType == LimitType.SUCCESS;
    }

    public boolean isLimitReached() {
        return limitType == LimitType.LIMIT_REACHED;
    }

    public boolean isUnlimited() {
        return limitType == LimitType.UNLIMITED;
    }
}

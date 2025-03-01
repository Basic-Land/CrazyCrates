package com.badbones69.crazycrates.api.objects.gacha.banners;

import java.time.LocalDateTime;
import java.util.List;

public record BannerData(LocalDateTime start,
                         LocalDateTime end,
                         List<BannerItem> items,
                         int modelData) {

    public boolean isBannerActive() {
        LocalDateTime now = LocalDateTime.now();
        return start.isBefore(now) && end.isAfter(now);
    }
}

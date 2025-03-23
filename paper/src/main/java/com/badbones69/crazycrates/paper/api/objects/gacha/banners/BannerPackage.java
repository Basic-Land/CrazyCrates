package com.badbones69.crazycrates.paper.api.objects.gacha.banners;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record BannerPackage(List<BannerData> bannerDataList, boolean enabled) {

    public BannerData getBanner() {
        return bannerDataList.stream()
                .filter(BannerData::isBannerActive)
                .min(Comparator.comparing(BannerData::end))
                .orElse(null);
    }

    public boolean isBannerActive() {
        return getBanner() != null;
    }

    public String getRemainingDuration() {
        BannerData banner = getBanner();
        if (banner == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = banner.end();

        Duration duration = Duration.between(now, endTime);
        return String.format("%dd %dh %dm",
                duration.toDaysPart(),
                duration.toHoursPart(),
                duration.toMinutesPart());
    }
}

package com.badbones69.crazycrates.api.objects.gacha.banners;

import java.time.Duration;
import java.time.LocalDateTime;

public record BannerPackage(BannerData currentBanner, BannerData nextBanner, boolean enabled) {

    public BannerData getBanner() {
        return enabled ? currentBanner.isBannerActive() ? currentBanner : nextBanner.isBannerActive() ? nextBanner : null : null;
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

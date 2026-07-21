package com.badbones69.crazycrates.paper.api.objects.gacha.banners;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record BannerPackage(List<BannerData> bannerDataList, boolean enabled) {

    public Optional<BannerData> getBanner() {
        return bannerDataList.stream()
                .filter(BannerData::isBannerActive)
                .min(Comparator.comparing(BannerData::end));
    }

    public boolean isBannerActive() {
        return getBanner().isPresent();
    }

    public String getRemainingDuration() {
        Optional<BannerData> bannerOpt = getBanner();
        if (bannerOpt.isEmpty()) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = bannerOpt.get().end();

        Duration duration = Duration.between(now, endTime);
        return String.format("%dd %dh %dm",
                duration.toDaysPart(),
                duration.toHoursPart(),
                duration.toMinutesPart());
    }
}

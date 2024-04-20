package com.badbones69.crazycrates.api.objects.gacha.banners;

import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record Banner(LocalDateTime startTime, LocalDateTime endTime, Map<Rarity, List<Integer>> itemMap) {
}

package com.badbones69.crazycrates.paper.api.objects.gacha.banners;

import com.badbones69.crazycrates.paper.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.RewardType;

import java.util.List;

public record BannerItem(Rarity rarity,
                         RewardType rewardType,
                         int number,
                         List<String> commands,
                         List<String> messages,
                         boolean give) {
}

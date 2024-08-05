package com.badbones69.crazycrates.api.objects.gacha.banners;

import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;

import java.util.List;

public record BannerItem(Rarity rarity,
                         RewardType rewardType,
                         int number,
                         List<String> commands,
                         List<String> messages,
                         boolean give) {
}

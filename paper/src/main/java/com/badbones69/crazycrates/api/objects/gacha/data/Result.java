package com.badbones69.crazycrates.api.objects.gacha.data;

import com.badbones69.crazycrates.api.builders.ItemBuilder;
import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.ResultType;
import cz.basicland.blibs.spigot.utils.item.NBT;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@ToString
@Getter
public class Result implements Serializable {
    @Serial
    private static final long serialVersionUID = 2975781183369196583L;
    private final Rarity rarity;
    private final int pity;
    private final long timestamp = System.currentTimeMillis();
    @Setter
    private ResultType won5050;
    private String itemName;
    private String rewardName;
    private transient Prize prize;

    public Result(Rarity rarity, ResultType won5050, int pity) {
        this.rarity = rarity;
        this.won5050 = won5050;
        this.pity = pity;
    }

    public boolean isLegendary() {
        return rarity == Rarity.LEGENDARY;
    }

    public boolean isWon5050() {
        return won5050 == ResultType.WON || won5050 == ResultType.GUARANTEED || won5050 == ResultType.WON_OF_RATE_UP;
    }

    public void setItemData(Prize prize) {
        this.prize = prize;
        if (prize == null) return;
        ItemBuilder item = prize.getDisplayItemBuilder();
        NBT nbt = new NBT(item.getItemStack());
        this.rewardName = nbt.getString("rewardName");
        this.itemName = item.getName();
        if (itemName.isEmpty()) {
            itemName = itemName(item.getMaterial().name());
        }
    }

    private String itemName(String type) {
        StringBuilder out = new StringBuilder();
        out.append("&7");
        for (String s : type.toLowerCase().split("_")) {
            out.append(s.substring(0, 1).toUpperCase()).append(s.substring(1)).append(" ");
        }

        return out.toString().trim();
    }
}

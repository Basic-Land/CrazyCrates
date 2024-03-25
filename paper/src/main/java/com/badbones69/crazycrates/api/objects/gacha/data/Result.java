package com.badbones69.crazycrates.api.objects.gacha.data;

import com.badbones69.crazycrates.api.objects.gacha.util.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.util.ResultType;
import cz.basicland.blibs.spigot.utils.item.CustomItemStack;
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
    @Setter
    private ResultType won5050;
    private final int pity;
    private final long timestamp = System.currentTimeMillis();
    private String itemName;
    private transient CustomItemStack item;

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

    public void setItem(CustomItemStack item) {
        this.item = item;
        this.itemName = item.getTitle();
    }
}

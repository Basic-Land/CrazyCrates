package com.badbones69.crazycrates.api.objects.gacha.data;

import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import com.badbones69.crazycrates.api.objects.gacha.util.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.util.ResultType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class PlayerProfile implements Serializable {
    @Serial
    private static final long serialVersionUID = 5987691544175538612L;
    @Getter
    private final String playerName;
    private final Map<Rarity, Pair<Integer, ResultType>> map = new HashMap<>();
    private final int extraRewardPity;
    @Getter
    private boolean nextLegendaryLimited;
    @Getter
    private int fatePoint, totalPity;
    @Getter
    private final List<Result> history = new ArrayList<>();
    @Setter
    @Getter
    private Pair<String, String> chosenReward = new Pair<>("", "");

    public PlayerProfile(String playerName, Collection<Rarity> rarities, int extraRewardPity) {
        this.playerName = playerName;
        this.extraRewardPity = extraRewardPity;
        for (Rarity rarity : rarities) {
            map.put(rarity, new Pair<>(0, ResultType.WON));
        }
    }

    public Pair<Integer, ResultType> getPity(Rarity rarity) {
        return map.get(rarity);
    }

    public void setPity(Rarity rarity, int newValue, ResultType value) {
        map.put(rarity, new Pair<>(newValue, value));
    }

    public void setNextLegendaryLimited() {
        nextLegendaryLimited = true;
    }

    public void resetNextLegendaryLimited() {
        nextLegendaryLimited = false;
    }

    public void resetFatePoint() {
        fatePoint = 0;
    }

    public void incrementFatePoint() {
        fatePoint++;
    }

    public void addHistory(Result history) {
        this.history.add(history);
    }

    public void cutHistory(int size) {
        if (history.size() > size) {
            history.subList(0, history.size() - size).clear();
        }
    }

    public void incrementTotalPity() {
        totalPity++;
    }

    public boolean reachedExtraRewardPity() {
        return totalPity >= extraRewardPity;
    }
}
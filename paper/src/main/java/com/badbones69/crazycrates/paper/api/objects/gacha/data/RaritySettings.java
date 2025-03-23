package com.badbones69.crazycrates.paper.api.objects.gacha.data;

public record RaritySettings(
        int pity,
        boolean is5050Enabled,
        double chanceFor5050,
        double baseChance,
        int softPityFrom,
        double softPityFormula,
        boolean staticFormula,
        int softPityLimit,
        int mysticTokens,
        int stellarShards
) {
    public double get5050Chance() {
        return is5050Enabled ? chanceFor5050 : 100;
    }

    public boolean softPityGen(int pityCounter) {
        return pityCounter - softPityFrom >= 0;
    }

    public double calculateSoftPityChance(int pityCounter) {
        int x = pityCounter - softPityFrom;
        return staticFormula ? softPityFormula : Math.min(softPityLimit, baseChance + (softPityFormula * (x == 0 ? 1 : x + 1)));
    }
}

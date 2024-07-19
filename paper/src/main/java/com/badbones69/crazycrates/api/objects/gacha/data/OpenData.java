package com.badbones69.crazycrates.api.objects.gacha.data;

public record OpenData(int selectAmount, int neededKeys, int currencyTake) {
    public boolean isZero() {
        return selectAmount == 0 && neededKeys == 0;
    }
}

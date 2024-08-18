package com.badbones69.crazycrates.api.objects.gacha.data;

import com.badbones69.crazycrates.api.objects.gacha.enums.CurrencyType;
import com.badbones69.crazycrates.api.objects.gacha.enums.ShopID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class PlayerBaseProfile implements Serializable {
    @Serial
    private static final long serialVersionUID = 4019127885119793810L;
    private final String playerName;
    private int stellarShards, mysticTokens, voteTokens, premiumCurrency;
    private final Map<ShopID, Map<Integer, Integer>> shops = new HashMap<>();

    public void addStellarShards(int amount) {
        stellarShards += amount;
    }

    public void addMysticTokens(int amount) {
        mysticTokens += amount;
    }

    public void addVoteTokens(int amount) {
        voteTokens += amount;
    }

    public void addPremiumCurrency(int amount) {
        premiumCurrency += amount;
    }

    public void add(int amount, CurrencyType currencyType) {
        switch (currencyType) {
            case VOTE_TOKENS -> addVoteTokens(amount);
            case MYSTIC_TOKENS -> addMysticTokens(amount);
            case STELLAR_SHARDS -> addStellarShards(amount);
            case PREMIUM_CURRENCY -> addPremiumCurrency(amount);
        }
    }

    public void removeStellarShards(int amount) {
        stellarShards -= amount;
    }

    public void removeMysticTokens(int amount) {
        mysticTokens -= amount;
    }

    public void removeVoteTokens(int amount) {
        voteTokens -= amount;
    }

    public void removePremiumCurrency(int amount) {
        premiumCurrency -= amount;
    }

    public void remove(int amount, CurrencyType currencyType) {
        switch (currencyType) {
            case VOTE_TOKENS -> removeVoteTokens(amount);
            case MYSTIC_TOKENS -> removeMysticTokens(amount);
            case STELLAR_SHARDS -> removeStellarShards(amount);
            case PREMIUM_CURRENCY -> removePremiumCurrency(amount);
        }
    }

    public boolean hasStellarShards(int amount) {
        return stellarShards >= amount;
    }

    public boolean hasMysticTokens(int amount) {
        return mysticTokens >= amount;
    }

    public boolean hasVoteTokens(int amount) {
        return voteTokens >= amount;
    }

    public boolean hasPremiumCurrency(int amount) {
        return premiumCurrency >= amount;
    }

    public boolean has(int amount, CurrencyType currencyType) {
        return switch (currencyType) {
            case VOTE_TOKENS -> hasVoteTokens(amount);
            case MYSTIC_TOKENS -> hasMysticTokens(amount);
            case STELLAR_SHARDS -> hasStellarShards(amount);
            case PREMIUM_CURRENCY -> hasPremiumCurrency(amount);
        };
    }

    public boolean removeStellarShardsIfHas(int amount) {
        if (hasStellarShards(amount)) {
            removeStellarShards(amount);
            return true;
        }
        return false;
    }

    public boolean removeMysticTokensIfHas(int amount) {
        if (hasMysticTokens(amount)) {
            removeMysticTokens(amount);
            return true;
        }
        return false;
    }

    public boolean removeVoteTokensIfHas(int amount) {
        if (hasVoteTokens(amount)) {
            removeVoteTokens(amount);
            return true;
        }
        return false;
    }

    public boolean removePremiumCurrencyIfHas(int amount) {
        if (hasPremiumCurrency(amount)) {
            removePremiumCurrency(amount);
            return true;
        }
        return false;
    }

    public boolean removeIfHas(int amount, CurrencyType currencyType) {
        return switch (currencyType) {
            case VOTE_TOKENS -> removeVoteTokensIfHas(amount);
            case MYSTIC_TOKENS -> removeMysticTokensIfHas(amount);
            case STELLAR_SHARDS -> removeStellarShardsIfHas(amount);
            case PREMIUM_CURRENCY -> removePremiumCurrencyIfHas(amount);
        };
    }

    public void convertPremiumToVote(int amount) {
        if (hasPremiumCurrency(amount)) {
            removePremiumCurrency(amount);
            addVoteTokens(amount);
        }
    }

    public void resetShopLimits() {
        shops.forEach((shopID, shop) -> shop.clear());
    }
}

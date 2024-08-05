package com.badbones69.crazycrates.api.objects.gacha.data;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PlayerBaseProfile implements Serializable {
    @Serial
    private static final long serialVersionUID = 4019127885119793810L;
    private final String playerName;
    private int stellarShards, mysticTokens, voteTokens, premiumCurrency;

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

    public void convertPremiumToVote(int amount) {
        if (hasPremiumCurrency(amount)) {
            removePremiumCurrency(amount);
            addVoteTokens(amount);
        }
    }
}

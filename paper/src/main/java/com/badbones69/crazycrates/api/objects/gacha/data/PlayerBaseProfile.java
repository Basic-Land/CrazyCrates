package com.badbones69.crazycrates.api.objects.gacha.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@ToString
@RequiredArgsConstructor
public class PlayerBaseProfile implements Serializable {
    @Serial
    private static final long serialVersionUID = 4019127885119793810L;
    private final String playerName;
    private int stellarShards, mysticTokens, voteTokens;

    public void addStellarShards(int amount) {
        stellarShards += amount;
    }

    public void addMysticTokens(int amount) {
        mysticTokens += amount;
    }

    public void addVoteTokens(int amount) {
        voteTokens += amount;
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

    public boolean hasStellarShards(int amount) {
        return stellarShards >= amount;
    }

    public boolean hasMysticTokens(int amount) {
        return mysticTokens >= amount;
    }

    public boolean hasVoteTokens(int amount) {
        return voteTokens >= amount;
    }
}

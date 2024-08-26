package com.badbones69.crazycrates.tasks.crates.other;

import com.badbones69.crazycrates.tasks.crates.types.roulette.RouletteStandard;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class GachaCrateManager extends AbstractCrateManager {
    private final Map<UUID, RouletteStandard> gachaRunnables = new HashMap<>();

    public GachaCrateManager() {
        super();
    }
}

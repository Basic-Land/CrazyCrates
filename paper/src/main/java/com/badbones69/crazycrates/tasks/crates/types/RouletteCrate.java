package com.badbones69.crazycrates.tasks.crates.types;

import com.badbones69.crazycrates.api.builders.CrateBuilder;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.tasks.BukkitUserManager;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import com.badbones69.crazycrates.tasks.crates.types.roulette.RouletteStandard;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazycrates.api.enums.types.KeyType;

public class RouletteCrate extends CrateBuilder {

    @NotNull
    private final CrateManager crateManager = this.plugin.getCrateManager();

    @NotNull
    private final BukkitUserManager userManager = this.plugin.getUserManager();

    public RouletteCrate(Crate crate, Player player, int size) {
        super(crate, player, size);
    }

    @Override
    public void open(KeyType type, boolean checkHand) {
        // Crate event failed so we return.
        if (isCrateEventValid(type, checkHand)) {
            return;
        }

        boolean keyCheck = this.userManager.takeKeys(1, getPlayer().getUniqueId(), getCrate().getName(), type, checkHand);

        if (!keyCheck) {
            // Remove from opening list.
            this.crateManager.removePlayerFromOpeningList(getPlayer());

            return;
        }

        setItem(22, getCrate().pickPrize(getPlayer()).getDisplayItem(getPlayer()));

        addCrateTask(new RouletteStandard(this, null, false).runTaskTimer(this.plugin, 2, 2));
    }

    @Override
    public void run() {

    }
}
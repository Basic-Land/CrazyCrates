package com.badbones69.crazycrates.commands.crates.types;

import com.badbones69.crazycrates.api.objects.Key;
import com.badbones69.crazycrates.commands.crates.BaseCommand;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandTest extends BaseCommand {

    private final @NotNull CrateManager crateManager = this.plugin.getCrateManager();

    @Command("test")
    public void test(Player player, @Suggestion("keys") String keyName) {
        //Key key = this.crateManager.getKey(keyName);
    }
}
package com.badbones69.crazycrates.commands.crates.types.player;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.commands.crates.types.BaseCommand;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class CommandTest extends BaseCommand {
    private @NotNull final CrateManager crateManager = JavaPlugin.getPlugin(CrazyCrates.class).getCrateManager();

    @Command(value = "test")
    @Permission(value = "test", def = PermissionDefault.TRUE)
    public void onTest(Player player) {
        crateManager.getDatabaseManager().getUltimateMenuManager().open(player);
    }
}

package com.badbones69.crazycrates.commands.crates.types.custom;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.commands.crates.types.BaseCommand;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import com.ryderbelserion.vital.paper.api.builders.PlayerBuilder;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class CommandPity extends BaseCommand {
    private @NotNull final CrazyCrates plugin = JavaPlugin.getPlugin(CrazyCrates.class);
    private @NotNull final CrateManager crateManager = this.plugin.getCrateManager();

    @Command("adminpity")
    @Permission(value = "crazycrates.command.player.history", def = PermissionDefault.TRUE)
    public void adminPity(Player player, @Suggestion("crates") String crateName, @Suggestion("players") PlayerBuilder target) {
        DatabaseManager playerDataManager = crateManager.getDatabaseManager();
        CrateSettings crateSettings = playerDataManager.getCrateSettings(crateName);
        playerDataManager.getHistory().sendPity(player, target.name(), crateSettings);
    }

    @Command("pity")
    @Permission(value = "crazycrates.command.player.history", def = PermissionDefault.TRUE)
    public void pity(Player player, @Suggestion("crates") String crateName) {
        DatabaseManager playerDataManager = crateManager.getDatabaseManager();
        CrateSettings crateSettings = playerDataManager.getCrateSettings(crateName);
        playerDataManager.getHistory().sendPity(player, player.getName(), crateSettings);
    }


}

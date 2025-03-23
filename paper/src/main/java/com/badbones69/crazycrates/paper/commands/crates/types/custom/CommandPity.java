package com.badbones69.crazycrates.paper.commands.crates.types.custom;

import com.badbones69.crazycrates.paper.api.PlayerBuilder;
import com.badbones69.crazycrates.paper.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.paper.commands.crates.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class CommandPity extends BaseCommand {

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

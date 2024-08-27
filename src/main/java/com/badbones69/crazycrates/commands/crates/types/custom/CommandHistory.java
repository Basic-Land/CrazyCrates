package com.badbones69.crazycrates.commands.crates.types.custom;

import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.commands.crates.types.BaseCommand;
import com.ryderbelserion.vital.paper.api.builders.PlayerBuilder;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Optional;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class CommandHistory extends BaseCommand {

    @Command(value = "history")
    @Permission(value = "crazycrates.command.player.history", def = PermissionDefault.TRUE)
    public void history(Player player, @Suggestion("crates") String crateName, @Optional @Suggestion("numbers") Integer page) {
        if (page == null) page = 1;
        DatabaseManager playerDataManager = crateManager.getDatabaseManager();
        CrateSettings crateSettings = playerDataManager.getCrateSettings(crateName);
        playerDataManager.getHistory().sendHistory(player, player.getName(), page, crateSettings);
    }

    @Command("adminhistory")
    @Permission(value = "crazycrates.command.admin.history", def = PermissionDefault.OP)
    public void adminHistory(Player player, @Suggestion("crates") String crateName, @Suggestion("players") PlayerBuilder target, @Optional @Suggestion("numbers") Integer page) {
        if (page == null) page = 1;
        DatabaseManager playerDataManager = crateManager.getDatabaseManager();
        CrateSettings crateSettings = playerDataManager.getCrateSettings(crateName);
        playerDataManager.getHistory().sendHistory(player, target.name(), page, crateSettings);
    }
}

package com.badbones69.crazycrates.paper.commands.crates.types.custom;

import com.badbones69.crazycrates.paper.api.PlayerBuilder;
import com.badbones69.crazycrates.paper.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.paper.commands.crates.types.BaseCommand;
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

    @Command(value = "historyrarity")
    @Permission(value = "crazycrates.command.player.history", def = PermissionDefault.TRUE)
    public void history(Player player, @Suggestion("crates") String crateName, @Suggestion("rarities") String type, @Optional @Suggestion("numbers") Integer page) {
        if (page == null) page = 1;
        DatabaseManager playerDataManager = crateManager.getDatabaseManager();
        CrateSettings crateSettings = playerDataManager.getCrateSettings(crateName);
        playerDataManager.getHistory().sendHistory(player, player.getName(), page, crateSettings, type);
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

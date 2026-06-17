package com.badbones69.crazycrates.paper.commands.crates.types.custom;

import com.badbones69.crazycrates.paper.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.paper.commands.crates.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.Comparator;

public class Count extends BaseCommand {

    @Command(value = "count")
    @Permission(value = "crazycrates.command.admin.count", def = PermissionDefault.OP)
    public void count(Player player, @Suggestion("crates") String crateName) {
        DatabaseManager playerDataManager = crateManager.getDatabaseManager();
        CrateSettings crateSettings = playerDataManager.getCrateSettings(crateName);
        playerDataManager.getPlayerProfiles(crateSettings).thenAccept(stream ->
                stream.sorted(Comparator.<PlayerProfile>comparingInt(p -> p.getHistory().size()).reversed()).filter(profile -> !profile.getHistory().isEmpty())
                        .forEach(profile ->
                                player.sendMessage(profile.getPlayerName() + " has opened " + profile.getHistory().size() + " crates.")
                        )
        );
    }
}

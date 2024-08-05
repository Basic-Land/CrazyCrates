package com.badbones69.crazycrates.commands.crates.types.admin.crates;

import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.commands.crates.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class CommandAdd extends BaseCommand {

    @Command(value = "add")
    @Permission(value = "crazycrates.command.admin.additems", def = PermissionDefault.OP)
    public void add(Player player, @Suggestion("cmdd") String type, @Suggestion("numbers") int i) {
        PlayerBaseProfile playerBaseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());
        if (type.equals("vote")) {
            playerBaseProfile.addVoteTokens(i);
        } else if (type.equals("premium")) {
            playerBaseProfile.addPremiumCurrency(i);
        }
        player.sendMessage("Added " + i + " " + type + " to current amount vote " + playerBaseProfile.getVoteTokens() + " premium " + playerBaseProfile.getPremiumCurrency());
    }
}

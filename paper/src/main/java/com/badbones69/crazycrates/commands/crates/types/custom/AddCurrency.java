package com.badbones69.crazycrates.commands.crates.types.custom;

import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.commands.crates.types.BaseCommand;
import com.ryderbelserion.vital.paper.api.builders.PlayerBuilder;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Optional;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class AddCurrency extends BaseCommand {

    @Command(value = "add")
    @Permission(value = "crazycrates.command.admin.add", def = PermissionDefault.OP)
    public void add(Player player, @Suggestion("cmdd") String type, @Suggestion("numbers") int i, @Optional @Suggestion("players") PlayerBuilder target) {
        if (target != null && target.getPlayer() != null) {
            player = target.getPlayer();
        }

        PlayerBaseProfile playerBaseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());
        if (type.equals("vote")) {
            playerBaseProfile.addVoteTokens(i);
        } else if (type.equals("premium")) {
            playerBaseProfile.addPremiumCurrency(i);
        }
        player.sendMessage("Added " + i + " " + type + " to current amount vote " + playerBaseProfile.getVoteTokens() + " premium " + playerBaseProfile.getPremiumCurrency());
    }
}

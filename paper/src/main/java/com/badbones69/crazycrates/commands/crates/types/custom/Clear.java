package com.badbones69.crazycrates.commands.crates.types.custom;

import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.commands.crates.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class Clear extends BaseCommand {

    @Command(value = "clear")
    @Permission(value = "crazycrates.command.admin.clear", def = PermissionDefault.OP)
    public void add(Player player) {
        PlayerBaseProfile playerBaseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());
        playerBaseProfile.resetShopLimits();
    }
}

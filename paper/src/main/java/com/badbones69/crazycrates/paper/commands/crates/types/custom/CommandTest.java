package com.badbones69.crazycrates.paper.commands.crates.types.custom;

import com.badbones69.crazycrates.paper.commands.crates.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class CommandTest extends BaseCommand {

    @Command(value = "gacha")
    @Permission(value = "", def = PermissionDefault.TRUE)
    public void onTest(Player player) {
        if (crateManager.isInOpeningList(player)) {
            player.sendMessage(Component.text("You are already opening a crate.", NamedTextColor.RED));
            return;
        }
        crateManager.getDatabaseManager().getUltimateMenuManager().open(player);
    }
}

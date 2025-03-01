package com.badbones69.crazycrates.commands.crates.types.custom;

import com.badbones69.crazycrates.api.builders.items.ItemAddMenu;
import com.badbones69.crazycrates.api.enums.Messages;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.commands.crates.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class AddItemCustom extends BaseCommand {

    @Command(value = "additems")
    @Permission(value = "crazycrates.command.admin.additems", def = PermissionDefault.OP)
    public void add(Player player, @Suggestion("crates") String crateName, @Suggestion("types") String type) {
        Crate crate = this.crateManager.getCrateFromName(crateName);

        if (crate == null || crate.getCrateSettings() == null) {
            player.sendMessage(Messages.not_a_crate.getMessage(player, crateName, "{crate}"));
            return;
        }

        ItemAddMenu inventory = new ItemAddMenu(player, 54, "<red><b>Add Items</red>", crate, RewardType.fromString(type));
        player.openInventory(inventory.build().getInventory());
    }
}

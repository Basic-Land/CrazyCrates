package com.badbones69.crazycrates.commands.crates.types.admin.crates;

import com.badbones69.crazycrates.api.builders.types.items.ItemPreview;
import com.badbones69.crazycrates.api.builders.types.items.RaritiesMenu;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.commands.crates.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class CommandEditItems extends BaseCommand {
    @Command("edititems")
    @Permission(value = "crazycrates.command.admin.edititems", def = PermissionDefault.OP)
    public void editItems(Player player, @Suggestion("both") String type) {
        RewardType rewardType = RewardType.fromString(type);
        Crate crate = this.crateManager.getCrateFromName(type);
        if (crate == null) {
            player.openInventory(new ItemPreview(player, 54, "&c&lEdit Items", rewardType).build().getInventory());
            return;
        }

        player.openInventory(new RaritiesMenu(crate, player, 27, "Rarities of " + crate.getName()).build().getInventory());
    }
}

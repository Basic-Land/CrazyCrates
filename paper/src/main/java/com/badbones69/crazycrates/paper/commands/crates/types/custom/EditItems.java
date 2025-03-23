package com.badbones69.crazycrates.paper.commands.crates.types.custom;

import com.badbones69.crazycrates.paper.api.builders.items.ItemPreview;
import com.badbones69.crazycrates.paper.api.builders.items.RaritiesMenu;
import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.Table;
import com.badbones69.crazycrates.paper.commands.crates.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class EditItems extends BaseCommand {
    @Command("edititems")
    @Permission(value = "crazycrates.command.admin.edititems", def = PermissionDefault.OP)
    public void editItems(Player player, @Suggestion("both") String type) {
        Table table = switch (type.toLowerCase()) {
            case "crateitems" -> Table.ALL_ITEMS;
            case "shopitems" -> Table.SHOP_ITEMS;
            default -> null;
        };

        Crate crate = this.crateManager.getCrateFromName(type);
        if (crate == null) {
            player.openInventory(new ItemPreview(player, 54, "<red><b>Edit Items", table).build().getInventory());
            return;
        }

        player.openInventory(new RaritiesMenu(crate, player, 27, "Rarities of " + crate.getFileName()).build().getInventory());
    }
}

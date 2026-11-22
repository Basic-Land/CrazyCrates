package com.badbones69.crazycrates.paper.commands.crates.types.custom;

import com.badbones69.crazycrates.paper.api.objects.gacha.constellation.ConManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.constellation.ItemConData;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.GachaMapMenuContent;
import com.badbones69.crazycrates.paper.commands.crates.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;

public class Test extends BaseCommand {

    private final MapMenu mapMenu = new MapMenu(plugin, new GachaMapMenuContent(plugin));

    @Command(value = "test")
    @Permission(value = "crazycrates.command.admin.test", def = PermissionDefault.OP)
    public void add(Player player) {
        ItemStack stack = player.getInventory().getItemInMainHand();
        ItemConData itemConData = new ItemConData(stack);
        ConManager.getType(stack).modification(itemConData);
    }

    @Command(value = "test2")
    @Permission(value = "crazycrates.command.admin.test", def = PermissionDefault.OP)
    public void add2(Player player) {
        mapMenu.open(player);
    }
}

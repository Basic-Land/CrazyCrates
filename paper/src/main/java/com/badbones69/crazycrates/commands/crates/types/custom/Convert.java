package com.badbones69.crazycrates.commands.crates.types.custom;

import com.badbones69.crazycrates.api.objects.gacha.DatabaseManager;
import com.badbones69.crazycrates.api.objects.gacha.ItemManager;
import com.badbones69.crazycrates.api.objects.gacha.enums.Table;
import com.badbones69.crazycrates.commands.crates.types.BaseCommand;
import cz.basicland.blibs.spigot.utils.item.DBItemStackNew;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.Arrays;

public class Convert extends BaseCommand {
    @Command(value = "convert")
    @Permission(value = "crates.convert", def = PermissionDefault.OP)
    public void onConvert(Player player) {
        DatabaseManager databaseManager = plugin.getCrateManager().getDatabaseManager();
        ItemManager itemManager = databaseManager.getItemManager();
        Arrays.stream(Table.values()).forEach(table ->
                itemManager.getAllItemsFromCache(table).forEach((id, stack) ->
                        itemManager.updateItem(id, DBItemStackNew.encodeItem(stack), table)));

        databaseManager.update();

        System.out.println("Converted all items to new format.");
        Bukkit.shutdown();
    }
}

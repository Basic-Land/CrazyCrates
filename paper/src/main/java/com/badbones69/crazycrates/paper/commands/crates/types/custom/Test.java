package com.badbones69.crazycrates.paper.commands.crates.types.custom;

import com.badbones69.crazycrates.paper.api.objects.gacha.constellation.ConManager;
import com.badbones69.crazycrates.paper.api.objects.gacha.constellation.ItemConData;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.MapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.editor.MenuEditor;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl.EmptyMapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl.GachaMapMenuContent;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl.PreviewMapMenu;
import com.badbones69.crazycrates.paper.api.objects.gacha.ultimatemenu.mcgui.menuimpl.shop.ShopVoteMapMenu;
import com.badbones69.crazycrates.paper.commands.crates.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Optional;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;

public class Test extends BaseCommand {

    @Command(value = "test")
    @Permission(value = "crazycrates.command.admin.test", def = PermissionDefault.OP)
    public void add(Player player) {
        ItemStack stack = player.getInventory().getItemInMainHand();
        ItemConData itemConData = new ItemConData(stack);
        ConManager.getType(stack).modification(itemConData);
    }

    @Command(value = "test2")
    @Permission(value = "crazycrates.command.admin.test", def = PermissionDefault.OP)
    public void add2(Player player, @Optional @Suggestion("testtt") String thing) {
        switch (thing) {
            case "gacha" -> new MapMenu(new GachaMapMenuContent(crateManager.getDatabaseManager().getCrateSettingsSplit().getFirst().getFirst().getCrate())).open(player);
            case "gacha-editor" -> {
                MapMenu mapMenu = new MapMenu(new GachaMapMenuContent(crateManager.getDatabaseManager().getCrateSettingsSplit().getFirst().getFirst().getCrate()));
                MenuEditor.attach(mapMenu);
                mapMenu.open(player);
            }

            case "preview" -> new MapMenu(new PreviewMapMenu()).open(player);
            case "preview-editor" -> {
                MapMenu mapMenu = new MapMenu(new PreviewMapMenu());
                MenuEditor.attach(mapMenu);
                mapMenu.open(player);
            }

            case "shop" -> new MapMenu(new ShopVoteMapMenu()).open(player);
            case "shop-editor" -> {
                MapMenu mapMenu = new MapMenu(new ShopVoteMapMenu());
                MenuEditor.attach(mapMenu);
                mapMenu.open(player);
            }

            case "empty" -> {
                MapMenu mapMenu = new MapMenu(new EmptyMapMenu());
                MenuEditor.attach(mapMenu);
                mapMenu.open(player);
            }
        }
    }
}

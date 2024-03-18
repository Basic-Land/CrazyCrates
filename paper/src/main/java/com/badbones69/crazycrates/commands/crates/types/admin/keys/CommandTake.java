package com.badbones69.crazycrates.commands.crates.types.admin.keys;

import com.badbones69.crazycrates.api.enums.Messages;
import com.badbones69.crazycrates.api.objects.Key;
import com.badbones69.crazycrates.api.utils.MiscUtils;
import com.badbones69.crazycrates.api.utils.MsgUtils;
import com.badbones69.crazycrates.commands.CommandManager;
import com.badbones69.crazycrates.commands.crates.BaseCommand;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazycrates.api.enums.types.KeyType;

public class CommandTake extends BaseCommand {

    private final @NotNull CrateManager crateManager = this.plugin.getCrateManager();

    @Command("take-random")
    @Permission(value = "crazycrates.takeerandomkey", def = PermissionDefault.OP)
    public void keyTakeRandom(CommandSender sender, @Suggestion("key-types") String keyType, @Suggestion("numbers") int amount, @Suggestion("players") CommandManager.CustomPlayer target) {
        //keyTake(sender, keyType, this.crateManager.getKeys().stream().toList().get(MiscUtils.randomNumber(0, this.crateManager.getKeys().size() - 2)).getName(), amount, target);
    }

    @Command("take")
    @Permission(value = "crazycrates.takekey", def = PermissionDefault.OP)
    public void keyTake(CommandSender sender, @Suggestion("key-types") String keyType, @Suggestion("keys") String keyName, @Suggestion("numbers") int amount, @Suggestion("players") CommandManager.CustomPlayer target) {
        KeyType type = KeyType.getFromName(keyType);

        if (type == null || type == KeyType.free_key) {
            sender.sendMessage(MsgUtils.color(MsgUtils.getPrefix() + "&cPlease use Virtual/V or Physical/P for a Key type."));
            return;
        }

        //Key key = this.crateManager.getKey(keyName);

        if (null == null) {
            if (sender instanceof Player player) {
                player.sendMessage(Messages.not_a_key.getMessage("{key}", keyName, player));
                return;
            }

            sender.sendMessage(Messages.not_a_key.getMessage("{key}", keyName));

            return;
        }

        if (amount <= 0) {
            if (sender instanceof Player player) {
                player.sendMessage(Messages.not_a_number.getMessage("{number}", String.valueOf(amount), player));

                return;
            }

            sender.sendMessage(Messages.not_a_number.getMessage("{number}", String.valueOf(amount)));

            return;
        }

        if (target.getPlayer() != null) {
            Player player = target.getPlayer();

            //takeKey(sender, player, null, key, type, amount);

            return;
        }

        OfflinePlayer offlinePlayer = target.getOfflinePlayer();

        //takeKey(sender, null, offlinePlayer, key, type, amount);
    }

    private void takeKey(CommandSender sender, Player player, OfflinePlayer offlinePlayer, Key key, KeyType type, int amount) {

    }
}
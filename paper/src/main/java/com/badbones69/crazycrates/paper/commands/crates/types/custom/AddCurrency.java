package com.badbones69.crazycrates.paper.commands.crates.types.custom;

import com.badbones69.crazycrates.paper.api.PlayerBuilder;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.CurrencyType;
import com.badbones69.crazycrates.paper.commands.crates.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Optional;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.Arrays;

public class AddCurrency extends BaseCommand {

    @Command(value = "add")
    @Permission(value = "crazycrates.command.admin.add", def = PermissionDefault.OP)
    public void add(CommandSender sender, @Suggestion("cmdd") String type, @Suggestion("numbers") int i, @Optional @Suggestion("players") PlayerBuilder target) {
        switch (sender) {
            case Player player -> {
                if (target == null || target.getPlayer() == null) {
                    addTokens(player, type, i);
                    return;
                }
                addTokens(target.getPlayer(), type, i);
            }
            case CommandSender console -> {
                if (target == null || target.getPlayer() == null) {
                    console.sendMessage("You must specify a player to add tokens to.");
                    return;
                }

                addTokens(target.getPlayer(), type, i);
            }
        }
    }

    private void addTokens(Player player, String type, int i) {
        PlayerBaseProfile playerBaseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());
        CurrencyType currency = CurrencyType.getFromName(type);

        if (type.equals("vote")) {
            currency = CurrencyType.VOTE_TOKENS;
        } else if (type.equals("premium")) {
            currency = CurrencyType.PREMIUM_CURRENCY;
        }

        if (currency == null) {
            player.sendRichMessage("<bold><red>Server </red></bold><bold><dark_gray>»</bold> <gray>Neplatný typ tokenu použij " +
                    Arrays.stream(CurrencyType.values()).map(CurrencyType::name).reduce("", (a, b) -> a + ", " + b));
            return;
        }

        playerBaseProfile.add(i, currency);
        player.sendRichMessage("<bold><red>Server </red></bold><bold><dark_gray>»</bold> <gray>Obdržel jsi <yellow>" + i + "</yellow> " + currency.translateMM() + " " +  type + " tokenů");
    }
}

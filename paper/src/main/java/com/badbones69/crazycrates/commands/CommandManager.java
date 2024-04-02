package com.badbones69.crazycrates.commands;

import com.badbones69.crazycrates.CrazyCratesPaper;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.RaritySettings;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.other.CrateLocation;
import com.badbones69.crazycrates.commands.relations.ArgumentRelations;
import com.badbones69.crazycrates.commands.relations.MiscRelations;
import com.badbones69.crazycrates.commands.subs.BaseKeyCommand;
import com.badbones69.crazycrates.commands.subs.CrateBaseCommand;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandManager {

    @NotNull
    private final CrazyCratesPaper plugin = CrazyCratesPaper.get();

    @NotNull
    private final BukkitCommandManager<CommandSender> bukkitCommandManager = this.plugin.getCommandManager();

    /**
     * Loads commands.
     */
    public void load() {
        new MiscRelations().build();
        new ArgumentRelations().build();

        this.bukkitCommandManager.registerSuggestion(SuggestionKey.of("crates"), (sender, context) -> {
            List<String> crates = new ArrayList<>(this.plugin.getFileManager().getAllCratesNames());

            crates.add("Menu");

            return crates;
        });

        this.bukkitCommandManager.registerSuggestion(SuggestionKey.of("key-types"), (sender, context) -> List.of("virtual", "v", "physical", "p"));

        this.bukkitCommandManager.registerSuggestion(SuggestionKey.of("online-players"), (sender, context) -> this.plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());

        this.bukkitCommandManager.registerSuggestion(SuggestionKey.of("locations"), (sender, context) -> this.plugin.getCrateManager().getCrateLocations().stream().map(CrateLocation::getID).toList());

        this.bukkitCommandManager.registerSuggestion(SuggestionKey.of("prizes"), (sender, context) -> {
            List<String> numbers = new ArrayList<>();

            this.plugin.getCrateManager().getCrateFromName(context.getArgs().get(0)).getPrizes().forEach(prize -> numbers.add(prize.getPrizeNumber()));

            return numbers;
        });

        this.bukkitCommandManager.registerSuggestion(SuggestionKey.of("tiers"), (sender, context) -> {
            List<String> numbers = new ArrayList<>();

            this.plugin.getCrateManager().getCrateFromName(context.getArgs().get(0)).getTiers().forEach(tier -> numbers.add(tier.getName()));

            return numbers;
        });

        this.bukkitCommandManager.registerSuggestion(SuggestionKey.of("numbers"), (sender, context) -> {
            List<String> numbers = new ArrayList<>();

            for (int i = 1; i <= 100; i++) numbers.add(String.valueOf(i));

            return numbers;
        });

        this.bukkitCommandManager.registerSuggestion(SuggestionKey.of("types"), (sender, context) -> {
            String subCommand = context.getSubCommand();
            if (subCommand.equalsIgnoreCase("additems")) {
                Crate crate = this.plugin.getCrateManager().getCrateFromName(context.getArgs().get(0));
                CrateSettings crateSettings = crate.getCrateSettings();
                if (crateSettings == null) return Collections.emptyList();
                if (context.getArgs().get(1).equalsIgnoreCase("EXTRA_REWARD")) {
                    return Collections.singletonList("EXTRA_REWARD");
                } else {
                    RaritySettings raritySettings = crateSettings.getRarityMap().get(Rarity.valueOf(context.getArgs().get(1)));
                    if (raritySettings.is5050Enabled()) {
                        return List.of("LIMITED", "STANDARD");
                    } else {
                        return Collections.singletonList("LIMITED");
                    }
                }
            }
            return Collections.emptyList();
        });

        this.bukkitCommandManager.registerSuggestion(SuggestionKey.of("rarities"), (sender, context) -> {
            String subCommand = context.getSubCommand();
            if (subCommand.equalsIgnoreCase("additems")) {
                Crate crate = this.plugin.getCrateManager().getCrateFromName(context.getArgs().get(0));
                CrateSettings crateSettings = crate.getCrateSettings();
                if (crateSettings == null) return Collections.emptyList();
                Set<Rarity> rarityMap = new HashSet<>(crateSettings.getRarityMap().keySet());
                rarityMap.add(Rarity.EXTRA_REWARD);
                return rarityMap.stream().map(Enum::name).toList();
            }
            return Collections.emptyList();
        });

        this.bukkitCommandManager.registerArgument(CrateBaseCommand.CustomPlayer.class, (sender, context) -> new CrateBaseCommand.CustomPlayer(context));

        this.bukkitCommandManager.registerCommand(new CrateBaseCommand());
        this.bukkitCommandManager.registerCommand(new BaseKeyCommand());
    }
}
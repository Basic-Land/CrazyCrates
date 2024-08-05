package com.badbones69.crazycrates.commands;

import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.RaritySettings;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.api.objects.other.CrateLocation;
import com.badbones69.crazycrates.commands.crates.types.admin.crates.CommandAddItem;
import com.badbones69.crazycrates.commands.crates.types.admin.crates.CommandMigrate;
import com.badbones69.crazycrates.commands.relations.ArgumentRelations;
import com.badbones69.crazycrates.commands.crates.types.player.CommandHelp;
import com.badbones69.crazycrates.commands.crates.types.admin.CommandAdmin;
import com.badbones69.crazycrates.commands.crates.types.admin.CommandReload;
import com.badbones69.crazycrates.commands.crates.types.admin.crates.*;
import com.badbones69.crazycrates.commands.crates.types.admin.keys.CommandGive;
import com.badbones69.crazycrates.commands.crates.types.admin.keys.CommandOpen;
import com.badbones69.crazycrates.commands.crates.types.admin.keys.CommandTake;
import com.badbones69.crazycrates.commands.crates.types.player.*;
import com.badbones69.crazycrates.commands.relations.ArgumentRelations;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import com.ryderbelserion.vital.paper.builders.PlayerBuilder;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.core.argument.keyed.ArgumentKey;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazycrates.CrazyCrates;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CommandManager {

    private final static @NotNull CrazyCrates plugin = JavaPlugin.getPlugin(CrazyCrates.class);
    private final static @NotNull CrateManager crateManager = plugin.getCrateManager();

    private final static @NotNull BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(plugin);

    /**
     * Loads commands.
     */
    public static void load() {
        new ArgumentRelations().build();

        commandManager.registerSuggestion(SuggestionKey.of("crates"), (sender, context) -> {
            final List<String> crates = new ArrayList<>(crateManager.getCrateNames());

            crates.add("Menu");

            return crates;
        });

        commandManager.registerSuggestion(SuggestionKey.of("keys"), (sender, context) -> List.of("virtual", "v", "physical", "p"));

        commandManager.registerSuggestion(SuggestionKey.of("admin-keys"), (sender, context) -> List.of("virtual", "v", "physical", "p", "free", "f"));

        commandManager.registerSuggestion(SuggestionKey.of("players"), (sender, context) -> plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());

        commandManager.registerSuggestion(SuggestionKey.of("locations"), (sender, context) -> crateManager.getCrateLocations().stream().map(CrateLocation::getID).toList());

        commandManager.registerSuggestion(SuggestionKey.of("prizes"), (sender, context) -> {
            final List<String> prizes = new ArrayList<>();

            Crate crate = crateManager.getCrateFromName(context.getFirst());

            if (crate != null) {
                crate.getPrizes().forEach(prize -> prizes.add(prize.getSectionName()));
            }

            return prizes;
        });

        commandManager.registerSuggestion(SuggestionKey.of("tiers"), (sender, context) -> {
            final List<String> tiers = new ArrayList<>();

            Crate crate = crateManager.getCrateFromName(context.getFirst());

            if (crate != null) {
                crate.getTiers().forEach(tier -> tiers.add(tier.getName()));
            }

            return tiers;
        });

        commandManager.registerSuggestion(SuggestionKey.of("numbers"), (sender, context) -> IntStream.rangeClosed(1, 100)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList()));

        commandManager.registerSuggestion(SuggestionKey.of("types"), (sender, context) -> {
            Crate crate = plugin.getCrateManager().getCrateFromName(context.getFirst());
            if (crate == null) return Collections.emptyList();
            CrateSettings crateSettings = crate.getCrateSettings();
            if (crateSettings == null) return Collections.emptyList();
            if (context.get(1).equalsIgnoreCase("EXTRA_REWARD")) {
                return Collections.singletonList("EXTRA_REWARD");
            } else {
                RaritySettings raritySettings = crateSettings.getRarityMap().get(Rarity.valueOf(context.get(1)));
                if (raritySettings.is5050Enabled()) {
                    return List.of("LIMITED", "STANDARD");
                } else {
                    return Collections.singletonList("LIMITED");
                }
            }
        });

        commandManager.registerSuggestion(SuggestionKey.of("rarities"), (sender, context) -> {
            Crate crate = plugin.getCrateManager().getCrateFromName(context.getFirst());
            if (crate == null) return Collections.emptyList();
            CrateSettings crateSettings = crate.getCrateSettings();
            if (crateSettings == null) return Collections.emptyList();
            Set<Rarity> rarityMap = new HashSet<>(crateSettings.getRarityMap().keySet());
            rarityMap.add(Rarity.EXTRA_REWARD);
            return rarityMap.stream().map(Enum::name).toList();
        });

        commandManager.registerSuggestion(SuggestionKey.of("both"), (sender, context) -> {
            List<String> collect = plugin.getCrateManager().getCrates().stream()
                    .filter(crate -> crate.getCrateSettings() != null)
                    .map(Crate::getName)
                    .collect(Collectors.toList());

            Arrays.stream(RewardType.values())
                    .map(Enum::name)
                    .forEach(collect::add);

            return collect;
        });

        commandManager.registerSuggestion(SuggestionKey.of("cmdd"), (sender, context) -> {
            return List.of("vote", "premium");
        });

        commandManager.registerArgument(PlayerBuilder.class, (sender, context) -> new PlayerBuilder(context));

        List.of(
                new CommandTeleport(),
                new CommandAddItem(),
                new CommandPreview(),
                new CommandDebug(),
                new CommandList(),
                new CommandSet(),
                new CommandAddItemCustom(),
                new CommandEditItems(),
                new CommandAdd(),

                new CommandGive(),
                new CommandOpen(),
                new CommandTake(),

                new CommandMigrate(),
                new CommandReload(),
                new CommandAdmin(),

                new CommandTransfer(),
                new CommandKey(),

                new CommandHelp(),
                new CommandHistory(),
                new CommandPity(),
                new CommandTest()
        ).forEach(commandManager::registerCommand);
    }

    public static @NotNull BukkitCommandManager<CommandSender> getCommandManager() {
        return commandManager;
    }
}
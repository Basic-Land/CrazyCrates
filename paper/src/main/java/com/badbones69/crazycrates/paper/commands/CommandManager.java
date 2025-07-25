package com.badbones69.crazycrates.paper.commands;

import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.crates.CrateLocation;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.CurrencyType;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.crates.CommandAddItem;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.crates.CommandClaim;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.crates.CommandMigrate;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.crates.editor.CommandEditor;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.crates.editor.CommandSet;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.crates.migrator.enums.MigrationType;
import com.badbones69.crazycrates.paper.commands.crates.types.custom.*;
import com.badbones69.crazycrates.paper.commands.relations.ArgumentRelations;
import com.badbones69.crazycrates.paper.commands.crates.types.player.CommandHelp;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.CommandAdmin;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.CommandReload;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.crates.CommandDebug;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.crates.CommandList;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.crates.CommandPreview;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.crates.CommandTeleport;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.keys.CommandGive;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.keys.CommandOpen;
import com.badbones69.crazycrates.paper.commands.crates.types.admin.keys.CommandTake;
import com.badbones69.crazycrates.paper.commands.crates.types.player.CommandKey;
import com.badbones69.crazycrates.paper.commands.crates.types.player.CommandTransfer;
import com.badbones69.crazycrates.paper.tasks.crates.CrateManager;
import com.badbones69.crazycrates.paper.api.PlayerBuilder;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.badbones69.crazycrates.paper.CrazyCrates;

import java.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CommandManager {

    private static final CrazyCrates plugin = CrazyCrates.getPlugin();
    private static final CrateManager crateManager = plugin.getCrateManager();

    private static final BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(plugin);

    /**
     * Loads commands.
     */
    public static void load() {
        new ArgumentRelations().build();

        commandManager.registerSuggestion(SuggestionKey.of("crates"), (context -> {
            final List<String> crates = new ArrayList<>(crateManager.getCrateNames());

            crates.add("Menu");

            return crates;
        }));

        commandManager.registerStaticSuggestion(SuggestionKey.of("keys"), Arrays.asList("virtual", "v", "physical", "p"));
        commandManager.registerStaticSuggestion(SuggestionKey.of("admin-keys"), Arrays.asList("virtual", "v", "physical", "p", "free", "f"));

        commandManager.registerSuggestion(SuggestionKey.of("players"), (context) -> plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());

        commandManager.registerSuggestion(SuggestionKey.of("locations"), (context) -> crateManager.getCrateLocations().stream().map(CrateLocation::getID).toList());

        commandManager.registerSuggestion(SuggestionKey.of("prizes"), (context) -> {
            final List<String> prizes = new ArrayList<>();

            Crate crate = crateManager.getCrateFromName(context.getArguments().getFirst());

            if (crate != null) {
                crate.getPrizes().forEach(prize -> prizes.add(prize.getSectionName()));
            }

            return prizes;
        });

        commandManager.registerSuggestion(SuggestionKey.of("tiers"), (context) -> {
            final List<String> tiers = new ArrayList<>();

            Crate crate = crateManager.getCrateFromName(context.getArguments().getFirst());

            if (crate != null) {
                crate.getTiers().forEach(tier -> tiers.add(tier.getName()));
            }

            return tiers;
        });

        commandManager.registerSuggestion(SuggestionKey.of("numbers"), (context) -> IntStream.rangeClosed(1, 100)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList()));

        commandManager.registerSuggestion(SuggestionKey.of("types"), context -> {
            Crate crate = plugin.getCrateManager().getCrateFromName(context.getArguments().getFirst());
            if (crate == null) return Collections.emptyList();
            CrateSettings crateSettings = crate.getCrateSettings();
            if (crateSettings == null) return Collections.emptyList();
            return Arrays.stream(RewardType.values()).map(RewardType::getName).toList();
        });

        commandManager.registerSuggestion(SuggestionKey.of("rarities"), context -> {
            Crate crate = plugin.getCrateManager().getCrateFromName(context.getArguments().getFirst());
            if (crate == null) return Collections.emptyList();
            CrateSettings crateSettings = crate.getCrateSettings();
            if (crateSettings == null) return Collections.emptyList();
            Set<Rarity> rarityMap = new HashSet<>(crateSettings.getRarityMap().keySet());
            rarityMap.add(Rarity.EXTRA_REWARD);
            return rarityMap.stream().map(Enum::name).toList();
        });

        commandManager.registerSuggestion(SuggestionKey.of("both"), context -> {
            List<String> collect = plugin.getCrateManager().getCrates().stream()
                    .filter(crate -> crate.getCrateSettings() != null)
                    .map(Crate::getFileName)
                    .collect(Collectors.toList());

            collect.add("crateitems");
            collect.add("shopitems");

            return collect;
        });

        commandManager.registerSuggestion(SuggestionKey.of("currencies"), context -> Arrays.stream(CurrencyType.values()).map(CurrencyType::getName).toList());

        commandManager.registerSuggestion(SuggestionKey.of("doubles"), (context) -> {
            final List<String> numbers = new ArrayList<>();

            int count = 0;

            while (count <= 1000) {
                double x = count / 10.0;

                numbers.add(String.valueOf(x));

                count++;
            }

            return numbers;
        });

        commandManager.registerSuggestion(SuggestionKey.of("migrators"), (context) -> {
            final List<String> migrators = new ArrayList<>();

            for (MigrationType value : MigrationType.values()) {
                final String name = value.getName();

                migrators.add(name);
            }

            return migrators;
        });

        commandManager.registerArgument(PlayerBuilder.class, (sender, context) -> new PlayerBuilder(context));

        List.of(
                new CommandTeleport(),
                new CommandAddItem(),
                new CommandPreview(),
                new CommandDebug(),
                new CommandList(),
                new CommandEditor(),
                new AddItemCustom(),
                new EditItems(),
                new AddCurrency(),
                new Clear(),
                new Stats(),
                new Convert(),
                new CommandSet(),

                new CommandGive(),
                new CommandOpen(),
                new CommandTake(),

                new CommandMigrate(),
                new CommandReload(),
                new CommandAdmin(),

                new CommandTransfer(),
                new CommandKey(),

                new CommandClaim(),

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
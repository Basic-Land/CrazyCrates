package com.badbones69.crazycrates.commands;

import com.badbones69.crazycrates.CrazyCratesPaper;
import com.badbones69.crazycrates.api.objects.other.CrateLocation;
import com.badbones69.crazycrates.api.utils.FileUtils;
import com.badbones69.crazycrates.commands.crates.types.CommandTest;
import com.badbones69.crazycrates.commands.crates.types.admin.*;
import com.badbones69.crazycrates.commands.crates.types.admin.keys.CommandGive;
import com.badbones69.crazycrates.commands.crates.types.admin.keys.CommandTake;
import com.badbones69.crazycrates.commands.relations.ArgumentRelations;
import com.badbones69.crazycrates.commands.crates.types.CommandHelp;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CommandManager {

    private final static @NotNull CrazyCratesPaper plugin = JavaPlugin.getPlugin(CrazyCratesPaper.class);

    private final static @NotNull CrateManager BUKKIT_CRATE_MANAGER = plugin.getCrateManager();

    private final static @NotNull BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(plugin);

    public static void load() {
        new ArgumentRelations().build();

        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();

        getCommandManager().registerSuggestion(SuggestionKey.of("players"), (sender, context) -> players.stream().map(Player::getName).toList());

        getCommandManager().registerSuggestion(SuggestionKey.of("numbers"), (sender, context) -> {
            List<String> numbers = new ArrayList<>();

            for (int i = 1; i <= 64; i++) numbers.add(String.valueOf(i));

            return numbers;
        });

        getCommandManager().registerSuggestion(SuggestionKey.of("key-types"), (sender, arguments) -> List.of("virtual", "v", "physical", "p"));

        getCommandManager().registerSuggestion(SuggestionKey.of("locations"), (sender, arguments) -> BUKKIT_CRATE_MANAGER.getCrateLocations().stream().map(CrateLocation::getID).toList());

        getCommandManager().registerSuggestion(SuggestionKey.of("crates"), (sender, arguments) -> FileUtils.getFiles("crates"));

        getCommandManager().registerSuggestion(SuggestionKey.of("keys"), (sender, arguments) -> FileUtils.getFiles("keys"));

        getCommandManager().registerArgument(CustomPlayer.class, (sender, arguments) -> new CustomPlayer(arguments));

        List.of(
                // Admin commands
                new CommandSchematic(),
                new CommandTeleport(),
                new CommandPreview(),
                new CommandReload(),
                new CommandDebug(),
                new CommandAdmin(),
                new CommandList(),
                new CommandGive(),
                new CommandTake(),

                // Test commands
                new CommandTest(),

                // Player commands
                new CommandHelp()
        ).forEach(getCommandManager()::registerCommand);
    }

    public record CustomPlayer(String name) {
        private static final @NotNull CrazyCratesPaper plugin = CrazyCratesPaper.getPlugin(CrazyCratesPaper.class);

        public @NotNull OfflinePlayer getOfflinePlayer() {
            CompletableFuture<UUID> future = CompletableFuture.supplyAsync(() -> plugin.getServer().getOfflinePlayer(name)).thenApply(OfflinePlayer::getUniqueId);

            return plugin.getServer().getOfflinePlayer(future.join());
        }

        public Player getPlayer() {
            return plugin.getServer().getPlayer(name);
        }
    }

    public static @NotNull BukkitCommandManager<CommandSender> getCommandManager() {
        return commandManager;
    }
}
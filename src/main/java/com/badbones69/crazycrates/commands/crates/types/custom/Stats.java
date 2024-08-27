package com.badbones69.crazycrates.commands.crates.types.custom;

import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.api.objects.gacha.data.Result;
import com.badbones69.crazycrates.api.objects.gacha.enums.ResultType;
import com.badbones69.crazycrates.commands.crates.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

public class Stats extends BaseCommand {
    @Command(value = "stats")
    @Permission(value = "stats", def = PermissionDefault.TRUE)
    public void onTest(Player player, @Suggestion("crates") String crateName) {
        CrateSettings crateSettings = crateManager.getDatabaseManager().getCrateSettings(crateName);
        if (crateSettings == null) return;

        PlayerProfile playerProfile = crateManager.getDatabaseManager().getPlayerProfile(player.getName(), crateSettings, false);
        if (playerProfile == null) return;

        List<Result> legendaryList = new ArrayList<>();
        List<Result> epicList = new ArrayList<>();

        playerProfile.getHistory().forEach(result -> {
            if (result.isLegendary()) legendaryList.add(result);
            if (result.isEpic()) epicList.add(result);
        });

        int totalSize = playerProfile.getHistory().size();
        StringBuilder sb = new StringBuilder();

        processResults(sb, legendaryList, totalSize);
        processResults(sb, epicList, totalSize);

        player.sendRichMessage(sb.toString());
    }

    private void processResults(StringBuilder sb, List<Result> list, int totalSize) {
        if (list.isEmpty()) return;

        String color = list.getFirst().getRarity().getColor().asHexString();
        String rarity = list.getFirst().getRarity().name();
        double raritySize = list.size();
        double guaranteed = list.stream().map(Result::getWon5050).filter(ResultType::isGuaranteed).count();
        long won = list.stream().map(Result::getWon5050).filter(ResultType::isWon).count();

        double win5050 = (100 / (raritySize - guaranteed)) * won;
        double chance = totalSize > 0 ? raritySize / totalSize * 100 : raritySize;
        double avgPity = list.stream().mapToInt(Result::getPity).average().orElse(0);

        sb.append("""
                <%s>%s staty:
                - Celkem: %.0f
                - Procenta: %.3f%%
                - Průměrná pity: %.2f
                
                %s 50/50 staty:
                - Výhry: %d
                - Průměrná šance: %.3f%%
                
                """
                .formatted(
                        color,
                        rarity,
                        raritySize,
                        chance,
                        avgPity,
                        rarity,
                        won,
                        win5050
                )
        );
    }
}

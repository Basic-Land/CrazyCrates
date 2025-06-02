package com.badbones69.crazycrates.paper.commands.crates.types.custom;

import com.badbones69.crazycrates.paper.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.Result;
import com.badbones69.crazycrates.paper.api.objects.gacha.enums.ResultType;
import com.badbones69.crazycrates.paper.commands.crates.types.BaseCommand;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Suggestion;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

public class Stats extends BaseCommand {
    @Command(value = "stats")
    @Permission(value = "crazycrates.stats", def = PermissionDefault.TRUE)
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
        long guaranteed = 0;
        long won = 0;
        long radiance = 0;
        long lost = 0;
        double sum = 0;

        for (Result result : list) {
            ResultType won5050 = result.getWon5050();
            switch (won5050) {
                case GUARANTEED -> guaranteed++;
                case WON -> won++;
                case RADIANCE -> radiance++;
                case LOST -> lost++;
            }
            sum += result.getPity();
        }

        double win5050 = (100 / (raritySize - guaranteed)) * (won + radiance);
        double chance = totalSize > 0 ? raritySize / totalSize * 100 : raritySize;
        double avgPity = raritySize > 0 ? sum / raritySize : 0;

        sb.append("""
                <%s>%s staty:
                - Celkem: %.0f
                - Procenta: %.3f%%
                - Průměrná pity: %.2f
                
                %s 50/50 staty:
                - Výhry: %d
                - Radiance: %d
                - Garantované: %d
                - Prohry: %d
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
                        radiance,
                        guaranteed,
                        lost,
                        win5050
                )
        );
    }
}

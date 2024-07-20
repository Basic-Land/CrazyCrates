package com.badbones69.crazycrates.api.objects.gacha.ultimatemenu;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.api.objects.gacha.data.RaritySettings;
import com.badbones69.crazycrates.api.objects.gacha.enums.NumberType;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.ResultType;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@UtilityClass
public final class ComponentBuilder {
    private static final CrazyCrates plugin = CrazyCrates.getPlugin(CrazyCrates.class);
    private static final Key KEY = Key.key("minecraft", "spaces");
    private static final TextColor COLOR = NamedTextColor.WHITE;
    private static final Component SPACE_BACK = translatable("space.-45", "").font(KEY);
    private static final Component SPACE_NEGATIVE = translatable("space.-1", "").font(KEY);
    private static final Component SPACE_PAGE = translatable("space.223", "").font(KEY);
    private static final Component SPACE_TIME = translatable("space.-335", "").font(KEY);
    private static final Component SPACE_PITY = translatable("space.-71", "").font(KEY);
    private static final Component SPACE_SLASH = translatable("space.9", "").font(KEY);
    private static final Component SPACE_NEWLINE = translatable("space.-72", "").font(KEY);
    private static final Component FILL_TOP = translatable("fill_top", "").color(COLOR).append(SPACE_NEGATIVE);
    private static final Component FILL_DOWN = translatable("fill_down", "").color(COLOR).append(SPACE_NEGATIVE);
    private static final Component FILL_TIME = translatable("fill_time", "").color(COLOR).append(SPACE_NEGATIVE);

    public static Component mainMenu(Player player, CrateSettings crateSettings) {
        TextComponent.Builder builder = text();
        String crateName = crateSettings.getCrateName();

        int virtualKeys = plugin.getUserManager().getVirtualKeys(player.getUniqueId(), crateName);
        PlayerProfile playerProfile = plugin.getCrateManager().getDatabaseManager().getPlayerProfile(player.getName(), crateSettings, false);
        PlayerBaseProfile baseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());

        crateName = crateName + " ";

        int spaceSize = getSize(crateName) + 62;
        builder.append(text(crateName));
        builder.append(translatable("space.-" + spaceSize, "").font(KEY));

        String voteTokens = String.valueOf(baseProfile.getVoteTokens());
        String premiumCurrency = String.valueOf(baseProfile.getPremiumCurrency());
        String virtual = String.valueOf(virtualKeys);
        String time = crateSettings.getBannerPackage().getRemainingDuration();

        int mysticSpace = voteTokens.length() * 6;
        int stellarSpace = premiumCurrency.length() * 6;
        int virtualSpace = virtual.length() * 6;
        int timeSpace = getSize(time) - 2;

        appendChars(builder, voteTokens, NumberType.TOP);

        down(builder, premiumCurrency, mysticSpace, FILL_TOP, SPACE_BACK);

        down(builder, virtual, stellarSpace, FILL_DOWN, SPACE_PAGE);

        while (virtualSpace < 45) {
            builder.append(FILL_DOWN);
            virtualSpace += 3;
        }

        builder.append(SPACE_TIME);

        int middle = Math.ceilDiv(69 - timeSpace, 2);
        int left = middle;
        int right = middle;

        while (--left > 0) {
            builder.append(FILL_TIME);
        }

        for (char c : time.toCharArray()) {
            builder.append(sw(c, NumberType.TIME, true));
            if (c == ' ') {
                builder.append(FILL_TIME).append(FILL_TIME);
            }
        }

        while (right-- > 0) {
            builder.append(FILL_TIME);
        }

        builder.append(SPACE_PITY);

        crateSettings.getRarityMap().forEach((rarity, settings) -> {
            if (rarity.equals(Rarity.COMMON)) return;
            pity(builder, playerProfile, rarity, settings);
        });

        return builder.build();
    }

    public static Component shop(Player player, String name) {
        TextComponent.Builder builder = text();

        PlayerBaseProfile baseProfile = plugin.getBaseProfileManager().getPlayerBaseProfile(player.getName());

        int spaceSize = getSize(name) + 62;
        builder.append(text(name));
        builder.append(translatable("space.-" + spaceSize, "").font(KEY));

        String mystic = String.valueOf(baseProfile.getMysticTokens());
        String stellar = String.valueOf(baseProfile.getStellarShards());
        String vote = String.valueOf(baseProfile.getVoteTokens());

        int mysticSpace = mystic.length() * 6;
        int stellarSpace = stellar.length() * 6;
        int virtualSpace = vote.length() * 6;

        appendChars(builder, mystic, NumberType.TOP);

        down(builder, stellar, mysticSpace, FILL_TOP, SPACE_BACK);

        down(builder, vote, stellarSpace, FILL_DOWN, SPACE_PAGE);

        while (virtualSpace < 45) {
            builder.append(FILL_DOWN);
            virtualSpace += 3;
        }

        return builder.build();
    }

    private static void pity(TextComponent.Builder builder, PlayerProfile playerProfile, Rarity rarity, RaritySettings settings) {
        Pair<Integer, ResultType> result = playerProfile.getPity(rarity);
        int currentPity = result.first();
        String resultType = result.second().getNext();
        String current = String.format("%02d", currentPity);
        String pity = String.format("%02d", settings.pity());

        NumberType numberType = rarity.getNumberType();

        appendChars(builder, resultType, numberType, false);
        appendChars(builder, current, numberType);
        builder.append(SPACE_SLASH);
        appendChars(builder, pity, numberType);
        builder.append(SPACE_NEWLINE);
    }

    private static void down(TextComponent.Builder builder, String amount, int spaceLength, Component filler, Component space) {
        while (spaceLength < 45) {
            builder.append(filler);
            spaceLength += 3;
        }

        builder.append(space);

        appendChars(builder, amount, NumberType.DOWN);
    }

    private static void appendChars(TextComponent.Builder builder, String str, NumberType numberType) {
        appendChars(builder, str, numberType, true);
    }

    private void appendChars(TextComponent.Builder builder, String str, NumberType numberType, boolean space) {
        for (char c : str.toCharArray()) {
            builder.append(sw(c, numberType, space));
        }
    }

    private static Component sw(char c, NumberType numberType, boolean space) {
        return translatable(charSwap(c, numberType), "").color(COLOR).append(space ? SPACE_NEGATIVE : Component.empty());
    }

    private static String charSwap(char c, NumberType numberType) {
        return switch (c) {
            case '0' -> "num0";
            case '1' -> "num1";
            case '2' -> "num2";
            case '3' -> "num3";
            case '4' -> "num4";
            case '5' -> "num5";
            case '6' -> "num6";
            case '7' -> "num7";
            case '8' -> "num8";
            case '9' -> "num9";
            case 'd' -> "days";
            case 'h' -> "hours";
            case 'm' -> "minutes";
            case ' ' -> "fill";
            case 'c' -> "color";
            case 'b' -> "black";
            default -> c + "";
        } + switch (numberType) {
            case TOP -> "_top";
            case DOWN -> "_down";
            case TIME -> "_time";
            case LEGENDARY_PITY -> "_leg";
            case EPIC_PITY -> "_epic";
            case RARE_PITY -> "_rare";
            case UNCOMMON_PITY -> "_unc";
            case NONE -> "";
        };
    }

    public static int getSize(String name) {
        int size = 0;
        boolean color = false;

        for (char c : name.toCharArray()) {
            if (c == '<') {
                color = true;
                continue;
            }

            if (c == '>') {
                color = false;
                continue;
            }

            if (color) {
                continue;
            }

            size += switch (c) {
                case 'q','w','e','r','z','u','o',
                     'p','a','s','d','g','h','j',
                     'y','x','c','v','b','n','m',
                     'Q','W','E','R','T','Z','U',
                     'O','P','A','S','D','F','G',
                     'H','J','K','L','Y','X','C',
                     'V','B','N','M','0','1','2',
                     '3','4','5','6','7','8','9',
                     '/','%','ě','š','č','ř','ž',
                     'ý','á','é','Ě','Š','Č',
                     'Ř','Ž','Ý','Á','É' -> 6;
                case 't', 'I', ' ', 'Í' -> 4;
                case 'i', ':', '.' -> 2;
                case 'f','k' -> 5;
                case 'l', '│', 'í' -> 3;
                default -> 0;
            };
        }
        return size;
    }
}

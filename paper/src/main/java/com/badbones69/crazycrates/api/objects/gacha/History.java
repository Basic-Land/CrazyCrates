package com.badbones69.crazycrates.api.objects.gacha;

import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.api.objects.gacha.data.RaritySettings;
import com.badbones69.crazycrates.api.objects.gacha.data.Result;
import com.badbones69.crazycrates.api.objects.gacha.enums.GachaType;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.ResultType;
import com.badbones69.crazycrates.api.objects.gacha.util.HSLColor;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.kyori.adventure.text.Component.*;

public class History {
    private final DatabaseManager playerDataManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private final int HISTORY_PER_PAGE = 5;

    public History(DatabaseManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public void sendHistory(Audience player, String target, int pageNumber, CrateSettings crateSettings) {
        PlayerProfile profile = playerDataManager.getPlayerProfile(target, crateSettings, true);

        if (profile == null) {
            player.sendMessage(text("Hráč " + target + " nemá žádnou historii otevření " + crateSettings.getCrateName() + " crate", NamedTextColor.RED));
            return;
        }

        List<Result> historyList = profile.getHistory();
        TextColor color = TextColor.fromHexString("#de7a00");

        if (historyList.isEmpty()) {
            player.sendMessage(text("Nemáte žádnou historii otevření " + crateSettings.getCrateName() + " crate", NamedTextColor.RED));
            return;
        }

        if (pageNumber <= 0) {
            pageNumber = 1;
        }

        int size = historyList.size();

        int startIndex = (pageNumber - 1) * HISTORY_PER_PAGE;
        int endIndex = Math.min(startIndex + HISTORY_PER_PAGE, size);
        int maxPage = (size / HISTORY_PER_PAGE) + (size % HISTORY_PER_PAGE == 0 ? 0 : 1);

        Component header = text()
                .appendNewline()
                .append(text("Historie otevření ", color))
                .append(text(crateSettings.getCrateName(), NamedTextColor.AQUA))
                .append(text(" crate", color))
                .appendNewline()
                .build();
        player.sendMessage(header);

        for (int i = startIndex; i < endIndex; i++) {
            Result history = historyList.get(size - i - 1);
            Rarity rarity = history.getRarity();

            Component component = text("» ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
                    .append(JSONComponentSerializer.json().deserialize(history.getItemName()).hoverEvent(getHover(history, rarity, crateSettings)))
                    .append(text(" - ", NamedTextColor.GRAY)
                            .append(text(rarity.name(), rarity.getColor()).hoverEvent(HoverEvent.showText(getHoverText(history, crateSettings)))));
            player.sendMessage(component);
        }

        int pageMinus = pageNumber - 1;
        int pagePlus = pageNumber + 1;

        Component pages = newline()
                .append(text("<<<<", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.callback(clickEvent -> sendHistory(clickEvent, target, pageMinus <= 0 ? maxPage : pageMinus, crateSettings)))
                        .hoverEvent(text("Předchozí stránka", NamedTextColor.GRAY)))
                .append(text(" Strana " + pageNumber + "/" + maxPage + " ", color))
                .append(text(">>>>", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.callback(clickEvent -> sendHistory(clickEvent, target, pagePlus > maxPage ? 1 : pagePlus, crateSettings)))
                        .hoverEvent(text("Další stránka", NamedTextColor.GRAY)));
        player.sendMessage(pages);
    }

    public void sendPity(Player player, String target, CrateSettings crateSettings) {
        PlayerProfile profile = playerDataManager.getPlayerProfile(target, crateSettings, true);

        if (profile == null) {
            player.sendMessage(text("Hráč " + target + " nemá žádnou historii otevření " + crateSettings.getCrateName() + " crate", NamedTextColor.RED));
            return;
        }

        Component header = newline()
                .append(text("Pity pro ", NamedTextColor.GRAY))
                .append(text(crateSettings.getCrateName(), NamedTextColor.AQUA))
                .append(text(" crate", NamedTextColor.GRAY))
                .appendNewline();
        player.sendMessage(header);

        crateSettings.getRarityMap().keySet().forEach(rarity -> {
            Pair<Integer, ResultType> pair = profile.getPity(rarity);
            int pity = pair.first();
            ResultType won5050 = pair.second();
            int color = getColor(pity);

            Component component = text(rarity.name(), rarity.getColor())
                    .append(text(" pity: ", NamedTextColor.GRAY))
                    .append(text(pity, TextColor.color(color)))
                    .append(text(" last 50/50: ", NamedTextColor.GRAY))
                    .append(text(won5050.name(), won5050.getColor()));
            player.sendMessage(component);
        });
    }

    private @NotNull HoverEvent<HoverEvent.ShowItem> getHover(Result history, Rarity rarity, CrateSettings crateSettings) {
        Set<Prize> items = crateSettings.getGachaType().equals(GachaType.OVERRIDE) ? crateSettings.getBoth(rarity) : new HashSet<>(history.isWon5050() ? crateSettings.getLimited() : crateSettings.getStandard());
        items.removeIf(item -> item.getRarity() != rarity || !item.getSectionName().equals(history.getRewardName()));

        ItemStack item = items.stream().findFirst().map(Prize::getDisplayItem).orElse(null);
        if (item == null) {
            String[] split = history.getRewardName().split("_");
            int id = Integer.parseInt(split[0]);

            item = playerDataManager.getItemManager().getItemFromCache(id).second();
            if (item == null) {
                throw new NullPointerException("Item with rewardName: " + history.getRewardName() + " does not exist, rarity: " + rarity + " 5050: " + history.isWon5050());
            }
        }

        NamespacedKey key = item.getType().getKey();
        int itemCount = item.getAmount();
        return HoverEvent.showItem(HoverEvent.ShowItem.showItem(key, itemCount, PaperAdventure.asAdventure(CraftItemStack.asNMSCopy(item).getComponentsPatch())));
    }

    private Component getHoverText(Result history, CrateSettings crateSettings) {
        RaritySettings raritySettings = crateSettings.getRarityMap().get(history.getRarity());

        int color = getColor(history.getPity());
        ResultType won5050 = history.getWon5050();

        return text()
                .append(text("- čas: ", NamedTextColor.GRAY)
                        .append(text(dateFormat.format(history.getTimestamp()), NamedTextColor.GOLD)))
                .appendNewline()
                .append(text("- pity: ", NamedTextColor.GRAY)
                        .append(text(history.getPity(), TextColor.color(color))))
                .append(raritySettings.is5050Enabled() ?
                        newline().append(text("- 50/50: ", NamedTextColor.GRAY))
                                .append(text(won5050.name(), won5050.getColor())) :
                        empty())
                .build();
    }

    private int getColor(int pity) {
        float hue = 119.978f - (1.33345f * pity);
        return new HSLColor(hue, 100f, 60f).getRGB().getRGB();
    }
}

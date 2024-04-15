package com.badbones69.crazycrates.api.objects.gacha;

import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.api.objects.gacha.data.RaritySettings;
import com.badbones69.crazycrates.api.objects.gacha.data.Result;
import com.badbones69.crazycrates.api.objects.gacha.enums.GachaType;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.ResultType;
import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.api.objects.gacha.util.HSLColor;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import cz.basicland.blibs.spigot.utils.item.NBT;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class History {
    private final DatabaseManager playerDataManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private final int HISTORY_PER_PAGE = 5;

    public History(DatabaseManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public void sendHistory(Player player, String target, int pageNumber, CrateSettings crateSettings) {
        PlayerProfile profile = playerDataManager.getPlayerProfile(target, crateSettings, true);

        if (profile == null) {
            player.sendMessage(Component.text("Hráč " + target + " nemá žádnou historii otevření " + crateSettings.getCrateName() + " crate", NamedTextColor.RED));
            return;
        }

        List<Result> historyList = profile.getHistory();
        TextColor color = TextColor.fromHexString("#de7a00");

        if (historyList.isEmpty()) {
            player.sendMessage(Component.text("Nemáte žádnou historii otevření " + crateSettings.getCrateName() + " crate", NamedTextColor.RED));
            return;
        }

        if (pageNumber <= 0) {
            pageNumber = 1;
        }

        int size = historyList.size();

        int startIndex = (pageNumber - 1) * HISTORY_PER_PAGE;
        int endIndex = Math.min(startIndex + HISTORY_PER_PAGE, size);
        int maxPage = (size / HISTORY_PER_PAGE) + (size % HISTORY_PER_PAGE == 0 ? 0 : 1);

        Component header = Component.text()
                .appendNewline()
                .append(Component.text("Historie otevření ", color))
                .append(Component.text(crateSettings.getCrateName(), NamedTextColor.AQUA))
                .append(Component.text(" crate", color))
                .appendNewline()
                .build();
        player.sendMessage(header);

        for (int i = startIndex; i < endIndex; i++) {
            Result history = historyList.get(size - i - 1);
            Rarity rarity = history.getRarity();

            Component component = Component.text()
                    .append(Component.text("» ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
                    .append(LegacyComponentSerializer.legacy('&').deserialize(history.getItemName()).hoverEvent(showItem(getItem(history, rarity, crateSettings))))
                    .append(Component.text(" - ", NamedTextColor.GRAY)
                            .append(Component.text(rarity.name(), rarity.getColor()).hoverEvent(HoverEvent.showText(getHoverText(history, crateSettings)))))
                    .build();
            player.sendMessage(component);
        }

        int pageMinus = pageNumber - 1;
        int pagePlus = pageNumber + 1;

        String cmd = player.getName().equals(target) ? "/cc history " + crateSettings.getCrateName() + " " : "/cc adminhistory " + crateSettings.getCrateName() + " " + target + " ";

        Component pages = Component.text()
                .appendNewline()
                .append(Component.text("<<<<", NamedTextColor.DARK_GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand(cmd + (pageMinus <= 0 ? maxPage : pageMinus)))
                        .hoverEvent(Component.text("Předchozí stránka", NamedTextColor.GRAY)))
                .append(Component.text(" Strana " + pageNumber + "/" + maxPage + " ", color))
                .append(Component.text(">>>>", NamedTextColor.DARK_GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand(cmd + (pagePlus > maxPage ? 1 : pagePlus)))
                        .hoverEvent(Component.text("Další stránka", NamedTextColor.GRAY)))
                .build();
        player.sendMessage(pages);
    }

    public void sendPity(Player player, String target, CrateSettings crateSettings) {
        PlayerProfile profile = playerDataManager.getPlayerProfile(target, crateSettings, true);

        if (profile == null) {
            player.sendMessage(Component.text("Hráč " + target + " nemá žádnou historii otevření " + crateSettings.getCrateName() + " crate", NamedTextColor.RED));
            return;
        }

        Component header = Component.text()
                .appendNewline()
                .append(Component.text("Pity pro ", NamedTextColor.GRAY))
                .append(Component.text(crateSettings.getCrateName(), NamedTextColor.AQUA))
                .append(Component.text(" crate", NamedTextColor.GRAY))
                .appendNewline()
                .build();
        player.sendMessage(header);

        crateSettings.getRarityMap().keySet().forEach(rarity -> {
            Pair<Integer, ResultType> pair = profile.getPity(rarity);
            int pity = pair.first();
            ResultType won5050 = pair.second();
            int color = getColor(pity);

            Component component = Component.text()
                    .append(Component.text(rarity.name(), rarity.getColor()))
                    .append(Component.text(" pity: ", NamedTextColor.GRAY))
                    .append(Component.text(pity, TextColor.color(color)))
                    .append(Component.text(" last 50/50: ", NamedTextColor.GRAY))
                    .append(Component.text(won5050.name(), won5050.getColor()))
                    .build();
            player.sendMessage(component);
        });
    }

    private ItemStack getItem(Result history, Rarity rarity, CrateSettings crateSettings) {
        Set<Prize> items = crateSettings.getGachaType().equals(GachaType.OVERRIDE) ? crateSettings.getBoth(rarity) : new HashSet<>(history.isWon5050() ? crateSettings.getLimited() : crateSettings.getStandard());
        items.removeIf(item -> item.getRarity() != rarity || !item.getPrizeNumber().equals(history.getRewardName()));

        ItemStack item = items.stream().findFirst().map(Prize::getDisplayItem).orElse(null);
        if (item == null) {
            String[] split = history.getRewardName().split("_");
            int id = Integer.parseInt(split[0]);

            item = playerDataManager.getItemManager().getItemFromCache(RewardType.LIMITED, id).second();
            if (item == null) {
                throw new NullPointerException("Item with rewardName: " + history.getRewardName() + " does not exist, rarity: " + rarity + " 5050: " + history.isWon5050());
            }
        }

        return item;
    }

    private HoverEvent<HoverEvent.ShowItem> showItem(ItemStack itemStack) {
        NBT nbt = new NBT(itemStack);
        NamespacedKey key = itemStack.getType().getKey();
        Key itemKey = Key.key(key.getNamespace(), key.getKey());
        int itemCount = itemStack.getAmount();
        BinaryTagHolder tag = BinaryTagHolder.binaryTagHolder(nbt.getNBTToString());
        return HoverEvent.showItem(HoverEvent.ShowItem.showItem(itemKey, itemCount, tag));
    }

    private Component getHoverText(Result history, CrateSettings crateSettings) {
        RaritySettings raritySettings = crateSettings.getRarityMap().get(history.getRarity());

        int color = getColor(history.getPity());
        ResultType won5050 = history.getWon5050();

        return Component.text()
                .append(Component.text("- čas: ", NamedTextColor.GRAY)
                        .append(Component.text(dateFormat.format(history.getTimestamp()), NamedTextColor.GOLD)))
                .appendNewline()
                .append(Component.text("- pity: ", NamedTextColor.GRAY)
                        .append(Component.text(history.getPity(), TextColor.color(color))))
                .append(raritySettings.is5050Enabled() ?
                        Component.newline().append(Component.text("- 50/50: ", NamedTextColor.GRAY))
                                .append(Component.text(won5050.name(), won5050.getColor())) :
                        Component.empty())
                .build();
    }

    private int getColor(int pity) {
        float hue = 119.978f - (1.33345f * pity);
        return new HSLColor(hue, 100f, 60f).getRGB().getRGB();
    }
}

package com.badbones69.crazycrates.api.objects.gacha.data;

import com.badbones69.crazycrates.api.objects.Prize;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.enums.ResultType;
import cz.basicland.blibs.spigot.utils.item.NBT;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

import static net.kyori.adventure.text.Component.text;

@ToString
@Getter
public class Result implements Serializable {
    @Serial
    private static final long serialVersionUID = 2975781183369196583L;
    private final Rarity rarity;
    private final int pity;
    private final long timestamp = System.currentTimeMillis();
    @Setter
    private ResultType won5050;
    private String itemName;
    private String rewardName;
    private transient Prize prize;
    @Setter
    private transient int mystic, stellar;

    public Result(Rarity rarity, ResultType won5050, int pity) {
        this.rarity = rarity;
        this.won5050 = won5050;
        this.pity = pity;
    }

    public boolean isLegendary() {
        return rarity == Rarity.LEGENDARY;
    }

    public boolean isEpic() {
        return rarity == Rarity.EPIC;
    }

    public boolean isWon5050() {
        return won5050 != ResultType.LOST;
    }

    public void setItemData(Prize prize) {
        this.prize = prize;
        if (prize == null) return;
        @NotNull ItemStack item = prize.getDisplayItem();
        NBT nbt = new NBT(item);
        rewardName = nbt.getString("rewardName");
        Component displayName = item.getItemMeta().displayName();
        if (displayName != null) {
            itemName = JSONComponentSerializer.json().serialize(displayName);
        } else {
            itemName = itemName(item.getType().name());
        }
    }

    private String itemName(String type) {
        TextComponent.Builder out = text();
        out.color(NamedTextColor.GRAY);

        Arrays.stream(type.toLowerCase().split("_"))
                .forEach(s -> out.append(text(s.substring(0, 1).toUpperCase()))
                        .append(text(s.substring(1)))
                        .appendSpace()
                );

        return JSONComponentSerializer.json().serialize(out.build());
    }
}

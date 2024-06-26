package com.badbones69.crazycrates.api.objects.gacha;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import com.badbones69.crazycrates.tasks.crates.CrateManager;
import cz.basicland.blibs.shared.databases.hikari.DatabaseConnection;
import cz.basicland.blibs.spigot.utils.item.DBItemStack;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemManager {
    private final DatabaseConnection connection;
    private final Map<Integer, ItemStack> allItems = new HashMap<>();

    /**
     * Constructor for the ItemManager class.
     *
     * @param connection The database connection to be used.
     */
    public ItemManager(DatabaseConnection connection) {
        this.connection = connection;
        allItems.putAll(getAllItems());
        convertItems();
    }

    private void convertItems() {
        CrazyCrates plugin = CrazyCrates.getPlugin(CrazyCrates.class);
        CrateManager crateManager = plugin.getCrateManager();

        if (!Bukkit.getPluginManager().isPluginEnabled("bSpecializedCrates")) return;

        Crate crate = crateManager.getCrateFromName("AllItems");

        if (crate == null) return;

        me.ztowne13.customcrates.crates.options.CReward.getAllRewards().forEach((id, reward) -> {
            ItemStack item = reward.getSaveBuilder().getStack();
            String rewardName = reward.getRewardName();
            double chance = reward.getChance();
            List<String> commands = reward.getCommands();
            boolean give = reward.isGiveDisplayItem();

            crate.addEditorItem(item, id, chance, commands);
        });
    }

    /**
     * Retrieves all items from a specific table.
     *
     * @return A list of pairs, where each pair consists of an item ID and an ItemStack.
     */
    private Map<Integer, ItemStack> getAllItems() {
        return connection.querySQLite("SELECT * FROM AllItems").thenApply(rs -> {
            Map<Integer, ItemStack> items = new HashMap<>();
            try {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    ItemStack item = DBItemStack.decodeItem(rs.getString("itemStack"));
                    items.put(id, item);
                }
            } catch (SQLException | IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return items;
        }).join();
    }

    private Map<Integer, ItemStack> get(Map<Integer, ItemStack> map, List<Integer> ids) {
        return map.entrySet().stream().filter(e -> (ids.isEmpty() || ids.contains(e.getKey()))).collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }

    /**
     * Retrieves a specific item from the cache based on its ID.
     *
     * @param id The ID of the item to retrieve.
     * @return A pair consisting of the item ID and an ItemStack, or null if the item is not found.
     */
    public Pair<Integer, ItemStack> getItemFromCache(int id) {
        return new Pair<>(id, allItems.get(id).clone());
    }

    public Map<Integer, ItemStack> getAllItemsFromCache() {
        return allItems;
    }

    /**
     * Adds an item to a specific table.
     *
     * @param item  The ItemStack to add.
     * @return The ID of the added item, or -1 if the item could not be added.
     */
    public int addItem(ItemStack item) {
        try {
            connection.updateSQLite("INSERT INTO AllItems(itemStack) VALUES(?)", DBItemStack.encodeItem(item)).join();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int id = connection.querySQLite("SELECT last_insert_rowid()").thenApply(rs -> {
            try {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1;
        }).join();

        if (id == -1) return id;

        allItems.put(id, item);

        return id;
    }

    /**
     * Updates an item in a specific table.
     *
     * @param id         The ID of the item to update.
     * @param item       The new ItemStack to replace the old one.
     */
    public void updateItem(int id, String item) {
        connection.updateSQLite("UPDATE AllItems SET itemStack = ? WHERE id = ?", item, id);
    }
}

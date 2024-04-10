package com.badbones69.crazycrates.api.objects.gacha;

import com.badbones69.crazycrates.api.objects.gacha.enums.RewardType;
import com.badbones69.crazycrates.api.objects.gacha.util.Pair;
import cz.basicland.blibs.shared.databases.hikari.DatabaseConnection;
import cz.basicland.blibs.spigot.utils.item.DBItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemManager {
    private final DatabaseConnection connection;
    private final Map<Pair<Integer, RewardType>, ItemStack> allItems = new HashMap<>();
    private final Map<Pair<Integer, RewardType>, ItemStack> extraRewards = new HashMap<>();

    /**
     * Constructor for the ItemManager class.
     *
     * @param connection The database connection to be used.
     */
    public ItemManager(DatabaseConnection connection) {
        this.connection = connection;
        allItems.putAll(getAllItems("AllItems"));
        extraRewards.putAll(getAllItems("ExtraRewards"));
    }

    /**
     * Retrieves all items from a specific table.
     *
     * @param table The name of the table to retrieve items from.
     * @return A list of pairs, where each pair consists of an item ID and an ItemStack.
     */
    public Map<Pair<Integer, RewardType>, ItemStack> getAllItems(String table) {
        return connection.query("SELECT * FROM " + table).thenApply(this::items).join();
    }

    /**
     * Retrieves specific items from a table based on their IDs.
     *
     * @param table The name of the table to retrieve items from.
     * @param ids The IDs of the items to retrieve.
     * @return A list of pairs, where each pair consists of an item ID and an ItemStack.
     */
    public Map<Pair<Integer, RewardType>, ItemStack> getItems(String table, List<Integer> ids) {
        return connection.query("SELECT * FROM " + table + " WHERE rewardName IN (" + String.join(",", Collections.nCopies(ids.size(), "?")) + ")", ids.toArray()).thenApply(this::items).join();
    }

    /**
     * Converts a ResultSet into a list of pairs, where each pair consists of an item ID and an ItemStack.
     *
     * @param rs The ResultSet to convert.
     * @return A list of pairs, where each pair consists of an item ID and an ItemStack.
     */
    private Map<Pair<Integer, RewardType>, ItemStack> items(ResultSet rs) {
        Map<Pair<Integer, RewardType>, ItemStack> items = new HashMap<>();
        try {
            while (rs.next()) {
                int id = rs.getInt("id");
                RewardType type = RewardType.valueOf(rs.getString("rewardType"));
                ItemStack item = DBItemStack.decodeItem(rs.getString("itemStack"));
                items.put(new Pair<>(id, type), item);
            }
        } catch (SQLException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Retrieves items from the cache based on their IDs.
     *
     * @param table The name of the table to retrieve items from.
     * @param ids The IDs of the items to retrieve.
     * @return A list of pairs, where each pair consists of an item ID and an ItemStack.
     */
    public Map<Integer, ItemStack> getItemsFromCache(RewardType table, List<Integer> ids) {
        return switch (table) {
            case STANDARD -> get(allItems, ids, RewardType.STANDARD);
            case LIMITED -> get(allItems, ids, RewardType.LIMITED);
            case EXTRA_REWARD -> get(extraRewards, ids, RewardType.STANDARD);
        };
    }

    private Map<Integer, ItemStack> get(Map<Pair<Integer, RewardType>, ItemStack> map, List<Integer> ids, RewardType type) {
        return map.entrySet().stream().filter(e -> {
            Pair<Integer, RewardType> key = e.getKey();
            return key.second() == type && (ids.isEmpty() || ids.contains(e.getKey().first()));
        }).collect(HashMap::new, (m, e) -> m.put(e.getKey().first(), e.getValue()), HashMap::putAll);
    }

    /**
     * Retrieves a specific item from the cache based on its ID.
     *
     * @param table The name of the table to retrieve the item from.
     * @param id The ID of the item to retrieve.
     * @return A pair consisting of the item ID and an ItemStack, or null if the item is not found.
     */
    public Pair<Integer, ItemStack> getItemFromCache(RewardType table, int id) {
        Map<Integer, ItemStack> items = switch (table) {
            case STANDARD, LIMITED -> get(allItems, List.of(id), table);
            case EXTRA_REWARD -> get(extraRewards, List.of(id), table);
        };
        return new Pair<>(id, items.get(id));
    }

    public Map<Integer, ItemStack> getAllItemsFromCache(RewardType table) {
        return switch (table) {
            case STANDARD, LIMITED -> get(allItems, List.of(), table);
            case EXTRA_REWARD -> get(extraRewards, List.of(), table);
        };
    }

    /**
     * Adds an item to a specific table.
     *
     * @param table The name of the table to add the item to.
     * @param item The ItemStack to add.
     * @return The ID of the added item, or -1 if the item could not be added.
     */
    public int addItem(RewardType table, ItemStack item) {
        try {
            connection.update("INSERT INTO " + table.getTableName() + "(rewardType,itemStack) VALUES(?,?)", table.name(), DBItemStack.encodeItem(item)).join();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int id = connection.query("SELECT last_insert_rowid()").thenApply(rs -> {
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

        switch (table) {
            case STANDARD, LIMITED -> allItems.put(new Pair<>(id, table), item);
            case EXTRA_REWARD -> extraRewards.put(new Pair<>(id, table), item);
        }

        return id;
    }

    /**
     * Updates an item in a specific table.
     *
     * @param rewardType The name of the table to update the item in.
     * @param id The ID of the item to update.
     * @param item The new ItemStack to replace the old one.
     */
    public void updateItem(RewardType rewardType, int id, String item) {
        connection.update("UPDATE " + rewardType.getTableName() + " SET itemStack = ? WHERE id = ? AND rewardType = ?", item, id, rewardType.name());
    }
}

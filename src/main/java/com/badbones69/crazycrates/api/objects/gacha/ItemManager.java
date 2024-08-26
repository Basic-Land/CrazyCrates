package com.badbones69.crazycrates.api.objects.gacha;

import com.badbones69.crazycrates.api.objects.gacha.enums.Table;
import cz.basicland.blibs.shared.databases.hikari.DatabaseConnection;
import cz.basicland.blibs.spigot.utils.item.DBItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.badbones69.crazycrates.CrazyCrates.LOGGER;

public class ItemManager {
    private final DatabaseConnection connection;
    private final Map<Table, Map<Integer, ItemStack>> items = new HashMap<>();

    /**
     * Constructor for the ItemManager class.
     *
     * @param connection The database connection to be used.
     */
    public ItemManager(DatabaseConnection connection) {
        this.connection = connection;
        for (Table table : Table.values()) {
            items.put(table, getAllItems(table));
        }
    }

    /**
     * Retrieves all items from a specific table.
     *
     * @return A list of pairs, where each pair consists of an item ID and an ItemStack.
     */
    private Map<Integer, ItemStack> getAllItems(Table table) {
        return connection.querySQLite("SELECT * FROM " + table.getTable()).thenApply(rs -> {
            Map<Integer, ItemStack> items = new HashMap<>();
            try {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    ItemStack item = DBItemStack.decodeItem(rs.getString("itemStack"));
                    items.put(id, item);
                }
            } catch (SQLException | IOException | ClassNotFoundException e) {
                LOGGER.warning(e.getMessage());
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
    public ItemStack getItemFromCache(int id, Table table) {
        ItemStack stack = getAllItemsFromCache(table).get(id);
        return stack == null ? null : stack.clone();
    }

    public Map<Integer, ItemStack> getAllItemsFromCache(Table table) {
        return items.get(table);
    }

    /**
     * Adds an item to a specific table.
     *
     * @param item  The ItemStack to add.
     * @return The ID of the added item, or -1 if the item could not be added.
     */
    public int addItem(ItemStack item, Table table) {
        try {
            connection.updateSQLite("INSERT INTO " + table.getTable() + "(itemStack) VALUES(?)", DBItemStack.encodeItem(item)).join();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int id = connection.querySQLite("SELECT last_insert_rowid()").thenApply(rs -> {
            try {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                LOGGER.warning(e.getMessage());
            }
            return -1;
        }).join();

        if (id == -1) return id;

        getAllItemsFromCache(table).put(id, item);

        return id;
    }

    /**
     * Updates an item in a specific table.
     *
     * @param id         The ID of the item to update.
     * @param item       The new ItemStack to replace the old one.
     */
    public void updateItem(int id, String item, Table table) {
        connection.updateSQLite("UPDATE " + table.getTable() + " SET itemStack = ? WHERE id = ?", item, id);
    }
}

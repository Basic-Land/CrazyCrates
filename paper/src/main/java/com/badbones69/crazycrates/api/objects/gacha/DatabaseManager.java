package com.badbones69.crazycrates.api.objects.gacha;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.banners.BannerPackage;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import com.badbones69.crazycrates.api.objects.gacha.shop.ShopManager;
import com.badbones69.crazycrates.api.objects.gacha.ultimatemenu.UltimateMenuManager;
import com.google.common.collect.Lists;
import cz.basicland.blibs.shared.databases.hikari.DatabaseConnection;
import cz.basicland.blibs.spigot.BLibs;
import cz.basicland.blibs.spigot.utils.item.DBItemStack;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseManager {
    private final DatabaseConnection connection;
    @Getter
    private final List<List<CrateSettings>> crateSettingsSplit;
    @Getter
    private final List<CrateSettings> crateSettings;
    @Getter
    private final History history;
    @Getter
    private final ItemManager itemManager;
    @Getter
    private final UltimateMenuManager ultimateMenuManager;
    @Getter
    private final ShopManager shopManager;

    public DatabaseManager(List<Crate> crateList) {
        connection = BLibs.getApi().getDatabaseHandler().loadSQLite(JavaPlugin.getPlugin(CrazyCrates.class), "gamba", "crates.db");
        crateSettings = crateList.stream().map(Crate::getCrateSettings).filter(Objects::nonNull).collect(Collectors.toList());
        crateSettingsSplit = Lists.partition(crateSettings, 3);
        createCrateTable();

        history = new History(this);
        itemManager = new ItemManager(connection);
        ultimateMenuManager = new UltimateMenuManager(this);
        shopManager = new ShopManager(this);

        for (Crate crate : crateList) {
            CrateSettings settings = crate.getCrateSettings();
            if (settings == null) continue;
            BannerPackage bannerPackage = settings.getBannerPackage();
            if (bannerPackage.isBannerActive() || !bannerPackage.enabled()) {
                settings.loadItems(crate, crate.getPrizes(), this);
            }
        }
    }

    private void createCrateTable() {
        connection.updateSQLite("CREATE TABLE IF NOT EXISTS PlayerData(playerName VARCHAR(16) PRIMARY KEY, baseData VARCHAR NULL)").join();
        connection.updateSQLite("CREATE TABLE IF NOT EXISTS AllItems(id INTEGER PRIMARY KEY AUTOINCREMENT, itemStack VARCHAR NULL)").join();
        connection.updateSQLite("CREATE TABLE IF NOT EXISTS ShopItems(id INTEGER PRIMARY KEY AUTOINCREMENT, itemStack VARCHAR NULL)").join();
        connection.updateSQLite("CREATE TABLE IF NOT EXISTS Backup(uuid VARCHAR(36) PRIMARY KEY, inventory VARCHAR NULL)").join();

        Set<String> playernames = new HashSet<>();
        connection.querySQLite("SELECT playerName FROM PlayerData").thenAccept(rs -> {
            try {
                while (rs.next()) {
                    playernames.add(rs.getString("playerName"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).join();

        connection.querySQLite("PRAGMA table_info(PlayerData)").thenAccept(rs -> {
            try {
                Set<String> columnNames = new HashSet<>();
                while (rs.next()) {
                    columnNames.add(rs.getString("name"));
                }

                columnNames.remove("playerName");
                columnNames.remove("baseData");

                crateSettings.forEach(crate -> {
                    String name = crate.getCrateName();
                    Set<Rarity> rarities = crate.getRarityMap().keySet();
                    int bonusPity = crate.getBonusPity();

                    if (!columnNames.contains(name)) {
                        connection.updateSQLite("ALTER TABLE PlayerData ADD COLUMN " + name + " VARCHAR NULL").join();
                        playernames.forEach(playerName -> connection.updateSQLite("UPDATE PlayerData SET " + name + " = ? WHERE playerName = ?", serializeProfile(new PlayerProfile(playerName, rarities, bonusPity)), playerName));
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).join();

        restoreInv(null);
    }

    public void restoreInv(UUID target) {
        String targetStr = target == null ? "" : " WHERE uuid = '" + target + "'";

        connection.querySQLite("SELECT * FROM Backup" + targetStr).thenAccept(rs -> {
            try {
                while (rs.next()) {
                    String uuid = rs.getString("uuid");
                    String inventory = rs.getString("inventory");

                    List<String> items = Arrays.asList(inventory.split("\n"));

                    ItemStack[] contents = DBItemStack.getItemStackList(items).toArray(new ItemStack[0]);

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

                    Bukkit.getScheduler().runTaskLater(CrazyCrates.getPlugin(CrazyCrates.class), () -> {
                        CMIUser user = CMI.getInstance().getPlayerManager().getUser(offlinePlayer);
                        Player player = user.getPlayer();

                        player.getInventory().setContents(contents);
                        player.saveData();
                        user.unloadData();
                    }, 100L);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            connection.updateSQLite("DELETE FROM Backup" + targetStr);
        });
    }

    private boolean hasPlayerData(String playerName) {
        try {
            return connection.querySQLite("SELECT playerName FROM PlayerData WHERE playerName = ?", playerName).join().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PlayerProfile addBlankPlayerData(String playerName, String crateName) {
        StringBuilder query = new StringBuilder("INSERT INTO PlayerData(playerName, baseData");

        for (CrateSettings crateSettings : crateSettings) {
            query.append(", ").append(crateSettings.getCrateName());
        }

        query.append(") VALUES('").append(playerName).append("'");
        query.append(", '").append(serializeProfile(new PlayerBaseProfile(playerName))).append("'");

        PlayerProfile newProfile = null;

        for (CrateSettings crateSettings : crateSettings) {
            PlayerProfile profile = new PlayerProfile(playerName, crateSettings.getRarityMap().keySet(), crateSettings.getBonusPity());

            if (crateSettings.getCrateName().equals(crateName)) {
                newProfile = profile;
            }

            query.append(", '").append(serializeProfile(profile)).append("'");
        }

        query.append(")");

        connection.updateSQLite(query.toString()).join();

        return newProfile;
    }

    public void savePlayerProfile(String playerName, CrateSettings crateSettings, PlayerProfile profile) {
        String name = crateSettings.getCrateName();
        if (!this.crateSettings.contains(crateSettings)) {
            System.out.println("Error: Crate " + name + " does not exist.");
            return;
        }

        String profileString = serializeProfile(profile);
        connection.updateSQLite("UPDATE PlayerData SET " + name + " = ? WHERE playerName = ?", profileString, playerName);
    }

    public PlayerProfile getPlayerProfile(String playerName, CrateSettings crateSettings, boolean override) {
        String crateName = crateSettings.getCrateName();

        if (!this.crateSettings.contains(crateSettings)) {
            System.out.println("Error: Crate " + crateName + " does not exist.");
            return null;
        }

        if (!hasPlayerData(playerName)) {
            if (!override) {
                return addBlankPlayerData(playerName, crateName);
            } else {
                return null;
            }
        }

        return connection.querySQLite("SELECT " + crateName + " FROM PlayerData WHERE playerName = ?", playerName).thenApply(rs -> {
            try {
                if (rs.next()) {
                    String profileString = rs.getString(crateName);
                    return deserializeProfile(profileString);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }).join();
    }

    public PlayerBaseProfile getPlayerBaseProfile(String playerName) {
        if (!hasPlayerData(playerName)) {
            addBlankPlayerData(playerName, null);
        }

        return connection.querySQLite("SELECT baseData FROM PlayerData WHERE playerName = ?", playerName).thenApply(rs -> {
            try {
                if (rs.next()) {
                    String profileString = rs.getString("baseData");
                    return deserializeBaseProfile(profileString);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }).join();
    }

    public void savePlayerBaseProfile(String playerName, PlayerBaseProfile profile) {
        String profileString = serializeProfile(profile);
        connection.updateSQLite("UPDATE PlayerData SET baseData = ? WHERE playerName = ?", profileString, playerName);
    }

    public CrateSettings getCrateSettings(String crateName) {
        return crateSettings.stream().filter(crate -> crate.getCrateName().equals(crateName)).findFirst().orElse(null);
    }

    public void saveInventory(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack[] contents = player.getInventory().getContents();
        List<String> items;
        try {
            items = DBItemStack.encodeItemList(Arrays.asList(contents));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String inventory = String.join("\n", items);

        connection.updateSQLite("INSERT OR REPLACE INTO Backup(uuid, inventory) VALUES(?, ?)", uuid, inventory);
    }

    public void clearInventory(Player player) {
        connection.updateSQLite("DELETE FROM Backup WHERE uuid = ?", player.getUniqueId());
    }

    private String serializeProfile(Object profile) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(profile);
            oos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private PlayerProfile deserializeProfile(String profileString) {
        return deserialize(profileString);
    }

    private PlayerBaseProfile deserializeBaseProfile(String profileString) {
        return deserialize(profileString);
    }

    private <OUT> OUT deserialize(String profileString) {
        try {
            byte[] bytes = Base64.getDecoder().decode(profileString);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (OUT) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}

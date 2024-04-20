package com.badbones69.crazycrates.api.objects.gacha;

import com.badbones69.crazycrates.CrazyCrates;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerBaseProfile;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.api.objects.gacha.enums.Rarity;
import cz.basicland.blibs.shared.databases.hikari.DatabaseConnection;
import cz.basicland.blibs.spigot.BLibs;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

public class DatabaseManager {
    private final DatabaseConnection connection;
    @Getter
    private final List<CrateSettings> crateSettings;
    private final Map<String, PlayerProfile> cache = new HashMap<>();
    @Getter
    private final History history;
    @Getter
    private final ItemManager itemManager;

    public DatabaseManager(List<Crate> crateList) {
        connection = BLibs.getApi().getDatabaseHandler().loadSQLite(JavaPlugin.getPlugin(CrazyCrates.class), "gamba", "crates.db");
        crateSettings = crateList.stream().map(Crate::getCrateSettings).filter(Objects::nonNull).toList();
        createCrateTable();

        history = new History(this);
        itemManager = new ItemManager(connection);

        for (Crate crate : crateList) {
            CrateSettings settings = crate.getCrateSettings();
            if (settings == null) continue;
            settings.loadItems(crate, crate.getPrizes(), this);
        }
    }

    private void createCrateTable() {
        connection.update("CREATE TABLE IF NOT EXISTS PlayerData(playerName VARCHAR(16) PRIMARY KEY, baseData VARCHAR NULL)").join();
        connection.update("CREATE TABLE IF NOT EXISTS AllItems(id INTEGER PRIMARY KEY AUTOINCREMENT, itemStack VARCHAR NULL)").join();

        Set<String> playernames = new HashSet<>();
        connection.query("SELECT playerName FROM PlayerData").thenAccept(rs -> {
            try {
                while (rs.next()) {
                    playernames.add(rs.getString("playerName"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).join();

        connection.query("PRAGMA table_info(PlayerData)").thenAccept(rs -> {
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
                        connection.update("ALTER TABLE PlayerData ADD COLUMN " + name + " VARCHAR NULL").join();
                        playernames.forEach(playerName -> connection.update("UPDATE PlayerData SET " + name + " = ? WHERE playerName = ?", serializeProfile(new PlayerProfile(playerName, rarities, bonusPity)), playerName));
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).join();
    }

    private boolean hasPlayerData(String playerName) {
        try {
            return connection.query("SELECT playerName FROM PlayerData WHERE playerName = ?", playerName).join().next();
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

        connection.update(query.toString()).join();

        return newProfile;
    }

    public void savePlayerProfile(String playerName, CrateSettings crateSettings, PlayerProfile profile) {
        String name = crateSettings.getCrateName();
        if (!this.crateSettings.contains(crateSettings)) {
            System.out.println("Error: Crate " + name + " does not exist.");
            return;
        }

        String profileString = serializeProfile(profile);
        connection.update("UPDATE PlayerData SET " + name + " = ? WHERE playerName = ?", profileString, playerName);
    }

    public PlayerProfile getPlayerProfile(String playerName, CrateSettings crateSettings, boolean override) {
        String crateName = crateSettings.getCrateName();

        if (!this.crateSettings.contains(crateSettings)) {
            System.out.println("Error: Crate " + crateName + " does not exist.");
            return null;
        }

        if (cache.containsKey(playerName)) {
            return cache.get(playerName);
        }

        if (!hasPlayerData(playerName)) {
            if (!override) {
                PlayerProfile playerProfile = addBlankPlayerData(playerName, crateName);
                cache.put(playerName, playerProfile);
                return playerProfile;
            } else {
                return null;
            }
        }

        PlayerProfile playerProfile = connection.query("SELECT " + crateName + " FROM PlayerData WHERE playerName = ?", playerName).thenApply(rs -> {
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

        if (playerProfile != null) cache.put(playerName, playerProfile);

        return playerProfile;
    }

    public PlayerBaseProfile getPlayerBaseProfile(String playerName) {
        if (!hasPlayerData(playerName)) {
            addBlankPlayerData(playerName, null);
        }

        return connection.query("SELECT baseData FROM PlayerData WHERE playerName = ?", playerName).thenApply(rs -> {
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
        connection.update("UPDATE PlayerData SET baseData = ? WHERE playerName = ?", profileString, playerName);
    }

    public CrateSettings getCrateSettings(String crateName) {
        return crateSettings.stream().filter(crate -> crate.getCrateName().equals(crateName)).findFirst().orElse(null);
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
        return (PlayerProfile) deserialize(profileString);
    }

    private PlayerBaseProfile deserializeBaseProfile(String profileString) {
        return (PlayerBaseProfile) deserialize(profileString);
    }

    private Object deserialize(String profileString) {
        try {
            byte[] bytes = Base64.getDecoder().decode(profileString);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}

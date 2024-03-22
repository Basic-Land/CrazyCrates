package com.badbones69.crazycrates.api.objects.gacha;

import com.badbones69.crazycrates.CrazyCratesPaper;
import com.badbones69.crazycrates.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.api.objects.gacha.data.PlayerProfile;
import com.badbones69.crazycrates.api.objects.gacha.util.Rarity;
import cz.basicland.blibs.shared.databases.hikari.DatabaseConnection;
import cz.basicland.blibs.spigot.BLibs;

import java.io.*;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerDataManager {
    private final DatabaseConnection connection;
    private final List<CrateSettings> crateSettings;

    public PlayerDataManager(List<CrateSettings> crateSettings) {
        CrazyCratesPaper plugin = CrazyCratesPaper.get();
        this.connection = BLibs.getApi().getDatabaseHandler().loadSQLite(plugin, "gamba", "crates.db");
        this.crateSettings = crateSettings;
        createCrateTable();
    }

    private void createCrateTable() {
        connection.update("CREATE TABLE IF NOT EXISTS PlayerData(playerName VARCHAR(16) PRIMARY KEY)").join();

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

                crateSettings.forEach(crate -> {
                    String name = crate.getName();
                    Set<Rarity> rarities = crate.getRarityMap().keySet();
                    int bonusPity = crate.getBonusPity();

                    if (!columnNames.contains(name)) {
                        connection.update("ALTER TABLE PlayerData ADD COLUMN " + name + " VARCHAR NULL");
                        playernames.forEach(playerName -> connection.update("UPDATE PlayerData SET " + name + " = ?", serializeProfile(new PlayerProfile(playerName, rarities, bonusPity))));
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).join();
    }

    public boolean hasPlayerData(String playerName) {
        try {
            return connection.query("SELECT playerName FROM PlayerData WHERE playerName = ?", playerName).join().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addBlankPlayerData(String playerName) {
        StringBuilder query = new StringBuilder("INSERT INTO PlayerData(playerName");
        for (CrateSettings crateSettings : crateSettings) {
            query.append(", ").append(crateSettings.getName());
        }
        query.append(") VALUES('").append(playerName).append("'");
        for (CrateSettings crateSettings : crateSettings) {
            PlayerProfile profile = new PlayerProfile(playerName, crateSettings.getRarityMap().keySet(), crateSettings.getBonusPity());
            query.append(", '").append(serializeProfile(profile)).append("'");
        }
        query.append(")");

        connection.update(query.toString()).join();
    }

    public void savePlayerProfile(String playerName, CrateSettings crateName, PlayerProfile profile) {
        String name = crateName.getName();
        if (!crateSettings.contains(crateName)) {
            System.out.println("Error: Crate " + name + " does not exist.");
            return;
        }

        String profileString = serializeProfile(profile);
        connection.update("UPDATE PlayerData SET " + name + " = ? WHERE playerName = ?", profileString, playerName);
    }

    public PlayerProfile getPlayerProfile(String playerName, CrateSettings crateName) {
        String name = crateName.getName();
        if (!crateSettings.contains(crateName)) {
            System.out.println("Error: Crate " + name + " does not exist.");
            return null;
        }

        return connection.query("SELECT " + name + " FROM PlayerData WHERE playerName = ?", playerName).thenApply(rs -> {
            try {
                if (rs.next()) {
                    String profileString = rs.getString(name);
                    return deserializeProfile(profileString);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }).join();
    }

    public CrateSettings getCrateSettings(String crateName) {
        return crateSettings.stream().filter(crate -> crate.getName().equals(crateName)).findFirst().orElse(null);
    }

    private String serializeProfile(PlayerProfile profile) {
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
        try {
            byte[] bytes = Base64.getDecoder().decode(profileString);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (PlayerProfile) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}

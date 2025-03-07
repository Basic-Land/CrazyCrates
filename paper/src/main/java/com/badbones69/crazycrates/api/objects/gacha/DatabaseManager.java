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
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.badbones69.crazycrates.CrazyCrates.LOGGER;

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
        connection = BLibs.getApi().getDatabaseHandler().loadSQLite(CrazyCrates.getPlugin(), "gamba", "crates.db");
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
        connection.updateSQLite("CREATE TABLE IF NOT EXISTS PlayerData(playerName VARCHAR(16) PRIMARY KEY, baseData BLOB NULL)").join();
        connection.updateSQLite("CREATE TABLE IF NOT EXISTS AllItems(id INTEGER PRIMARY KEY AUTOINCREMENT, itemStack VARCHAR NULL)").join();
        connection.updateSQLite("CREATE TABLE IF NOT EXISTS ShopItems(id INTEGER PRIMARY KEY AUTOINCREMENT, itemStack VARCHAR NULL)").join();
        connection.updateSQLite("CREATE TABLE IF NOT EXISTS Backup(uuid VARCHAR(36) PRIMARY KEY, inventory VARCHAR NULL)").join();
        connection.updateSQLite("CREATE TABLE IF NOT EXISTS ShopInfo(nextReset VARCHAR)").join();

        Set<String> playernames = new HashSet<>();
        connection.querySQLite("SELECT playerName FROM PlayerData").thenAccept(rs -> {
            try {
                while (rs.next()) {
                    playernames.add(rs.getString("playerName"));
                }
            } catch (SQLException e) {
                LOGGER.warning(e.getMessage());
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
                        connection.updateSQLite("ALTER TABLE PlayerData ADD COLUMN " + name + " BLOB NULL").join();
                        playernames.forEach(playerName -> connection.updateSQLite("UPDATE PlayerData SET " + name + " = ? WHERE playerName = ?", serializeProfile(new PlayerProfile(playerName, rarities, bonusPity)), playerName));
                    }
                });
            } catch (SQLException e) {
                LOGGER.warning(e.getMessage());
            }
        }).join();

        restoreInv(null);
        checkAndResetShopInfo(LocalDate.now());
    }

    private void checkAndResetShopInfo(LocalDate date) {
        LocalDate firstDayOfNextMonth = date.with(TemporalAdjusters.firstDayOfNextMonth());

        connection.querySQLite("SELECT * FROM ShopInfo").thenAccept(rs -> {
            try {
                if (rs.next()) {
                    LocalDate nextReset = LocalDate.parse(rs.getString("nextReset"));

                    if (date.equals(nextReset)) {
                        connection.updateSQLite("UPDATE ShopInfo SET nextReset = ?", firstDayOfNextMonth.toString()).join();
                        resetLimits();
                    }
                } else {
                    connection.updateSQLite("INSERT INTO ShopInfo(nextReset) VALUES(?)", firstDayOfNextMonth.toString()).join();
                    checkAndResetShopInfo(date);
                }
            } catch (SQLException e) {
                LOGGER.warning(e.getMessage());
            }
        }).join();
    }

    private void resetLimits() {
        connection.querySQLite("SELECT baseData from PlayerData").thenAccept(rs -> {
            try {
                while (rs.next()) {
                    byte[] baseData = (byte[]) rs.getObject("baseData");
                    PlayerBaseProfile baseProfile = deserializeBaseProfile(baseData);

                    baseProfile.resetShopLimits();
                    savePlayerBaseProfile(baseProfile.getPlayerName(), baseProfile);
                }
            } catch (SQLException e) {
                LOGGER.warning(e.getMessage());
            }
        }).join();
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

                    Bukkit.getScheduler().runTaskLater(CrazyCrates.getPlugin(), () -> {
                        CMIUser user = CMI.getInstance().getPlayerManager().getUser(offlinePlayer);
                        Player player = user.getPlayer();

                        player.getInventory().setContents(contents);
                        player.saveData();
                        user.unloadData();
                    }, 100L);
                }
            } catch (SQLException e) {
                LOGGER.warning(e.getMessage());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            connection.updateSQLite("DELETE FROM Backup" + targetStr);
        });
    }

    @SuppressWarnings({"resource", "BooleanMethodIsAlwaysInverted"})
    private boolean hasPlayerData(String playerName) {
        try {
            return connection.querySQLite("SELECT playerName FROM PlayerData WHERE playerName = ?", playerName).join().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PlayerProfile addBlankPlayerData(String playerName, String crateName) {
        StringBuilder query = new StringBuilder("INSERT INTO PlayerData(playerName, baseData");

        crateSettings.forEach(crateSettings -> query.append(", ").append(crateSettings.getCrateName()));

        List<byte[]> profiles = new ArrayList<>();

        query.append(") VALUES('").append(playerName).append("'");
        query.append(", ?");

        profiles.add(serializeProfile(new PlayerBaseProfile(playerName)));

        PlayerProfile newProfile = null;

        for (CrateSettings crateSettings : crateSettings) {
            PlayerProfile profile = new PlayerProfile(playerName, crateSettings.getRarityMap().keySet(), crateSettings.getBonusPity());

            if (crateSettings.getCrateName().equals(crateName)) {
                newProfile = profile;
            }

            query.append(", ?");
            profiles.add(serializeProfile(profile));
        }

        query.append(")");

        connection.updateSQLite(query.toString(), profiles.toArray()).join();

        return newProfile;
    }

    public void savePlayerProfile(String playerName, CrateSettings crateSettings, PlayerProfile profile) {
        String name = crateSettings.getCrateName();
        if (this.crateSettings.stream().noneMatch(cr -> cr.getCrateName().equals(crateSettings.getCrateName()))) {
            LOGGER.warning("Error: Crate " + name + " does not exist.");
            return;
        }

        byte[] profileString = serializeProfile(profile);
        connection.updateSQLite("UPDATE PlayerData SET " + name + " = ? WHERE playerName = ?", profileString, playerName).join();
    }

    public PlayerProfile getPlayerProfile(String playerName, CrateSettings crateSettings, boolean override) {
        String crateName = crateSettings.getCrateName();

        if (this.crateSettings.stream().noneMatch(cr -> cr.getCrateName().equals(crateSettings.getCrateName()))) {
            LOGGER.warning("Error: Crate " + crateName + " does not exist.");
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
                    byte[] profileString = (byte[]) rs.getObject(crateName);
                    return deserializeProfile(profileString);
                }
            } catch (SQLException e) {
                LOGGER.warning(e.getMessage());
            }
            return null;
        }).join();
    }

    public CompletableFuture<PlayerBaseProfile> getPlayerBaseProfile(String playerName) {
        if (!hasPlayerData(playerName)) {
            addBlankPlayerData(playerName, null);
        }

        return connection.querySQLite("SELECT baseData FROM PlayerData WHERE playerName = ?", playerName).thenApply(rs -> {
            try {
                if (rs.next()) {
                    byte[] profileString = (byte[]) rs.getObject("baseData");
                    return deserializeBaseProfile(profileString);
                }
            } catch (SQLException e) {
                LOGGER.warning(e.getMessage());
            }
            return null;
        });
    }

    public void savePlayerBaseProfile(String playerName, PlayerBaseProfile profile) {
        byte[] profileString = serializeProfile(profile);
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

    private byte[] serializeProfile(Object profile) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(profile);
            oos.close();
            return compress(baos.toByteArray());
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
            return null;
        }
    }

    private PlayerProfile deserializeProfile(byte[] profileString) {
        return deserialize(profileString);
    }

    private PlayerBaseProfile deserializeBaseProfile(byte[] profileString) {
        return deserialize(profileString);
    }

    @SuppressWarnings("unchecked")
    private <OUT> OUT deserialize(byte[] profileString) {
        try {
            byte[] bytes = decompress(profileString);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (OUT) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warning(e.getMessage());
            return null;
        }
    }

    private byte[] compress(final byte[] data) {
        LZ4Compressor fastCompressor = LZ4Factory.fastestInstance().highCompressor();
        byte[] comp = new byte[fastCompressor.maxCompressedLength(data.length)];
        int compressedLength = fastCompressor.compress(data, 0, data.length, comp, 0, comp.length);
        return Arrays.copyOf(comp, compressedLength);
    }

    private byte[] decompress(final byte[] compressed) {
        LZ4SafeDecompressor decompressor = LZ4Factory.fastestInstance().safeDecompressor();
        return decompressor.decompress(compressed, compressed.length * 100);
    }
}

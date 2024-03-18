package us.crazycrew.crazycrates.api.users;

import net.kyori.adventure.audience.Audience;

import java.util.UUID;

/**
 * A class that handles fetching users, checking virtual keys, adding virtual keys or physical keys
 * Ability to set keys, get keys, getting total keys or checking total crates opened or individual crates opened.
 *
 * @author Ryder Belserion
 * @version 0.4
 */
public abstract class UserManager {

    /**
     * Checks if user is null.
     *
     * @param uuid The uuid of the player
     * @return true or false
     */
    public abstract boolean isUserNull(UUID uuid);

    /**
     * Get the player
     *
     * @param uuid The uuid of the player
     * @return player
     */
    public abstract Audience getUser(UUID uuid);

    /**
     * Get the amount of virtual keys a player has.
     *
     * @param uuid The uuid of the player
     * @param keyName The name of the key
     * @return the amount of virtual keys
     */
    public abstract int getVirtualKeys(UUID uuid, String keyName);

    /**
     * Give a player virtual keys for a crate.
     *
     * @param amount The amount of keys you are giving them.
     * @param uuid The player you want to give the keys to.
     * @param keyName The keys you are giving.
     */
    public abstract void addVirtualKeys(int amount, UUID uuid, String keyName);

    /**
     * Set the amount of virtual keys a player has.
     *
     * @param amount The amount the player will have.
     * @param uuid The uuid of the player you are setting the keys to.
     * @param keyName The keys being set.
     */
    public abstract void setKeys(int amount, UUID uuid, String keyName);

    /**
     * Give a player keys for a crate.
     *
     * @param amount The amount of keys you are giving them.
     * @param uuid The player you want to give the keys to.
     * @param keyName The key you are giving.
     * @param isVirtual If the key is virtual or not.
     */
    public abstract void addKeys(int amount, UUID uuid, String keyName, boolean isVirtual);

    /**
     * Get the total amount of keys a player has.
     *
     * @param uuid The player you want to get keys from.
     * @param crateName The crate you want to use.
     * @param keyName The key you want to use.
     * @return total amount of keys a player has.
     */
    public abstract int getTotalKeys(UUID uuid, String crateName, String keyName);

    /**
     * Get the physical amount of keys a player has.
     *
     * @param uuid The player you want to get keys from.
     * @param crateName The crate you want to use.
     * @param keyName The key you want to use.
     * @return the amount of physical keys
     */
    public abstract int getPhysicalKeys(UUID uuid, String crateName, String keyName);

    /**
     * Take a key from a player.
     *
     * @param amount The amount of keys you wish to take.
     * @param uuid The uuid of the player you wish to take keys from.
     * @param crateName The crate you want to use.
     * @param keyName The key you are taking.
     * @param isVirtual If the key is virtual or not.
     * @param loopInventory If it just checks the players hand or if it checks their inventory.
     * @return true if successfully taken keys and false if not.
     */
    public abstract boolean takeKeys(int amount, UUID uuid, String crateName, String keyName, boolean isVirtual, boolean loopInventory);

    /**
     * Checks to see if the player has a physical key of the crate in their main hand or inventory.
     *
     * @param uuid The uuid of the player being checked.
     * @param crateName The crate you want to use.
     * @param keyName The key you are checking.
     * @param loopInventory If it just checks the players hand or if it checks their inventory.
     * @return true if they have the key and false if not.
     */
    public abstract boolean hasPhysicalKey(UUID uuid, String crateName, String keyName, boolean loopInventory);

    /**
     * Give keys to an offline player.
     *
     * @param uuid The uuid of the offline player you wish to give keys to.
     * @param keyName The key you are giving to the player.
     * @param keys The amount of keys you wish to give to the player.
     * @param isVirtual If the key is virtual or not.
     * @return true if it successfully gave the offline player a key and false if there was an error.
     */
    public abstract boolean addOfflineKeys(UUID uuid, String keyName, int keys, boolean isVirtual);

    /**
     * Take keys from an offline player.
     *
     * @param uuid The uuid of the offline player you wish to take keys from.
     * @param keyName The key you are taking from the player.
     * @param keys The amount of keys you wish to take from the player.
     * @param isVirtual If the key is virtual or not.
     * @return Returns true if it successfully took the key from the offline player and false if there was an error.
     */
    public abstract boolean takeOfflineKeys(UUID uuid, String keyName, int keys, boolean isVirtual);

    /**
     * Gets the total amount of crates this player opened.
     *
     * @param uuid The uuid of the player you wish to check.
     * @return Returns the amount of total crates opened.
     */
    public abstract int getTotalCratesOpened(UUID uuid);

    /**
     * Gets the amount of a specific crate this player opened.
     *
     * @param uuid The uuid of the player you wish to check.
     * @param crateName The name of the crate.
     * @return Returns the amount of times the player opened this crate.
     */
    public abstract int getCrateOpened(UUID uuid, String crateName);

    /**
     * Adds how many times a player has opened a crate.
     *
     * @param uuid The uuid of the player you wish to check.
     * @param amount The amount of times they opened.
     * @param crateName The name of the crate.
     */
    public abstract void addOpenedCrate(UUID uuid, int amount, String crateName);

    /**
     * Adds how many times a player has opened a crate.
     *
     * @param uuid The uuid of the player you wish to check.
     * @param crateName The name of the crate.
     */
    public abstract void addOpenedCrate(UUID uuid, String crateName);

}
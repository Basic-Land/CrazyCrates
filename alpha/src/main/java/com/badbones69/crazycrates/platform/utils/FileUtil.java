package com.badbones69.crazycrates.platform.utils;

import com.badbones69.crazycrates.CrazyCratesPaper;
import com.badbones69.crazycrates.api.enums.Files;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class FileUtil {

    private static final @NotNull CrazyCratesPaper plugin = JavaPlugin.getPlugin(CrazyCratesPaper.class);

    public static void copyFiles(Path directory, String folder, List<String> names) {
        names.forEach(name -> copyFile(directory, folder, name));
    }

    /**
     * @return A list of any file.
     */
    public static List<String> getFiles(String folder) {
        List<String> files = new ArrayList<>();

        File crateDirectory = new File(plugin.getDataFolder(), "/" + folder);

        String[] file = crateDirectory.list();

        if (file != null) {
            File[] filesList = crateDirectory.listFiles();

            if (filesList != null) {
                for (File directory : filesList) {
                    if (directory.isDirectory()) {
                        String[] folderList = directory.list();

                        if (folderList != null) {
                            for (String name : folderList) {
                                if (!name.endsWith(".yml")) continue;

                                files.add(name.replaceAll(".yml", ""));
                            }
                        }
                    }
                }
            }

            for (String name : file) {
                if (!name.endsWith(".yml")) continue;

                files.add(name.replaceAll(".yml", ""));
            }
        }

        return Collections.unmodifiableList(files);
    }

    public static void loadFiles() {
        File file = new File(plugin.getDataFolder(), "examples");

        if (file.exists()) {
            String[] entries = file.list();

            if (entries != null) {
                for (String entry : entries) {
                    File currentFile = new File(file.getPath(), entry);

                    currentFile.delete();
                }
            }

            file.delete();
        }

        copyFile(file.toPath(), "config.yml");
        copyFile(file.toPath(), "messages.yml");

        copyFiles(new File(file, "crates").toPath(), "crates", List.of(
                "QuadCrateExample.yml",
                "QuickCrateExample.yml",
                "WarCrateExample.yml",
                "CrateExample.yml"
        ));
    }

    public static void copyFile(Path directory, String name) {
        File file = directory.resolve(name).toFile();

        if (file.exists()) return;

        File dir = directory.toFile();

        if (!dir.exists()) {
            if (dir.mkdirs()) {
                if (MiscUtil.isLogging()) plugin.getLogger().warning("Created " + dir.getName() + " because we couldn't find it.");
            }
        }

        ClassLoader loader = plugin.getClass().getClassLoader();

        getResource(name, file, loader);
    }

    private static void getResource(String name, File file, ClassLoader loader) {
        URL resource = loader.getResource(name);

        if (resource == null) {
            if (MiscUtil.isLogging()) plugin.getLogger().severe("Failed to find file: " + name);

            return;
        }

        try {
            grab(resource.openStream(), file);
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to copy file: " + name, exception);
        }
    }

    public static void copyFile(Path directory, String folder, String name) {
        File file = directory.resolve(name).toFile();

        if (file.exists()) return;

        File dir = directory.toFile();

        if (!dir.exists()) {
            if (dir.mkdirs()) {
                if (MiscUtil.isLogging()) plugin.getLogger().warning("Created " + dir.getName() + " because we couldn't find it.");
            }
        }

        ClassLoader loader = plugin.getClass().getClassLoader();

        String url = folder + "/" + name;

        getResource(url, file, loader);
    }

    private static void grab(InputStream input, File output) throws Exception {
        try (InputStream inputStream = input; FileOutputStream outputStream = new FileOutputStream(output)) {
            byte[] buf = new byte[1024];
            int i;

            while ((i = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, i);
            }
        }
    }

    public static void cleanFiles() {
        FileConfiguration locations = Files.locations.getFile();
        FileConfiguration data = Files.data.getFile();

        if (!locations.contains("Locations")) {
            locations.set("Locations.Clear", null);

            Files.locations.save();
        }

        if (!data.contains("Players")) {
            data.set("Players.Clear", null);

            Files.data.save();
        }
    }
}
package com.ncombatlog;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FileUpdater {

    public static void updateConfig(JavaPlugin plugin, String fileName) {
        Logger log = plugin.getLogger();
        File file = new File(plugin.getDataFolder(), fileName);

        // existance checking 
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
            log.info(fileName + " not found. Creating new file...");
            return;
        }

        FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(file);

        InputStream defStream = plugin.getResource(fileName);
        if (defStream == null) {
            log.warning("Default config " + fileName + " not found inside jar!");
            return;
        }

        FileConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));

        int currentVersion = currentConfig.getInt("config-version", -1);
        int latestVersion = defConfig.getInt("config-version", -1);

        if (currentVersion != -1 && latestVersion != -1) {
            if (currentVersion >= latestVersion) {
                log.info(fileName + " is already up to date (v" + currentVersion + ")");
                return; 
            }

            log.info("Updating " + fileName + " from v" + currentVersion + " to v" + latestVersion + "...");
        } else {
            log.warning(fileName + ": config-version missing, forcing update...");
        }

        Map<String, Object> oldValues = new HashMap<>();
        for (String key : currentConfig.getKeys(true)) {
            oldValues.put(key, currentConfig.get(key));
        }

        plugin.saveResource(fileName, true);

        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(file);

        for (Map.Entry<String, Object> entry : oldValues.entrySet()) {
            String key = entry.getKey();

            if (key.equalsIgnoreCase("config-version")) continue;

            if (newConfig.contains(key)) {
                newConfig.set(key, entry.getValue());
            }
        }

        try {
            newConfig.save(file);
            log.info(fileName + " has been successfully updated!");
        } catch (IOException e) {
            log.severe("Failed to save updated config: " + fileName);
            e.printStackTrace();
        }
    }
}

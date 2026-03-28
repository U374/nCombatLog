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

        // Step 1: If file doesn't exist → just save default
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
            log.info(fileName + " not found. Creating new file...");
            return;
        }

        // Load current config
        FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(file);

        // Load default config from jar
        InputStream defStream = plugin.getResource(fileName);
        if (defStream == null) {
            log.warning("Default config " + fileName + " not found inside jar!");
            return;
        }

        FileConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));

        // Check config-version
        int currentVersion = currentConfig.getInt("config-version", -1);
        int latestVersion = defConfig.getInt("config-version", -1);

        if (currentVersion != -1 && latestVersion != -1) {
            if (currentVersion >= latestVersion) {
                log.info(fileName + " is already up to date (v" + currentVersion + ")");
                return; // skip update
            }

            log.info("Updating " + fileName + " from v" + currentVersion + " to v" + latestVersion + "...");
        } else {
            log.warning(fileName + ": config-version missing, forcing update...");
        }

        // Step 2: Store old values
        Map<String, Object> oldValues = new HashMap<>();
        for (String key : currentConfig.getKeys(true)) {
            oldValues.put(key, currentConfig.get(key));
        }

        // Step 3: Replace with new config (keeps comments)
        plugin.saveResource(fileName, true);

        // Step 4: Load new config
        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(file);

        // Step 5: Re-apply old values (except config-version)
        for (Map.Entry<String, Object> entry : oldValues.entrySet()) {
            String key = entry.getKey();

            // Prevent version rollback loop
            if (key.equalsIgnoreCase("config-version")) continue;

            if (newConfig.contains(key)) {
                newConfig.set(key, entry.getValue());
            }
        }

        // Step 6: Save final config
        try {
            newConfig.save(file);
            log.info(fileName + " has been successfully updated!");
        } catch (IOException e) {
            log.severe("Failed to save updated config: " + fileName);
            e.printStackTrace();
        }
    }
}
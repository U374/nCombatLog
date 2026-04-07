package com.ncombatlog;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {

    private final NCombatLog plugin;

    // Config files
    private FileConfiguration discordConfig;
    private FileConfiguration messagesConfig;

    // Cache ignored sources
    private List<String> ignoredSources;

    public ConfigManager(NCombatLog plugin) {
        this.plugin = plugin;

        reloadDiscord();
        reloadMessages(); 
        loadMainConfig();
    }

    public void reloadDiscord() {
        FileUpdater.updateConfig(plugin, "discordhook.yml");

        File file = new File(plugin.getDataFolder(), "discordhook.yml");
        discordConfig = YamlConfiguration.loadConfiguration(file);
    }

    public void reloadMessages() {
        FileUpdater.updateConfig(plugin, "message.yml");

        File file = new File(plugin.getDataFolder(), "message.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(file);
    }

    public void loadMainConfig() {
        ignoredSources = plugin.getConfig()
                .getStringList("ignored-damage-sources")
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    public FileConfiguration getDiscordConfig() {
        return discordConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public String getDiscordWebhook() {
        if (discordConfig == null) return null;
        return discordConfig.getString("webhook-url", "");
    }

    public boolean isIgnored(String cause) {
        if (cause == null) return false;
        return ignoredSources.contains(cause.toUpperCase());
    }

    // MESSAGE SYSTEM
    
    public String getMessage(String path) {
        if (messagesConfig == null) return "Messages not loaded!";

        String prefix = messagesConfig.getString("prefix", "");
        String msg = messagesConfig.getString(path, "Missing message: " + path);

        return color(prefix + " " + msg);
    }

    public String getRawMessage(String path) {
        if (messagesConfig == null) return "Messages not loaded!";

        String msg = messagesConfig.getString(path, "Missing message: " + path);
        return color(msg);
    }
    
    private String color(String msg) {
        return msg.replace("&", "§");
    }
}

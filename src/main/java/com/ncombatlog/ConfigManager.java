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

    /**
     * Reload discordhook.yml
     */
    public void reloadDiscord() {
        FileUpdater.updateConfig(plugin, "discordhook.yml");

        File file = new File(plugin.getDataFolder(), "discordhook.yml");
        discordConfig = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Reload message.yml
     */
    public void reloadMessages() {
        FileUpdater.updateConfig(plugin, "message.yml");

        File file = new File(plugin.getDataFolder(), "message.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Load main config values (config.yml)
     */
    public void loadMainConfig() {
        ignoredSources = plugin.getConfig()
                .getStringList("ignored-damage-sources")
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    /**
     * Get discordhook.yml
     */
    public FileConfiguration getDiscordConfig() {
        return discordConfig;
    }

    /**
     * Get messages.yml
     */
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    /**
     * Get Discord webhook URL
     */
    public String getDiscordWebhook() {
        if (discordConfig == null) return null;
        return discordConfig.getString("webhook-url", "");
    }

    /**
     * Check if damage cause is ignored
     */
    public boolean isIgnored(String cause) {
        if (cause == null) return false;
        return ignoredSources.contains(cause.toUpperCase());
    }

    // ============================================
    // MESSAGE SYSTEM
    // ============================================

    /**
     * Get message with prefix
     */
    public String getMessage(String path) {
        if (messagesConfig == null) return "Messages not loaded!";

        String prefix = messagesConfig.getString("prefix", "");
        String msg = messagesConfig.getString(path, "Missing message: " + path);

        return color(prefix + " " + msg);
    }

    /**
     * Get message WITHOUT prefix
     */
    public String getRawMessage(String path) {
        if (messagesConfig == null) return "Messages not loaded!";

        String msg = messagesConfig.getString(path, "Missing message: " + path);
        return color(msg);
    }

    /**
     * Color utility
     */
    private String color(String msg) {
        return msg.replace("&", "§");
    }
}
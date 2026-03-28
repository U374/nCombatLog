package com.ncombatlog;

import com.ncombatlog.commands.ReloadCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NCombatLog extends JavaPlugin {

    private static NCombatLog instance;

    private CombatManager combatManager;
    private ConfigManager configManager;
    private DiscordWebhook discordWebhook;

    private boolean stopping = false;
    private final Set<UUID> kickedPlayers = new HashSet<>();


    @Override
    public void onEnable() {
        instance = this;

        FileUpdater.updateConfig(this, "config.yml");
            reloadConfig();

        configManager = new ConfigManager(this);
        discordWebhook = new DiscordWebhook(this);
        combatManager = new CombatManager(this);

        discordWebhook.reload();

        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new KickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatRestrictionsListener(this), this);

        if (getCommand("ncombatlogreload") != null) {
            getCommand("ncombatlogreload").setExecutor(new ReloadCommand(this));
        } else {
            getLogger().warning("Command 'ncombatlogreload' not found in plugin.yml!");
        }

        // Auto-clean kicked players (safety)
        Bukkit.getScheduler().runTaskTimer(this, () -> kickedPlayers.clear(), 20L * 60, 20L * 60);

        getLogger().info("nCombatLog by nRealParadox enabled!");
    }

    @Override
    public void onDisable() {
        stopping = true;

        if (combatManager != null) {
            Bukkit.getOnlinePlayers().forEach(player -> combatManager.remove(player));
        }

        getLogger().info("nCombatLog disabled!");
    }

    public static NCombatLog getInstance() {
        return instance;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DiscordWebhook getDiscord() {
        return discordWebhook;
    }

    public boolean isServerStopping() {
        return stopping;
    }

    public Set<UUID> getKickedPlayers() {
        return kickedPlayers;
    }
}
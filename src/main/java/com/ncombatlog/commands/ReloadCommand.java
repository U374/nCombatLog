package com.ncombatlog.commands;

import com.ncombatlog.NCombatLog;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final NCombatLog plugin;

    public ReloadCommand(NCombatLog plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        try {
            plugin.reloadConfig();
            plugin.getConfigManager().loadMainConfig();
            plugin.getConfigManager().reloadMessages();
            plugin.getConfigManager().reloadDiscord();
            plugin.getDiscord().reload();

            sender.sendMessage("§a[nCombatLog] Config reloaded successfully!");
        } catch (Exception e) {
            sender.sendMessage("§c[nCombatLog] Reload failed! Check console.");
            e.printStackTrace();
        }

        return true;
    }
}
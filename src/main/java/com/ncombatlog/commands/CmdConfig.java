package com.ncombatlog.commands;

import com.ncombatlog.CombatRestrictionsListener;
import com.ncombatlog.NCombatLog;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdConfig implements CommandExecutor, TabCompleter {

    private final NCombatLog plugin;
    private final CombatRestrictionsListener listener;

    public CmdConfig(NCombatLog plugin, CombatRestrictionsListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    // -------------------------------
    // COMMAND EXECUTION
    // -------------------------------
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Command format: /ncombatlog config ender-pearl-toggle <world> <true|false>
        if (args.length != 4) return false;

        if (!args[0].equalsIgnoreCase("config") || !args[1].equalsIgnoreCase("ender-pearl-toggle")) {
            return false;
        }

        String worldName = args[2];
        String value = args[3].toLowerCase();

        // Validate world name
        List<String> allWorlds = listener.getAllWorlds();
        if (!allWorlds.contains(worldName)) {
            sender.sendMessage("§cWorld not found: " + worldName);
            return true;
        }

        boolean disable;
        if (value.equals("true")) {
            disable = true;
        } else if (value.equals("false")) {
            disable = false;
        } else {
            sender.sendMessage("§cUsage: /ncombatlog config ender-pearl-toggle <world> <true|false>");
            return true;
        }

        // Update config
        List<String> disabledWorlds = plugin.getConfig().getStringList("ender-pearl-disabled-worlds");
        if (disable) {
            if (!disabledWorlds.contains(worldName)) disabledWorlds.add(worldName);
        } else {
            disabledWorlds.remove(worldName);
        }

        plugin.getConfig().set("ender-pearl-disabled-worlds", disabledWorlds);
        plugin.saveConfig();

        sender.sendMessage("§aEnder pearl toggle updated for world §e" + worldName + "§a! Currently " + (disable ? "disabled" : "enabled"));
        return true;
    }

    // -------------------------------
    // TAB COMPLETION
    // -------------------------------
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        // Root command: /ncombatlog <TAB>
        if (args.length == 1) {
            // Only "config" available for now
            return Collections.singletonList("config");
        }

        // Subcommand: /ncombatlog config <TAB>
        if (args.length == 2 && args[0].equalsIgnoreCase("config")) {
            // Only "ender-pearl-toggle" available for now
            return Collections.singletonList("ender-pearl-toggle");
        }

        // Sub-subcommand: /ncombatlog config ender-pearl-toggle <TAB>
        if (args.length == 3 && args[0].equalsIgnoreCase("config") && args[1].equalsIgnoreCase("ender-pearl-toggle")) {
            return listener.getAllWorlds();
        }

        // Argument: /ncombatlog config ender-pearl-toggle <world> <TAB>
        if (args.length == 4 && args[0].equalsIgnoreCase("config") && args[1].equalsIgnoreCase("ender-pearl-toggle")) {
            return Arrays.asList("true", "false");
        }

        return Collections.emptyList();
    }
}
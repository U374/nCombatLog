package com.ncombatlog;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombatRestrictionsListener implements Listener {

    private final NCombatLog plugin;

    // Allowed safe commands
    private final List<String> allowedCommands = Arrays.asList(
            "msg", "tell", "w", "whisper", "r", "reply"
    );

    public CombatRestrictionsListener(NCombatLog plugin) {
        this.plugin = plugin;

        // Ensure default worlds are in the config on first load
        List<String> disabledWorlds = plugin.getConfig().getStringList("ender-pearl-disabled-worlds");

        boolean changed = false;

        if (!disabledWorlds.contains("world")) {
            disabledWorlds.add("world");
            changed = true;
        }

        if (!disabledWorlds.contains("world_nether")) {
            disabledWorlds.add("world_nether");
            changed = true;
        }

        // Save only if something changed (better practice)
        if (changed) {
            plugin.getConfig().set("ender-pearl-disabled-worlds", disabledWorlds);
            plugin.saveConfig();
        }
    }

    // -------------------------------
    // BLOCK COMMANDS IN COMBAT
    // -------------------------------
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        var player = event.getPlayer();

        if (!plugin.getCombatManager().isInCombat(player)) return;
        if (plugin.getCombatManager().canUseCommand()) return;

        if (player.isOp() && plugin.getConfig().getBoolean("allow-commands-to-ops")) {
            return;
        }

        String message = event.getMessage().toLowerCase();
        String cmd = message.split(" ")[0].substring(1);

        if (allowedCommands.contains(cmd)) return;

        event.setCancelled(true);
        String combatCmdMsg = plugin.getConfigManager().getMessage("cannot-use-command");
        player.sendMessage(combatCmdMsg);
    }

    // -------------------------------
    // BLOCK ENDER PEARL PER WORLD
    // -------------------------------
    @EventHandler
    public void onPearlUse(PlayerInteractEvent event) {
        var player = event.getPlayer();

        if (!plugin.getCombatManager().isInCombat(player)) return;
        if (plugin.getCombatManager().canUsePearl()) return;

        if (event.getHand() == null) return;

        var item = (event.getHand() == EquipmentSlot.HAND)
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();

        if (item == null || item.getType() != Material.ENDER_PEARL) return;

        // Check per-world config
        if (!isEnderPearlAllowedInWorld(player.getWorld().getName())) {
            event.setCancelled(true);
            String combatPearlMsg = plugin.getConfigManager().getMessage("cannot-use-pearl");
            player.sendMessage(combatPearlMsg);
        }
    }

    // -------------------------------
    // PER-WORLD CHECK
    // -------------------------------
    private boolean isEnderPearlAllowedInWorld(String worldName) {
        List<String> disabledWorlds = plugin.getConfig().getStringList("ender-pearl-disabled-worlds");
        return !disabledWorlds.contains(worldName);
    }

    // -------------------------------
    // GET ALL WORLDS (Bukkit ONLY)
    // -------------------------------
    public List<String> getAllWorlds() {
        List<String> worlds = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            worlds.add(world.getName());
        }

        return worlds;
    }
}
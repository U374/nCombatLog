package com.ncombatlog;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerQuitListener implements Listener {

    private final NCombatLog plugin;

    public PlayerQuitListener(NCombatLog plugin) {
        this.plugin = plugin;
    }

    private void spawnSafeLightning(Location loc) {
      if (loc == null) return;
        Location strikeLoc = loc.clone().add(0, 2, 0);
        if (loc.getWorld() != null) {
         loc.getWorld().strikeLightningEffect(strikeLoc);
           }
       }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CombatManager manager = plugin.getCombatManager();

        if (!manager.isInCombat(player)) return;

        if (plugin.isServerStopping()) {
            manager.remove(player);
            return;
        }

        if (plugin.getKickedPlayers().contains(player.getUniqueId())) {
            plugin.getKickedPlayers().remove(player.getUniqueId());
            manager.remove(player);
            return;
        }

        // last opponent
        UUID opponentId = manager.getLastOpponent(player);
        Player opponent = opponentId != null ? Bukkit.getPlayer(opponentId) : null;

        int mode = plugin.getConfig().getInt("action-mode");

        // KEEEP IT ASYNCHRONOUS
        if (mode == 1 || mode == 2) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.getDiscord().logCombat(player, opponent);
            });
        }

        if (mode == 2 || mode == 3) {

            boolean dropItems = plugin.getConfig().getBoolean("drop-item");

            if (dropItems) {
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }
                }
            }

            player.getInventory().clear();

            player.setNoDamageTicks(0);
            if (plugin.getConfig().getBoolean("summon-lightning-on-combat-log-death")) {
             spawnSafeLightning(player.getLocation());
            }
            player.damage(Double.MAX_VALUE, opponent);
        }

        manager.remove(player);
        manager.remove(opponent);
    }
}

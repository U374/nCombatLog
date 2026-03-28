package com.ncombatlog;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class KickListener implements Listener {

    private final NCombatLog plugin;

    public KickListener(NCombatLog plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        plugin.getKickedPlayers().add(event.getPlayer().getUniqueId());
    }
}
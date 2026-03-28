package com.ncombatlog;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

public class CombatListener implements Listener {

    private final NCombatLog plugin;

    public CombatListener(NCombatLog plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        // Ignore cancelled damage
        if (event.isCancelled()) return;

        // Ignore zero/invalid damage (IMPORTANT)
        if (event.getFinalDamage() <= 0) return;

        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        Player attacker = null;

        // Direct melee
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        }
        // Projectile-based damage
        else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player) {
                attacker = (Player) source;
            }
        }

        // No valid attacker
        if (attacker == null) return;

        // Self-damage
        if (victim.getUniqueId().equals(attacker.getUniqueId())) return;

        // Invulnerable / creative / spectator checks
        if (victim.isInvulnerable() || victim.getGameMode() == GameMode.CREATIVE || victim.getGameMode() == GameMode.SPECTATOR) return;
        if (attacker.isInvulnerable() || attacker.getGameMode() == GameMode.CREATIVE || attacker.getGameMode() == GameMode.SPECTATOR) return;

        // Ignored damage sources (config)
        if (plugin.getConfigManager().isIgnored(event.getCause().name())) return;

        // Tag combat
        plugin.getCombatManager().tag(victim, attacker);
    }

     @EventHandler
  public void onDeath(PlayerDeathEvent event) {
    Player dead = event.getEntity();

    // Get opponent BEFORE removing
    UUID opponentUUID = plugin.getCombatManager().getLastOpponent(dead);

    // Remove dead player
    plugin.getCombatManager().remove(dead);

    // Remove opponent too
    if (opponentUUID != null) {
        Player opponent = Bukkit.getPlayer(opponentUUID);
        if (opponent != null && opponent.isOnline()) {
            plugin.getCombatManager().remove(opponent);
            opponent.sendMessage(plugin.getConfigManager().getMessage("combat-end"));
        }
    }
}
}
package com.ncombatlog;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;

public class CombatManager {

    private final NCombatLog plugin;

    private final Map<UUID, Long> combatMap = new HashMap<>();
    private final Map<UUID, UUID> lastOpponent = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    private final int combatTime;

    public CombatManager(NCombatLog plugin) {
        this.plugin = plugin;
        this.combatTime = plugin.getConfig().getInt("combat-timer");
        startTask();
    }

    private void spawnCombatParticles(Location loc) {
    if (loc == null || loc.getWorld() == null) return;

    Particle.DustOptions redDust = new Particle.DustOptions(Color.RED, 1.5F);

    loc.getWorld().spawnParticle(
            Particle.DUST,
            loc.clone().add(0, 1, 0),
            10, // amount
            0.5, 0.5, 0.5,
            0,
            redDust
      );
    }

    // Tag players in combat
    public void tag(Player p1, Player p2) {

        if (!p1.isOnline() || !p2.isOnline()) return;
        if (p1.isInvulnerable() || p1.getGameMode() == GameMode.CREATIVE) return;
        if (p2.isInvulnerable() || p2.getGameMode() == GameMode.CREATIVE) return;

        boolean p1WasInCombat = isInCombat(p1);
        boolean p2WasInCombat = isInCombat(p2);

        long expire = System.currentTimeMillis() + (combatTime * 1000L);

        combatMap.put(p1.getUniqueId(), expire);
        combatMap.put(p2.getUniqueId(), expire);

        lastOpponent.put(p1.getUniqueId(), p2.getUniqueId());
        lastOpponent.put(p2.getUniqueId(), p1.getUniqueId());

        // BossBar if enabled in config
        if (plugin.getConfig().getBoolean("bossbar-timer-show")) {
            bossBars.computeIfAbsent(p1.getUniqueId(), id -> createBossBar(p1));
            bossBars.computeIfAbsent(p2.getUniqueId(), id -> createBossBar(p2));
        }

        if (!p1WasInCombat) {
            String msg = plugin.getConfigManager().getMessage("combat-start")
                    .replace("{opponent}", p2.getName());
            p1.sendMessage(msg);
            if (plugin.getConfig().getBoolean("combat-enter-particle")) {
             spawnCombatParticles(p1.getLocation());
              }
            }

        if (!p2WasInCombat) {
            String msg = plugin.getConfigManager().getMessage("combat-start")
                    .replace("{opponent}", p1.getName());
            p2.sendMessage(msg);
            if (plugin.getConfig().getBoolean("combat-enter-particle")) {
             spawnCombatParticles(p2.getLocation());
            }
        }
    }

    public boolean isInCombat(Player player) {
        Long expire = combatMap.get(player.getUniqueId());
        return expire != null && System.currentTimeMillis() < expire;
    }

    public UUID getLastOpponent(Player player) {
        return lastOpponent.get(player.getUniqueId());
    }

    public void remove(Player player) {
        UUID uuid = player.getUniqueId();

        combatMap.remove(uuid);
        lastOpponent.remove(uuid);

        BossBar bar = bossBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }

    private BossBar createBossBar(Player player) {
        BossBar bar = Bukkit.createBossBar("§cDON'T LEAVE!", BarColor.RED, BarStyle.SOLID);
        bar.addPlayer(player);
        return bar;
    }

    private void startTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            Iterator<Map.Entry<UUID, Long>> iterator = combatMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, Long> entry = iterator.next();

                UUID uuid = entry.getKey();
                Player player = Bukkit.getPlayer(uuid);

                if (player == null || !player.isOnline()) {
                    cleanup(uuid);
                    iterator.remove();
                    continue;
                }

                long timeLeft = entry.getValue() - System.currentTimeMillis();

                if (timeLeft <= 0) {
                    iterator.remove();
                    lastOpponent.remove(uuid);

                    BossBar bar = bossBars.remove(uuid);
                    if (bar != null) bar.removeAll();

                    player.sendMessage(plugin.getConfigManager().getMessage("combat-end"));
                    continue;
                }

                long seconds = timeLeft / 1000;

                // BossBar update (only if enabled)
                if (plugin.getConfig().getBoolean("bossbar-timer-show")) {
                    BossBar bar = bossBars.get(uuid);
                    if (bar != null) {
                        double progress = Math.max(0, (double) timeLeft / (combatTime * 1000L));
                        bar.setProgress(progress);
                        bar.setTitle("§cDON'T LEAVE FOR " + seconds + "s");
                    }
                }

                if (plugin.getConfig().getBoolean("action-bar-timer-show")) {
                    String color;

                    if (seconds > 6) color = "§a"; // Green
                    else if (seconds > 3) color = "§e"; // Yellow
                    else color = "§c"; // Red

                    boolean blink = seconds <= 3 && (System.currentTimeMillis() / 300) % 2 == 0;

                    String msg = blink
                            ? "§c⚔ " + color + "Combat: " + seconds + "s"
                            : "⚔ " + color + "Combat: " + seconds + "s";

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
                }
            }

        }, 0L, 28L);
    }

    private void cleanup(UUID uuid) {
        BossBar bar = bossBars.remove(uuid);
        if (bar != null) bar.removeAll();

        lastOpponent.remove(uuid);
    }

    // restrictions checks from config
    public boolean canUseCommand() {
        return plugin.getConfig().getBoolean("allowed-to-use-command");
    }

    public boolean canUsePearl() {
        return plugin.getConfig().getBoolean("allow-to-pearl");
    }
}

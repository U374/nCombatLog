package com.ncombatlog;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

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
    }

    // BLOCK COMMANDS IN COMBAT
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        var player = event.getPlayer();

        if (!plugin.getCombatManager().isInCombat(player)) return;
        if (plugin.getCombatManager().canUseCommand()) return;

        if (player.isOp() && plugin.getConfig().getBoolean("allow-commands-to-ops")) {
            return;
        }

        String message = event.getMessage().toLowerCase();

        // Extract base command
        String cmd = message.split(" ")[0].substring(1);

        // Allow safe commands
        if (allowedCommands.contains(cmd)) {
            return;
        }

        event.setCancelled(true);

        String combatCmdMsg = plugin.getConfigManager().getMessage("cannot-use-command");
        player.sendMessage(combatCmdMsg);
    }

    // BLOCK ENDER PEARL (main hand + off-hand, no duplicate msg)
    @EventHandler
    public void onPearlUse(PlayerInteractEvent event) {
        var player = event.getPlayer();

        if (!plugin.getCombatManager().isInCombat(player)) return;
        if (plugin.getCombatManager().canUsePearl()) return;

        if (event.getHand() == null) return;

        // Get correct item based on hand used
        var item = (event.getHand() == EquipmentSlot.HAND)
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();

        if (item == null || item.getType() != Material.ENDER_PEARL) return;

        event.setCancelled(true);

        String combatPearlMsg = plugin.getConfigManager().getMessage("cannot-use-pearl");
        player.sendMessage(combatPearlMsg);
    }
}
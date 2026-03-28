package com.ncombatlog;

import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordWebhook {

    private final NCombatLog plugin;

    private String webhook;
    private boolean enabled;
    private int color;

    public DiscordWebhook(NCombatLog plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        var cfg = plugin.getConfigManager().getDiscordConfig();
        if (cfg == null) return;

        webhook = cfg.getString("webhook-url");
        enabled = cfg.getBoolean("enabled");

        String colorString = cfg.getString("embed-color");

        if (colorString != null && colorString.startsWith("#")) {
            try {
                color = Integer.parseInt(colorString.substring(1), 16);
            } catch (Exception e) {
                color = 16711680; // fallback red
            }
        } else {
            color = cfg.getInt("embed-color");
        }
    }

    public void logCombat(Player logger, Player involved) {
        if (!enabled || webhook == null || webhook.isEmpty()) return;

        try {
            URL url = new URL(webhook);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String involvedName = (involved != null) ? involved.getName() : "Unknown";

            int loggerPing = getPingSafe(logger);
            int involvedPing = involved != null ? getPingSafe(involved) : -1;

            long unixTime = System.currentTimeMillis() / 1000;
            String discordTime = "<t:" + unixTime + ":F>";

            String json = "{ \"embeds\": [{"
                    + "\"title\": \"⚠ Combat Logger Detected\","
                    + "\"description\": \"A player left during combat.\","
                    + "\"color\": " + color + ","

                    + "\"fields\": ["

                    + "{"
                    + "\"name\": \"👤 Players\","
                    + "\"value\": \""
                    + "**Logger:** " + logger.getName() + "\\n"
                    + "**Opponent:** " + involvedName
                    + "\","
                    + "\"inline\": false"
                    + "},"

                    + "{"
                    + "\"name\": \"📡 Connection\","
                    + "\"value\": \""
                    + "**Logger:** " + (loggerPing >= 0 ? loggerPing + " ms" : "Unknown") + "\\n"
                    + "**Opponent:** " + (involvedPing >= 0 ? involvedPing + " ms" : "Unknown")
                    + "\","
                    + "\"inline\": false"
                    + "},"

                    + "{"
                    + "\"name\": \"🕒 Timestamp\","
                    + "\"value\": \"" + discordTime + "\","
                    + "\"inline\": false"
                    + "}"

                    + "],"

                    + "\"footer\": {"
                    + "\"text\": \"nCombatLog • Fair PvP System\""
                    + "}"

                    + "}]}";

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.flush();
            os.close();

            conn.getInputStream().close();

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send Discord webhook!");
        }
    }

    private int getPingSafe(Player player) {
        try {
            return player.getPing();
        } catch (Exception e) {
            return -1;
        }
    }
}
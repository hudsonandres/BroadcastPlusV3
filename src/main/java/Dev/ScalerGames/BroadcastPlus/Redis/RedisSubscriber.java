package Dev.ScalerGames.BroadcastPlus.Redis;

import Dev.ScalerGames.BroadcastPlus.Files.Engine;
import Dev.ScalerGames.BroadcastPlus.Main;
import Dev.ScalerGames.BroadcastPlus.Methods.Advancement;
import Dev.ScalerGames.BroadcastPlus.Methods.BroadcastMethods;
import Dev.ScalerGames.BroadcastPlus.Methods.Features;
import Dev.ScalerGames.BroadcastPlus.Utils.Format;
import Dev.ScalerGames.BroadcastPlus.Utils.Messages;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;

/**
 * Receives messages from the Redis channel and executes them on the local server.
 * Runs on a separate thread; broadcasts are dispatched
 * back to the main Bukkit thread via the scheduler.
 */
public class RedisSubscriber extends JedisPubSub {

    private final Main plugin;

    public RedisSubscriber(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessage(String channel, String json) {
        RedisMessage msg = RedisMessage.fromJson(json);

        // Ignore messages originating from this same server
        String thisServer = Engine.getEngineConfig().getString("Redis.server-name", "default");
        if (thisServer.equalsIgnoreCase(msg.getOriginServer())) return;

        // If specific target server(s) are set, only process on those servers.
        // targetServer may be a single name ("survival") or a comma-separated list ("survival,minigames").
        String target = msg.getTargetServer();
        if (target != null && !target.isEmpty()) {
            boolean isTargeted = Arrays.stream(target.split(","))
                    .map(String::trim)
                    .anyMatch(t -> t.equalsIgnoreCase(thisServer));
            if (!isTargeted) return;
        }

        // Dispatch to the main Bukkit thread
        Bukkit.getScheduler().runTask(plugin, () -> dispatch(msg));
    }

    private void dispatch(RedisMessage msg) {
        switch (msg.getType().toLowerCase()) {

            case "chat" -> Bukkit.getOnlinePlayers().forEach(p ->
                    Features.broadcastChat(msg.getMessage(), p));

            case "title" -> Bukkit.getOnlinePlayers().forEach(p ->
                    BroadcastMethods.sendTitle(p, msg.getMessage()));

            case "bar" -> {
                int timing = 5;
                try { timing = Integer.parseInt(msg.getExtra()); } catch (NumberFormatException ignored) {}
                final int t = timing;
                Bukkit.getOnlinePlayers().forEach(p ->
                        BroadcastMethods.sendActionBar(p, msg.getMessage(), t));
            }

            case "boss" -> {
                // extra: "<time>|<color>|<style>"
                String[] parts = msg.getExtra().split("\\|", 3);
                if (parts.length == 3) {
                    try {
                        int time = Integer.parseInt(parts[0]);
                        Main.bar.createBar(time, parts[1], parts[2], msg.getMessage());
                        Bukkit.getOnlinePlayers().forEach(p -> Main.bar.addPlayer(p));
                    } catch (Exception e) {
                        Messages.logger("&cRedis boss broadcast failed: " + e.getMessage());
                    }
                }
            }

            case "advancement" -> {
                // extra: "<item>|<style>"
                String[] parts = msg.getExtra().split("\\|", 2);
                if (parts.length == 2) {
                    try {
                        Advancement.Style style = Advancement.Style.valueOf(parts[1]);
                        Bukkit.getOnlinePlayers().forEach(p ->
                                Advancement.display(p, parts[0].toLowerCase(), style,
                                        Format.placeholder(p, msg.getMessage())));
                    } catch (Exception e) {
                        Messages.logger("&cRedis advancement broadcast failed: " + e.getMessage());
                    }
                }
            }

            case "autobroadcast" -> Bukkit.getOnlinePlayers().forEach(p -> {
                if (Main.plugin.autoBroadcast.get(p.getUniqueId()) == null
                        || Main.plugin.autoBroadcast.get(p.getUniqueId())) {
                    Features.broadcastChat(msg.getMessage(), p);
                }
            });

            default -> Messages.logger("&cRedis: tipo de broadcast desconhecido: " + msg.getType());
        }
    }
}

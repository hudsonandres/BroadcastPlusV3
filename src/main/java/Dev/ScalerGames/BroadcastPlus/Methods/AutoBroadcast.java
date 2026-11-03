package Dev.ScalerGames.BroadcastPlus.Methods;

import Dev.ScalerGames.BroadcastPlus.Files.Engine;
import Dev.ScalerGames.BroadcastPlus.Main;
import Dev.ScalerGames.BroadcastPlus.Redis.RedisMessage;
import Dev.ScalerGames.BroadcastPlus.Utils.Format;
import Dev.ScalerGames.BroadcastPlus.Utils.Messages;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.Random;

public class AutoBroadcast {

    public Main plugin;
    public AutoBroadcast(Main plugin) {
        this.plugin = plugin;
    }
    public Integer msgLine = 0;

    public void autoMessage() {
        if (Main.getInstance().getConfig().getBoolean("AutoBroadcast.enabled")) {

            // If Redis is active and this server is NOT the master, do not start local AutoBroadcast.
            // Messages will be received from the master server via Redis.
            boolean redisEnabled = Engine.getEngineConfig().getBoolean("Redis.enabled", false);
            boolean isMaster    = Engine.getEngineConfig().getBoolean("Redis.autobroadcast-master", false);
            if (redisEnabled && !isMaster) return;

            Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {

                if (Main.getInstance().getConfig().contains("AutoBroadcast.method") && Objects.requireNonNull(Main.getInstance().getConfig().getString("AutoBroadcast.method")).equalsIgnoreCase("order")) {
                    String msg = Main.getInstance().getConfig().getStringList("AutoBroadcast.messages").get(msgLine);
                    Bukkit.getConsoleSender().sendMessage(Format.color(Messages.autoBroadcastMSG(msg)));
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (Main.plugin.autoBroadcast.get(player.getUniqueId()) == null || Main.plugin.autoBroadcast.get(player.getUniqueId())) {
                            Features.broadcastChat(Messages.autoBroadcastMSG(msg), player);
                        }
                    });
                    // Publish to Redis if it is the master server.
                    if (redisEnabled && Main.redis != null && Main.redis.isEnabled()) {
                        String server = Engine.getEngineConfig().getString("Redis.server-name", "default");
                        Main.redis.publish(new RedisMessage("autobroadcast", Messages.autoBroadcastMSG(msg), "", server));
                    }
                    if (msgLine == Main.getInstance().getConfig().getStringList("AutoBroadcast.messages").size()-1) {
                        msgLine = 0;
                    } else {
                        msgLine++;
                    }
                } else {
                    String msg = Main.getInstance().getConfig().getStringList("AutoBroadcast.messages").get(new Random().nextInt(Main.getInstance().getConfig().getStringList("AutoBroadcast.messages").size()));
                    Bukkit.getConsoleSender().sendMessage(Format.color(Messages.autoBroadcastMSG(msg)));
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (Main.plugin.autoBroadcast.get(player.getUniqueId()) == null || Main.plugin.autoBroadcast.get(player.getUniqueId())) {
                            Features.broadcastChat(Messages.autoBroadcastMSG(msg), player);
                        }
                    });
                    // Publish to Redis if it is the master server.
                    if (redisEnabled && Main.redis != null && Main.redis.isEnabled()) {
                        String server = Engine.getEngineConfig().getString("Redis.server-name", "default");
                        Main.redis.publish(new RedisMessage("autobroadcast", Messages.autoBroadcastMSG(msg), "", server));
                    }
                }

            }, 0L, 20L * Main.getInstance().getConfig().getInt("AutoBroadcast.time"));
        }
    }

}
